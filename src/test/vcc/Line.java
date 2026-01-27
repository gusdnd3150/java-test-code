package test.vcc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx.Snapshot;

import test.HMap;
import test.TestGoStopClass;
import test.Utility;

public class Line {

	final String lineCd;
	final String lineTy;
	private volatile List<Proc> procList;
	int bringIndex = 0;

	public Line(String lineCd, String lineTy, List<Proc> procList) {
		this.lineCd = lineCd;
		this.lineTy = lineTy;
		Collections.sort(procList, (a, b) -> Integer.compare(a.getSortNo(), b.getSortNo()));
		this.procList = procList;
	}

	public Proc getProc(String curProcCd) {
		List<Proc> list = this.procList; // volatile snapshot
		int curIdx = indexOfProc(list, curProcCd);
		if (curIdx < 0)return null;
		return list.get(curIdx);
	}

	public String getLineCd() {
		return lineCd;
	}

	// 외부에서 리스트를 막 바꾸지 못하게 방어적으로 반환
	public List<Proc> getProcList() {
		return Collections.unmodifiableList(procList);
	}

	public synchronized void setProcList(List<Proc> procList) {
		Collections.sort(procList, (a, b) -> Integer.compare(a.getSortNo(), b.getSortNo()));
		this.procList = procList;
	}

	
	// PLC or LS 진입신호 수신시 처리
	public synchronized HMap<String,Object> plcOrLsSignal(String procCd) {
		Proc proc = getProc(procCd);
		if (proc == null)return null;
		MoveContext ctx = new MoveContext();
		
		// 1. 실공정 뒤에 버퍼구간 체크 후 자동 쉬프트
		bufferShift(procCd, ctx);
		
		// 2. 현재공정 확인 후 차량이 있을경우 앞으로 쉬프트
		procShift(procCd, ctx);

		// [라인끝 마지막 공정처리]
		// 앞공정이 없으면 라인끝 공정으로봄
		Proc nextProc = nextProc(proc.getProcCd());
		if (nextProc == null) {
			if (proc.hasCar()) {
			    ctx.addIdle(proc.getInBodyNo());
			    proc.procOut();
			}
		}

		Proc preProc = preProc(proc.getProcCd());
		// [라인별 맨앞 공정처리]
		// 이전공정이 없는경우 라인초입 공정임
		if (preProc == null) {
			if(proc.hasCar()) {
				System.out.println("현재공정에 차가 있어 진입할 수 없습니다.");
			}else {
				System.out.println("태그,바코드,브링 처리필요");
				bringIndex++;
				String tempBody = "CAR_" + bringIndex;
				ctx.addMove(tempBody, proc.getProcCd());
				proc.procIn(tempBody);		
			}

			// 이전공정이 존재하는경우
		} else {
			
			if (preProc.hasCar()) { // 이전 공정에 차량이 있으면
				
				if(proc.hasCar()) {
					System.out.println("현재공정에 차가 있어 진입할 수 없습니다.");
					
				}else {
					String bodyNo = preProc.getInBodyNo();
					ctx.addMove(bodyNo, proc.getProcCd());
					preProc.procOut();
					proc.procIn(bodyNo);
				}

			}
		}
		lineSnapShot();
		return ctx.resultData();
	}
	

	public void lineSnapShot() {
		String signal = "";
		String procInfo = "";
		String procTyInfo = "";
		String bodyInfo = "";

		for (Proc proc : this.procList) {

			procInfo += Utility.centerPad(proc.getProcCd(), 10, ' ') + " ||";
			procTyInfo += Utility.centerPad(proc.getProcTyCd(), 10, ' ') + " ||";

			if (!"Buffer".equals(proc.getProcTyCd())) {
				signal += Utility.centerPad(TestGoStopClass.dupCheckSignal.getString(proc.getProcCd()), 10, ' ')
						+ " ||";
			} else {
				signal += Utility.centerPad("-", 10, ' ') + " ||";
			}

		}
		for (Proc proc : this.procList) {
			if (proc.hasCar()) {
				bodyInfo += Utility.centerPad(proc.getInBodyNo(), 10, ' ') + " ||";
			} else {
				bodyInfo += Utility.centerPad("", 10, ' ') + " ||";
			}
		}
		System.out.println(procInfo);
		System.out.println(procTyInfo);
		System.out.println(signal);
		System.out.println(bodyInfo);
	}
	
	
	private Proc preProc(String curProcCd) {
		return preProc(curProcCd, 1);
	}

	// 현재공정 기준 "이전" 공정: Process/Buffer만 카운트해서 gap만큼 이동
	private Proc preProc(String curProcCd, int gap) {
		return moveByType(curProcCd, gap, -1);
	}

	private Proc nextProc(String curProcCd) {
		return nextProc(curProcCd, 1);
	}

	// 현재공정 기준 "다음" 공정: Process/Buffer만 카운트해서 gap만큼 이동
	private Proc nextProc(String curProcCd, int gap) {
		return moveByType(curProcCd, gap, +1);
	}

	private Proc moveByType(String curProcCd, int gap, int dir) {
		if (curProcCd == null || curProcCd.isEmpty())
			return null;
		if (gap <= 0)
			gap = 1;

		List<Proc> list = this.procList; // volatile snapshot
		int curIdx = indexOfProc(list, curProcCd);
		if (curIdx < 0)
			return null;

		int moved = 0;
		for (int i = curIdx + dir; i >= 0 && i < list.size(); i += dir) {
			Proc p = list.get(i);
			if (isProcessOrBuffer(p)) {
				moved++;
				if (moved == gap)
					return p;
			}
		}
		return null; // 범위 밖
	}

	private int indexOfProc(List<Proc> list, String procCd) {
		for (int i = 0; i < list.size(); i++) {
			if (procCd.equals(list.get(i).getProcCd()))
				return i;
		}
		return -1;
	}

	private boolean isProcessOrBuffer(Proc p) {
		String ty = p.getProcTyCd();
		return "Process".equals(ty) || "Buffer".equals(ty);
	}
	
	
	// 현재공정 실차 확인 후 있으면 다음공정으로 쉬프트
	private void procShift(String procCd ,MoveContext ctx) {
		Proc proc = getProc(procCd);
		if (proc.hasCar()) { // 현재 공정에 차량이 있으면
			System.out.println("현재 공정에 차량이 있어 쉬프트 합니다.");
			// 뒷공정부터 신호가 차례대로 들어와야 정상이나,
			// 뒷공정 신호가 누락될 경우 필요할듯
			Proc nextProc = nextProc(proc.getProcCd());
			if (nextProc != null) {
				if(!nextProc.hasCar()) {
					String curBody = proc.getInBodyNo();
					HMap<String, Object> shiftData = new HMap<>();
					proc.procOut();
					nextProc.procIn(curBody);
					ctx.addMove(curBody, nextProc.getProcCd());
				}
			}
		}
	}
	
	// 현재공정 포함, 이전 공정을 N개까지 확인하며(총 N개 또는 N+1 정책 선택)
	// "각 공정의 차를 다음 공정으로 1칸" 쉬프트
	private void procShift(String procCd, int prevCount, MoveContext ctx) {
	    if (prevCount < 0) prevCount = 0;

	    // 0번째 = 현재공정, 1번째=이전1, ... prevCount번째=이전N
	    List<Proc> targets = new ArrayList<>();
	    Proc cur = getProc(procCd);
	    if (cur == null) return;

	    targets.add(cur);
	    Proc p = cur;
	    for (int i = 0; i < prevCount; i++) {
	        p = preProc(p.getProcCd());
	        if (p == null) break;
	        targets.add(p);
	    }

	    // ✅ 뒤에서 앞으로(현재에 가까운 공정부터) 쉬프트
	    for (int i = 0; i < targets.size(); i++) {
	        Proc src = targets.get(i); // i=0이 현재, i가 커질수록 더 앞공정
	        shiftOneStep(src, ctx);
	    }
	}
	
	/** 공정 하나를 다음 공정으로 1칸 쉬프트 (가능할 때만) */
	private void shiftOneStep(Proc src, MoveContext ctx) {
	    if (src == null || !src.hasCar()) return;

	    Proc dst = nextProc(src.getProcCd());
	    if (dst == null) return;          // 라인 끝이면 여기서 처리 안 함(별도 IDLE 정책)
	    if (dst.hasCar()) return;         // 다음 공정이 차 있으면 못 민다

	    String body = src.getInBodyNo();
	    if (body == null || body.isEmpty()) return;

	    src.procOut();
	    dst.procIn(body);
	    ctx.addMove(body, dst.getProcCd());
	}
	
	
	
	// 프로세스 공정 뒤에 버퍼구간 자동 채움
	private void bufferShift(String procCd ,MoveContext ctx) {
		List<Proc> buffList = selectNextBuffers(procCd);
		for (int j = buffList.size() - 1; j >= 0; j--) {
			Proc curProc = buffList.get(j);
			Proc preProc = preProc(curProc.getProcCd());
			if ("Buffer".equals(curProc.getProcTyCd())) {
				if (curProc.hasCar()) {
					System.out.println(
							String.format(" %s:%s 해당 공정에 차량이 이미 있습니다.", curProc.getProcCd(), curProc.getInBodyNo()));
					continue;
				} else {
					String bodyNo = preProc.getInBodyNo();
					ctx.addMove(bodyNo, curProc.getProcCd());
					curProc.procIn(bodyNo);
					preProc.procOut();
				}
			}
		}
	}

	// 현재공정기준으로 내앞에 버퍼 공정이 얼마나 있는지
	private List<Proc> selectNextBuffers(String targetProcCd) {
		List<Proc> bufferList = new ArrayList<Proc>();
		List<Proc> list = this.procList;
		int index = indexOfProc(list, targetProcCd);
		if (index < 0) {
			return bufferList;
		}

		for (int i = index + 1; i < list.size(); i++) {
			if ("Buffer".equals(list.get(i).getProcTyCd())) {
				bufferList.add(list.get(i));
			} else {
				break; // 연속이 끊김
			}
		}
		return bufferList;
	}

}
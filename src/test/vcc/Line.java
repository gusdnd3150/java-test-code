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
		if (curIdx < 0)
			return null;
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

	
	public synchronized HMap<String,Object> plcOrLsSignal(String procCd) {
		HMap<String,Object> returnMap = new HMap<String,Object>();
		List<HMap<String, Object>> moveCarList = new ArrayList<>();
		List<HMap<String, Object>> idleList = new ArrayList<>();
		Proc proc = getProc(procCd);
		if (proc == null)
			return null;

		//실공정 뒤에 버퍼구간 체크 후 자동 쉬프트
		bufferShift(procCd, moveCarList, idleList);
		// 현재공정 확인 후 차량이 있을경우 앞으로 쉬프트
		procShift(procCd, moveCarList, idleList);

		// [라인끝 마지막 공정처리]
		// 앞공정이 없으면 라인끝 공정으로봄
		Proc nextProc = nextProc(proc.getProcCd());
		if (nextProc == null) {
			HMap<String, Object> idleData = new HMap<>();
			idleData.put("BODY_NO", proc.getInBodyNo());
			idleData.put("PROC_CD", "IDLE");
			idleList.add(idleData);
			proc.procOut();
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
				HMap<String, Object> bringData = new HMap<>();
				String tempBody = "CAR_" + bringIndex;
				bringData.put("BODY_NO", tempBody);
				bringData.put("PROC_CD", proc.getProcCd());
				moveCarList.add(bringData);
				proc.procIn(tempBody);		
			}

		} else {
			
			if (preProc.hasCar()) { // 이전 공정에 차량이 있으면
				
				if(proc.hasCar()) {
					System.out.println("현재공정에 차가 있어 진입할 수 없습니다.");
					
				}else {
					HMap<String, Object> inData = new HMap<>();
					String bodyNo = preProc.getInBodyNo();
					preProc.procOut();
					proc.procIn(bodyNo);

					inData.put("BODY_NO", bodyNo);
					inData.put("PROC_CD", proc.getProcCd());
					moveCarList.add(inData);					
				}

			}
		}
		returnMap.put("MOVE_CAR", moveCarList);
		returnMap.put("MOVE_IDLE", idleList);
		lineSnapShot();
		return returnMap;
	}
	
	
	public synchronized HMap<String,Object> procIn(String procCd) {
		HMap<String,Object> returnMap = new HMap<String,Object>();
		List<HMap<String, Object>> moveCarList = new ArrayList<>();
		List<HMap<String, Object>> idleList = new ArrayList<>();
		Proc proc = getProc(procCd);
		if (proc == null)
			return null;

		//실공정 뒤에 버퍼구간 체크 후 자동 쉬프트
		bufferShift(procCd, moveCarList, idleList);
		
		// 현재공정 확인 후 차량이 있을경우 앞으로 쉬프트
		procShift(procCd, moveCarList, idleList);

		// [라인끝 마지막 공정처리]
		// 앞공정이 없으면 라인끝 공정으로봄
		Proc nextProc = nextProc(proc.getProcCd());
		if (nextProc == null) {
			HMap<String, Object> idleData = new HMap<>();
			idleData.put("BODY_NO", proc.getInBodyNo());
			idleData.put("PROC_CD", "IDLE");
			idleList.add(idleData);
			proc.procOut();
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
				HMap<String, Object> bringData = new HMap<>();
				String tempBody = "CAR_" + bringIndex;
				bringData.put("BODY_NO", tempBody);
				bringData.put("PROC_CD", proc.getProcCd());
				moveCarList.add(bringData);
				proc.procIn(tempBody);		
			}

		} else {
			
			if (preProc.hasCar()) { // 이전 공정에 차량이 있으면
				
				if(proc.hasCar()) {
					System.out.println("현재공정에 차가 있어 진입할 수 없습니다.");
					
				}else {
					HMap<String, Object> inData = new HMap<>();
					String bodyNo = preProc.getInBodyNo();
					preProc.procOut();
					proc.procIn(bodyNo);

					inData.put("BODY_NO", bodyNo);
					inData.put("PROC_CD", proc.getProcCd());
					moveCarList.add(inData);					
				}

			}
		}
		returnMap.put("MOVE_CAR", moveCarList);
		returnMap.put("MOVE_IDLE", idleList);
		lineSnapShot();
		return returnMap;
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
	
	private void procShift(String procCd ,List<HMap<String,Object>> moveList,List<HMap<String,Object>> idleList) {
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
					
					shiftData.put("BODY_NO", curBody);
					shiftData.put("PROC_CD", nextProc.getProcCd());
					moveList.add(shiftData);
				}
			}
		}
	}
	
	
	
	// 프로세스 공정 뒤에 버퍼구간 자동 채움
	private void bufferShift(String procCd ,List<HMap<String,Object>> moveList,List<HMap<String,Object>> idleList) {
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
					curProc.procIn(bodyNo);
					preProc.procOut();

					HMap<String, Object> bufData = new HMap<>();
					bufData.put("BODY_NO", bodyNo);
					bufData.put("PROC_CD", curProc.getProcCd());
					moveList.add(bufData);
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
			if ("Buffer".equals(list.get(i).getProcCd())) {
				bufferList.add(list.get(i));
			} else {
				break; // 연속이 끊김
			}
		}
		return bufferList;
	}

}
package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

public class TestGoStop {

	String oldData = null;
	String newData = null;
	// String[] aryProcCd = new String[] { "TA0101", "TA0102", "TA0103", "TA0104",
	// "TA0105" }; //
	int aryProcCdPoint[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }; // 공정별 신호정보
	HMap<String, Object> inMap = new HMap<>(); // 각 공정 진입차량
	HMap<String, Object> procLsSigDupChk = new HMap<>(); // 각 공정 LS 신호 중복체크
	HMap<String, Object> dupCheckSignal = new HMap<>(); // ls or plc 각 공정별 신호변과 중복체크
	List<HMap<String, Object>> procList = new ArrayList<>(); // index 0부터 초입공정
	int bringIndex = 1;
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	public TestGoStop() throws IOException {

		loadDbData();
		initDefualData();

		while (true) {
			System.out.println("command:");

			String cmd = br.readLine();

			switch (cmd) {
			case "PLC": // PLC 시그널
				lsSigAll();
				break;
			case "PROC_IN": // 공정진입
				procIn();
				break;
			case "PROC_OUT": // 공정진입
				procOut();
				break;
			case "LS": // 공정진입
				lsSig();
				break;
			case "SNAP": // 메모리확인
				snapshot();
				break;
			case "X":
				return;
			}
		}

	}

	// 기준정보 초기화
	public void initDefualData() {
		for (HMap<String, Object> proc : procList) {
			dupCheckSignal.put(proc.getString("PROC_CD"), "0");
		}
	}

	// 공정진입 처리
	public void procIn() throws IOException {

		String procCd = null;
		String bodyNo = null;

		System.out.println("procCd:");
		procCd = br.readLine();

		System.out.println("bodyNo:");
		bodyNo = br.readLine();

		System.out.println(String.format("PROC_IN = procCd: %s , bodyNO: %s", procCd, bodyNo));
		inMap.put(procCd, bodyNo);
	}

	// 공정진입 처리
	public void procOut() throws IOException {
		String procCd = null;
		String bodyNo = null;

		System.out.println("procCd:");
		procCd = br.readLine();

		System.out.println("bodyNo:");
		bodyNo = br.readLine();

		if (bodyNo.equals(inMap.getString(procCd))) {
			inMap.put(procCd, "");
			System.out.println(String.format("PROC_OUT = procCd: %s , bodyNO: %s", procCd, bodyNo));
		}
	}

	// LS 신호 수신시
	public void lsSig() throws IOException {
		System.out.println("LS procCd:");
		String procCd = br.readLine();

		System.out.println("LS flag: 1 or 0");
		String lsFlag = br.readLine();

		boolean isChange = isChangeLsSig(procCd, lsFlag);

		List<HMap<String, Object>> moveCatList = new ArrayList<>(); //
		List<HMap<String, Object>> idleList = new ArrayList<>(); //

		if (!isChange) {
			return;
		}

		goStop(procCd, lsFlag, moveCatList, idleList);

		System.out.println("============= MOVECAR 전송 리스트===");
		for (HMap<String, Object> hMap : moveCatList) {
			System.out.println(hMap.toString());
		}
		System.out.println("=================================");

		System.out.println("============= IDLE 전송 리스트======");
		for (HMap<String, Object> hMap : idleList) {
			System.out.println(hMap.toString());
		}
		System.out.println("=================================");

		snapshot();
	}

	public void lsSigAll() throws IOException {
		if (null == oldData) {
			oldData = Utility.rpad(oldData, procList.size(), '0');
			; // 디폴트 세팅
		}

		System.out.println("all signal :");
		String scan = br.readLine();
		newData = Utility.rpad(scan, procList.size(), '0');

		List<HMap<String, Object>> moveCatList = new ArrayList<>(); //
		List<HMap<String, Object>> idleList = new ArrayList<>(); //

		for (int i = procList.size() - 1; i >= 0; i--) {
			// char d =newData.charAt(aryProcCdPoint[i]);
			String strSignal = newData.substring(i, i + 1); // 1자리 데이터 가져옴
			HMap<String, Object> mapProc = procList.get(i);
			String strProcCd = mapProc.getString("PROC_CD");

			if (isChangeLsSig(strProcCd, strSignal)) {
				goStop(strProcCd, strSignal, moveCatList, idleList);
			}
		}

		System.out.println("=============MOVECAR 전송 리스트");
		for (HMap<String, Object> hMap : moveCatList) {
			System.out.println(hMap.toString());
		}
		System.out.println("=================================");

		System.out.println("=============IDLE 전송 리스트");
		for (HMap<String, Object> hMap : idleList) {
			System.out.println(hMap.toString());
		}
		System.out.println("=================================");

		snapshot();
	}

	// ls 변화에 따라 처리
	public void goStop(String lsProcCd, String lsSig, List<HMap<String, Object>> moveCatList,
			List<HMap<String, Object>> idleList) {

		for (int i = procList.size() - 1; i >= 0; i--) {
			HMap<String, Object> mapProc = procList.get(i);

			if (mapProc.getString("PROC_CD").equals(lsProcCd) && "1".equals(lsSig)) { // 1= ls 신호 혹은 plc go 신호

				String strProcCd = mapProc.getString("PROC_CD");

				
				
				
				// 공정 초입 혹은 공정 끝부분 신호에따라 차를 끌어올 대상 공정이 달라져야할듯. 플래그로 현재공정기준 앞으로 밀지
				// 뒷 공정차를 현재공정으로 끌어올지 고민해봐야함 현재기준은 해당공정 차량을 앞으로 미는 로직임
				String targetCar = inMap.getString(strProcCd);
				
				if (mapProc != null && targetCar != null && !targetCar.equals("")) {

					HMap<String, Object> inData = new HMap<>();
					inData.put("BODY_NO", targetCar);

					if (mapProc.getString("PROC_CD_NEXT") == null || mapProc.getString("PROC_CD_NEXT").isEmpty()) { // 앞공정이
																													// 비어있으면
																													// 라인끝.
																													// idle
																													// 처리
						inMap.remove(strProcCd);
						inData.put("PROC_CD", "IDLE");
						idleList.add(inData);
						continue;
					}
					
					String nextProcCar = inMap.getString(mapProc.getString("PROC_CD_NEXT")); // 다음 공정 조회
					if (nextProcCar != null && !nextProcCar.isEmpty()) { // 앞 공정에 차량이 있으면 PASS
						String log = String.format("already car in ( %s:%s to %s:%s )", mapProc.getString("PROC_CD"),
								inMap.getString(mapProc.getString("PROC_CD")), mapProc.getString("PROC_CD_NEXT"),
								inMap.getString(mapProc.getString("PROC_CD_NEXT")));
						System.out.println(log);
						continue;
					}

					inData.put("PROC_CD", mapProc.getString("PROC_CD_NEXT"));
					inMap.remove(strProcCd);
					// inMap.put(strProcCd, "OUT");// 현재공정 초기화
					inMap.put(mapProc.getString("PROC_CD_NEXT"), targetCar); // 대상공정으로 바디이관

					moveCatList.add(inData);

				}

				// 이전공정이 없는경우 라인초입 공정임
				if (mapProc.getString("PROC_CD_PRE") == null || mapProc.getString("PROC_CD_PRE").isEmpty()) {
					System.out.println("태그,바코드,브링 처리필요");
					inMap.put(strProcCd, "CAR_" + bringIndex);
					HMap<String, Object> bringData = new HMap<>();
					bringData.put("BODY_NO", "CAR_" + bringIndex);
					bringData.put("PROC_CD", strProcCd);
					moveCatList.add(bringData);
					bringIndex++;
				}
			}

		}

	}

	// 현재 메모리정보 확인
	public void snapshot() {

		String signal = "";
		String procInfo = "";
		String bodyInfo = "";

		for (HMap<String, Object> proc : procList) {
			procInfo += Utility.centerPad(proc.getString("PROC_CD"), 10, ' ') + " ||";
			signal += Utility.centerPad(dupCheckSignal.getString(proc.getString("PROC_CD")), 10, ' ') + " ||";
		}
		for (HMap<String, Object> proc : procList) {
			bodyInfo += Utility.centerPad(inMap.getString(proc.getString("PROC_CD")), 10, ' ') + " ||";
		}
		System.out.println(procInfo);
		System.out.println(signal);
		System.out.println(bodyInfo);
	}

	public HMap<String, Object> getProcMap(String procCd) {
		for (int i = 0; i < procList.size(); i++) {
			if (procList.get(i).getString("PROC_CD").equals(procCd)) {
				return procList.get(i);
			}
		}
		return null;
	}

	public void loadDbData() {

		String driver = "com.tmax.tibero.jdbc.TbDriver";
		String url = "jdbc:tibero:thin:@dev.teia.co.kr:8629:tibero";
		String user = "HMUL61_ADM";
		String pass = "HMUL61_ADM";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// 1. 드라이버 로딩
			Class.forName(driver);
			// 2. DB 연결
			conn = DriverManager.getConnection(url, user, pass);

			// String sql = "SELECT PROC_CD FROM TB_BI_PROC WHERE 1=1 AND LINE_CD = 'FN01'
			// AND PROC_TY_CD ='Process' ORDER BY SORT_NO ASC";
			String sql = "SELECT" + "     PROC_CD"
					+ "    ,LEAD(PROC_CD) OVER (ORDER BY TO_NUMBER(SORT_NO) ASC) AS PROC_CD_NEXT"
					+ "    ,LAG(PROC_CD)  OVER (ORDER BY TO_NUMBER(SORT_NO) ASC) AS PROC_CD_PRE" + " FROM TB_BI_PROC"
					+ " WHERE LINE_CD = 'FN01'" + " AND PROC_TY_CD = 'Process'" + " ORDER BY TO_NUMBER(SORT_NO) ASC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			// 4. 결과 처리
			while (rs.next()) {
				HMap<String, Object> proc = new HMap<>();
				proc.put("PROC_CD", rs.getString("PROC_CD"));
				proc.put("PROC_CD_NEXT", rs.getString("PROC_CD_NEXT"));
				proc.put("PROC_CD_PRE", rs.getString("PROC_CD_PRE"));
				procList.add(proc);
				// String remk = String.format( "procCd:%s, next:%s,
				// pre:%s",rs.getString("PROC_CD"),rs.getString("PROC_CD_NEXT"),rs.getString("PROC_CD_PRE"));
				// System.out.println(remk);
			}
			for (int i = 0; i < procList.size(); i++) {
				System.out.println(String.format("map %s, index %s", procList.get(i).toString(), "" + i));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 5. 자원 정리
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
			}
		}
	}

	// 공정별 중복체크
	public synchronized boolean isChangeLsSig(String procCd, String lsFlag) {
		boolean result = false;

		String flag = dupCheckSignal.getString(procCd);
		if (flag == null) {
			dupCheckSignal.put(procCd, lsFlag);
			return false;
		}

		if (!lsFlag.equals(flag)) {
			result = true;
		}
		dupCheckSignal.put(procCd, lsFlag);

		return result;
	}

}

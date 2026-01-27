package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import test.vcc.Line;
import test.vcc.Proc;

public class TestGoStopClass {

	String oldData = null;
	String newData = null;
	int aryProcCdPoint[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }; // 공정별 신호정보
	public static HMap<String, Object> dupCheckSignal = new HMap<>(); // ls or plc 각 공정별 신호변과 중복체크
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	ConcurrentHashMap<String, Line> lineInfo = new ConcurrentHashMap<>();

	public TestGoStopClass() throws IOException {

		loadDbData();
		initDefualData();

		while (true) {
			System.out.println("command:");

			String cmd = br.readLine().toUpperCase();

			switch (cmd) {
			case "PLC": // PLC 시그널
				lsSigAll();
				break;
			case "PROC_IN": // 공정진입
				procIn();
				break;
			case "LS": // 공정진입
				lsSig();
				break;
			case "SNAP": // 메모리확인
				// snapshot();
				break;
			case "X":
				return;
			}
		}
	}

	// 기준정보 초기화
	public void initDefualData() {

		System.out.println("--Memory update--");
		for (Entry<String, Line> entry : lineInfo.entrySet()) {
			String lineCd = entry.getKey();
			Line line = entry.getValue();
			for (Proc proc : line.getProcList()) {
				dupCheckSignal.put(proc.getProcCd(), "0");
			}
		}
		System.out.println(dupCheckSignal);
	}

	// 공정진입 처리
	public void procIn() throws IOException {

		String procCd = null;
		String lineCd = null;
		String bodyNo = null;

		System.out.println("lineCd:");
		lineCd = br.readLine().toUpperCase();

		System.out.println("procCd:");
		procCd = br.readLine().toUpperCase();

		System.out.println("bodyNo:");
		bodyNo = br.readLine().toUpperCase();

		Line line = lineInfo.get(lineCd);
		Proc proc = line.getProc(procCd);
		proc.procIn(bodyNo);

		System.out.println(String.format("PROC_IN = procCd: %s , bodyNO: %s", procCd, bodyNo));
		line.lineSnapShot();
	}


	// LS 신호 수신시
	public void lsSig() throws IOException {

		System.out.println("LS lineCd:");
		String lineCd = br.readLine().toUpperCase();

		System.out.println("LS procCd:");
		String procCd = br.readLine().toUpperCase();

		System.out.println("LS flag: 1 or 0");
		String lsFlag = br.readLine().toUpperCase();

		boolean isChange = isChangeLsSig(procCd, lsFlag);

//		if (!isChange) {
//			return;
//		}

		if ("1".equals(lsFlag)) {
			System.out.println("LS GO");
			Line line = lineInfo.get(lineCd);
			HMap<String, Object> moveMap = line.plcOrLsSignal(procCd);

			if (moveMap != null) {
				List<HMap<String, Object>> moveList = moveMap.getList("MOVE_CAR");
				List<HMap<String, Object>> idleList = moveMap.getList("MOVE_IDLE");

				if (moveList.size() > 0) {
					System.out.println("============= MOVECAR 전송 리스트===");
					for (HMap<String, Object> hMap : moveList) {
						System.out.println(hMap.toString());
					}
					System.out.println("=================================");
				}

				if (idleList.size() > 0) {
					System.out.println("============= IDLE 전송 리스트======");
					for (HMap<String, Object> hMap : idleList) {
						System.out.println(hMap.toString());
					}
					System.out.println("=================================");
				}
			}
		}

	}

	public void lsSigAll() throws IOException {
		if (null == oldData) {
			oldData = Utility.rpad(oldData, 20, '0');
			; // 디폴트 세팅
		}

		System.out.println("all signal :");
		String scan = br.readLine();
		newData = Utility.rpad(scan, 20, '0');

		List<HMap<String, Object>> moveCatList = new ArrayList<>(); //
		List<HMap<String, Object>> idleList = new ArrayList<>(); //

//		for (int i = procList.size() - 1; i >= 0; i--) {
//			String strSignal = newData.substring(i, i + 1); // 1자리 데이터 가져옴
//			HMap<String, Object> mapProc = procList.get(i);
//			String strProcCd = mapProc.getString("PROC_CD");
//
//			if (isChangeLsSig(strProcCd, strSignal)) {
//				//goStop("TEST",strProcCd, strSignal, moveCatList, idleList);
//			}
//		}

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
			String sql = "SELECT " + "    PROC_CD, " + "    PROC_TY_CD, " + "    'IN' AS INOUT_TY " + "FROM TB_BI_PROC "
					+ "WHERE LINE_CD = 'TEST' " + "--AND PROC_TY_CD = 'Process' " + "ORDER BY TO_NUMBER(SORT_NO) ASC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCnt = meta.getColumnCount();

			List<Proc> procList = new ArrayList<>();

			// 4. 결과 처리
			while (rs.next()) {
				HMap<String, Object> proc = new HMap<>();
				for (int i = 1; i <= colCnt; i++) {
					String colName = meta.getColumnLabel(i); // ⭐ alias 포함
					String value = rs.getString(colName);
					proc.put(colName, value);
				}
				Proc procInfo = new Proc(proc.getString("PROC_CD"), proc.getString("PROC_TY_CD"),
						proc.getInt("SORT_NO"));
				System.out.println(String.format("%s", procInfo.toString()));
				procList.add(procInfo);
			}
			Line line = new Line("TEST", "GO_AND_STOP", procList);
			lineInfo.put("TEST", line);

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

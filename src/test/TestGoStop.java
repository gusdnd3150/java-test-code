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
import java.util.Stack;

public class TestGoStop {

	String oldData = null;
	String newData = null;
	String[] aryProcCd = new String[] { "TA0101", "TA0102", "TA0103", "TA0104", "TA0105" }; // index 0부터 초입공정
	int aryProcCdPoint[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }; // 공정별 신호정보
	HMap<String, Object> inMap = new HMap<>(); // 각 공정 진입차량
	List<HMap<String,Object>> procList = new ArrayList<>(); // 
	
	public TestGoStop() throws IOException {

		loadDbData();

		while (true) {
			System.out.println("command:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String cmd = br.readLine();
			
			switch (cmd) {
			case "all":
				testGoAndStop();
				break;
			case "x":
				return;
			}
		}

	}

	public void testGoAndStop() {
		System.out.println("signal data:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Stack<HMap<String, Object>> stack = new Stack<>();

		for (int i = 0; i < aryProcCd.length; i++) {

			if (i == 0) {
				inMap.put(aryProcCd[i], "CAR 123456");
			} else {
				inMap.put(aryProcCd[i], "");
			}
		}

		if (null == oldData) {
			oldData = "000000000"; // 임시처리값
		}

		while (true) {
			try {
				String cmd;
				cmd = br.readLine();

				if ("x".equalsIgnoreCase(cmd)) {
					System.out.println("종료 all mode");
					break;
				}
				// 전체 쉬프트정보가 통으로 바뀔때
				newData = cmd;
				// 기존 신호랑 다를경우
				if (!oldData.equals(newData)) {
					List<HMap<String,Object>> moveCatList = new ArrayList<>(); // 
//						strDatas.charAt(aryProcCdPoint[i])
					System.out.println("입력값: " + cmd);
					System.out.println("old 데이터: " + oldData);
					System.out.println("new 데이터: " + newData);
					for (int i = 0; i < aryProcCd.length; i++) {
						// char d =newData.charAt(aryProcCdPoint[i]);
						String strSignal = newData.substring(i, i + 1); // 1자리 데이터 가져옴
						String strProcCd = aryProcCd[i];

						if ("1".equals(strSignal)) { // 1= ls 신호 혹은 plc go 신호
							System.out.println(strProcCd);
							System.out.println(strSignal);
							String targetCar = inMap.getString(strProcCd);
							HMap<String,Object> procMap = getProcMap(strProcCd);
							if (procMap != null && targetCar != null && !targetCar.equals("")) {
								HMap<String,Object> inData = new HMap<>();
								inData.put("BODY_NO", stack);
								inData.put("PROC_CD", procMap.getString("PROC_CD_NEXT"));
								moveCatList.add(procMap);
							}
						}
					}
				}

				oldData = newData;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public HMap<String,Object> getProcMap(String procCd){
		for(int i=0;i<procList.size();i++) {
			if(procList.get(i).getString("PROC_CD").equals(procCd)) {
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

			//String sql = "SELECT PROC_CD FROM TB_BI_PROC WHERE 1=1 AND LINE_CD = 'FN01' AND PROC_TY_CD ='Process' ORDER BY SORT_NO ASC";
			String sql  = "SELECT "
			+ "	PROC_CD "
			+ "	,LEAD(PROC_CD) OVER(ORDER BY PROC_CD) AS PROC_CD_NEXT"
			+ "	,LAG(PROC_CD) OVER(ORDER BY PROC_CD)  AS PROC_CD_PRE"
			+ " FROM TB_BI_PROC "
			+ " WHERE 1=1 "
			+ " AND LINE_CD = 'FN01' "
			+ " AND PROC_TY_CD ='Process'"
			+ " ORDER BY SORT_NO ASC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			// 4. 결과 처리
			while (rs.next()) {
				HMap<String,Object> proc = new HMap<>();
				proc.put("PROC_CD", rs.getString("PROC_CD"));
				proc.put("PROC_CD_NEXT", rs.getString("PROC_CD_NEXT"));
				proc.put("PROC_CD_PRE", rs.getString("PROC_CD_PRE"));
				procList.add(proc);
				//String remk = String.format( "procCd:%s, next:%s, pre:%s",rs.getString("PROC_CD"),rs.getString("PROC_CD_NEXT"),rs.getString("PROC_CD_PRE"));
				//System.out.println(remk);
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

}

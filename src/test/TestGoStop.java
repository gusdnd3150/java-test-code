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
	String[] aryProcCd = new String[] { "TA0101", "TA0102", "TA0103", "TA0104", "TA0105" }; // 
	int aryProcCdPoint[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }; // 공정별 신호정보
	HMap<String, Object> inMap = new HMap<>(); // 각 공정 진입차량
	HMap<String, Object> dupCheckSignal = new HMap<>(); // ls or plc 각 공정별 신호변과 중복체크
	List<HMap<String,Object>> procList = new ArrayList<>(); //  index 0부터 초입공정
	int bringIndex = 1;
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	public TestGoStop() throws IOException {

		loadDbData();

		while (true) {
			System.out.println("command:");
			
			String cmd = br.readLine();
			
			switch (cmd) {
			case "ALL": // PLC 시그널
				testGoAndStop();
				break;
			case "PROC_IN": // 공정진입
				procIn();
				break;

			case "X":
				return;
			}
		}

	}
	
	public void procIn() throws IOException {
		
		String procCd = null;
		String bodyNo = null;
		
		System.out.println("procCd:");
		procCd = br.readLine();
		
		System.out.println("bodyNo:");
		bodyNo = br.readLine();
		
		System.out.println(String.format("PROC_IN = procCd: %s , bodyNO: %s", procCd,bodyNo));
		inMap.put(procCd, bodyNo);
	}

	@SuppressWarnings("null")
	public void testGoAndStop() {
		
		if (null == oldData) {
			oldData = Utility.rpad(oldData, procList.size(), '0');; // 디폴트 세팅
		}

		while (true) {
			try {
				System.out.println("signal data:");
				String cmd = br.readLine();

				if ("X".equalsIgnoreCase(cmd)) {
					System.out.println("종료 all mode");
					break;
				}
				if ("SNAP".equalsIgnoreCase(cmd)) {
					snapshot();
					continue;
				}
				// 전체 쉬프트정보가 통으로 바뀔때
				//newData = cmd;
				newData = Utility.rpad(cmd, procList.size(), '0');
				// 기존 신호랑 다를경우
				if (!oldData.equals(newData)) {
					List<HMap<String,Object>> moveCatList = new ArrayList<>(); // 
					List<HMap<String,Object>> idleList = new ArrayList<>(); // 
					
					for(int i = procList.size() - 1; i >= 0; i--) {	
						// char d =newData.charAt(aryProcCdPoint[i]);
						String strSignal = newData.substring(i, i + 1); // 1자리 데이터 가져옴
						
						
						if ("1".equals(strSignal)) { // 1= ls 신호 혹은 plc go 신호
							
							HMap<String,Object> mapProc =  procList.get(i);
							String strProcCd = mapProc.getString("PROC_CD");
							//System.out.println(String.format("%s ,%s ", strProcCd,strSignal));
							
							String targetCar = inMap.getString(strProcCd);
							if (mapProc != null && targetCar != null && !targetCar.equals("")) {
								
								HMap<String,Object> inData = new HMap<>();
								inData.put("BODY_NO", targetCar);
								
								if(mapProc.getString("PROC_CD_NEXT") == null || mapProc.getString("PROC_CD_NEXT").isEmpty()) { // 앞공정이 비어있으면 라인끝. idle 처리
									inMap.remove(strProcCd);
									inData.put("PROC_CD", "IDLE");
									idleList.add(mapProc);
									continue;
								}
								
								
								String nextProcCar = inMap.getString(mapProc.getString("PROC_CD_NEXT")); // 다음 공정 조회
								if(nextProcCar != null && !nextProcCar.isEmpty()) { // 앞 공정에 차량이 있으면 PASS
									String log = String.format("already car in ( %s:%s to %s:%s )",mapProc.getString("PROC_CD"),inMap.getString(mapProc.getString("PROC_CD")),mapProc.getString("PROC_CD_NEXT"),inMap.getString(mapProc.getString("PROC_CD_NEXT")));
									System.out.println(log);
									continue;
								}
								
								inData.put("PROC_CD", mapProc.getString("PROC_CD_NEXT"));
								inMap.remove(strProcCd);
								//inMap.put(strProcCd, "OUT");// 현재공정 초기화
								inMap.put(mapProc.getString("PROC_CD_NEXT"), targetCar); // 대상공정으로 바디이관
								
								moveCatList.add(inData);
								
							}
							
							if(mapProc.getString("PROC_CD_PRE") == null || mapProc.getString("PROC_CD_PRE").isEmpty()) { // 이전공정이 없는경우 라인초입 공정임
								System.out.println("태그,바코드,브링 처리필요");
								inMap.put(strProcCd,"CAR_"+bringIndex);
								HMap<String,Object> bringData = new HMap<>();
								bringData.put("BODY_NO", "CAR_"+bringIndex);
								bringData.put("PROC_CD", strProcCd);
								moveCatList.add(bringData);
								bringIndex++;
							}
						}
					}
					
					
					System.out.println("=============MOVECAR 전송 리스트");
					for (HMap<String, Object> hMap : moveCatList) {
						System.out.println(hMap.toString());
					}
					System.out.println("=================================");
				}

				oldData = newData;
				snapshot();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void snapshot() {
		
		String procInfo = "";
		String bodyInfo = "";
		
		for (HMap<String,Object> proc : procList) {
			procInfo += Utility.centerPad(proc.getString("PROC_CD"), 10,' ')+" ||";
		}
		for (HMap<String,Object> proc : procList) {
			bodyInfo += Utility.centerPad(inMap.getString(proc.getString("PROC_CD")), 10, ' ')+" ||";
		}
		System.out.println(procInfo);
		System.out.println(bodyInfo);
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
			String sql  = "SELECT"
					+ "     PROC_CD"
					+ "    ,LEAD(PROC_CD) OVER (ORDER BY TO_NUMBER(SORT_NO) ASC) AS PROC_CD_NEXT"
					+ "    ,LAG(PROC_CD)  OVER (ORDER BY TO_NUMBER(SORT_NO) ASC) AS PROC_CD_PRE"
					+ " FROM TB_BI_PROC"
					+ " WHERE LINE_CD = 'FN01'"
					+ " AND PROC_TY_CD = 'Process'"
					+ " ORDER BY TO_NUMBER(SORT_NO) ASC";
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
			for(int i=0;i< procList.size();i++) {
				System.out.println(String.format("map %s, index %s", procList.get(i).toString(), ""+i));
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

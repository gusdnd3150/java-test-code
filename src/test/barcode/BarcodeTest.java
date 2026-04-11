package test.barcode;

import test.Main;
import test.Utilitys.HMap;
import test.Utilitys.JdbcTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BarcodeTest {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	HMap<String,Object> lastBarcode = new HMap<>(); // 실시간 공정별 하나
	HMap<String,List<HMap<String,Object>>> unScopedProcBarcodeList = new HMap<>(); // 공정별 3개 저장
	JdbcTest database;

    public BarcodeTest() throws Exception {

		database = new JdbcTest();
		//hasTargetCarWithinNextScope(lastBcdData.getString(strProcCd),strCurMesProdSeq,iniManager.bcdScopeChkCnt)

        while (true) {
			//System.out.println("COMMAND:::");
			//String cmd = br.readLine().toUpperCase();

			String mesProdSeq = params("MES_PROD_SEQ");
			//String procCd = params("PROC_CD");
			String procCd = "TR01B1";
			logicBarcode(procCd,mesProdSeq);
//			switch (cmd) {
//			case "BCD": // PLC 시그널
//
//			    break;
//			case "X":
//				return;
//			}
		}
    }


	public void logicBarcode(String procCd,String mesProdSeq) throws Exception {
		boolean rslt = hasTargetCarWithinNextScope(lastBarcode.getString(procCd),mesProdSeq,4);
		if(rslt){
			System.out.println(String.format("logicBarcode ::send [MOVE_CAR] mesProdSeq=%s , procCd=%s", mesProdSeq,procCd));
			lastBarcode.put(procCd, mesProdSeq); // 마지막 진입 데이터 저장
			clearScopeData(procCd); // 정상처리시 초기화
		}else{
			System.out.println(String.format("logicBarcode :: none scope !!!!! %s, procCd=%s", mesProdSeq,procCd));
			addUnScopeData(procCd,mesProdSeq); // 이상데이터 공정별 3개까지 누적 체크

			//// !!! 여기에 3대분을 가지고 확인을 해야함
			boolean chkRslt = checkAndRecoverFromUnScopeData(procCd,lastBarcode.getString(procCd));
			if(chkRslt){
				System.out.println(String.format("logicBarcode chkRslt true ::send [MOVE_CAR] mesProdSeq=%s , procCd=%s", mesProdSeq,procCd));
				lastBarcode.put(procCd, mesProdSeq); // 마지막 진입 데이터 저장
			}
		}

		if (unScopedProcBarcodeList.get(procCd) != null) {
			System.out.println(String.format("unScopedProcBarcodeList size  %s", unScopedProcBarcodeList.get(procCd).size()));
		}
	}

	
	public String params(String type) throws IOException {
		System.out.println(type.toUpperCase()+"=");
		return br.readLine();
	}


	public boolean checkAndRecoverFromUnScopeData(String procCd, String lastMesSeq){
		List<HMap<String,Object>> unScopeData = unScopedProcBarcodeList.get(procCd);
		if (unScopeData == null || unScopeData.size() != 3) return false;

		// 들어온 순서 그대로 유지
		List<String> storedSeqs = unScopeData.stream()
			.map(s -> s.getString("MES_PROD_SEQ"))
			.collect(Collectors.toList());

		// lastMesSeq 이후 최대 20대 조회
		String query =
			"SELECT MES_PROD_SEQ FROM ( " +
			"    SELECT ROWNUM AS NUM, A.* " +
			"    FROM ( " +
			"        SELECT MES_PROD_SEQ " +
			"        FROM TB_IF_MES_PROD " +
			"        WHERE MES_PROD_SEQ > ? " +
			"        AND CAR_CD NOT IN ('888') " +
			"        ORDER BY MES_PROD_SEQ ASC " +
			"    ) A " +
			") WHERE NUM <= 20";

		List<HMap<String,Object>> checkMes = database.selectData(query, lastMesSeq);

		List<String> dbSeqs = checkMes.stream()
			.map(s -> s.getString("MES_PROD_SEQ"))
			.collect(Collectors.toList());

		// 20대 안에서 storedSeqs[0] 위치를 찾아 3대 연속 일치 확인
		int startIdx = dbSeqs.indexOf(storedSeqs.get(0));
		if (startIdx == -1 || startIdx + 2 >= dbSeqs.size()) {
			System.out.println("checkAndRecover mismatch :: stored=" + storedSeqs + " :: db=" + dbSeqs);
			return false;
		}

		for (int i = 0; i < 3; i++) {
			if (!storedSeqs.get(i).equals(dbSeqs.get(startIdx + i))) {
				System.out.println("checkAndRecover mismatch :: stored=" + storedSeqs + " :: db=" + dbSeqs);
				return false;
			}
		}
		return true;
	}

	public synchronized void clearScopeData(String procCd){
		unScopedProcBarcodeList.remove(procCd); // 정상처리시 초기화
	}

	public synchronized void addUnScopeData(String procCd, String mesProdSeq){
		List<HMap<String,Object>> unScopeData = unScopedProcBarcodeList.get(procCd);
		HMap<String,Object> unScoped = new HMap<>();
		unScoped.put("MES_PROD_SEQ", mesProdSeq);
		if (unScopeData == null) {
			List<HMap<String,Object>> procList = new ArrayList<>();

			procList.add(unScoped);
			unScopedProcBarcodeList.put(procCd, procList);
			return;
		}
		if (unScopeData.size() >= 3){ // 3개 까지만 저장
			unScopeData.remove(0);
		}
		unScopeData.add(unScoped);
	}

	public boolean hasTargetCarWithinNextScope(String lastMesSeq,String nextMesSeq, int scope) throws Exception {
		if(scope == 0) return true; // 별도의 설정값이 없을 경우
		if(lastMesSeq == null) return true; // 프로그램 재기동시 초기화
		String query =
			"SELECT M.* FROM ( " +
			"    SELECT ROWNUM AS NUM, A.* " +
			"    FROM ( " +
			"        SELECT MES_PROD_SEQ, BODY_NO, DP_CMT " +
			"        FROM TB_IF_MES_PROD " +
			"        WHERE MES_PROD_SEQ > ? " +
			"        AND CAR_CD NOT IN ('888') " +
			"        ORDER BY MES_PROD_SEQ ASC " +
			"    ) A " +
			"    WHERE 1=1 " +
			") M " +
			"WHERE 1=1 " +
			"AND NUM <= ? " +
			"AND MES_PROD_SEQ = ?";
		List<HMap<String,Object>> scopeCar =  database.selectData(query,lastMesSeq,scope,nextMesSeq);
		if(scopeCar.size() > 0) {
			System.out.println(String.format("hasTargetCarWithinNextScope :: :: scopeCar=%s", scopeCar.get(0)));
			return true;
		}else{
			System.out.println(String.format("hasTargetCarWithinNextScope :: :: NONE scopeCar=%s",""));
		}
		return false;
	}
}

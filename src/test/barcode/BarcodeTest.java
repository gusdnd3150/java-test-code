package test.barcode;

import test.Main;
import test.Utilitys.HMap;
import test.Utilitys.JdbcTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
			String procCd = params("PROC_CD");
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
			System.out.println(String.format("logicBarcode ::send move_car mesProdSeq=%s , procCd=%s", mesProdSeq,procCd));
			lastBarcode.put(procCd, mesProdSeq);
			unScopedProcBarcodeList.remove(procCd); // 정상처리시 초기화
		}else{
			System.out.println(String.format("logicBarcode :: none scope %s, procCd=%s", mesProdSeq,procCd));
			addUnScopeData(procCd,mesProdSeq); // 이상데이터 공정별 3개까지 누적 체크
		}
	}

	
	public String params(String type) throws IOException {
		System.out.println(type.toUpperCase()+"=");
		return br.readLine();
	}
	

	public void addUnScopeData(String procCd, String mesProdSeq){
		List<HMap<String,Object>> unScopeData = unScopedProcBarcodeList.get(procCd);
		HMap<String,Object> unScoped = new HMap<>();
		unScoped.put("MES_PROD_SEQ", mesProdSeq);

		if (unScopeData == null) {
			List<HMap<String,Object>> procList = new ArrayList<>();

			procList.add(unScoped);
			unScopedProcBarcodeList.put(procCd, procList);
			return;
		}

		if (unScopeData.size() > 3){
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
			"        AND CAR_CD NOT IN ('999','888') " +
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
			System.out.println(String.format("hasTargetCarWithinNextScope :: :: scopeCar=%",""));
		}
		return false;
	}
}

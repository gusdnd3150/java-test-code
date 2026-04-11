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
    int recoverChkCnt = 3;
    int scopeCnt = 3;

    public BarcodeTest() throws Exception {
        database = new JdbcTest();
        while (true) {
            String mesProdSeq = params("MES_PROD_SEQ");
            String procCd = "TR01B1";
            logicBarcode(procCd, mesProdSeq);
        }
    }

    /** 테스트용 생성자 - while 루프 없이 로직만 사용 */
    BarcodeTest(JdbcTest mockDatabase) {
        this.database = mockDatabase;
    }

    public void logicBarcode(String procCd, String mesProdSeq) throws Exception {
        boolean scopeOk = hasTargetCarWithinNextScope(lastBarcode.getString(procCd), mesProdSeq, scopeCnt);
        tryCommitCar(procCd, mesProdSeq, scopeOk);

        List<HMap<String,Object>> unScope = unScopedProcBarcodeList.get(procCd);
        if (unScope != null) {
            System.out.printf("[logicBarcode] unScope size=%d%n", unScope.size());
        }
    }

    private boolean tryCommitCar(String procCd, String mesProdSeq, boolean scopeOk) {
        if (scopeOk) {
            log(procCd, mesProdSeq, "scope OK → MOVE_CAR");
            commitCar(procCd, mesProdSeq);
            return true;
        }

        log(procCd, mesProdSeq, "scope MISS → unScope 누적");
        addUnScopeData(procCd, mesProdSeq);

        if (checkAndRecoverFromUnScopeData(procCd, lastBarcode.getString(procCd))) {
            log(procCd, mesProdSeq, "recover OK → MOVE_CAR");
            commitCar(procCd, mesProdSeq);
            return true;
        }
        return false;
    }

    private void commitCar(String procCd, String mesProdSeq) {
        lastBarcode.put(procCd, mesProdSeq);
        clearScopeData(procCd);
    }

    public String params(String type) throws IOException {
        System.out.println(type.toUpperCase() + "=");
        return br.readLine();
    }

    public boolean checkAndRecoverFromUnScopeData(String procCd, String lastMesSeq) {
        List<HMap<String,Object>> unScopeData = unScopedProcBarcodeList.get(procCd);
        if (unScopeData == null || unScopeData.size() < recoverChkCnt) return false;

        // 들어온 순서 그대로 유지 - 스캔 순서가 DB 연속 순서와 일치해야 함
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

        List<String> dbSeqs = database.selectData(query, lastMesSeq).stream()
            .map(s -> s.getString("MES_PROD_SEQ"))
            .collect(Collectors.toList());

        // 20대 안에서 첫 번째 stored SEQ 위치를 찾아 recoverChkCnt 대 연속 일치 확인
        int startIdx = dbSeqs.indexOf(storedSeqs.get(0));
        if (startIdx == -1 || startIdx + recoverChkCnt > dbSeqs.size()) {
            System.out.println("[checkAndRecover] FAIL :: stored=" + storedSeqs + " :: db=" + dbSeqs);
            return false;
        }

        for (int i = 0; i < recoverChkCnt; i++) {
            if (!storedSeqs.get(i).equals(dbSeqs.get(startIdx + i))) {
                System.out.println("[checkAndRecover] FAIL :: stored=" + storedSeqs + " :: db=" + dbSeqs);
                return false;
            }
        }
        System.out.println("[checkAndRecover] OK :: stored=" + storedSeqs);
        return true;
    }

    public synchronized void clearScopeData(String procCd) {
        unScopedProcBarcodeList.remove(procCd);
    }

    public synchronized void addUnScopeData(String procCd, String mesProdSeq) {
        List<HMap<String,Object>> unScopeData = unScopedProcBarcodeList.get(procCd);
        HMap<String,Object> unScoped = new HMap<>();
        unScoped.put("MES_PROD_SEQ", mesProdSeq);
        if (unScopeData == null) {
            List<HMap<String,Object>> procList = new ArrayList<>();
            procList.add(unScoped);
            unScopedProcBarcodeList.put(procCd, procList);
            return;
        }
        if (unScopeData.size() >= recoverChkCnt) {
            unScopeData.remove(0);
        }
        unScopeData.add(unScoped);
    }

    public boolean hasTargetCarWithinNextScope(String lastMesSeq, String nextMesSeq, int scope) {
        if (scope == 0) return true;      // 별도의 설정값이 없을 경우
        if (lastMesSeq == null) return true; // 프로그램 재기동시 초기화
        String query =
            "SELECT M.* FROM ( " +
            "    SELECT ROWNUM AS NUM, A.* " +
            "    FROM ( " +
            "        SELECT MES_PROD_SEQ, BODY_NO, DP_CMT " +
            "        FROM TB_IF_MES_PROD " +
            "        WHERE MES_PROD_SEQ > ? " +
            "        AND CAR_CD NOT IN ('888') " + // 999도 있으나, 테스트때문에
            "        ORDER BY MES_PROD_SEQ ASC " +
            "    ) A " +
            ") M " +
            "WHERE NUM <= ? " +
            "AND MES_PROD_SEQ = ?";
        List<HMap<String,Object>> scopeCar = database.selectData(query, lastMesSeq, scope, nextMesSeq);
        if (!scopeCar.isEmpty()) {
            System.out.println("[hasTargetCar] OK :: " + scopeCar.get(0));
            return true;
        }
        System.out.println("[hasTargetCar] MISS :: seq=" + nextMesSeq + ", last=" + lastMesSeq + ", scope=" + scope);
        return false;
    }

    private void log(String procCd, String mesProdSeq, String msg) {
        System.out.printf("[logicBarcode] procCd=%s seq=%s → %s%n", procCd, mesProdSeq, msg);
    }
}
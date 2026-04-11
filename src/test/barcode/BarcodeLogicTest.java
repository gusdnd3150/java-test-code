package test.barcode;

import test.Utilitys.HMap;
import test.Utilitys.JdbcTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BarcodeTest 로직 검증용 테스트 러너
 *
 * Mock DB: TB_IF_MES_PROD 시뮬레이션 (실제 DB 연결 불필요)
 *
 * 시나리오:
 *   1. 정상 스캔 (scope OK)
 *   2. scope MISS 후 연속 3대 → recover 성공
 *   3. scope MISS 후 갭 있는 3대 → recover 실패
 *   4. scope MISS 후 20대 밖 → recover 실패
 */
public class BarcodeLogicTest {

    // 실제 시퀀스 값 기반 Mock DB 데이터 (오름차순 정렬 상태로 유지)
    static final List<String> DB_SEQS = Arrays.asList(
        "20250526004146000",
        "20250526004147000",
        "20250526004148000",
        "20250526004149000",
        "20250526004150000",
        "20250526004151000",
        "20250526004152000",
        "20250526004153000",
        "20250526004154000",
        "20250526004155000",
        "20250526004156000",
        "20250526004157000",
        "20250526004158000",
        "20250526004159000",
        "20250526004160000",
        "20250526004161000",
        "20250526004162000",
        "20250526004163000",
        "20250526004164000",
        "20250526004165000",
        "20250526004166000",
        "20250526004167000",
        "20250526004168000",
        "20250526004169000",
        "20250526004170000",
        "20250526004171000",
        "20250526004172000",
        "20250526004173000"
    );

    /**
     * Mock DB: 쿼리 파라미터 개수로 어떤 쿼리인지 구분
     *
     * params 1개 → checkAndRecoverFromUnScopeData 쿼리
     *   WHERE MES_PROD_SEQ > ? ... NUM <= 20
     *
     * params 3개 → hasTargetCarWithinNextScope 쿼리
     *   WHERE MES_PROD_SEQ > ? ... NUM <= ? AND MES_PROD_SEQ = ?
     */
    static JdbcTest createMockDb() {
        return new JdbcTest(true) {
            @Override
            public List<HMap<String, Object>> selectData(String query, Object... params) {
                if (params.length == 1) {
                    // checkAndRecover: SEQ > lastMesSeq, limit 20
                    String lastMesSeq = String.valueOf(params[0]);
                    return DB_SEQS.stream()
                        .filter(seq -> seq.compareTo(lastMesSeq) > 0)
                        .limit(20)
                        .map(seq -> {
                            HMap<String, Object> row = new HMap<>();
                            row.put("MES_PROD_SEQ", seq);
                            return row;
                        })
                        .collect(Collectors.toList());

                } else if (params.length == 3) {
                    // hasTargetCar: SEQ > lastMesSeq, limit scope, filter SEQ == nextMesSeq
                    String lastMesSeq  = String.valueOf(params[0]);
                    int    scope       = Integer.parseInt(String.valueOf(params[1]));
                    String nextMesSeq  = String.valueOf(params[2]);
                    return DB_SEQS.stream()
                        .filter(seq -> seq.compareTo(lastMesSeq) > 0)
                        .limit(scope)
                        .filter(seq -> seq.equals(nextMesSeq))
                        .map(seq -> {
                            HMap<String, Object> row = new HMap<>();
                            row.put("MES_PROD_SEQ", seq);
                            row.put("BODY_NO", "BODY_" + seq.substring(12));
                            row.put("DP_CMT", "");
                            return row;
                        })
                        .collect(Collectors.toList());
                }
                return List.of();
            }
        };
    }

    public static void main(String[] args) throws Exception {
        int pass = 0, fail = 0;

        // ──────────────────────────────────────────────
        // 시나리오 1: 정상 스캔 (scope OK)
        // lastMesSeq=...155, scope=4, 연속 스캔
        // ──────────────────────────────────────────────
        {
            String title = "시나리오1: 정상 스캔 (scope OK)";
            BarcodeTest bt = new BarcodeTest(createMockDb());
            String procCd = "TR01B1";
            bt.lastBarcode.put(procCd, "20250526004155000");

            bt.logicBarcode(procCd, "20250526004156000"); // NUM=1 → OK
            bt.logicBarcode(procCd, "20250526004157000"); // NUM=1 → OK
            bt.logicBarcode(procCd, "20250526004158000"); // NUM=1 → OK

            boolean ok = bt.unScopedProcBarcodeList.get(procCd) == null
                      && "20250526004158000".equals(bt.lastBarcode.getString(procCd));
            print(title, ok, "unScope 없음 + lastBarcode 정상 갱신");
            if (ok) pass++; else fail++;
        }

        // ──────────────────────────────────────────────
        // 시나리오 2: scope MISS → 연속 3대 → recover 성공
        // lastMesSeq=...155, scope=4
        // ...160(NUM=5) ...161(NUM=6) ...162(NUM=7) → 모두 scope MISS
        // 저장된 3대 [160,161,162] = DB 20대 안의 연속 구간 → true
        // ──────────────────────────────────────────────
        {
            String title = "시나리오2: scope MISS 연속 3대 → recover 성공";
            BarcodeTest bt = new BarcodeTest(createMockDb());
            String procCd = "TR01B1";
            bt.lastBarcode.put(procCd, "20250526004155000");

            bt.logicBarcode(procCd, "20250526004160000"); // NUM=5 > scope 4 → MISS
            bt.logicBarcode(procCd, "20250526004161000"); // MISS
            bt.logicBarcode(procCd, "20250526004162000"); // MISS → recover 시도

            boolean ok = "20250526004162000".equals(bt.lastBarcode.getString(procCd))
                      && bt.unScopedProcBarcodeList.get(procCd) == null;
            print(title, ok, "recover 후 lastBarcode=162, unScope 초기화");
            if (ok) pass++; else fail++;
        }

        // ──────────────────────────────────────────────
        // 시나리오 3: scope MISS → 갭 있는 3대 → recover 실패
        // 저장: [160, 162, 163] → 161 빠짐 → false
        // ──────────────────────────────────────────────
        {
            String title = "시나리오3: scope MISS 갭 있는 3대 → recover 실패";
            BarcodeTest bt = new BarcodeTest(createMockDb());
            String procCd = "TR01B1";
            bt.lastBarcode.put(procCd, "20250526004155000");

            // 161을 건너뛰고 스캔
            bt.addUnScopeData(procCd, "20250526004160000");
            bt.addUnScopeData(procCd, "20250526004162000"); // 161 건너뜀
            bt.addUnScopeData(procCd, "20250526004163000");

            boolean chkRslt = bt.checkAndRecoverFromUnScopeData(procCd, "20250526004155000");
            boolean ok = !chkRslt; // recover는 실패해야 정상
            print(title, ok, "161 갭 존재 → recover false 기대");
            if (ok) pass++; else fail++;
        }

        // ──────────────────────────────────────────────
        // 시나리오 4: scope MISS → 20대 밖 → recover 실패
        // lastMesSeq=...146, 20대 = [147~166]
        // 저장: [170, 171, 172] → 20대 밖 → startIdx=-1 → false
        // ──────────────────────────────────────────────
        {
            String title = "시나리오4: scope MISS 20대 밖 → recover 실패";
            BarcodeTest bt = new BarcodeTest(createMockDb());
            String procCd = "TR01B1";

            bt.addUnScopeData(procCd, "20250526004170000");
            bt.addUnScopeData(procCd, "20250526004171000");
            bt.addUnScopeData(procCd, "20250526004172000");

            boolean chkRslt = bt.checkAndRecoverFromUnScopeData(procCd, "20250526004146000");
            boolean ok = !chkRslt; // 20대 밖이므로 실패해야 정상
            print(title, ok, "20대(147~166) 밖 170~ → recover false 기대");
            if (ok) pass++; else fail++;
        }

        // ──────────────────────────────────────────────
        // 시나리오 5: scope MISS → 순서 뒤바뀐 3대 → recover 실패
        // 스캔 순서: [162, 160, 161] → DB 순서와 불일치 → false
        // ──────────────────────────────────────────────
        {
            String title = "시나리오5: scope MISS 순서 뒤바뀐 3대 → recover 실패";
            BarcodeTest bt = new BarcodeTest(createMockDb());
            String procCd = "TR01B1";

            bt.addUnScopeData(procCd, "20250526004162000"); // 순서 뒤바뀜
            bt.addUnScopeData(procCd, "20250526004160000");
            bt.addUnScopeData(procCd, "20250526004161000");

            boolean chkRslt = bt.checkAndRecoverFromUnScopeData(procCd, "20250526004155000");
            boolean ok = !chkRslt;
            print(title, ok, "스캔 순서 [162,160,161] ≠ DB 순서 [162,163,164] → false 기대");
            if (ok) pass++; else fail++;
        }

        System.out.println("──────────────────────────────");
        System.out.printf("결과: %d/%d PASS%n", pass, pass + fail);
    }

    static void print(String title, boolean ok, String detail) {
        System.out.printf("[%s] %s%n  → %s%n", ok ? "PASS" : "FAIL", title, detail);
    }
}
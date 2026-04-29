package test.Utilitys;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTest {

    String driver = "com.tmax.tibero.jdbc.TbDriver";
    String url = "jdbc:tibero:thin:@dev.teia.co.kr:8629:tibero";
    String user = "KH4SMTDBADM";
    String pass = "Rhdwjd!2xla";
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    public JdbcTest() throws Exception {
        Class.forName(driver);
        conn = DriverManager.getConnection(url, user, pass);
    }

    /** 테스트용 mock 생성자 - DB 연결 없이 사용 */
    protected JdbcTest(boolean mock) {}

//    // 파라미터 없음
//    List<SMap<String, Object>> result1 = db.selectData(
//            "SELECT * FROM TB_BI_PROC WHERE 1=1"
//    );
//    // 파라미터 1개
//    List<SMap<String, Object>> result2 = db.selectData(
//            "SELECT * FROM TB_BI_PROC WHERE LINE_CD = ?",
//            "FN01"
//    );
//    // 파라미터 여러 개
//    List<SMap<String, Object>> result3 = db.selectData(
//            "SELECT * FROM TB_BI_PROC WHERE LINE_CD = ? AND PROC_TY_CD = ? ORDER BY SORT_NO ASC",
//            "FN01", "Process"
//    );

    public List<SMap<String, Object>> selectData(String query, Object... params) {
        List<SMap<String, Object>> resultList = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(query);

            // 파라미터 동적 바인딩
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            rs = pstmt.executeQuery();

            ResultSetMetaData meta = rs.getMetaData();
            int colCnt = meta.getColumnCount();

            while (rs.next()) {
                SMap<String, Object> row = new SMap<>();
                for (int i = 1; i <= colCnt; i++) {
                    String colName = meta.getColumnLabel(i);
                    row.put(colName, rs.getObject(colName));
                }
                resultList.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception ignored) {
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception ignored) {
            }
        }

        return resultList;
    }
}

package test.core;

import io.netty.buffer.ByteBuf;
import test.Utilitys.JdbcTest;
import test.Utilitys.SMap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MessageTest {

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    List<SMap<String, Object>> messageList;
    JdbcTest db;

    // 3초 안에 특정 신호가 3번 오는지 확인하는 로직
    public MessageTest() throws Exception {
        db = new JdbcTest();
        messageList = db.selectData("SELECT * FROM TB_IF_MES_PROD", "LOGIC");
        System.out.println(String.format("MessageTest :: %s", messageList.size()));
        while (true) {
            String mesProdSeq = params("cnt");
            if ("1".equals(mesProdSeq)) {

            }
        }
    }

    public String params(String type) throws IOException {
        System.out.println(type.toUpperCase() + "=");
        return br.readLine();
    }

    public static String rpad(String str, int length, char padChar) {
        if (str == null) str = "";

        // 문자열이 길면 왼쪽 기준으로 length만큼 자르기
        if (str.length() > length) {
            return str.substring(0, length);
        }

        // 패딩 추가
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < length - str.length(); i++) {
            sb.append(padChar);
        }

        return sb.toString();
    }

    public static void writeBody(List<SMap<String, Object>> bodyInfo, ByteBuf buf, SMap<String, Object> dataMap, String div) {
        boolean hasDivider = (div != null);
        int lastIndex = bodyInfo.size() - 1;

        for (int i = 0; i < bodyInfo.size(); i++) {
            SMap<String, Object> msgDt = bodyInfo.get(i);
            String valId = msgDt.getString("VAL_ID");
            String valType = msgDt.getString("VAL_TYPE");
            int valLen = msgDt.getInt("VAL_LEN");

            Object rawValue = dataMap.get(valId);
            boolean appendDiv = hasDivider && (i < lastIndex);

            if ("STRING".equals(valType)) {
                String val = (rawValue != null) ? dataMap.getString(valId) : "";
                buf.writeBytes(rpad(val, valLen, ' ').getBytes());

            } else if ("INT".equals(valType)) {
                int val = (rawValue != null) ? dataMap.getInt(valId) : 0;
                buf.writeInt(val);
            } else if ("INT_LE".equals(valType)) {
                int val = (rawValue != null) ? dataMap.getInt(valId) : 0;
                buf.writeIntLE(val);
            } else if ("SHORT".equals(valType)) {
                short val = (rawValue != null) ? dataMap.getShort(valId) : 0;
                buf.writeShort(val);
            } else if ("SHORT_LE".equals(valType)) {
                short val = (rawValue != null) ? dataMap.getShort(valId) : 0;
                buf.writeShortLE(val);
            } else if ("DOUBLE".equals(valType)) {
                double val = (rawValue != null) ? dataMap.getDouble(valId) : 0.0;
                buf.writeDouble(val);
            } else if ("DOUBLE_LE".equals(valType)) {
                double val = (rawValue != null) ? dataMap.getDouble(valId) : 0.0;
                buf.writeDoubleLE(val);
            } else {
                String val = (rawValue != null) ? dataMap.getString(valId) : "";
                buf.writeBytes(rpad(val, valLen, ' ').getBytes());
            }

            if (hasDivider && appendDiv) {
                buf.writeBytes(div.getBytes());
            }
        }
    }


}

package test.LS;
import test.HMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LsXgtPacket {

    private static final Map<Integer, Function<byte[], HMap>> parseFrame = new HashMap<>();
    {
        parseFrame.put(0x5500, LsXgtPacket::responseReadFrame); // READ 응답
        parseFrame.put(0x5900, LsXgtPacket::responseWriteFrame); // WRITE 응답
    }

    public void getResponseData(byte[] resBytes) {
        int mid = ((resBytes[20] & 0xFF) << 8)| (resBytes[21] & 0xFF);
        Function<byte[], HMap> func = parseFrame.get(mid);
        if (func != null) {
            HMap<String,Object> test = func.apply(resBytes);
        }
    }

    public static HMap responseReadFrame(byte[] resBytes) {
        System.out.println("responseReadFrame");
        return null;
    }

    public static HMap responseWriteFrame(byte[] resBytes) {
        System.out.println("responseWriteFrame");
        return null;
    }


    // 연속읽기만 처리
    public byte[] readReqFrame(String targetAddr, int invokeId, int length) throws IOException {
        short addrLen = (short) targetAddr.length();
        short reqBodyLength = (short) ((12 +addrLen));
        short reqInvokeId = (short) invokeId;
        short reqLength = (short) length;
        short blockCnt = 1;
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();

        System.out.println(String.format("addrLen:%s, reqBodyLength:%s, reqInvokeId:%s, reqLength:%s",addrLen, reqBodyLength,reqInvokeId,reqLength));
        try {
            // 해더
            outBuf.write("LSIS-XGT".getBytes());
            outBuf.write(new byte[]{0x00, 0x00});
            outBuf.write(new byte[]{0x00, 0x00});
            outBuf.write(new byte[]{0x00});
            outBuf.write(new byte[]{0x33});
            outBuf.write(shortToBytes(reqInvokeId));
            outBuf.write(shortToBytes(reqBodyLength));
            outBuf.write(new byte[]{0x00});
            outBuf.write(new byte[]{0x00});
            // 바디
            outBuf.write(new byte[]{0x54, 0x00}); // command
            outBuf.write(new byte[]{0x14, 0x00}); // data type  (BLOCK:연속읽기)
            outBuf.write(new byte[]{0x00, 0x00}); // reserved
            outBuf.write(shortToBytes(blockCnt)); // block count
            outBuf.write(shortToBytes(addrLen)); // variable length
            outBuf.write(targetAddr.getBytes()); // variable name
            outBuf.write(shortToBytes(reqLength)); // request length

            return outBuf.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            outBuf.close();
        }
    }


    public static byte[] shortToBytes(short value, boolean littleEndian) {
        byte[] result = new byte[2];

        if (littleEndian) {
            result[0] = (byte) (value & 0xFF);
            result[1] = (byte) ((value >> 8) & 0xFF);
        } else {
            result[0] = (byte) ((value >> 8) & 0xFF);
            result[1] = (byte) (value & 0xFF);
        }

        return result;
    }

    public static byte[] shortToBytes(short value) {
        return shortToBytes(value,true);
    }
}

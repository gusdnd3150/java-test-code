package test.LS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LsXgtPacket {

    private static final Map<Integer, Function<byte[], XgtResponseVo>> parseFrame = new HashMap<>();
    static {
        parseFrame.put(0x5500, LsXgtPacket::responseReadFrame);  // READ 응답 처리
        parseFrame.put(0x5900, LsXgtPacket::responseWriteFrame); // WRITE 응답 처리
    }

    public XgtResponseVo getResponseData(byte[] resBytes) {
        int mid = ((resBytes[20] & 0xFF) << 8) | (resBytes[21] & 0xFF);
        Function<byte[], XgtResponseVo> func = parseFrame.get(mid);
        if (func != null) {
            return func.apply(resBytes);
        }
        return null;
    }

    // READ 응답 파싱
    // [header 20bytes] [cmd 2] [dataType 2] [reserved 2] [errorCode 2] [blockCnt 2]
    // blockCnt 만큼 반복: [dataLen 2] [data N]
    public static ReadResponseVo responseReadFrame(byte[] resBytes) {
        short invokeId  = bytesToShort(resBytes, 14);
        int   errorCode = (resBytes[26] & 0xFF) | ((resBytes[27] & 0xFF) << 8);

        if (errorCode != 0) {
            return new ReadResponseVo(invokeId, errorCode);
        }

        int blockCount = (resBytes[28] & 0xFF) | ((resBytes[29] & 0xFF) << 8);

        List<ReadBlockVo> blocks = new ArrayList<>();
        int offset = 30; // blockCount(2bytes) 이후 첫 번째 블록 시작

        for (int i = 0; i < blockCount; i++) {
            int    dataLen = (resBytes[offset] & 0xFF) | ((resBytes[offset + 1] & 0xFF) << 8);
            offset += 2;
            byte[] data    = Arrays.copyOfRange(resBytes, offset, offset + dataLen);
            offset += dataLen;
            blocks.add(new ReadBlockVo(dataLen, data));
        }

        return new ReadResponseVo(invokeId, errorCode, blockCount, blocks);
    }

    // WRITE 응답 파싱
    // [header 20bytes] [cmd 2] [dataType 2] [reserved 2] [errorCode 2]
    public static WriteResponseVo responseWriteFrame(byte[] resBytes) {
        short invokeId  = bytesToShort(resBytes, 14);
        int   errorCode = (resBytes[26] & 0xFF) | ((resBytes[27] & 0xFF) << 8);

        return new WriteResponseVo(invokeId, errorCode);
    }

    // 연속읽기 요청 패킷
    public byte[] readReqFrame(String targetAddr, int invokeId, int length) throws IOException {
        short addrLen      = (short) targetAddr.length();
        short reqBodyLength = (short) (12 + addrLen);
        short reqInvokeId  = (short) invokeId;
        short reqLength    = (short) length;
        short blockCnt     = 1;

        System.out.println(String.format("addrLen:%s, reqBodyLength:%s, reqInvokeId:%s, reqLength:%s", addrLen, reqBodyLength, reqInvokeId, reqLength));
        try(ByteArrayOutputStream outBuf = new ByteArrayOutputStream()) {
            // 헤더
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
            outBuf.write(new byte[]{0x54, 0x00}); // command (READ)
            outBuf.write(new byte[]{0x14, 0x00}); // data type (BLOCK:연속읽기)
            outBuf.write(new byte[]{0x00, 0x00}); // reserved
            outBuf.write(shortToBytes(blockCnt));  // block count
            outBuf.write(shortToBytes(addrLen));   // variable length
            outBuf.write(targetAddr.getBytes());   // variable name
            outBuf.write(shortToBytes(reqLength)); // request length

            return outBuf.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 연속쓰기 요청 패킷
    public byte[] writeReqFrame(String targetAddr, int invokeId, byte[] data) throws IOException {
        short addrLen       = (short) targetAddr.length();
        short dataLen       = (short) data.length;
        short reqBodyLength = (short) (12 + addrLen + dataLen);
        short reqInvokeId   = (short) invokeId;
        short blockCnt      = 1;
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();

        System.out.println(String.format("addrLen:%s, reqBodyLength:%s, reqInvokeId:%s, dataLen:%s", addrLen, reqBodyLength, reqInvokeId, dataLen));
        try {
            // 헤더
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
            outBuf.write(new byte[]{0x58, 0x00}); // command (WRITE)
            outBuf.write(new byte[]{0x14, 0x00}); // data type (BLOCK:연속쓰기)
            outBuf.write(new byte[]{0x00, 0x00}); // reserved
            outBuf.write(shortToBytes(blockCnt));  // block count
            outBuf.write(shortToBytes(addrLen));   // variable length
            outBuf.write(targetAddr.getBytes());   // variable name
            outBuf.write(shortToBytes(dataLen));   // data length
            outBuf.write(data);                    // data

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
        return shortToBytes(value, true);
    }

    private static short bytesToShort(byte[] bytes, int offset) {
        return (short) ((bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8));
    }
}

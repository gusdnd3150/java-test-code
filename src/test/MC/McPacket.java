package test.MC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class McPacket {

    // Commands (2bytes LE in frame)
    private static final byte[] CMD_READ  = {0x01, 0x04};
    private static final byte[] CMD_WRITE = {0x01, 0x14};

    // Subcommands (2bytes LE in frame)
    private static final byte[] SUBCMD_WORD = {0x00, 0x00};
    private static final byte[] SUBCMD_BIT  = {0x01, 0x00};

    // ── 요청 빌더 ────────────────────────────────────────────────────────────

    /** 워드 읽기 요청 패킷 생성 */
    public byte[] buildWordReadRequest(String address, int points) {
        return buildReadRequest(address, points, SUBCMD_WORD);
    }

    /** 비트 읽기 요청 패킷 생성 */
    public byte[] buildBitReadRequest(String address, int points) {
        return buildReadRequest(address, points, SUBCMD_BIT);
    }

    private byte[] buildReadRequest(String address, int points, byte[] subCmd) {
        McDeviceAddress dev = McDeviceAddress.parse(address);
        // timer(2) + cmd(2) + subcmd(2) + devNo(3) + devCode(1) + points(2) = 12
        short reqDataLen = 12;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeFixedHeader(out, reqDataLen);
            out.write(new byte[]{0x10, 0x00});                // CPU Monitor Timer
            out.write(CMD_READ);                               // Command
            out.write(subCmd);                                 // Subcommand
            out.write(intTo3BytesLE(dev.getDeviceNumber()));   // Device No. (3bytes LE)
            out.write(dev.getDeviceCode() & 0xFF);             // Device Code (1byte)
            out.write(shortToBytes((short) points, true));     // Number of Points (LE)
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 워드 쓰기 요청 패킷 생성 */
    public byte[] buildWordWriteRequest(String address, int[] wordValues) {
        McDeviceAddress dev  = McDeviceAddress.parse(address);
        byte[]          data = packWordData(wordValues);
        short reqDataLen = (short) (12 + data.length);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeFixedHeader(out, reqDataLen);
            out.write(new byte[]{0x10, 0x00});                // CPU Monitor Timer
            out.write(CMD_WRITE);                              // Command
            out.write(SUBCMD_WORD);                            // Subcommand
            out.write(intTo3BytesLE(dev.getDeviceNumber()));   // Device No. (3bytes LE)
            out.write(dev.getDeviceCode() & 0xFF);             // Device Code (1byte)
            out.write(shortToBytes((short) wordValues.length, true)); // Number of Points (LE)
            out.write(data);                                   // Word data (points × 2bytes)
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 비트 쓰기 요청 패킷 생성 */
    public byte[] buildBitWriteRequest(String address, boolean[] bitValues) {
        McDeviceAddress dev  = McDeviceAddress.parse(address);
        byte[]          data = packBitData(bitValues);
        short reqDataLen = (short) (12 + data.length);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeFixedHeader(out, reqDataLen);
            out.write(new byte[]{0x10, 0x00});                // CPU Monitor Timer
            out.write(CMD_WRITE);                              // Command
            out.write(SUBCMD_BIT);                             // Subcommand
            out.write(intTo3BytesLE(dev.getDeviceNumber()));   // Device No. (3bytes LE)
            out.write(dev.getDeviceCode() & 0xFF);             // Device Code (1byte)
            out.write(shortToBytes((short) bitValues.length, true)); // Number of Points (LE)
            out.write(data);                                   // Bit data (nibble packed)
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 고정 헤더 9bytes 출력 (Subheader~Request data length) */
    private static void writeFixedHeader(ByteArrayOutputStream out, short reqDataLen) throws IOException {
        out.write(new byte[]{0x50, 0x00});                    // Subheader
        out.write(0x00);                                       // Network No.
        out.write(0xFF);                                       // PC No.
        out.write(new byte[]{(byte) 0xFF, 0x03});             // I/O No. (LE, 0x03FF)
        out.write(0x00);                                       // Station No.
        out.write(shortToBytes(reqDataLen, true));             // Request data length (LE)
    }

    // ── 응답 파서 ────────────────────────────────────────────────────────────

    /**
     * 워드 읽기 응답 파싱
     * [0-8] 고정헤더 echo / [7-8] 응답 데이터 길이 / [9-10] End code / [11+] 워드 데이터
     */
    public McWordReadResponseVo parseWordReadResponse(byte[] resBytes, int points) {
        int endCode = readLE16(resBytes, 9);
        if (endCode != 0) {
            return new McWordReadResponseVo(endCode);
        }
        int[] wordValues = new int[points];
        for (int i = 0; i < points; i++) {
            wordValues[i] = readLE16(resBytes, 11 + i * 2);
        }
        return new McWordReadResponseVo(endCode, wordValues);
    }

    /**
     * 비트 읽기 응답 파싱
     * [11+] nibble 패킹 데이터 → boolean[] 언패킹
     */
    public McBitReadResponseVo parseBitReadResponse(byte[] resBytes, int points) {
        int endCode = readLE16(resBytes, 9);
        if (endCode != 0) {
            return new McBitReadResponseVo(endCode);
        }
        boolean[] bitValues = new boolean[points];
        int byteCount = (points + 1) / 2;
        for (int i = 0; i < byteCount; i++) {
            int b       = resBytes[11 + i] & 0xFF;
            int evenIdx = 2 * i;
            int oddIdx  = 2 * i + 1;
            // 하위 nibble = 짝수 인덱스, 상위 nibble = 홀수 인덱스
            if (evenIdx < points) bitValues[evenIdx] = (b & 0x0F) == 0x01;
            if (oddIdx  < points) bitValues[oddIdx]  = ((b >> 4) & 0x0F) == 0x01;
        }
        return new McBitReadResponseVo(endCode, bitValues);
    }

    /**
     * 쓰기 응답 파싱 (응답 데이터 없음, End code만 확인)
     */
    public McWriteResponseVo parseWriteResponse(byte[] resBytes) {
        int endCode = readLE16(resBytes, 9);
        return new McWriteResponseVo(endCode);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    /** short → 2bytes (LS XGT 패턴 동일) */
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

    /** int → 3bytes LE (디바이스 번호용) */
    private static byte[] intTo3BytesLE(int value) {
        return new byte[]{
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF)
        };
    }

    /** 2bytes LE → int (응답 파싱용) */
    private static int readLE16(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
    }

    /** int[] → byte[] (워드 직렬화, 각 워드 2bytes LE) */
    private static byte[] packWordData(int[] wordValues) {
        byte[] data = new byte[wordValues.length * 2];
        for (int i = 0; i < wordValues.length; i++) {
            data[i * 2]     = (byte) (wordValues[i] & 0xFF);
            data[i * 2 + 1] = (byte) ((wordValues[i] >> 8) & 0xFF);
        }
        return data;
    }

    /**
     * boolean[] → byte[] (비트 nibble 패킹)
     * byte[i] = (bitValues[2i+1] ? 0x01 : 0x00) << 4
     *          | (bitValues[2i]   ? 0x01 : 0x00)
     */
    private static byte[] packBitData(boolean[] bitValues) {
        int    byteCount = (bitValues.length + 1) / 2;
        byte[] data      = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            int evenIdx = 2 * i;
            int oddIdx  = 2 * i + 1;
            int b = 0;
            if (evenIdx < bitValues.length && bitValues[evenIdx]) b |= 0x01; // 하위 nibble
            if (oddIdx  < bitValues.length && bitValues[oddIdx])  b |= 0x10; // 상위 nibble
            data[i] = (byte) b;
        }
        return data;
    }
}
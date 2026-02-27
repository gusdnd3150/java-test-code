package test;

import test.LS.LsXgtPacket;
import test.LS.ReadBlockVo;
import test.LS.ReadResponseVo;
import test.LS.XgtResponseVo;
import test.MC.McBitReadResponseVo;
import test.MC.McPacket;
import test.MC.McWordReadResponseVo;
import test.MC.McWriteResponseVo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {

	public static HMap<String,List<HMap<String,Object>>> inBarcodeMap = new HMap<String,List<HMap<String,Object>>>();	//진입라인정보

	public static void main(String[] args) throws InterruptedException, IOException {

		// ── LS XGT 사용 예 ────────────────────────────────────────────────────
		//new TestGoStop();
		//new TestGoStopClass();

		LsXgtPacket packet = new LsXgtPacket();
		byte[] frameBytes = packet.readReqFrame("%MB200",1, 20);
		System.out.println("==========================: = "+Arrays.toString(frameBytes));
		String testdata = "TFNJUy1YR1QAAAQBoBEBACAAADJVABQAAAAAAAEAFABUVFRUVFRUVFRUMDAwMDAxICAgIA==";
		byte[] data = Base64.getDecoder().decode(testdata);

		XgtResponseVo res = packet.getResponseData(data);
		if(res instanceof ReadResponseVo){
			ReadResponseVo dataVo = (ReadResponseVo) res;
			for (ReadBlockVo datum : dataVo.getBlocks()) {
				System.out.println("==========================: = "+datum.getDataLen());
				System.out.println("==========================: = "+Arrays.toString(datum.getData()));
			}
		}else{
			System.out.println("==========================: = "+res.toString());
			System.out.println("==========================: = "+Arrays.toString(data));
		}

		// ── MC 3E Binary 사용 예 ──────────────────────────────────────────────
		McPacket mc = new McPacket();

		// 1. 워드 읽기 요청 패킷 생성 (D100 부터 5점)
		byte[] wordReadReq = mc.buildWordReadRequest("D*100", 5);
		System.out.println("[MC] 워드 읽기 요청: " + Arrays.toString(wordReadReq));

		// 2. 워드 읽기 응답 파싱 (PLC 실제 응답 바이트 배열 전달)
		//    응답 예: D0 00 00 FF FF 03 00 0C 00 00 00 | 01 00 02 00 03 00 04 00 05 00
		byte[] wordReadRes = {
			(byte)0xD0, 0x00,                   // Subheader
			0x00, (byte)0xFF, (byte)0xFF, 0x03, 0x00, // Network/PC/IO/Station echo
			0x0C, 0x00,                          // Response data length
			0x00, 0x00,                          // End code (성공)
			0x01, 0x00, 0x02, 0x00, 0x03, 0x00, 0x04, 0x00, 0x05, 0x00  // D100~D104
		};
		McWordReadResponseVo wordRes = mc.parseWordReadResponse(wordReadRes);
		System.out.println("[MC] 워드 읽기 성공: " + wordRes.isSuccess());
		System.out.println("[MC] 워드 값: " + Arrays.toString(wordRes.getWordValues())); // [1, 2, 3, 4, 5]

		// 3. 비트 읽기 요청 패킷 생성 (M0 부터 4점)
		byte[] bitReadReq = mc.buildBitReadRequest("M*0", 4);
		System.out.println("[MC] 비트 읽기 요청: " + Arrays.toString(bitReadReq));

		// 4. 비트 읽기 응답 파싱
		//    nibble 패킹: ON,OFF,ON,ON → byte[0x01, 0x11]
		byte[] bitReadRes = {
			(byte)0xD0, 0x00,
			0x00, (byte)0xFF, (byte)0xFF, 0x03, 0x00,
			0x04, 0x00,
			0x00, 0x00,                          // End code (성공)
			0x01, 0x11                           // M0=ON, M1=OFF, M2=ON, M3=ON
		};
		McBitReadResponseVo bitRes = mc.parseBitReadResponse(bitReadRes, 4);
		System.out.println("[MC] 비트 읽기 성공: " + bitRes.isSuccess());
		System.out.println("[MC] 비트 값: " + Arrays.toString(bitRes.getBitValues())); // [true, false, true, true]

		// 5. 워드 쓰기 요청 패킷 생성 (D200 에 100, 200 쓰기 → 각 워드 2bytes LE)
		byte[] wordWriteReq = mc.buildWordWriteRequest("D*200", new byte[]{0x64, 0x00, (byte)0xC8, 0x00});
		System.out.println("[MC] 워드 쓰기 요청: " + Arrays.toString(wordWriteReq));

		// 6. 비트 쓰기 요청 패킷 생성 (X0 부터 4점 ON/OFF/ON/ON)
		byte[] bitWriteReq = mc.buildBitWriteRequest("X*0", new boolean[]{true, false, true, true});
		System.out.println("[MC] 비트 쓰기 요청: " + Arrays.toString(bitWriteReq));

		// 7. 쓰기 응답 파싱 (성공 시 End code = 0x0000)
		byte[] writeRes = {
			(byte)0xD0, 0x00,
			0x00, (byte)0xFF, (byte)0xFF, 0x03, 0x00,
			0x02, 0x00,
			0x00, 0x00                           // End code (성공)
		};
		McWriteResponseVo mcWriteRes = mc.parseWriteResponse(writeRes);
		System.out.println("[MC] 쓰기 성공: " + mcWriteRes.isSuccess());

    }


}

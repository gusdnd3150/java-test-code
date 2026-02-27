package test;

import test.LS.LsXgtPacket;
import test.LS.ReadBlockVo;
import test.LS.ReadResponseVo;
import test.LS.XgtResponseVo;

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

    }


}

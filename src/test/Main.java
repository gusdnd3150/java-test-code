package test;

import test.LS.LsXgtPacket;

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
		byte[] frameBytes = packet.readReqFrame("%MB200",1, 10);
		System.out.println("==========================: = "+Arrays.toString(frameBytes));
		String testdata = "TFNJUy1YR1QAAAQBoBEBABYAAChVABQAAAAAAAEACgBUVFRUVFRUVFRU";
		byte[] data = Base64.getDecoder().decode(testdata);

		packet.getResponseData(data);
		System.out.println("==========================: = "+Arrays.toString(data));
    }


}

package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
		
		
		new TestGoStop();
		
    }


}

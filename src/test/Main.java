package test;

import test.LS.LsXgtPacket;
import test.LS.ReadBlockVo;
import test.LS.ReadResponseVo;
import test.LS.XgtResponseVo;
import test.MC.McBitReadResponseVo;
import test.MC.McPacket;
import test.MC.McWordReadResponseVo;
import test.MC.McWriteResponseVo;
import test.Utilitys.HMap;
import test.Utilitys.JdbcTest;
import test.barcode.BarcodeTest;
import test.core.MessageTest;
import test.ifPacket.InterfaceHandler;
import test.time.TimeEvent;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;


public class Main {

	public static HMap<String,List<HMap<String,Object>>> inBarcodeMap = new HMap<String,List<HMap<String,Object>>>();	//진입라인정보

	public static void main(String[] args) throws Exception {
//		HMap<String,Object> testParam = new HMap<String,Object>();
//		testParam.put("BODY_NO","7WX 000001");
//		testParam.put("MES_PROD_SEQ","12345678901234567");
		//new TestGoStop();
		//new TestGoStopClass();
//		new BarcodeTest();
//		new TimeEvent();
		//new MessageTest();

//		short test =0;
//		double ss = test/10.0;
//		System.out.println(ss);

//        String test = "UZ  000001";
        String test = "UZ 000001";
        test = test.replaceAll("\\s+", "");

        System.out.println(test);
        System.out.println(test.substring(0, 3)); // 앞 3자리
        System.out.println(test.substring(3)); // 앞 3자리
        System.out.println(test.substring(0, 2)); // 앞 2자리
        System.out.println(test.substring(2)); // 앞 2자리
    }

	public static String formatNumber(double value) {
    if (value == Math.floor(value)) {
        return String.valueOf((int) value);
    } else {
        return String.valueOf(value);
    }
}


}

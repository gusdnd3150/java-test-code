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
import java.util.Arrays;
import java.util.Base64;
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
		new MessageTest();
    }


}

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
import test.ifPacket.InterfaceHandler;
import test.time.TimeEvent;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;


public class Main {

	public static HMap<String,List<HMap<String,Object>>> inBarcodeMap = new HMap<String,List<HMap<String,Object>>>();	//진입라인정보
	
	
	public static void main(String[] args) throws Exception {
		
//		String ascii = "0020";
//		byte[] test = Utility.hexStringToBytes(ascii); // {0x00, 0x20}
//        boolean rslt = Utility.isBitSet(test[1],5);
        
        byte[] tt2 = new byte[] {0x00,0x00};
        short flag = ByteBuffer.wrap(tt2).order(ByteOrder.BIG_ENDIAN).getShort();
        System.out.println("sdsd :: "+ flag);
    }

	public static String formatNumber(double value) {
    if (value == Math.floor(value)) {
        return String.valueOf((int) value);
    } else {
        return String.valueOf(value);
    }
}


}

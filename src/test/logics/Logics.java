package test.logics;

public class Logics {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		bytesRevert();
	}

	
	// 1 word 2byte 방향전환 테스트
	// melsec 아스키모드에서는 write 시 역방향으로 써짐.
	// read 시 역방향으로 써진 데이터를 역방향으로 다시 전환해야함
	public static void bytesRevert() {
		
		String body = "W7 X214365";
		byte[] bodyBytes = body.getBytes();
		if(bodyBytes.length % 2 != 0) {
			System.out.println("반드시 짝수 바이트어야합니다. (1 word 2 bytes)");
			return;
		}
	    for (int i = 0; i < bodyBytes.length; i += 2) {
	    	byte b1 = bodyBytes[i];
	    	byte b2 = bodyBytes[i+1];
	    	bodyBytes[i] = b2;
	    	bodyBytes[i+1] = b1;
	    }
	    System.out.println("rslt = "+new String(bodyBytes));
	}
}

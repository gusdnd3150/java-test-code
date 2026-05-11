package test.ifPacket;

import test.Utilitys.HMap;

import java.util.function.Function;

public class InterfaceHandler {

//    public final HMap<String, Function<String, byte[]>> handlers = new HMap<>();
//     {
//        handlers.put("A", this::proccessA);
//        handlers.put("B", this::proccessA);
//        handlers.put("C", this::proccessA);
//     }
//     public InterfaceHandler() {
//          handlers.put("A", this::processA);
//          handlers.put("B", this::processB);
//          handlers.put("C", this::processC);
//      }

    public static final HMap<String, Function<HMap, byte[]>> handlers = new HMap<>();
    static{
        handlers.put("A", InterfaceHandler::processA);
        handlers.put("B", InterfaceHandler::processB);
    }

    public static void sendIfData(String skId, String ifFlag, HMap<String, Object> data) {
        String flags = ifFlag.toUpperCase();
        for (char c : flags.toCharArray()) {
            String flag = String.valueOf(c);
            Function<HMap, byte[]> handler = handlers.get(flag);
            if (handler == null) {
                System.out.println(String.format("sendIfData :: skId: %s , flag: %s has no Function", skId,flag));
                continue;
            }
            // 추가 필요한 기준정보 세팅 후 apply 해도됌
//            data.put("SK_ID", skId);
//            data.put("SK_ID", skId);
//            data.put("SK_ID", skId);//
            byte[] resultData = handler.apply(data);
            if (resultData.length > 0) {
                System.out.println(String.format("sendIfData :: skId: %s , flag: %s , sendData:%s", skId,flag, new String(resultData)));
            }
        }
    }


    public static byte[] processA(HMap<String, Object> flag) {
        try {
            String strBodyNo = flag.getString("BODY_NO");
            String strMesProdSeq = flag.getString("MES_PROD_SEQ");
            return (strBodyNo+strMesProdSeq).getBytes();
        } catch (Exception e) {
            System.out.println("processA exception :: " + flag.toString());
        }
        return new byte[0];
    }

    public static byte[] processB(HMap<String, Object> flag) {
        try {
            String strMesProdSeq = flag.getString("MES_PROD_SEQ");
            return (strMesProdSeq).getBytes();
        } catch (Exception e) {
            System.out.println("processB exception :: " + flag.toString());
        }
        return new byte[0];
    }

    public static byte[] processMsg(HMap<String, Object> flag) {
        try {
            String strMesProdSeq = flag.getString("MES_PROD_SEQ");
            String strMsgId = flag.getString("MSG_ID");
            return (strMesProdSeq).getBytes();
        } catch (Exception e) {
            System.out.println("processB exception :: " + flag.toString());
        }
        return new byte[0];
    }
}

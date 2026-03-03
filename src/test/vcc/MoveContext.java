package test.vcc;

import java.util.ArrayList;
import java.util.List;

import test.Utilitys.HMap;

public class MoveContext {
	
	List<HMap<String, Object>> moveCarList = new ArrayList<>();
    List<HMap<String, Object>> idleList = new ArrayList<>();

    void addMove(String bodyNo, String procCd) {
        HMap<String, Object> d = new HMap<>();
        d.put("BODY_NO", bodyNo);
        d.put("PROC_CD", procCd);
        moveCarList.add(d);
    }

    void addIdle(String bodyNo) {
        HMap<String, Object> d = new HMap<>();
        d.put("BODY_NO", bodyNo);
        d.put("PROC_CD", "IDLE");
        idleList.add(d);
    }
    
    
    public HMap<String, Object> resultData() {
    	HMap<String, Object> result = new HMap<>();
    	result.put("MOVE_CAR", moveCarList);
    	result.put("MOVE_IDLE", idleList);
    	
    	return result;
    }

}

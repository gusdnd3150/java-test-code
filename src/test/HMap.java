package test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HMap<String, Object> extends HashMap<String, Object> {
	private static final long serialVersionUID = 6723434363565852261L;

	public HMap() {}
	
	public HMap(HMap map) {
		super(map);
	}
	
	/**
	 * default null
	 * @param key
	 * @return
	 */
	public String getString(String key){
		String rt = null;
		 
		try{
			Object obj = super.get(key);
			
			if(null != obj && (obj instanceof Integer || obj instanceof BigDecimal || obj instanceof Double || obj instanceof Float)){ 
				rt = (String) ("" + get(key));
			}else{
				rt = (String)obj;
			}
		}catch(Exception e){}
		
		return rt;
	}
 
	
	/**
	 * default -1
	 * @param key
	 * @return
	 */
	public int getInt(String key){
		Object obj = super.get(key);
		
		if(null == obj) {
			obj = (Object) "-1";
		}
		
		return Integer.parseInt("" + obj);
	}
	
	
	/**
	 * exception null
	 * @param key
	 * @return
	 */
	public byte[] getBytes(String key){
		try {
			return (byte[])super.get(key);
		}catch(Exception e) {
			return null;
		}
	}
	
	
	public byte getByte(String key){
		return (Byte)super.get(key);
	}
	
	
	public double getDouble(String key){
		return (Double)super.get(key);
	}
	
	
	public float getFloat(String key){
		return (Float)super.get(key);
	}
	
	public short getShort(String key){
		return (Short)super.get(key);
	}
	
	
	public char getChar(String key){
		return (Character)super.get(key);
	}
	
	public boolean getBoolean(String key){
		boolean isTrue = false;
		
		if(null != super.get(key)) {
			isTrue = (Boolean)super.get(key);
		}
		
		return isTrue;
	}
	
	public long getLong(String key){
		return (Long)super.get(key);
	}
	
	public HMap<String, Object> getHMap(String key){
		HMap<String, Object> rtn = null;
		
		try {
			rtn = (HMap<String, Object>) super.get(key);
		}catch(Exception e) {
			rtn = new HMap();
		}
		
		return rtn;
	}
	
	public List<HMap<String, Object>> getListHMap(String key){
		List<HMap<String, Object>> rtn = null;
		
		try {
			rtn = (List<HMap<String, Object>>) super.get(key);
		}catch(Exception e) {
			rtn = new ArrayList();
		}
		
		return rtn;
	}
	
	public List getList(String key){
		List rtn = null;
		
		try {
			rtn = (List) super.get(key);
		}catch(Exception e) {
			rtn = new ArrayList();
		}
		
		return rtn;
	}
	
	public boolean isExist(String key) {
		boolean isHave = true;
		
		try {
			Object obj = super.get(key);
			
			if(null == obj) {
				isHave = false;
			}
		}catch(Exception e) {
			isHave = false;
		}
		
		return isHave;
	}
	
}
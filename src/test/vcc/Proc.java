package test.vcc;

import java.util.Deque;

public class Proc {
	
	
	String procCd;
	int sortNo;
	Deque<Car> buf;
	
	public int getSortNo() {
		return sortNo;
	}
	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}
	
	
	public String getProcCd() {
		return procCd;
	}
	public void setProcCd(String procCd) {
		this.procCd = procCd;
	}
	public Deque<Car> getBuf() {
		return buf;
	}
	public void setBuf(Deque<Car> d) {
		this.buf = d;
	}

}

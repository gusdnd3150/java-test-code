package test.vcc;


public class Proc {
	
	private final String procCd;
	private final String procTyCd; // Process , buffer ...
	private volatile String inBodyNo; // 진입바디
	private volatile String outBodyNo; // 진입바디
	private int sortNo;
	
	public int getSortNo() {
		return sortNo;
	}

	public Proc(String procCd, String procTyCd, int sortNo) {
		this.procCd = procCd;
		this.procTyCd = procTyCd;
		this.sortNo = sortNo;
	}

	public Proc(String procCd, String procTyCd, String inBodyNo, String outBodyNo, int sortNo) {
		this.procCd = procCd;
		this.procTyCd = procTyCd;
		this.inBodyNo = inBodyNo;
		this.sortNo = sortNo;
	}

	public String getProcCd() {
		return procCd;
	}

	public String getProcTyCd() {
		return procTyCd;
	}

	public String getInBodyNo() {
		return inBodyNo;
	}
	
	
	public String getOutBodyNo() {
		return outBodyNo;
	}


	public synchronized void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}

	
	public synchronized void procIn(String bodyNo) {
		this.outBodyNo = null;
		this.inBodyNo = bodyNo;
	}
	
	public synchronized boolean hasCar() {
	    return inBodyNo != null && !inBodyNo.isEmpty();
	}

	public synchronized void procOut() {
	    if (inBodyNo == null || inBodyNo.isEmpty()) return;
	    outBodyNo = inBodyNo;
	    inBodyNo = null;
	}

	@Override
	public String toString() {
		return "Proc [procCd=" + procCd + ", procTyCd=" + procTyCd + ", inBodyNo=" + inBodyNo + ", outBodyNo="
				+ outBodyNo + ", sortNo=" + sortNo + "]";
	}
	
	
}

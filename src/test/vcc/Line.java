package test.vcc;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class Line {
	
	String lineCd;
	String lineTy;
	List<Proc> procList;
	

	public Line(String lineCd, String lineTy, List<Proc> procList) {
		this.lineCd = lineCd;
		this.lineTy = lineTy;
		Collections.sort(procList, (a, b) -> a.getSortNo() - b.getSortNo());
		this.procList = procList;
		
	}
	
	
	public Proc preProc() {
		return null;
	}
	
	public Proc nextProc() {
		return null;
	}
	

	public String getLineCd() {
		return lineCd;
	}

	public void setLineCd(String lineCd) {
		this.lineCd = lineCd;
	}

	public List<Proc> getProcList() {
		return procList;
	}

	public void setProcList(List<Proc> procList) {
		this.procList = procList;
	}

}

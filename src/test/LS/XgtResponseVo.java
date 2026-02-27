package test.LS;

public abstract class XgtResponseVo {

    private final short invokeId;
    private final int   errorCode;
    private final boolean success;

    protected XgtResponseVo(short invokeId, int errorCode) {
        this.invokeId  = invokeId;
        this.errorCode = errorCode;
        this.success   = (errorCode == 0);
    }

    public short   getInvokeId()  { return invokeId;  }
    public int     getErrorCode() { return errorCode; }
    public boolean isSuccess()    { return success;   }
}
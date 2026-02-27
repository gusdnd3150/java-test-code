package test.MC;

public abstract class McResponseVo {

    private final int     endCode;
    private final boolean success;

    protected McResponseVo(int endCode) {
        this.endCode = endCode;
        this.success = (endCode == 0);
    }

    public int     getEndCode() { return endCode; }
    public boolean isSuccess()  { return success; }
}
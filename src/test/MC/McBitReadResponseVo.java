package test.MC;

public class McBitReadResponseVo extends McResponseVo {

    private final boolean[] bitValues;

    public McBitReadResponseVo(int endCode, boolean[] bitValues) {
        super(endCode);
        this.bitValues = bitValues;
    }

    /** 에러 응답용 생성자 */
    public McBitReadResponseVo(int endCode) {
        super(endCode);
        this.bitValues = new boolean[0];
    }

    public boolean[] getBitValues() { return bitValues; }
}
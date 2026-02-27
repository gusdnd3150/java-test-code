package test.MC;

public class McWordReadResponseVo extends McResponseVo {

    private final int[] wordValues;

    public McWordReadResponseVo(int endCode, int[] wordValues) {
        super(endCode);
        this.wordValues = wordValues;
    }

    /** 에러 응답용 생성자 */
    public McWordReadResponseVo(int endCode) {
        super(endCode);
        this.wordValues = new int[0];
    }

    public int[] getWordValues() { return wordValues; }
}
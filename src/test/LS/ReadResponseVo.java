package test.LS;

import java.util.Collections;
import java.util.List;

public class ReadResponseVo extends XgtResponseVo {

    private final int             blockCount;
    private final List<ReadBlockVo> blocks;

    public ReadResponseVo(short invokeId, int errorCode, int blockCount, List<ReadBlockVo> blocks) {
        super(invokeId, errorCode);
        this.blockCount = blockCount;
        this.blocks     = Collections.unmodifiableList(blocks);
    }

    /** 에러 응답용 생성자 */
    public ReadResponseVo(short invokeId, int errorCode) {
        super(invokeId, errorCode);
        this.blockCount = 0;
        this.blocks     = Collections.emptyList();
    }

    public int               getBlockCount() { return blockCount; }
    public List<ReadBlockVo> getBlocks()     { return blocks;     }
}

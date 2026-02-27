package test.LS;

public class ReadBlockVo {

    private final int    dataLen;
    private final byte[] data;

    public ReadBlockVo(int dataLen, byte[] data) {
        this.dataLen = dataLen;
        this.data    = data;
    }

    public int    getDataLen() { return dataLen; }
    public byte[] getData()    { return data;    }
}

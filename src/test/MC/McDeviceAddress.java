package test.MC;

public class McDeviceAddress {

    // Device code constants
    public static final int D  = 0xA8;
    public static final int W  = 0xB4;
    public static final int M  = 0x90;
    public static final int L  = 0x92;
    public static final int SM = 0x91;
    public static final int SD = 0xA9;
    public static final int X  = 0x9C;
    public static final int Y  = 0x9D;
    public static final int B  = 0xA0;

    private final int deviceCode;
    private final int deviceNumber;

    private McDeviceAddress(int deviceCode, int deviceNumber) {
        this.deviceCode   = deviceCode;
        this.deviceNumber = deviceNumber;
    }

    /** "D*100", "X*1A" 형식의 주소 문자열 파싱 */
    public static McDeviceAddress parse(String address) {
        String[] parts = address.split("\\*", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid device address format: " + address);
        }
        String type   = parts[0].toUpperCase();
        String numStr = parts[1];

        int code;
        int number;

        switch (type) {
            case "D":  code = D;  number = Integer.parseInt(numStr, 10); break;
            case "W":  code = W;  number = Integer.parseInt(numStr, 10); break;
            case "M":  code = M;  number = Integer.parseInt(numStr, 10); break;
            case "L":  code = L;  number = Integer.parseInt(numStr, 10); break;
            case "SM": code = SM; number = Integer.parseInt(numStr, 10); break;
            case "SD": code = SD; number = Integer.parseInt(numStr, 10); break;
            case "X":  code = X;  number = Integer.parseInt(numStr, 16); break;
            case "Y":  code = Y;  number = Integer.parseInt(numStr, 16); break;
            case "B":  code = B;  number = Integer.parseInt(numStr, 16); break;
            default:
                throw new IllegalArgumentException("Unknown device type: " + type);
        }

        return new McDeviceAddress(code, number);
    }

    public int getDeviceCode()   { return deviceCode;   }
    public int getDeviceNumber() { return deviceNumber; }
}
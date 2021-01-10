package club.qqtim.util;

/**
 * @author rezeros.github.io
 */
public final class ConvertUtil {

    private ConvertUtil(){}


    public static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++){
            bytes[i] = oBytes[i];
        }
        return bytes;
    }


}

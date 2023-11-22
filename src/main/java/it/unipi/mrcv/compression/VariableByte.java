package it.unipi.mrcv.compression;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static java.lang.Math.log;

public class VariableByte {
    public static byte[] fromLongToVarByte(long input){ //the bit of control is the first one, its 1 if its the last byte, 0 otherwise
        byte[] ret;
        if(input==0){
            ret=new byte[]{0};
            return ret;
        }
        int n_bytes=(int) (log(input)/log(128))+1; //n of bytes necessary for encoding, cast to int and + 1 cause the cast "truncates" the decimal part
        ret=new byte[n_bytes];
        for(int byteEncoder=n_bytes-1;byteEncoder>=0;byteEncoder--){ //we start encoding from the last byte 7 bits at times
            ret[byteEncoder]=(byte) (input%128);
            input=input/128;
        }
        ret[n_bytes-1]+=128;
        return ret;
    }

    public static Long fromVarByteToLong(byte[] bytes) {
        long ret=0;
        if(bytes.length==1){
            if((int)bytes[0]==0){
                return (long)0;
            }
        }
        for (byte b : bytes) {
            ret=ret*128+b;
        }
        ret=ret+128;
        return ret;
    }
    public static byte[] fromArrayLongToVarByte(ArrayList<Long> input){
        ByteBuffer buf = ByteBuffer.allocate(input.size() * (Long.SIZE / Byte.SIZE));
        for (Long number : input)
            buf.put(fromLongToVarByte(number));

        buf.flip();
        byte[] ret = new byte[buf.remaining()];
        buf.get(ret);

        return ret;
    }
    public static byte[] fromArrayIntToVarByte(ArrayList<Integer> input){
        ByteBuffer buf = ByteBuffer.allocate(input.size() * (Long.SIZE / Byte.SIZE));
        for (int number : input)
            buf.put(fromLongToVarByte(number));

        buf.flip();
        byte[] ret = new byte[buf.remaining()];
        buf.get(ret);

        return ret;
    }
    public static ArrayList<Long> fromByteToArrayLong(byte[] bytes){
        ArrayList<Long> numbers = new ArrayList<>();
        long n = 0;

        for (byte b : bytes) {
            if ((b & 0xff) < 128) {
                n = 128 * n + b;
            } else {
                long num = (128 * n + ((b - 128) & 0xff));
                numbers.add(num);
                n = 0;
            }
        }

        return numbers;
    }

}

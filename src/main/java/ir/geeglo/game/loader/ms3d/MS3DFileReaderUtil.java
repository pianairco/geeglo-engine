package ir.geeglo.game.loader.ms3d;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MS3DFileReaderUtil {
    int readed;
    byte[] temp;
    ByteBuffer wrapped;
    BufferedInputStream inputStream;

    public MS3DFileReaderUtil(InputStream inputStream) {
        this.inputStream = new BufferedInputStream(inputStream);
    }

    boolean available() {
        try {
            return inputStream.available() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    byte readByte() throws IOException {
        temp = new byte[1];
        readed += inputStream.read(temp, 0, 1);
        return temp[0];
    }

    boolean readBoolean() throws IOException {
        temp = new byte[1];
        readed += inputStream.read(temp, 0, 1);
        return temp[0] == 0 ? false : true;
    }

    char readChar() throws IOException {
        temp = new byte[2];
        readed += inputStream.read(temp, 0, 2);
        ArrayUtils.reverse(temp);
        wrapped = ByteBuffer.wrap(temp);
        return wrapped.getChar();
    }

    short readShort() throws IOException {
        temp = new byte[2];
        readed += inputStream.read(temp, 0, 2);
        ArrayUtils.reverse(temp);
        wrapped = ByteBuffer.wrap(temp);
        return wrapped.getShort();
    }

    int readInt() throws IOException {
        temp = new byte[4];
        readed += inputStream.read(temp, 0, 4);
        ArrayUtils.reverse(temp);
        wrapped = ByteBuffer.wrap(temp);
        return wrapped.getInt();
    }

    long readLong() throws IOException {
        temp = new byte[8];
        readed += inputStream.read(temp, 0, 8);
        ArrayUtils.reverse(temp);
        wrapped = ByteBuffer.wrap(temp);
        return wrapped.getLong();
    }

    float readFloat() throws IOException {
        temp = new byte[4];
        readed += inputStream.read(temp, 0, 4);
        ArrayUtils.reverse(temp);
        wrapped = ByteBuffer.wrap(temp);
        return wrapped.getFloat();
    }

    double readDouble() throws IOException {
        temp = new byte[8];
        readed += inputStream.read(temp, 0, 8);
        ArrayUtils.reverse(temp);
        wrapped = ByteBuffer.wrap(temp);
        return wrapped.getDouble();
    }

    String readString(int length) throws IOException {
        temp = new byte[length];
        readed += inputStream.read(temp, 0, length);
        return new String(temp);
    }

    String readStringFreeSize(int length) throws IOException {
        temp = new byte[length];
        readed += inputStream.read(temp, 0, length);
        String tempString = new String(temp);
        return tempString.substring(0, tempString.indexOf("\000"));
    }
}

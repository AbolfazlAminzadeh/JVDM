package Tools.Logger;

import java.util.Arrays;

public final class LowString implements CharSequence{

    private final byte[] bytes;
    private final int length;

    public LowString(byte[] bytes) {
        this.bytes = bytes;
        length = bytes.length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return (char) bytes[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new LowString(Arrays.copyOfRange(bytes, start, end));
    }
}

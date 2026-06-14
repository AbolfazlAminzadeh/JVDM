package org.Kroj.Core.Tools.String;

public class StrPart implements CharSequence {
    private final CharSequence parent;
    private final int start;
    private final int length;

    public StrPart(CharSequence parent, int start, int length) {
        this.parent = parent;
        this.start = start;
        this.length = length;
    }
    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= length) {
            return (char) -1;
        }
        return parent.charAt(start + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0 || end > length || start > end || length == 0) {
            return "";
        }
        return new StrPart(this, start, end - start);
    }

    @Override
    public String toString() {
        return parent.subSequence(start, start + length).toString();
    }
}

package org.Kroj.Core.Tools.String;


import java.util.Arrays;

public class Splitter {
    // 2X Faster Than Normal Split
    public static CharSequence[] split(final CharSequence input, final char separator) {
        if (input == null || input.isEmpty()) return null;
        final int dataLength = input.length();
        CharSequence[] result = new CharSequence[dataLength + 1];
        int listLength = 0;
        int start = 0;
        for (int i = 0; i < dataLength; i++) {
            if (separator == input.charAt(i)) {
                result[listLength] = new StrPart(input, start, i - start);
                listLength++;
                start = i + 1;
            }
        }
        result[listLength++] = new StrPart(input, start, dataLength - start);
        return Arrays.copyOf(result, listLength);
    }

}

package com.prikolz.justhelper.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JustMCUtils {
    public static String zlibCompress(String str) {
        var input = str.getBytes();
        var output = new byte[input.length * 4];
        var compressor = new Deflater();
        compressor.setInput(input);
        compressor.finish();
        int l = compressor.deflate(output);
        byte[] arr = new byte[l];
        System.arraycopy(output, 0, arr, 0, l);
        return Base64.getEncoder().encodeToString(arr);
    }

    public static String zlibDecompress(byte[] arr) throws DataFormatException {
        var inflater = new Inflater();
        var outStream = new ByteArrayOutputStream();
        var buffer = new byte[1024];
        inflater.setInput(arr);
        int c = -1;
        while (c != 0) {
            c = inflater.inflate(buffer);
            outStream.write(buffer, 0, c);
        }
        inflater.end();
        return outStream.toString(StandardCharsets.UTF_8);
    }
}

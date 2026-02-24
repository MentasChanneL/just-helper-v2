package com.prikolz.justhelper.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.*;

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

    public static String zlibDecompress(String base64) throws DataFormatException {
        byte[] arr = Base64.getDecoder().decode(base64);
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

    public static String gzipCompress(String str) throws IOException {
        if (str == null || str.isEmpty()) return "";
        byte[] input = str.getBytes(StandardCharsets.UTF_8);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(input);
            gzip.finish();
            byte[] compressed = baos.toByteArray();
            return Base64.getEncoder().encodeToString(compressed);
        }
    }

    public static String gzipDecompress(String base64) {
        byte[] arr = Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(arr);
             GZIPInputStream gzip = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

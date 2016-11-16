package com.platypii.baseline.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

    public static void copy(InputStream input, OutputStream output) throws IOException {
        final byte buffer[] = new byte[1024];
        int bytesRead;
        while((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }

    public static String toString(InputStream input) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final byte buffer[] = new byte[1024];
        int bytesRead;
        while((bytesRead = input.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, bytesRead));
        }
        return builder.toString();
    }

}
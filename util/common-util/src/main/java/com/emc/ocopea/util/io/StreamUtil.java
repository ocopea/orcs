// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util.io;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by liebea on 8/4/16.
 * Drink responsibly
 */
public abstract class StreamUtil {
    public static int copy(InputStream input, OutputStream output) {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output) {
        return copyLarge(input, output, new byte[4096]);
    }

    @NoJavadoc
    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) {
        try {
            long count = 0L;
            int n1;
            for (; -1 != (n1 = input.read(buffer)); count += (long) n1) {
                output.write(buffer, 0, n1);
            }
            return count;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

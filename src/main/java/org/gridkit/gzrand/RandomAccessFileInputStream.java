/**
 * Copyright 2015 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.gzrand;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RandomAccessFileInputStream extends RandomAccessInputStream {

    protected final RandomAccessFile raf;
    private FileChannel chan;

    public RandomAccessFileInputStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    @Override
    public int read() throws IOException {
        try {
            return raf.read();
        } finally {
            afterRead();
        }
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        if (buffer.hasArray()) {
            int n = read(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
            if (n < 0) {
                return -1;
            }
            else {
                buffer.position(buffer.position() + n);
                return n;
            }
        }
        else {
            if (chan == null) {
                chan = raf.getChannel();
            }
            chan.position(raf.getFilePointer());
            int n = chan.read(buffer);
            raf.seek(chan.position());
            return n;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            return raf.read(b);
        } finally {
            afterRead();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return raf.read(b, off, len);
        } finally {
            afterRead();
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return n < 0 ? 0 : raf.skipBytes((int) Math.min(n, Integer.MAX_VALUE));
        } finally {
            afterRead();
        }
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(raf.length() - raf.getFilePointer(), Integer.MAX_VALUE);
    }

    @Override
    public void close() throws IOException {
        raf.close();
        if (chan != null) {
            try {
                chan.close();
            }
            catch(IOException e) {
                // ignore
            }
        }
    }

    public void seek(long offset) throws IOException {
        raf.seek(offset);
        afterRead();
    }

    public long length() throws IOException {
        return raf.length();
    }

    @Override
    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    protected void afterRead() {
    };
}

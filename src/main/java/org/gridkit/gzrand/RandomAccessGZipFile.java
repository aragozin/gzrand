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

import org.gridkit.gzrand.StreamIndex.Checkpoint;

public class RandomAccessGZipFile extends RandomAccessInputStream {

    StreamIndex index;
    private long inflatedSize;

    private RandomAccessInputStream inStream;
    private Extractor extractor;
    private long pos;
    private boolean closed;

    /**
     * Process provided data is gzip archive format. Places seek points roughly
     * at intravals specified by <code>span</code>. <br/>
     * After index is constructed, decompressed data can be accessed randomly
     * with {@link RandomAccessInputStream} interface.
     */
    public RandomAccessGZipFile(RandomAccessInputStream data, int span) throws IOException {
        StreamIndex idx = StreamIndex.buildIndex(data, span);
        construct(idx, data);
    }

    RandomAccessGZipFile(StreamIndex index, RandomAccessInputStream inStream) throws IOException {
        construct(index, inStream);
    }

    protected void construct(StreamIndex index, RandomAccessInputStream inStream) throws IOException {
        this.index = index;
        this.inflatedSize = index.getInflatedSize();
        this.inStream = inStream;
        this.pos = 0;
        this.extractor = new Extractor(inStream, 4 << 20);
        this.extractor.seek(null, 0);
    }

    public void seek(long offset) throws IOException {
        if (closed) {
            throw new IllegalStateException("Stream closed");
        }
        if (offset < 0) {
            throw new IllegalStateException("Negative offset provided: " + offset);
        }

        if (offset >= inflatedSize) {
            pos = inflatedSize;
        }
        else if (pos == offset) {
            return;
        }
        else {
            Checkpoint cp = index.seekIndexPoint(offset);
            long cout = cp == null ? 0 : cp.out;
            if (pos <= offset && pos > cout) {
                pos += extractor.skipBytes(offset - pos);
            }
            else {
                this.pos = offset;
                this.extractor.seek(cp, offset);
            }
        }
    }

    @Override
    public long getFilePointer() throws IOException {
        return pos;
    }

    @Override
    public int read() throws IOException {
        if (pos >= inflatedSize) {
            return -1;
        }
        byte[] b = new byte[1];
        return read(b) > 0 ? b[0] : -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (pos >= inflatedSize) {
            return -1;
        }
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        ensureOpen();
        long toSkip = Math.min(n, length() - pos);
        if (toSkip > 0) {
            seek(pos + toSkip);
            return toSkip;
        }
        else {
            return 0;
        }
    }

    @Override
    public int available() throws IOException {
        ensureOpen();
        return (int) Math.min((long) Integer.MAX_VALUE, length() - pos);
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (extractor != null) {
            extractor.close();
        }
        if (inStream != null) {
            inStream.close();
        }
    }

    public int read(byte[] buf, int offset, int len) throws IOException {
        ensureOpen();
        if (pos >= inflatedSize) {
            return -1;
        }
        else {
            int n = extractor.extract(buf, offset, len);
            pos += n;
            return n;
        }
    }

    public long length() {
        return inflatedSize;
    }
    
    StreamIndex getIndex() {
        return index;
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }
}
 
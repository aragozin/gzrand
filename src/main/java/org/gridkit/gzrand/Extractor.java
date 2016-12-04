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
import java.nio.ByteBuffer;

class Extractor {

    private final byte[] skipBuf = new byte[64 << 10];
    private final byte[] input;
    private final Inflater inflater;
    private final RandomAccessInputStream inStream;
    private boolean pastEof;

    Extractor(RandomAccessInputStream inStream, int inBufSize) throws IOException {
        this.inStream = inStream;
        this.inflater = new Inflater();
        this.input = new byte[inBufSize];
    }

    public void seek(StreamIndex.Checkpoint here, long offset) throws IOException {
        
        pastEof = false;
        long startOffs;
        if (here == null) {
            inflater.init(WrapperType.GZIP);

            inflater.resetInput(null);

            inStream.seek(0);
            startOffs = 0;
        } else {
            inflater.init(true);

            inflater.resetInput(null);

            inStream.seek(here.in);
            here.apply(inflater.restoreInfState());
            startOffs = here.out;
        }

        long len = offset - startOffs;
        skipBytes(len);
    }

    public long skipBytes(long len) throws IOException {
        long skipped = 0;
        for (long rem = len; rem > 0;) {
            int n = extract(skipBuf, 0, (rem > skipBuf.length) ? skipBuf.length : (int) rem);
            if (n < 0) {
                return skipped;
            }
            rem -= n;
            skipped += n;
        }
        return skipped;
    }

    public void close() {
    }

    public int extract(ByteBuffer bb) throws IOException {
        if (pastEof) {
            return -1;
        }
        if (bb.hasArray()) {
            byte[] a = bb.array();
            int offs = bb.arrayOffset() + bb.position();
            int n = extract(a, offs, bb.remaining());
            bb.position(bb.position() + n);
            return n;
        }
        if (bb.remaining() > 0) {
            int n = extract(skipBuf, 0, bb.remaining());
            if (n < 0) {
                return -1;
            }
            bb.put(skipBuf, 0, n);
            return n;
        }
        return 0;
    }
    
    public int extract(byte[] buffer, int offs, int len) throws IOException {
        if (pastEof) {
            return -1;
        }
        inflater.setOutput(buffer, offs, len);
        if (inflater.getAvailIn() == 0) {
            int nr = inStream.read(input);
            if (nr == -1) {
                throw errorUnexpededEndOfFile();
            }
            inflater.resetInput(input, 0, nr);
        }
        int ret = inflater.inflateBuffer();
        if (ret != Inflater.Z_OK) {
            if (ret == Inflater.Z_STREAM_END && inflater.getAvailOut() != len) {
                // ok
                pastEof = true;
            } else {
                throw new IOException("Decomression error (" + ret + ")");
            }
        }
        return len - inflater.getAvailOut();
    }

    protected Error errorUnexpededEndOfFile() throws IOException {
        throw new IOException("End of stream");
    }
}
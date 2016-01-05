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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class StreamIndex {

    static final int WINSIZE = 2 << 15;
    
    static class Checkpoint implements InfState {
    
        long out;
        long in;
        
        private int bitk;
        private int bitb;
        private byte[] window;
        private int windowOffs;
    
        Checkpoint(long out, long in) {
            this.out = out;
            this.in = in;
        }
    
        public long getOut() {
            return out;
        }
    
        public long getIn() {
            return in;
        }
    
        @Override
        public void atBlock(byte[] window, int windowOffset, int bitk, int bitb) {
            this.window = Arrays.copyOf(window, window.length);
            this.windowOffs = windowOffset;
            this.bitk = bitk;
            this.bitb = bitb;
        }
    
        public void apply(InfState state) {
            state.atBlock(window, windowOffs, bitk, bitb);
        }
        
        @Override
        public String toString() {
            return "in:" + in + ",out:" + out;
        }
    }

    private List<StreamIndex.Checkpoint> idx;
    private long inflatedSize;

    public StreamIndex(List<StreamIndex.Checkpoint> idx, long inflatedSize) {
        this.idx = idx;
        this.inflatedSize = inflatedSize;
    }

    public long getInflatedSize() {
        return inflatedSize;
    }

    public StreamIndex.Checkpoint seekIndexPoint(long offset) {
        if (offset >= inflatedSize) {
            return null;
        }
        if (idx.size() == 0 || offset < idx.get(0).getOut()) {
            return null;
        }
        int i;
        for (i = 1; i != idx.size(); ++i) {
            if (idx.get(i).getOut() > offset) {
                break;
            }
        }
        return idx.get(i - 1);
    }
    
    @Override
    public String toString() {
        return "Index[points:" + idx.size() + ",infSize:" + inflatedSize + "]";
    }

    public static StreamIndex buildIndex(InputStream in, long span) throws IOException {
        int ret;
        long totin, totout; /* our own total counters to avoid 4GB limit */
        long last; /* totout value of last access Point */
        List<Checkpoint> index; /* access points being generated */
    
        Inflater strm = new Inflater(WrapperType.GZIP);
    
        byte[] input = new byte[4 << 20];
        byte[] window = new byte[WINSIZE];
    
        /* initialize inflate */
        strm.resetInput(null);
    
        try {
    
            /*
             * inflate the input, maintain a sliding window, and build an index
             * -- this also validates the integrity of the compressed data using
             * the check information at the end of the gzip or zlib stream
             */
            totin = totout = last = 0;
            index = new ArrayList<Checkpoint>(); /*
                                                  * will be allocated by first
                                                  * addpoint()
                                                  */
            strm.setOutput(null);
            do {
    
                /* get some compressed data from input file */
                int bc = in.read(input, 0, input.length);
                if (bc == -1) {
                    throw new IOException("Unexpected end of compressed file");
                }
                strm.resetInput(input, 0, bc);
    
                /* process all of that, or until end of stream */
                do {
                    /* reset sliding window if necessary */
                    if (strm.getAvailOut() == 0) {
                        strm.setOutput(window);
                    }
    
                    /*
                     * inflate until out of input, output, or at end of block --
                     * update the total input and output counters
                     */
                    int inUsed = strm.getAvailIn();
                    int outUsed = strm.getAvailOut();
                    ret = strm.inflateBlock(); /* return at end of block */
                    inUsed = inUsed - strm.getAvailIn();
                    outUsed = outUsed - strm.getAvailOut();
                    totin += inUsed;
                    totout += outUsed;
                    // flush remaining window
                    {
                        strm.setOutput(window);
                        outUsed = strm.getAvailOut();
                        strm.flushWindow();
                        outUsed = outUsed - strm.getAvailOut();
                        totout += outUsed;
                    }
    
                    if (ret == Inflater.Z_STREAM_END) {
                        break;
                    } else if (ret != Inflater.Z_OK) {
                        throw new IOException("Decompression error (" + ret + ")");
                    }
    
                    /*
                     * if at end of block, consider adding an index entry (note
                     * that if data_type indicates an end-of-block, then all of
                     * the uncompressed data from that block has been delivered,
                     * and none of the compressed data after that block has been
                     * consumed, except for up to seven bits) -- the totout == 0
                     * provides an entry Point after the zlib or gzip header,
                     * and assures that the index always has at least one access
                     * Point; we avoid creating an access Point after the last
                     * block by checking bit 6 of data_type
                     */
                    if (totout == 0 || totout - last > span) {
                        final Checkpoint point = new Checkpoint(totout, totin);
                        if (strm.copyInfState(point)) {
                            index.add(point);
                            last = totout;
                        }
                    }
                } while (strm.getAvailIn() != 0);
            } while (ret != Inflater.Z_STREAM_END);
    
            return new StreamIndex(index, totout);
        } finally {
            // do nothing
        }
    }
}

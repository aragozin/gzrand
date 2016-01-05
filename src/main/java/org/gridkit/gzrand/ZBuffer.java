/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
 Copyright (c) 2000-2011 ymnk, JCraft,Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright 
 notice, this list of conditions and the following disclaimer in 
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * This program is based on zlib-1.1.3, so all credit should go authors
 * Jean-loup Gailly(jloup@gzip.org) and Mark Adler(madler@alumni.caltech.edu)
 * and contributors of zlib.
 */

package org.gridkit.gzrand;


/**
 * ZStream
 */
class ZBuffer {

    static final private int Z_OK = 0;

    byte[] next_in; // next input byte
    int next_in_index;
    int avail_in; // number of bytes available at next_in
    long total_in; // total nb of input bytes read so far

    byte[] next_out; // next output byte should be put there
    int next_out_index;
    int avail_out; // remaining free space at next_out
    long total_out; // total nb of bytes output so far

    String msg;

    Checksum checksum;

    public ZBuffer() {
        this(new Adler32());
    }

    public ZBuffer(Checksum checksum) {
        this.checksum = checksum;
    }

    public void setOutput(byte[] buf) {
        if (buf == null) {
            setOutput(null, 0, 0);
        } else {
            setOutput(buf, 0, buf.length);
        }
    }

    public void setOutput(byte[] buf, int off, int len) {
        next_out = buf;
        next_out_index = off;
        avail_out = len;
    }

    /**
     * Replaces input buffer. Remaining data in old buffer is discarded.
     */
    public void resetInput(byte[] buf) {
        if (buf == null) {
            resetInput(null, 0, 0);
        } else {
            resetInput(buf, 0, buf.length);
        }
    }

    /**
     * Replaces input buffer. Remaining data in old buffer is discarded.
     */
    public void resetInput(byte[] buf, int off, int len) {
        next_in = buf;
        next_in_index = off;
        avail_in = len;
    }

    public byte[] getNextIn() {
        return next_in;
    }

    public void setNextIn(byte[] next_in) {
        this.next_in = next_in;
    }

    public int getNextInIndex() {
        return next_in_index;
    }

    public void setNextInIndex(int next_in_index) {
        this.next_in_index = next_in_index;
    }

    public int getAvailIn() {
        return avail_in;
    }

    public void setAvailIn(int avail_in) {
        this.avail_in = avail_in;
    }

    public byte[] getNextOut() {
        return next_out;
    }

    public void setNextOut(byte[] next_out) {
        this.next_out = next_out;
    }

    public int getNextOutIndex() {
        return next_out_index;
    }

    public void setNextOutIndex(int next_out_index) {
        this.next_out_index = next_out_index;
    }

    public int getAvailOut() {
        return avail_out;

    }

    public void setAvailOut(int avail_out) {
        this.avail_out = avail_out;
    }

    public long getTotalOut() {
        return total_out;
    }

    public long getTotalIn() {
        return total_in;
    }

    public String getMessage() {
        return msg;
    }

    /**
     * Those methods are expected to be override by Inflater and Deflater. In
     * the future, they will become abstract methods.
     */
    public int end() {
        return Z_OK;
    }

    public boolean finished() {
        return false;
    }
}

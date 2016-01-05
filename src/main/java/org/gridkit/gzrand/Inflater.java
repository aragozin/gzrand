/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
 Copyright (c) 2011 ymnk, JCraft,Inc. All rights reserved.

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

public class Inflater extends ZBuffer {

    static final private int MAX_WBITS = 15; // 32K LZ77 window
    static final private int DEF_WBITS = MAX_WBITS;

    public static final int Z_OK = 0;
    public static final int Z_STREAM_END = 1;
    public static final int Z_STREAM_ERROR = -2;

    public Inflater() {
        super();
        init();
    }

    public Inflater(WrapperType wrapperType) throws GZipException {
        this(DEF_WBITS, wrapperType);
    }

    public Inflater(int w, WrapperType wrapperType) throws GZipException {
        super();
        int ret = init(w, wrapperType);
        if (ret != Z_OK)
            throw new GZipException(ret + ": " + msg);
    }

    public Inflater(int w) throws GZipException {
        this(w, false);
    }

    public Inflater(boolean nowrap) throws GZipException {
        this(DEF_WBITS, nowrap);
    }

    public Inflater(int w, boolean nowrap) throws GZipException {
        super();
        int ret = init(w, nowrap);
        if (ret != Z_OK)
            throw new GZipException(ret + ": " + msg);
    }

    Inflate istate;

    public int init() {
        return init(DEF_WBITS);
    }

    public int init(WrapperType wrapperType) {
        return init(DEF_WBITS, wrapperType);
    }

    public int init(int w, WrapperType wrapperType) {
        boolean nowrap = false;
        if (wrapperType == WrapperType.NONE) {
            nowrap = true;
        } else if (wrapperType == WrapperType.GZIP) {
            w += 16;
        } else if (wrapperType == WrapperType.ANY) {
            w |= Inflate.INFLATE_ANY;
        } else if (wrapperType == WrapperType.ZLIB) {
        }
        return init(w, nowrap);
    }

    public int init(boolean nowrap) {
        return init(DEF_WBITS, nowrap);
    }

    public int init(int w) {
        return init(w, false);
    }

    public int init(int w, boolean nowrap) {
        istate = new Inflate(this);
        return istate.inflateInit(nowrap ? -w : w);
    }

    public int inflateBuffer() {
        if (istate == null)
            return Z_STREAM_ERROR;
        int ret = istate.inflate(0, false);
        return ret;
    }

    public int inflateBlock() {
        if (istate == null)
            return Z_STREAM_ERROR;
        int ret = istate.inflate(0, true);
        return ret;
    }

    public int flushWindow() {
        int ret = istate.blocks.inflate_flush(Z_OK);
        return ret;
    }

    public int end() {
        if (istate == null)
            return Z_STREAM_ERROR;
        int ret = istate.inflateEnd();
        return ret;
    }

    public int sync() {
        if (istate == null)
            return Z_STREAM_ERROR;
        return istate.inflateSync();
    }

    public int syncPoint() {
        if (istate == null)
            return Z_STREAM_ERROR;
        return istate.inflateSyncPoint();
    }

    public boolean copyInfState(InfState state) {
        return istate.copyInfState(state);
    }

    public InfState restoreInfState() {
        return istate.stateReset();
    }

    public int setDictionary(byte[] dictionary, int dictLength) {
        if (istate == null)
            return Z_STREAM_ERROR;
        return istate.inflateSetDictionary(dictionary, dictLength);
    }

    public boolean finished() {
        return istate.mode == 12 /* DONE */;
    }
}

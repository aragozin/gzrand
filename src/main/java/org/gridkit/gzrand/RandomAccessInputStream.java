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
import java.io.RandomAccessFile;

/**
 * {@link InputStream} extended with seek functionality.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public abstract class RandomAccessInputStream extends InputStream {

    /**
     * Position stream so next byte read via {@link InputStream#read()} will be
     * at absolute position <code>offset</code>
     */
    public abstract void seek(long offset) throws IOException;

    /**
     * @return size of underlying data
     */
    public abstract long length() throws IOException;

    /**
     * See {@link RandomAccessFile#getFilePointer()}
     */
    public abstract long getFilePointer() throws IOException;

    @Override
    public long skip(long n) throws IOException {
        long fp = getFilePointer();
        seek(fp + n);
        return getFilePointer() - fp;
    }

    public int skipBytes(int n) throws IOException {
        return (int) this.skip(n);
    }
}

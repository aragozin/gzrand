package org.gridkit.gzrand;

import java.io.UnsupportedEncodingException;

/**
 * @see "http://www.ietf.org/rfc/rfc1952.txt"
 */
public class GZIPHeader implements Cloneable {

    static final byte OS_MSDOS = (byte) 0x00;
    static final byte OS_AMIGA = (byte) 0x01;
    static final byte OS_VMS = (byte) 0x02;
    static final byte OS_UNIX = (byte) 0x03;
    static final byte OS_ATARI = (byte) 0x05;
    static final byte OS_OS2 = (byte) 0x06;
    static final byte OS_MACOS = (byte) 0x07;
    static final byte OS_TOPS20 = (byte) 0x0a;
    static final byte OS_WIN32 = (byte) 0x0b;
    static final byte OS_VMCMS = (byte) 0x04;
    static final byte OS_ZSYSTEM = (byte) 0x08;
    static final byte OS_CPM = (byte) 0x09;
    static final byte OS_QDOS = (byte) 0x0c;
    static final byte OS_RISCOS = (byte) 0x0d;
    static final byte OS_UNKNOWN = (byte) 0xff;

    private long mtime = 0;
    private int xflags;
    private int os = 255;
    private byte[] extra;
    private byte[] name;
    private byte[] comment;
    private int hcrc;
    private long crc;

    public void setModifiedTime(long mtime) {
        this.mtime = mtime;
    }

    public long getModifiedTime() {
        return mtime;
    }

    public void setOS(int os) {
        if ((0 <= os && os <= 13) || os == 255)
            this.os = os;
        else
            throw new IllegalArgumentException("os: " + os);
    }

    public int getOS() {
        return os;
    }

    public int getXFlags() {
        return xflags;
    }

    public void setXFlags(int xflags) {
        this.xflags = xflags;
    }

    public void setName(String name) {
        try {
            this.name = name.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("name must be in ISO-8859-1 " + name);
        }
    }

    public void setName(byte[] name) {
        this.name = name;
    }

    public String getName() {
        if (name == null)
            return "";
        try {
            return new String(name, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }

    public void setComment(String comment) {
        try {
            this.comment = comment.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("comment must be in ISO-8859-1 " + name);
        }
    }

    public void setComment(byte[] comment) {
        this.comment = comment;
    }

    public String getComment() {
        if (comment == null)
            return "";
        try {
            return new String(comment, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }

    public void setCRC(long crc) {
        this.crc = crc;
    }

    public long getCRC() {
        return crc;
    }

    public int getHCRC() {
        return hcrc;
    }

    public void setHCRC(int hcrc) {
        this.hcrc = hcrc;
    }

    public byte[] getExtra() {
        return extra;
    }

    public void setExtra(byte[] extra) {
        this.extra = extra;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GZIPHeader gheader = (GZIPHeader) super.clone();
        byte[] tmp;
        if (gheader.extra != null) {
            tmp = new byte[gheader.extra.length];
            System.arraycopy(gheader.extra, 0, tmp, 0, tmp.length);
            gheader.extra = tmp;
        }

        if (gheader.name != null) {
            tmp = new byte[gheader.name.length];
            System.arraycopy(gheader.name, 0, tmp, 0, tmp.length);
            gheader.name = tmp;
        }

        if (gheader.comment != null) {
            tmp = new byte[gheader.comment.length];
            System.arraycopy(gheader.comment, 0, tmp, 0, tmp.length);
            gheader.comment = tmp;
        }

        return gheader;
    }
}

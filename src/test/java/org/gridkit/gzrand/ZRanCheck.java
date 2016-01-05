package org.gridkit.gzrand;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("resource")
public class ZRanCheck {

    @Test
    public void test_extract() throws IOException {
        File file = new File("C:\\fire_at_will\\_samara\\access\\false2014-08-13.gz");
//        File file = new File("C:\\fire_at_will\\_samara\\l\\2-2.hprof.gz");
        RandomAccessFileInputStream seeker = new RandomAccessFileInputStream(new RandomAccessFile(file, "r"));
        RandomAccessGZipFile index = new RandomAccessGZipFile(seeker, 2 << 20);
        
        System.out.println(index.index.toString());
     
        index.seek(16 << 20);
        
        byte[] buffer = new byte[10 << 10];
        int n = index.read(buffer);
        System.out.write(buffer, 0, n);
        
    }

    @Test
    public void test_backward_reading() throws IOException {
//        File file = new File("C:\\fire_at_will\\_samara\\access\\false2014-08-13.gz");
//        File file2 = new File("C:\\fire_at_will\\_samara\\access\\false2014-08-13");
        File file = new File("C:\\fire_at_will\\_samara\\l\\2-2.hprof.gz");
        File file2 = new File("C:\\fire_at_will\\_samara\\l\\2-2.hprof");
        RandomAccessFileInputStream seeker = new RandomAccessFileInputStream(new RandomAccessFile(file, "r"));
        RandomAccessFileInputStream seeker2 = new RandomAccessFileInputStream(new RandomAccessFile(file2, "r"));
        RandomAccessGZipFile index = new RandomAccessGZipFile(seeker, 1 << 20);
        
        System.out.println(index.index.toString());
        
        Assert.assertEquals(file2.length(), index.length());
        long offs = index.length();
        while(offs != 0) {
            offs -= 10 << 10;
            if (offs < 0) {
                offs = 0;
            }
            System.out.println("Verify offset " + offs);
            byte[] b1 = new byte[10 << 10];
            byte[] b2 = new byte[10 << 10];
            index.seek(offs);
            index.read(b1);
            seeker2.seek(offs);
            seeker2.read(b2);
            Assert.assertArrayEquals(b2, b1);
        }        
    }

    @Test
    public void test_index_large() throws IOException {
//        File file = new File("C:\\fire_at_will\\_samara\\access\\false2014-08-13.gz");
//        File file2 = new File("C:\\fire_at_will\\_samara\\access\\false2014-08-13");
        File file = new File("C:\\fire_at_will\\_samara\\l\\2-2.hprof.gz");
        RandomAccessFileInputStream seeker = new RandomAccessFileInputStream(new RandomAccessFile(file, "r"));
        RandomAccessGZipFile index = new RandomAccessGZipFile(seeker, 1 << 20);
        
        System.out.println(index.index.toString());
    }
    
    @Test
    public void test_extract_small() throws IOException {
        System.out.println("Start");
        File file = new File("C:\\fire_at_will\\_samara\\21.08.dead-3.hh.gz");
        RandomAccessFileInputStream seeker = new RandomAccessFileInputStream(new RandomAccessFile(file, "r"));
        RandomAccessGZipFile index = new RandomAccessGZipFile(seeker, 1);
        
        System.out.println("Index points: " + index.index.toString());
        
        index.seek(65 << 10);
        
        byte[] buffer = new byte[1 << 10];
        int n = index.read(buffer);
        System.out.write(buffer, 0, n);
        System.out.println("\n");
        
        index.seek(10 << 10);

        n = index.read(buffer);
        System.out.write(buffer, 0, n);
        System.out.println("\n");

        index.seek(0 << 10);
        
        n = index.read(buffer);
        System.out.write(buffer, 0, n);
    }
}

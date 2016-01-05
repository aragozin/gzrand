GZ File Random Access
=========

This is an implementation of random access interface to gzip archive files.

Data compressed with DEFLATE algorith is not support random access. Though it is posible
to create recovery points in compressed stream (by memorizing internal state of decompression
algorithm) and implement pseudo random access to compressed data.

Accessing data require one pass to index stream before any access is possible. 
Using this approach is generally slower than uncomression archive to temporary store, though
for large archive saving space on temporary store may be benefitial.

This implementation was originally developed to deal with large compress JVM heap dumps.


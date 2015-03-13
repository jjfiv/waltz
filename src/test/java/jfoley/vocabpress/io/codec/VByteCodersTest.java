package jfoley.vocabpress.io.codec;

import jfoley.vocabpress.io.Codec;
import jfoley.vocabpress.io.util.BufferList;
import jfoley.vocabpress.io.util.StreamFns;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class VByteCodersTest {
  @Test
  public void testInts() {
    Codec<Integer> c = VByteCoders.ints;
    BufferList bl = new BufferList();

    Random r = new Random();
    List<Integer> data = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      int x = Math.abs(r.nextInt());
      data.add(x);
      bl.add(c,x);
    }

    List<Integer> readData = new ArrayList<>();
    InputStream input = StreamFns.fromByteBuffer(bl.compact());
    for (int i = 0; i < 1000; i++) {
      readData.add(c.read(input));
    }
    assertEquals(data, readData);
  }

  @Test
  public void testLongs() {
    Codec<Long> c = VByteCoders.longs;
    BufferList bl = new BufferList();

    Random r = new Random();
    List<Long> data = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      long x = Math.abs(r.nextLong());
      data.add(x);
      bl.add(c,x);
    }

    List<Long> readData = new ArrayList<>();
    InputStream input = StreamFns.fromByteBuffer(bl.compact());
    for (int i = 0; i < 1000; i++) {
      readData.add(c.read(input));
    }
    assertEquals(data, readData);
  }

}
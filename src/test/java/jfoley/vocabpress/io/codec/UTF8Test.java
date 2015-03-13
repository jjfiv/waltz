package jfoley.vocabpress.io.codec;

import jfoley.vocabpress.io.Codec;
import jfoley.vocabpress.io.util.BufferList;
import jfoley.vocabpress.io.util.StreamFns;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class UTF8Test {

  @Test
  public void testWithoutLengthPrefix() {
    Codec<String> c = UTF8.withoutLengthPrefix;
    assertFalse(c.knowsOwnSize());

    BufferList output = new BufferList();
    output.add(c, "this will ");
    System.out.println(output.byteCount());
    output.add(c, "get concatenated on read!");
    System.out.println(output.byteCount());
    assertEquals("this will get concatenated on read!", c.read(output.compact()));
  }

  @Test
  public void testWithLengthPrefix() {
    Codec<String> c = UTF8.withVByteLength;
    assertTrue(c.knowsOwnSize());

    BufferList output = new BufferList();
    output.add(c, "this will not ");
    System.out.println(output.byteCount());
    output.add(c, "get concatenated on read!");
    System.out.println(output.byteCount());

    InputStream input = StreamFns.fromByteBuffer(output.compact());
    assertEquals("this will not ", c.read(input));
    assertEquals("get concatenated on read!", c.read(input));
  }

}
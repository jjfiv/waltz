package jfoley.vocabpress.io.coders;

import jfoley.vocabpress.io.Coder;
import jfoley.vocabpress.io.CodecException;
import jfoley.vocabpress.io.util.BufferList;
import jfoley.vocabpress.io.util.StreamFns;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class UTF8Test {

  @Test
  public void testWithoutLengthPrefix() {
    Coder<String> c = UTF8.withoutLengthPrefix;
    assertFalse(c.knowsOwnSize());

    BufferList output = new BufferList();
    output.add(c, "this will ");
    System.out.println(output.byteCount());
    output.add(c, "get concatenated on read!");
    System.out.println(output.byteCount());
    assertEquals("this will get concatenated on read!", c.read(output.compact()));
  }

  @Test
  public void testWithLengthPrefix() throws IOException {
    Coder<String> c = UTF8.withVByteLength;
    assertTrue(c.knowsOwnSize());

    BufferList output = new BufferList();
    output.add(c, "this will not ");
    output.add(c, "get concatenated on read!");

    // Make sure the first byte has a vbyte ending marker.
    assert((output.getByte(0) & 0x80) > 0);

    InputStream input = StreamFns.fromByteBuffer(output.compact());
    assertEquals("this will not ", c.read(input));
    assertEquals("get concatenated on read!", c.read(input));

    try {
      String ignored = c.read(input);
      assertNull(ignored);
      fail("Shouldn't get here.");
    } catch (CodecException ex) {
      assertTrue(ex.getCause() instanceof EOFException);
    }
  }

}
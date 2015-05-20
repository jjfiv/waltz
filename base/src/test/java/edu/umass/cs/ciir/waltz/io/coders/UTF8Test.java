package edu.umass.cs.ciir.waltz.io.coders;

import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.CoderException;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.galago.io.coders.CharsetCoders;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class UTF8Test {

  @Test
  public void testWithoutLengthPrefix() {
    Coder<String> c = CharsetCoders.withoutLengthPrefix;
    assertFalse(c.knowsOwnSize());

    BufferList output = new BufferList();
    output.add(c, "this will ");
    System.out.println(output.byteCount());
    output.add(c, "get concatenated on read!");
    System.out.println(output.byteCount());
    assertEquals("this will get concatenated on read!", c.read(output.asByteBuffer()));
  }

  @Test
  public void testWithLengthPrefix() throws IOException {
    Coder<java.lang.String> c = CharsetCoders.withVByteLength;
    assertTrue(c.knowsOwnSize());

    BufferList output = new BufferList();
    output.add(c, "this will not ");
    output.add(c, "get concatenated on read!");

    // Make sure the first byte has a vbyte ending marker.
    assert((output.getByte(0) & 0x80) > 0);

    InputStream input = StreamFns.fromByteBuffer(output.asByteBuffer());
    assertEquals("this will not ", c.read(input));
    assertEquals("get concatenated on read!", c.read(input));

    try {
      java.lang.String ignored = c.read(input);
      assertNull(ignored);
      fail("Shouldn't get here.");
    } catch (CoderException ex) {
      assertTrue(ex.getCause() instanceof EOFException);
    }
  }

}
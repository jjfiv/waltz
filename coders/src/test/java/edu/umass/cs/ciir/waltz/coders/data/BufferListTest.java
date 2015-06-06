package edu.umass.cs.ciir.waltz.coders.data;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BufferListTest {
  @Test
  public void testWithStringCodec() {
    Coder<String> str = CharsetCoders.utf8;

    BufferList bl = new BufferList();
    bl.add(str, "hello");
    assertEquals(5, bl.byteCount());
    bl.add(str, " world");
    assertEquals(11, bl.byteCount());
    java.lang.String hw = str.read(bl.asByteBuffer());
    assertEquals("hello world", hw);
  }

  @Test
  public void testToByteArray() throws UnsupportedEncodingException {
    Coder<String> str = CharsetCoders.utf8;

    BufferList bl = new BufferList();
    bl.add(str, "hello");
    assertEquals(5, bl.byteCount());
    bl.add(str, " world");
    assertEquals(11, bl.byteCount());

    java.lang.String hw = str.read(bl.asByteBuffer());
    assertArrayEquals("hello world".getBytes("UTF-8"), bl.asByteArray());

  }
}
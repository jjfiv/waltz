package edu.umass.cs.ciir.waltz.io;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.galago.io.coders.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BufferListTest {

  @Test
  public void testByteCount() throws Exception {
    BufferList bl = new BufferList();
    bl.add("hello".getBytes("UTF-8"));
    assertEquals(5, bl.byteCount());
    bl.add("world".getBytes("UTF-8"));
    assertEquals(10, bl.byteCount());
  }

  @Test
  public void testCompact() throws Exception {
    BufferList bl = new BufferList();
    bl.add("hello".getBytes("UTF-8"));
    assertEquals(5, bl.byteCount());
    bl.add(" world".getBytes("UTF-8"));
    assertEquals(11, bl.byteCount());
    java.lang.String hw = new java.lang.String(bl.asByteBuffer().array(), "UTF-8");
    assertEquals("hello world", hw);
  }

  @Test
  public void testWithStringCodec() {
    Coder<String> str = CharsetCoders.withoutLengthPrefix;

    BufferList bl = new BufferList();
    bl.add(str, "hello");
    assertEquals(5, bl.byteCount());
    bl.add(str, " world");
    assertEquals(11, bl.byteCount());
    java.lang.String hw = str.read(bl.asByteBuffer());
    assertEquals("hello world", hw);
  }
}
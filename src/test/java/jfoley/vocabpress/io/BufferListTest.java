package jfoley.vocabpress.io;

import jfoley.vocabpress.io.codec.UTF8;
import jfoley.vocabpress.io.util.BufferList;
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
    String hw = new String(bl.compact().array(), "UTF-8");
    assertEquals("hello world", hw);
  }

  @Test
  public void testWithStringCodec() {
    Codec<String> str = UTF8.withoutLengthPrefix;

    BufferList bl = new BufferList();
    bl.add(str, "hello");
    assertEquals(5, bl.byteCount());
    bl.add(str, " world");
    assertEquals(11, bl.byteCount());
    String hw = str.read(bl.compact());
    assertEquals("hello world", hw);
  }
}
package edu.umass.cs.ciir.waltz.coders;

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
    String hw = new String(bl.asByteBuffer().array(), "UTF-8");
    assertEquals("hello world", hw);
  }
}
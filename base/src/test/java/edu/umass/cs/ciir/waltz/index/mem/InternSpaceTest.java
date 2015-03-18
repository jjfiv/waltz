package edu.umass.cs.ciir.waltz.index.mem;

import ciir.jfoley.chai.collections.util.IterableFns;
import edu.umass.cs.ciir.waltz.index.intern.InternSpace;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InternSpaceTest {

  @Test
  public void testGetId() throws Exception {
    testSpace(new DoubleMapInternSpace<>());
  }

  /** Test an InternSpace implementation passes at least a smoke-test. */
  public static void testSpace(InternSpace<String> item) {
    String className = item.getClass().getName();

    assertEquals(className, 0, IterableFns.intoList(item.getAllItems()).size());
    int helloId = item.insertOrGet("hello");
    int worldId = item.insertOrGet("world");

    assertEquals(className, helloId, item.getId("hello"));
    assertEquals(className, "hello", item.getValue(helloId));
    assertEquals(className, helloId, item.insertOrGet("hello"));

    assertEquals(className, worldId, item.getId("world"));
    assertEquals(className, "world", item.getValue(worldId));
    assertEquals(className, worldId, item.insertOrGet("world"));

    int barId = 2;
    item.put(barId, "bar");

    assertEquals(className, helloId, item.getId("hello"));
    assertEquals(className, "hello", item.getValue(helloId));
    assertEquals(className, helloId, item.insertOrGet("hello"));

    assertEquals(className, barId, item.getId("bar"));
    assertEquals(className, "bar", item.getValue(barId));
    assertEquals(className, barId, item.insertOrGet("bar"));

    assertEquals(className, worldId, item.getId("world"));
    assertEquals(className, "world", item.getValue(worldId));
    assertEquals(className, worldId, item.insertOrGet("world"));
  }
}
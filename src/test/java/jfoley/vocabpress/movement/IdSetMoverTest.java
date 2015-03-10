package jfoley.vocabpress.movement;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IdSetMoverTest {

  @Test
  public void testIteration() {
    List<Integer> rawData = Arrays.asList(1,2,3,4,11,13);
    List<Integer> collected = new ArrayList<>();
    for(Mover mover = new IdSetMover(new HashSet<>(rawData)); !mover.isDone(); mover.next()) {
      collected.add(mover.currentKey());
    }

    assertEquals(rawData, collected);
  }

  @Test
  public void testBlockSwitching() {
    List<Integer> rawData = Arrays.asList(1,2,3,4,11,13);
    List<Integer> collected = new ArrayList<>();
    for(Mover mover = new IdSetMover(new HashSet<>(rawData), 2); !mover.isDone(); mover.next()) {
      collected.add(mover.currentKey());
    }

    assertEquals(rawData, collected);
  }
}
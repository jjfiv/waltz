package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.blockiter.ListBlockPostingsIterator;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MoverTest {

  @Test
  public void testXY() {
    // AND(x,y)
    List<CountPosting> xdata = new ArrayList<>();
    List<CountPosting> ydata = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      xdata.add(new SimpleCountPosting(i * 2, i));
      ydata.add(new SimpleCountPosting(i * 3, i));
    }

    FeatureMover<CountPosting> xs = new FeatureMover<>(new ListBlockPostingsIterator<>(xdata));
    FeatureMover<CountPosting> ys = new FeatureMover<>(new ListBlockPostingsIterator<>(ydata));

    Mover expr = AllOfMover.of(xs, ys);
    int total = 0;
    for(; !expr.isDone(); expr.next()) {
      total++;
      int doc = expr.currentKey();
      assertEquals(0, doc % 3);
      assertEquals(0, doc % 2);
    }
    assertEquals((200 / 6) + 1, total); // +1 for zero
  }

  @Test
  public void testXZ() {
    // AND(x,z)
    List<CountPosting> xdata = new ArrayList<>();
    List<CountPosting> ydata = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      xdata.add(new SimpleCountPosting(i * 2, i));
      ydata.add(new SimpleCountPosting(i * 5, i));
    }

    FeatureMover<CountPosting> xs = new FeatureMover<>(new ListBlockPostingsIterator<>(xdata));
    FeatureMover<CountPosting> ys = new FeatureMover<>(new ListBlockPostingsIterator<>(ydata));

    Mover expr = AllOfMover.of(xs, ys);
    int total = 0;
    for(; !expr.isDone(); expr.next()) {
      total++;
      int doc = expr.currentKey();
      assertEquals(0, doc % 5);
      assertEquals(0, doc % 2);
      System.out.println(doc);
    }
    assertEquals((200 / 10), total); // +1 for zero
  }

  @Test
  public void testEstimateKeyLowerBound() throws Exception {
    // Evaluate OR(AND(x,y), AND(x,z))
    List<CountPosting> xdata = new ArrayList<>();
    List<CountPosting> ydata = new ArrayList<>();
    List<CountPosting> zdata = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      xdata.add(new SimpleCountPosting(i * 2, i));
      ydata.add(new SimpleCountPosting(i * 3, i));
      zdata.add(new SimpleCountPosting(i * 5, i));

      // xy = 0,6,12,18,24,30,36,...
      // xz = 0,10,20,30,...
    }

    FeatureMover<CountPosting> xs = new FeatureMover<>(new ListBlockPostingsIterator<>(xdata));
    FeatureMover<CountPosting> ys = new FeatureMover<>(new ListBlockPostingsIterator<>(ydata));
    FeatureMover<CountPosting> zs = new FeatureMover<>(new ListBlockPostingsIterator<>(zdata));

    Mover xym = AllOfMover.of(xs, ys);
    Mover xzm = AllOfMover.of(xs, zs);
    Mover expr = AnyOfMover.of(xym, xzm);

    for(; !expr.isDone(); expr.next()) {
      int doc = expr.currentKey();
      System.out.println("> x="+xs.currentKey()+" y="+ys.currentKey()+" z="+zs.currentKey());
      System.out.println(doc+" AND(x,y)="+xym.estimateKeyLowerBound()+" AND(x,z)="+xzm.estimateKeyLowerBound());
      System.out.println();
    }
  }
}
package edu.umass.cs.ciir.waltz.coders.kinds;

import edu.umass.cs.ciir.waltz.coders.Coder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley
 */
public class ClassNameCoderTest {
  @Test
  public void testClassNameCoder() {
    Class<Iterable> iterableClass = Iterable.class;
    Class<List> listClass = List.class;

    Coder<Class<? extends Iterable>> coder = new ClassNameCoder<>(iterableClass);
    Class<? extends Iterable> foundClass = coder.read(coder.write(listClass));
    assertEquals(listClass, foundClass);
  }

}
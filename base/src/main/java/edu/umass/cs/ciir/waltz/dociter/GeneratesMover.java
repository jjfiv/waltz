package edu.umass.cs.ciir.waltz.dociter;

import edu.umass.cs.ciir.waltz.dociter.movement.Mover;

/**
 * @author jfoley
 */
public interface GeneratesMover<X extends Mover> {
  X getMover();
}

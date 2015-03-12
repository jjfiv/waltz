package jfoley.vocabpress.dociter;

import jfoley.vocabpress.dociter.movement.Mover;

/**
 * @author jfoley
 */
public interface GeneratesMover<X extends Mover> {
  public X getMover();
}

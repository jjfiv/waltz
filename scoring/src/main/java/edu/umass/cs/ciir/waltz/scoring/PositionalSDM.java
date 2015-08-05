package edu.umass.cs.ciir.waltz.scoring;

import edu.umass.cs.ciir.waltz.dociter.movement.AnyOfMover;
import edu.umass.cs.ciir.waltz.dociter.movement.Mover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.index.mem.MemoryPositionsIndex;
import edu.umass.cs.ciir.waltz.phrase.Bigram;
import edu.umass.cs.ciir.waltz.postings.positions.EmptyPositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of SDM that computes bigrams and unordered windows based on positions, rather than having them precomputed.
 * @author jfoley
 */
public class PositionalSDM {
  private final Feature<Integer> lengths;
  private final int collectionLength;
  private final int numTerms;
  private List<PostingMover<PositionsList>> iters;

  /** filled out by calculateStats() */
  private double term_bg[], od_bg[], uw_bg[];

  public double mu;
  public double unigramWeight;
  public double bigramWeight;
  public double ubigramWeight;

  /**
   * This class defines the required parameters to run this SDM scoring model.
   */
  public static class SDMParameters {
    /** Dirichlet smoothing mu. */
    public double mu = 2500.0;
    /** Unigram combination weight. */
    public double unigramWeight = 0.8;
    /** Bigram combination weight. */
    public double bigramWeight = 0.15;
    /** Skipgram combination weight. */
    public double unorderedWeight = 0.05;
  }

  public PositionalSDM(MemoryPositionsIndex index, List<String> terms, SDMParameters cfg) {
    this.lengths = index.getLengths();
    this.collectionLength = index.getCollectionLength();
    iters = new ArrayList<>();
    for (String term : terms) {
      iters.add(index.getPositionsMover(term));
    }
    this.numTerms = iters.size();

    double N = terms.size();
    mu = cfg.mu;
    unigramWeight = cfg.unigramWeight / N;
    bigramWeight = cfg.bigramWeight / (N-1.0);
    ubigramWeight = cfg.unorderedWeight / (N-1.0);

    //long startStats = System.currentTimeMillis();
    calculateStats();
    //long endStats = System.currentTimeMillis();

    //System.out.println("Stats: " + (endStats - startStats)+"ms. ");
  }

  public Mover getMover() {
    return new AnyOfMover<>(iters);
  }

  private void calculateStats() {
    long term_cf[], od_cf[], uw_cf[];

    term_cf = new long[numTerms];
    od_cf = new long[numTerms-1];
    uw_cf = new long[numTerms-1];

    term_bg = new double[numTerms];
    od_bg = new double[numTerms-1];
    uw_bg = new double[numTerms-1];

    Mover mover = getMover();
    for(mover.start(); !mover.isDone(); mover.next()) {
      int doc = mover.currentKey();
      List<PositionsList> extents = new ArrayList<>(numTerms);
      for (int i = 0; i < iters.size(); i++) {
        PostingMover<PositionsList> pos = iters.get(i);
        pos.moveTo(doc);
        if (pos.matches(doc)) {
          PositionsList p = pos.getCurrentPosting();
          extents.add(p);
          term_cf[i] += p.size();
        } else {
          assert(pos.currentKey() > doc);
          extents.add(EmptyPositionsList.instance);
        }
      }

      for (int i = 0; i < numTerms - 1; i++) {
        PositionsList left = extents.get(i);
        PositionsList right = extents.get(i + 1);

        od_cf[i] += Bigram.count(left, right);
        uw_cf[i] += Bigram.countUnordered(left, right, 8);
      }
    }

    for (int i = 0; i < numTerms; i++) {
      double cfv = Math.max(0.5, term_cf[i]);
      term_bg[i] = cfv / collectionLength;
    }
    for (int i = 0; i < numTerms - 1; i++) {
      double od_cfv = Math.max(0.5, od_cf[i]);
      od_bg[i] = od_cfv / collectionLength;

      double uw_cfv = Math.max(0.5, uw_cf[i]);
      uw_bg[i] = uw_cfv / collectionLength;
    }

    // reset so we can re-use them
    for (Mover iter : iters) {
      iter.reset();
    }
  }

  public double score(int doc) {
    long length = lengths.getFeature(doc, 0);

    double unigram = 0.0;
    // collect extent arrays

    List<PositionsList> pos = new ArrayList<>(numTerms);

    for (int i = 0; i < iters.size(); i++) {
      iters.get(i).moveTo(doc);

      int term_tf = 0;
      if(iters.get(i).matches(doc)) {
        PositionsList p = iters.get(i).getCurrentPosting();
        term_tf = p.size();
        pos.add(p);
      } else {
        pos.add(EmptyPositionsList.instance);
      }

      unigram += logDirichlet(term_tf, length, mu, term_bg[i]);
    }

    double bigram = 0.0;
    double ubigram = 0.0;

    for (int i = 0; i < numTerms - 1; i++) {
      PositionsList left = pos.get(i);
      PositionsList right = pos.get(i + 1);
      long od_tf = Bigram.count(left, right);
      long uw_tf = Bigram.countUnordered(left, right, 8);

      bigram += logDirichlet(od_tf, length, mu, od_bg[i]);
      ubigram += logDirichlet(uw_tf, length, mu, uw_bg[i]);
    }

    return unigram * unigramWeight + bigram * bigramWeight + ubigram * ubigramWeight;
  }

  public static double logDirichlet(double count, double length, double mu, double background) {
    double numerator = count + mu * background;
    double denominator = length + mu;
    return Math.log(numerator / denominator);
  }
}

package edu.umass.cs.ciir.waltz.index.mem;

import java.util.Map;

/**
 * @author jfoley.
 */
public interface InternSpace<Val> {
	public int getId(Val query);
	public Val getValue(int query);
	public void put(int first, Val second);
  public int insertOrGet(Val k);
  public Iterable<Map.Entry<Val, Integer>> getAllItems();
}

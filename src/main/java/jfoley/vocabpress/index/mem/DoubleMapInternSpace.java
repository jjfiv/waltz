package jfoley.vocabpress.index.mem;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jfoley.
 */
public class DoubleMapInternSpace<Val> implements InternSpace<Val> {
	public final Map<Integer, Val> firstLookup;
	public final Map<Val, Integer> secondLookup;
  private int nextId;

	public DoubleMapInternSpace(Map<Integer, Val> first, Map<Val, Integer> second) {
		firstLookup = first;
		secondLookup = second;
    nextId = 0;
	}
	public DoubleMapInternSpace() {
		this(new HashMap<>(), new HashMap<>());
	}

	@Override
	public int getId(Val query) {
		Integer x = secondLookup.get(query);
    if(x == null) return -1;
    return x;
	}

	@Override
	public Val getValue(int query) {
		return firstLookup.get(query);
	}

	@Override
	public void put(int first, Val second) {
		firstLookup.put(first, second);
		secondLookup.put(second, first);
	}

  @Override
  public int insertOrGet(Val k) {
    int found = getId(k);
    if(found != -1) return found;

    int id = nextId++;
    put(id, k);
    return id;
  }
}

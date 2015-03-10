package jfoley.vocabpress.mem;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jfoley.
 */
public class DoubleMapInternSpace<Id, Val> implements InternSpace<Id, Val> {
	public final Map<Id, Val> firstLookup;
	public final Map<Val, Id> secondLookup;

	public DoubleMapInternSpace(Map<Id, Val> first, Map<Val, Id> second) {
		firstLookup = first;
		secondLookup = second;
	}
	public DoubleMapInternSpace() {
		this(new HashMap<Id, Val>(), new HashMap<Val, Id>());
	}

	@Override
	public Id getId(Val query) {
		return secondLookup.get(query);
	}

	@Override
	public Val getValue(Id query) {
		return firstLookup.get(query);
	}

	@Override
	public void put(Id first, Val second) {
		firstLookup.put(first, second);
		secondLookup.put(second, first);
	}
}

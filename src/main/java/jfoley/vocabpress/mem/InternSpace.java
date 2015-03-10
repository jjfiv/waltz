package jfoley.vocabpress.mem;

/**
 * @author jfoley.
 */
public interface InternSpace<Id, Val> {
	public Id getId(Val query);
	public Val getValue(Id query);
	public void put(Id first, Val second);
}

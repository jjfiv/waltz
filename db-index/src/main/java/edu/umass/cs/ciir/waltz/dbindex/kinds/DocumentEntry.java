package edu.umass.cs.ciir.waltz.dbindex.kinds;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * This is an ORMLite-mapped item to support our database-backed index.
 * @author jfoley.
 */
@DatabaseTable(tableName = "documents")
public class DocumentEntry {
	public static final int MaxNameWidth = 1024;

	/** Document id. */
	@DatabaseField(id=true, throwIfNull=true)
	public int id;

	/** Document name. */
	@DatabaseField(canBeNull=false, throwIfNull=true, width= MaxNameWidth)
	public String name;

	/** Document length. */
	@DatabaseField(throwIfNull=true)
	public int length;

	/** Needed for reflection. */
	@SuppressWarnings("unused")
	public DocumentEntry() {
		this.id = -1;
		this.name = null;
		this.length = 0;
	}

	public DocumentEntry(int id, String name, int length) {
		this.id = id;
		this.name = name;
		this.length = length;
		assert(name.length() < MaxNameWidth);
	}
}

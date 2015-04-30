package edu.umass.cs.ciir.waltz.dbindex;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * This is an ORMLite-mapped item to support our database-backed index.
 * @author jfoley.
 */
@DatabaseTable(tableName = "documents")
public class DocumentEntry {
	@DatabaseField(id=true, throwIfNull=true)
	public int id;

	@DatabaseField(canBeNull=false, throwIfNull=true, width=1024)
	public String name;

	public DocumentEntry() {
		this.id = Integer.MIN_VALUE;
		this.name = null;
	}
}

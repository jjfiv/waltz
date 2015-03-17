package edu.umass.cs.ciir.waltz;

import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley.
 */
public class PostingsGen {

	public static class PostingFieldDef {
		public int index;
		public String name;
		public String type;

		public PostingFieldDef(int index, String name, String type) {
			this.index = index;
			this.name = name;
			this.type = type;
		}
	}
	public static class PostingDef {
		public String javaPackage;
		public String name;
		public PostingFieldDef keyField;
		private List<PostingFieldDef> fields;

		public PostingDef(String javaPackage, String name) {
			this(javaPackage, name, null);
		}
		public PostingDef(String javaPackage, String name, PostingFieldDef keyField) {
			this.javaPackage = javaPackage;
			this.name = name;
			this.keyField = keyField;
			this.fields = new ArrayList<>();
		}

		public void addField(PostingFieldDef fieldDef) {
			this.fields.add(fieldDef);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(javaPackage).append(".").append(name).append(" { ");
			for (PostingFieldDef field : fields) {
				sb.append(field.name).append(": ").append(field.type);
				if(keyField != null) {
					if (field.index == keyField.index) {
						sb.append(" default");
					}
				}
				sb.append("; ");
			}
			sb.append("}");

			return sb.toString();
		}
	}

	public static void main(String[] args) throws IOException {
		Parameters info = Parameters.parseStream(PostingsGen.class.getResourceAsStream("/postings.json"));
		String jpackage = info.getString("package");
		for (Parameters jsonPDef  : info.getAsList("postings", Parameters.class)) {
			PostingDef pDef = new PostingDef(jsonPDef.get("package", jpackage), jsonPDef.getString("name"));
			String defaultKey = jsonPDef.getString("key");
			List<Parameters> fieldJSONList = jsonPDef.getAsList("fields", Parameters.class);
			for (int i = 0; i < fieldJSONList.size(); i++) {
				Parameters jsonField = fieldJSONList.get(i);
				PostingFieldDef fieldDef = new PostingFieldDef(i, jsonField.getString("name"), jsonField.getString("type"));
				pDef.addField(fieldDef);
				if(fieldDef.name.equals(defaultKey)) {
					pDef.keyField = fieldDef;
				}
			}

			System.out.println(pDef);

		}
	}
}

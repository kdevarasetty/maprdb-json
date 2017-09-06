package samples.ojai.maprdb_json;

import static java.lang.System.out;
import static samples.ojai.maprdb_json.util.TableUtil.deleteDocumentById;
import static samples.ojai.maprdb_json.util.TableUtil.getTable;
import static samples.ojai.maprdb_json.util.TableUtil.insertOneBean;
import static samples.ojai.maprdb_json.util.TableUtil.printAll;
import static samples.ojai.maprdb_json.util.TableUtil.printTableInformation;

import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.DocumentMutation;
import org.ojai.store.QueryCondition;

import com.mapr.db.MapRDB;
import com.mapr.db.Table;

import samples.ojai.maprdb_json.beans.Address;
import samples.ojai.maprdb_json.beans.SampleBean;

/**
 * Hello world!
 *
 */
public class App {

	public static final String TABLE_PATH = "/apps/sample";

	public static void main(String[] args) throws Exception {
		new App().run();
	}

	private Table table;

	private void run() throws Exception {
		// this.deleteTable(TABLE_PATH);
		this.table = getTable(TABLE_PATH);
		printTableInformation(TABLE_PATH);

		out.println("\n\n========== INSERT NEW RECORDS ==========");
		this.createDocuments();
		insertBean();

		out.println("\n\n========== QUERIES ==========");
		this.queryAll();
		this.queryDocuments();
		this.getBeanResultFromQuery();
		queryWithProjection();
		queryConditionEx();

		out.println("\n\n========== UPDATE ==========");
		this.updateDocuments();

		out.println("\n\n========== DELETE ==========");
		this.deleteById("3");

		this.table.close();

	}

	private void getBeanResultFromQuery() {
		out.println("Bean result from query");
		SampleBean bean = table.findById("1").toJavaBean(SampleBean.class);
		out.println(bean);

		out.println("Bean result from query");
		bean = table.findById("3").toJavaBean(SampleBean.class);
		out.println(bean);
	}

	private void queryWithProjection() {
		// all records in the table with projection
		out.println("\n\nAll records with projection");

		try (DocumentStream documentStream = table.find("name", "last_name")) {
			for (Document doc : documentStream) {
				out.println("\t" + doc);
			}
		}

		try (DocumentStream documentStream = table.find("_id", "name", "last_name")) {
			for (Document doc : documentStream) {
				out.println("\t" + doc);
			}
		}
	}

	private void queryAll() {
		printAll(table);
	}

	private void updateDocuments() {
		out.println("\t\tAdd last_name 1");
		out.println("before :\t" + table.findById("1"));

		// create a mutation
		DocumentMutation mutation = MapRDB.newMutation().set("last_name", "devarasetty");
		DocumentMutation anotherMutation = MapRDB.newMutation().set("active", true)
				.set("address.line", "1015 15th Avenue").set("address.city", "Redwood City").set("address.zip", 94065);
		table.update("1", mutation);
		table.update("1", anotherMutation);
		table.flush();

		out.println("after :\t" + table.findById("1"));
	}

	private void queryDocuments() {
		// get a single document
		Document record = table.findById("1");
		out.print("Single record\n\t");
		out.println(record);

		// print individual fields
		out.println("Id : " + record.getIdString() + " - name : " + record.getString("name"));
	}

	private void queryConditionEx() {
		QueryCondition condition = MapRDB.newCondition().is("name", QueryCondition.Op.EQUAL, "kiran").build();
		out.println("\n\nCondition: " + condition);
		try (DocumentStream docStream = table.find(condition)) {
			for (Document doc : docStream)
				out.println("\t" + doc);
		}
	}

	private void insertBean() {
		out.println("\n\nInserting bean");
		Address addr = new Address();
		addr.setCity("hyd");
		addr.setPincode("500007");

		SampleBean person = new SampleBean();
		person.setAddress(addr);
		person.setId("4");
		person.setName("kiran");
		person.setLastName("d");

		insertOneBean(table, person);
	}

	private void createDocuments() {
		Document doc = MapRDB.newDocument();
		doc.set("_id", "3");// only binary or string value
		doc.set("name", "dkk");
		table.insertOrReplace(doc);

		table.flush();
	}

	private void deleteById(String _id) {
		out.println("before :\t" + table.findById("3"));
		deleteDocumentById(table, _id);
		out.println("after :\t" + table.findById("3"));
	}

}

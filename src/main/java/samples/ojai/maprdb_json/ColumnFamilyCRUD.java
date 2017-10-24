package samples.ojai.maprdb_json;

import static java.lang.System.out;
import static org.ojai.store.QueryCondition.Op.*;
import static samples.ojai.maprdb_json.util.TableUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.json.Json;
import org.ojai.store.DocumentMutation;
import org.ojai.store.QueryCondition;
import org.ojai.store.exceptions.DocumentExistsException;
import org.ojai.types.ODate;

import com.mapr.db.Admin;
import com.mapr.db.MapRDB;
import com.mapr.db.Table;
import com.mapr.db.TableDescriptor;

import samples.ojai.maprdb_json.annotations.AboutTest;
import samples.ojai.maprdb_json.annotations.OverrideConf;
import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;
import samples.ojai.maprdb_json.beans.Address;
import samples.ojai.maprdb_json.beans.User;

@UseBean({
		@Property(name = "bean", value = "samples.ojai.maprdb_json.beans.User"),
		@Property(name = "bean.size", value = "10"),
		@Property(name = "bean.address.phone.size", value = "2"),
		@Property(name = "table.name", value = "/apps/cf_user") })
@AboutTest({ @Property(name = "test.name", value = "CRUD"),
		@Property(name = "test.category", value = "cf,crud,create,read,update,delete,select,insert"),
		@Property(name = "test.desc", value = "simple crud operations"),
		@Property(name = "cleanup.after", value = "true"),
		@Property(name = "setup.before", value = "true"),
		@Property(name = "cleanup.before", value = "true"),
		@Property(name = "data.file", value = "cf_crud_user.json") })
public class ColumnFamilyCRUD extends TestConfig {
	public static String TABLE_PATH = null;

	private Table table;

	void init() {
		TABLE_PATH = getProperty("table.name");
	}

	void setup() {
		init();
		table = getTable(TABLE_PATH);
		out.println(table.getTableDescriptor());
	}

	void cleanup() {
		init();
		overrideConf.clear();
		deleteTable(TABLE_PATH);
	}

	Table getTable(String tablePath) {
		// delete table
		if (MapRDB.tableExists(tablePath)) {
			MapRDB.deleteTable(tablePath);
		}

		TableDescriptor td = newTd(tablePath, 512/* MB */,
				/* bulkLoad = */false);
		addFamilyDescriptor(td, /* default = */true, /* inMemory = */true,
				null);
		addFamilyDescriptor(td, /* default = */false, /* inMemory = */false,
				"address");

		// Admin Tool
		Admin admin = MapRDB.newAdmin();
		return admin.createTable(td);
	}

	@OverrideConf({ @Property(name = "bean.size", value = "5"),
			@Property(name = "bean.interests.size", value = "3"),
			@Property(name = "data.file", value = "cf_crud_samplebean_insertseveralbeans.json"),
			@Property(name = "test.category", value = "insert several documents"),
			@Property(name = "test.keywords", value = "documents, several, multiple, insert, all") })
	void insertSeveralBeans() {
		out.println("\n\nInserting multiple beans");
		List<User> beans = new ArrayList<>();

		int numBeanSize = getInt("bean.size");
		for (int i = 0; i < numBeanSize; i++) {
			beans.add(generateBean());
		}

		insertMultipleBeans(table, beans);
		saveDataToFile(beans);
	}

	@OverrideConf({
			@Property(name = "data.file", value = "cf_crud_samplebean_insertseveralbeansWithoutAddress.json"),
			@Property(name = "test.category", value = "insert several documents"),
			@Property(name = "test.keywords", value = "documents, several, multiple, insert, all") })
	void insertSeveralBeansWithoutAddress() {
		out.println("\n\nInserting multiple beans excluding address");
		List<User> beans = new ArrayList<>();

		int numBeanSize = getInt("bean.size");
		for (int i = 0; i < numBeanSize; i++) {
			beans.add(generateBeanWithoutAddress());
	}

		insertMultipleBeans(table, beans);
		saveDataToFile(beans);
	}

	/**
	 * Create a User bean with randomly generated values. User.random() also
	 * calls Address.random()
	 * 
	 * @return
	 */
	private User generateBean() {
		return User.random(this);
	}

	/**
	 * Create a User bean with randomly generated values. Address is not set.
	 * 
	 * @return
	 */
	private User generateBeanWithoutAddress() {
		User bean = User.random(this);
		bean.setAddress(null);// too lazy to add a method in User.java
		return bean;
	}

	/**
	 * Create a Address bean with randomly generated values.
	 * 
	 * @return
	 */
	private Address generateAddress() {
		return Address.random(this);
	}

	/**
	 * Add address field to an existing "User" Document. Steps: 1. Generate a
	 * random Address bean. 2. Create a DocumenMutation object. 3. Create
	 * Document object from Address bean. 4. Set the document to the mutation.
	 * 5. Update table.
	 */
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "update document"),
			@Property(name = "test.keywords", value = "update, add, add attribute") })
	void addAddressToDocument() {
		Address address = generateAddress();
		// create a mutation and set document
		DocumentMutation mutation = newMutation().set("",
				Json.newDocument(address));
		update(table, "jdoe", mutation, true);
		printDocument("jdoe");
	}

	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "array, update document"),
			@Property(name = "test.keywords", value = "add to array, array, append"),
			@Property(name = "test.name", value = "add to array") })
	void addNewInterests() {
		// create a mutation
		DocumentMutation mutation = newMutation().append("interests",
				Collections.singletonList("development"));

		update(table, "jdoe", mutation, false);// don't flush yet
		update(table, "mdupont", mutation, true);// flush now
		// Get and display results
		out.println("Results of update operations");
		printDocument("jdoe");
		printDocument("mdupont");
	}

	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "delete attribute, remove attribute, update document"),
			@Property(name = "test.keywords", value = "delete, remove, attribute"),
			@Property(name = "test.name", value = "remove attribute") })
	void removeAttribute() {
		// create a mutation
		DocumentMutation mutation = MapRDB.newMutation().delete("dob");

		update(table, "jdoe", mutation, true);
		printDocument("jdoe");
	}

	private void printDocument(String _id) {
		// out.println(findById(table, _id).toJavaBean(User.class));
		out.println(findById(table, _id).asJsonString());
	}

	// Query tests
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "query by id"),
			@Property(name = "test.keywords", value = "id, one result, query"),
			@Property(name = "test.name", value = "query by id") })
	void queryById() {
		Document doc = findById(table, "jdoe");
		out.println(doc.asJsonString());
	}

	/**
	 * Choose column names in the table for selection
	 */
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "queryById, projection"),
			@Property(name = "test.keywords", value = "print, query, projection"),
			@Property(name = "test.name", value = "query by id with projection") })
	void queryByIdWithProjection() {
		Document doc = findById(table, "jdoe", "first_name", "dob");
		out.println(doc.asJsonString());
	}

	/**
	 * Query with a condition(QueryCondition)
	 */
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "query, condition"),
			@Property(name = "test.keywords", value = "query, condition, string, equals"),
			@Property(name = "test.name", value = "query with string equals condition") })
	void queryWithStringCondition() {
		// find with condition
		out.println("\n\nFind with condition");
		out.println("\n\n");

		// Condition equals a string
		QueryCondition condition = MapRDB.newCondition()
				.is("last_name", QueryCondition.Op.EQUAL, "Doe").build();
		out.println("\n\nCondition: " + condition);
		executeQueryAndPrintResults(table, condition);
	}

	/**
	 * Query with a condition(QueryCondition) for date range
	 */
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "query, condition, date"),
			@Property(name = "test.keywords", value = "query, condition, date, range"),
			@Property(name = "test.name", value = "query with date range condition") })
	void queryWithDateRangeCondition() {
		// Condition as date range
		QueryCondition condition = MapRDB.newCondition().and()
				.is("dob", GREATER_OR_EQUAL, ODate.parse("1980-01-01"))
				.is("dob", LESS, ODate.parse("1981-01-01")).close().build();
		out.println("\n\nCondition: " + condition);
		executeQueryAndPrintResults(table, condition);
	}

	/**
	 * Query with a condition(QueryCondition) on sub document
	 */
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "query, condition, subdocument"),
			@Property(name = "test.keywords", value = "query, condition, subdocument, equals"),
			@Property(name = "test.name", value = "query with condition on sub document") })
	void queryWithSubDocumentCondition() {
		// Condition in sub document
		QueryCondition condition = MapRDB.newCondition()
				.is("address.pincode", EQUAL, 95109).build();
		out.println("\n\nCondition: " + condition);
		executeQueryAndPrintResults(table, condition);
	}

	/**
	 * Query with a condition(QueryCondition) on array value
	 */
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "query, condition, array"),
			@Property(name = "test.keywords", value = "query, condition, array, equals"),
			@Property(name = "test.name", value = "query with condition on array") })
	void queryWithArrayValue() {
		// Contains a specific value in an array
		QueryCondition condition = MapRDB.newCondition().is("interests[]",
				EQUAL, "sports");

		out.println("\n\nCondition: " + condition);
		executeQueryAndPrintResults(table, condition);
	}

	/**
	 * Query with a condition(QueryCondition) on array index
	 */
	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "query, condition, array, index"),
			@Property(name = "test.keywords", value = "query, condition, array, index, equals"),
			@Property(name = "test.name", value = "query with condition with value at array index") })
	void queryWithArrayIndex() {
		// Contains a value at a specific index
		QueryCondition condition = MapRDB.newCondition()
				.is("interests[0]", EQUAL, "sports").build();
		out.println("\n\nCondition: " + condition);
		executeQueryAndPrintResults(table, condition);
	}

	@OverrideConf({ @Property(name = "depends", value = "createDocuments"),
			@Property(name = "test.category", value = "query, condition, date"),
			@Property(name = "test.keywords", value = "query, condition, date, range"),
			@Property(name = "test.name", value = "query with date range condition - duplicate") })
	void queryWithDateRange() {
		// Condition as date range
		QueryCondition condition = MapRDB.newCondition().and()
				.is("dob", GREATER_OR_EQUAL, ODate.parse("1980-01-01"))
				.is("dob", LESS, ODate.parse("1981-01-01")).close().build();
		out.println("\n\nCondition: " + condition);
		executeQueryAndPrintResults(table, condition);
	}

	/**
	 * Inserts documents with known values instead of randomly generated values.
	 * Useful for some dependent methods if data file is not necessary.
	 */
	@OverrideConf({ @Property(name = "test.category", value = "create"),
			@Property(name = "test.keywords", value = "create, insert, multiple, reference"),
			@Property(name = "test.name", value = "crete multiple documents with known values") })
	void createDocuments() throws IOException {

		// Create a new document (simple format)
		Document document = MapRDB.newDocument().set("_id", "jdoe")
				.set("first_name", "John").set("last_name", "Doe")
				.set("dob", ODate.parse("1970-06-23"));

		// save document into the table
		table.insertOrReplace(document);

		// create a new document without _id
		document = MapRDB.newDocument().set("first_name", "David")
				.set("last_name", "Simon")
				.set("dob", ODate.parse("1980-10-13"));

		table.insert("dsimon", document);

		// create a new document from a simple bean
		// look at the User class to see how you can use JSON Annotation to
		// drive the format of the document
		User user = new User();
		user.setId("alehmann");
		user.setFirstName("Andrew");
		user.setLastName("Lehmann");
		user.setDob(ODate.parse("1980-10-13"));
		user.addInterest("html");
		user.addInterest("css");
		user.addInterest("js");
		document = MapRDB.newDocument(user);

		// save document into the table
		table.insertOrReplace(document);

		// try to insert the same document ID
		try {
			table.insert("dsimon", document);
		} catch (DocumentExistsException dee) {
			out.println("Document with key dsimon already exists");
	}

		// Create more complex Record
		document = MapRDB.newDocument().set("_id", "mdupont")
				.set("first_name", "Maxime").set("last_name", "Dupont")
				.set("dob", ODate.parse("1982-02-03"))
				.set("interests",
						Arrays.asList("sports", "movies", "electronics"))
				.set("address.city", "San Jose")
				.set("address.pincode", "95109");
		table.insert(document);

		// Another way to create sub document
		// Create the sub document as document and use it to set the value
		Document addressRecord = MapRDB.newDocument()
				.set("city", "San Francisco").set("pincode", "94105");

		document = MapRDB.newDocument().set("_id", "rsmith")
				.set("first_name", "Robert").set("last_name", "Smith")
				.set("dob", ODate.parse("1982-02-03"))
				.set("interests",
						Arrays.asList("electronics", "music", "sports"))
				.set("address", addressRecord);
		table.insert(document);

		table.flush(); // flush to the server

	}


	/**
	 * Print all records/documents in the table.
	 */
	@OverrideConf({ @Property(name = "bean.size", value = "5"),
			@Property(name = "bean.address.phone.size", value = "3"),
			@Property(name = "depends", value = "insertSeveralBeans"),
			@Property(name = "test.category", value = "query all documents"),
			@Property(name = "test.keywords", value = "print, all, query"),
			@Property(name = "test.name", value = "query all") })
	void queryAll() {
		printAll(table);
	}

	@OverrideConf({
			@Property(name = "depends", value = "insertSeveralBeansFromFile"),
			@Property(name = "test.category", value = "query all documents"),
			@Property(name = "test.keywords", value = "print, all, query, file"),
			@Property(name = "test.name", value = "query all with data from file"),
			@Property(name = "use.data.file", value = "cf_crud_samplebean_insertseveralbeans.json") })
	void queryAllWithDataFile() {
		try {
			// importData(table);
			// insertSeveralBeansFromFile();
		printAll(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

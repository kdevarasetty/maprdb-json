/**
 * 
 */
package samples.ojai.maprdb_json;

import static java.lang.System.out;
import static samples.ojai.maprdb_json.util.TableUtil.deleteAllDocuments;
import static samples.ojai.maprdb_json.util.TableUtil.deleteTable;
import static samples.ojai.maprdb_json.util.TableUtil.findBeanById;
import static samples.ojai.maprdb_json.util.TableUtil.getTable;
import static samples.ojai.maprdb_json.util.TableUtil.insertMultipleBeans;
import static samples.ojai.maprdb_json.util.TableUtil.insertOneBean;
import static samples.ojai.maprdb_json.util.TableUtil.printAll;

import java.util.ArrayList;
import java.util.List;

import com.mapr.db.Table;

import samples.ojai.maprdb_json.annotations.AboutTest;
import samples.ojai.maprdb_json.annotations.OverrideConf;
import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;
import samples.ojai.maprdb_json.beans.SampleBean;

/**
 * @author kirand
 *
 */

@UseBean({
		@Property(name = "bean", value = "samples.ojai.maprdb_json.beans.SampleBean"),
		@Property(name = "bean.size", value = "10"),
		@Property(name = "bean.address.phone.size", value = "2"),
		@Property(name = "table.name", value = "/apps/nested_docs") })
@AboutTest({ @Property(name = "test.name", value = "AnotherTest"),
		@Property(name = "test.category", value = "cat1"),
		@Property(name = "test.desc", value = "simple description"),
		@Property(name = "cleanup.after", value = "false"),
		@Property(name = "setup.before", value = "false"),
		@Property(name = "data.file", value = "anothertest_samplebean.json") })
public class AnotherTest extends TestConfig {

	public static String TABLE_PATH = null;

	private Table table;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AnotherTest().run();
	}

	private void run() {
		deleteTable(TABLE_PATH);
		// get or create-get table
		table = getTable(TABLE_PATH);
		insertBean();
		queryAll();
		findBean();
		insertSeveralBeans();
		queryAll();
	}

	void init() {
		TABLE_PATH = getProperty("table.name");
	}

	void setup() {
		init();
		table = getTable(TABLE_PATH);
	}

	void cleanup() {
		init();
		overrideConf.clear();
		deleteTable(TABLE_PATH);
	}

	@OverrideConf({ @Property(name = "cleanup.after", value = "true"),
			@Property(name = "setup.before", value = "true") })
	void insertBean() {
		out.println("\n\nInserting bean");
		SampleBean person = generateBean();
		insertOneBean(table, person);
	}

	@OverrideConf({ @Property(name = "bean.size", value = "5"),
			@Property(name = "bean.address.phone.size", value = "3"),
			@Property(name = "cleanup.after", value = "true"),
			@Property(name = "setup.before", value = "true"),
			@Property(name = "data.file", value = "anothertest_samplebean_insertseveralbeans.json"),
			@Property(name = "test.category", value = "insert several documents"),
			@Property(name = "test.keywords", value = "documents, several, multiple, insert, all") })
	void insertSeveralBeans() {
		out.println("\n\nInserting multiple beans");
		List<SampleBean> beans = new ArrayList<>();

		int numBeanSize = getInt("bean.size");
		for (int i = 0; i < numBeanSize; i++) {
			beans.add(generateBean());
		}

		insertMultipleBeans(table, beans);
		saveDataToFile(beans);
	}

	private void insertSeveralBeansAsDocumentStream() {

	}

	private SampleBean generateBean() {
		return SampleBean.random(this);
	}

	@OverrideConf({ @Property(name = "cleanup.before", value = "true"),
			@Property(name = "cleanup.after", value = "true"),
			@Property(name = "setup.before", value = "true"),
			@Property(name = "depends", value = "insertSeveralBeansFromFile"),
			@Property(name = "test.category", value = "find one document"),
			@Property(name = "test.keywords", value = "print, one, query, file, id"),
			@Property(name = "test.name", value = "query for one document with data from file"),
			@Property(name = "use.data.file", value = "anothertest_samplebean.json") })
	void findBean() {
		SampleBean resultBean = findBeanById(table, "4XZGb", SampleBean.class);
		out.println("\n\nAfter converting result to Java Bean");
		out.println("\t" + resultBean);
	}

	/**
	 * Print all records/documents in the table.
	 */
	@OverrideConf({ @Property(name = "bean.size", value = "5"),
			@Property(name = "bean.address.phone.size", value = "3"),
			@Property(name = "cleanup.before", value = "true"),
			@Property(name = "cleanup.after", value = "true"),
			@Property(name = "setup.before", value = "true"),
			@Property(name = "depends", value = "insertSeveralBeans"),
			@Property(name = "test.category", value = "query all documents"),
			@Property(name = "test.keywords", value = "print, all, query"),
			@Property(name = "test.name", value = "query all") })
	void queryAll() {
		printAll(table);
	}

	@OverrideConf({ @Property(name = "cleanup.before", value = "true"),
			@Property(name = "cleanup.after", value = "true"),
			@Property(name = "setup.before", value = "true"),
			@Property(name = "depends", value = "insertSeveralBeansFromFile"),
			@Property(name = "test.category", value = "query all documents"),
			@Property(name = "test.keywords", value = "print, all, query, file"),
			@Property(name = "test.name", value = "query all with data from file"),
			@Property(name = "use.data.file", value = "anothertest_samplebean.json") })
	void queryAllWithDataFile() {
		try {
			// importData(table);
			// insertSeveralBeansFromFile();
			printAll(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void insertSeveralBeansFromFile() {
		out.println("\n\nInserting multiple beans");
		loadData(getProperty("use.data.file"), getProperty("bean"));
		List<Object> beans = (List<Object>) context.get("beans.list");
		insertMultipleBeans(table, beans);
	}

	private void deleteAll() {
		deleteAllDocuments(table);
	}
}

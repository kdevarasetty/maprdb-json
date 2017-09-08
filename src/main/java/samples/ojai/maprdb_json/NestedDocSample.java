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

import samples.ojai.maprdb_json.annotations.Property;
import samples.ojai.maprdb_json.annotations.UseBean;
import samples.ojai.maprdb_json.beans.Address;
import samples.ojai.maprdb_json.beans.SampleBean;
import samples.ojai.maprdb_json.util.ConfigUtil;

/**
 * @author kirand
 *
 */

@UseBean({
		@Property(name = "bean", value = "samples.ojai.maprdb_json.beans.SampleBean"),
		@Property(name = "bean.size", value = "10"),
		@Property(name = "bean.address.phone.size", value = "2")
		})
public class NestedDocSample extends TestConfig {

	public static final String TABLE_PATH = "/apps/nested_docs";

	private Table table;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NestedDocSample().run();
	}

	private void run() {
		deleteTable(TABLE_PATH);
		// get or create-get table
		table = getTable(TABLE_PATH);
		insertBean();
		queryAll();
		findBean("4");
		insertSeveralBeans();
		queryAll();
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

	private void insertSeveralBeans() {
		out.println("\n\nInserting multiple beans");
		List<SampleBean> beans = new ArrayList<>();
		Address addr = new Address();
		addr.setCity("hyd");
		addr.setPincode("500007");

		SampleBean person = new SampleBean();
		person.setAddress(addr);
		person.setId("4");
		person.setName("kiran");
		person.setLastName("d");
		beans.add(person);
		
		int numBeanSize = ConfigUtil.getInt(config, "bean.size");
		for (int i = 0; i < numBeanSize; i++) {
			beans.add(generateBean());
		}
		
		insertMultipleBeans(table, beans);
	}
	
	private void insertSeveralBeansAsDocumentStream() {
		
	}

	private SampleBean generateBean() {
		return SampleBean.random(config);
	}

	private void findBean(String _id) {
		SampleBean resultBean = findBeanById(table, _id, SampleBean.class);
		out.println("\n\nAfter converting result to Java Bean");
		out.println("\t" + resultBean);
	}

	/**
	 * Print all records/documents in the table.
	 */
	private void queryAll() {
		printAll(table);
	}

	private void deleteAll() {
		deleteAllDocuments(table);
	}

}

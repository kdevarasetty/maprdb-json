package samples.ojai.maprdb_json;

import static java.lang.System.out;
import static samples.ojai.maprdb_json.util.TableUtil.*;

import com.mapr.db.Table;

import samples.ojai.maprdb_json.annotations.*;

@UseBean({
	@Property(name = "bean", value = "")
})
public class OJAITypesTest extends TestConfig {

	public static void main(String[] args) {
		new OJAITypesTest().run();
	}

	private void run() {
		
	}

}

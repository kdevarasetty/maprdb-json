package samples.ojai.maprdb_json.beans;

import static samples.ojai.maprdb_json.util.Randoms.randomAlphabeticString;
import static samples.ojai.maprdb_json.util.Randoms.randomAlphanumericString;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SampleBean {
	private String id;
	private String name;
	private String lastName;
	private Address address;

	@JsonProperty("address")
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@JsonProperty("_id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("last_name")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@Override
	public String toString() {
		return String.format("SampleBean [id=%s, name=%s, lastName=%s, address=%s]", id, name, lastName, address);
	}

	public static SampleBean random() {
		SampleBean bean = new SampleBean();
		bean.setId(randomAlphanumericString(5));
		bean.setName(randomAlphabeticString(6));
		bean.setLastName(randomAlphabeticString(5));
		bean.setAddress(Address.random());
		return bean;
	}

	public static SampleBean random(Properties config) {
		SampleBean bean = new SampleBean();
		bean.setId(randomAlphanumericString(5));
		bean.setName(randomAlphabeticString(6));
		bean.setLastName(randomAlphabeticString(5));
		bean.setAddress(Address.random(config));
		return bean;
	}
}

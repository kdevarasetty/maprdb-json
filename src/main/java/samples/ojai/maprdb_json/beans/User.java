package samples.ojai.maprdb_json.beans;

import static samples.ojai.maprdb_json.util.Randoms.randomAlphabeticString;
import static samples.ojai.maprdb_json.util.Randoms.randomAlphanumericString;
import static samples.ojai.maprdb_json.util.Randoms.randomDate;
import static samples.ojai.maprdb_json.util.Randoms.randomNumericString;

import java.util.ArrayList;
import java.util.List;

import org.ojai.types.ODate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import samples.ojai.maprdb_json.TestConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

	private String id;
	private String firstName;
	private String lastName;
	private ODate dob;
	private List<String> interests;
	private Address address;

	public User() {
	}

	@JsonProperty("_id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("first_name")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@JsonProperty("last_name")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public ODate getDob() {
		return dob;
	}

	public void setDob(ODate dob) {
		this.dob = dob;
	}

	public List<String> getInterests() {
		return interests;
	}

	public void setInterests(List<String> interests) {
		this.interests = interests;
	}

	public void addInterest(String interest) {
		if (interests == null) {
			interests = new ArrayList<String>();
		}
		interests.add(interest);
	}

	@JsonProperty("address")
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "User{" + "interests=" + interests + ", id='" + id + '\''
				+ ", firstName='" + firstName + '\'' + ", lastName='" + lastName
				+ '\'' + ", dob=" + dob + ", address=" + address + '}';
	}

	public static User random(TestConfig config) {
		User bean = new User();
		bean.setId(randomAlphanumericString(5));
		bean.setFirstName(randomAlphabeticString(6));
		bean.setLastName(randomAlphabeticString(5));
		bean.setDob(randomDate());
		int interestsCount = config.getInt("bean.interests.size");
		List<String> interests = new ArrayList<>();
		for (int i = 0; i < interestsCount; i++) {
			interests.add(randomNumericString(10));
		}
		bean.setInterests(interests);

		bean.setAddress(Address.random(config));
		return bean;
	}
}

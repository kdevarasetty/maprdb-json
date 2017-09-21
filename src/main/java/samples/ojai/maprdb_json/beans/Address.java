package samples.ojai.maprdb_json.beans;

import static samples.ojai.maprdb_json.util.Randoms.randomAlphabeticString;
import static samples.ojai.maprdb_json.util.Randoms.randomNumericString;

import java.util.Arrays;

import samples.ojai.maprdb_json.TestConfig;

public class Address {
	private String city;
	private String pincode;
	private String[] phone;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	@Override
	public String toString() {
		return String.format("Address [city=%s, pincode=%s, phone=%s]", city,
				pincode, Arrays.toString(phone));
	}

	public static Address random() {
		Address addr = new Address();
		addr.setCity(randomAlphabeticString(10));
		addr.setPincode(randomNumericString(7));
		addr.setPhone(new String[] { "1234567890" });
		return addr;
	}

	public static Address random(TestConfig config) {
		Address addr = new Address();
		addr.setCity(randomAlphabeticString(10));
		addr.setPincode(randomNumericString(7));

		int phoneCount = config.getInt("bean.address.phone.size");
		String[] phone = new String[phoneCount];
		for (int i = 0; i < phoneCount; i++) {
			phone[i] = randomNumericString(10);
		}
		addr.setPhone(phone);
		return addr;
	}

	public String[] getPhone() {
		return phone;
	}

	public void setPhone(String[] phone) {
		this.phone = phone;
	}
}

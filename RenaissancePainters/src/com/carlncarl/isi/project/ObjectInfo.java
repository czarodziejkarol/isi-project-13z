package com.carlncarl.isi.project;

public class ObjectInfo {

	public static final String FEMALE = "f";
	public static final String MALE = "m";

	private String value;
	private String type;

	public ObjectInfo(String value, String type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		ObjectInfo oB = (ObjectInfo) obj;
		if (this.value.equals(oB.getValue())) {
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return value;
	}
}

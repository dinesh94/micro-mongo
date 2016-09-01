package generic.mongo.microservices.model;

import java.util.List;

public class Condition {

	private String searchpath;
	private List<String> values;
	private Boolean isOr = true;

	public String getSearchpath() {
		return searchpath;
	}

	public List<String> getValues() {
		return values;
	}

	public Boolean getIsOr() {
		return isOr;
	}

	public void setSearchpath(String searchpath) {
		this.searchpath = searchpath;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void setIsOr(Boolean isOr) {
		this.isOr = isOr;
	}

}

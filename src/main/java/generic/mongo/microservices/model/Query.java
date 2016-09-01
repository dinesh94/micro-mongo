package generic.mongo.microservices.model;

import java.util.List;

public class Query {
	private List<Condition> condition;
	private String sortOn;
	private boolean isSortAscending;

	public String getSortOn() {
		return sortOn;
	}

	public boolean isSortAscending() {
		return isSortAscending;
	}

	public void setSortOn(String sortOn) {
		this.sortOn = sortOn;
	}

	public void setSortAscending(boolean isSortAscending) {
		this.isSortAscending = isSortAscending;
	}

	public List<Condition> getCondition() {
		return condition;
	}

	public void setCondition(List<Condition> condition) {
		this.condition = condition;
	}
}

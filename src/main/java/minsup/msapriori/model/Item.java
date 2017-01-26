package minsup.msapriori.model;

public class Item {

	private String itemName;
	private Double minimumSupportValue;
	private Double actualSupportValue;
	private int itemCount;

	public Item() {
		super();
	}

	public Item(String itemName,int itemCount, Double minimumSupportValue, Double actualSupportValue) {
		super();
		this.itemName = itemName;
		this.itemCount = itemCount;
		this.minimumSupportValue = minimumSupportValue;
		this.actualSupportValue = actualSupportValue;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public double getMinimumSupportValue() {
		return minimumSupportValue;
	}

	public void setMinimumSupportValue(Double minimumSupportValue) {
		this.minimumSupportValue = minimumSupportValue;
	}

	public Double getActualSupportValue() {
		return actualSupportValue;
	}

	public void setActualSupportValue(Double actualSupportValue) {
		this.actualSupportValue = actualSupportValue;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getItemName();
		//		return GsonUtil.getGson().toJson(this);
	}
}

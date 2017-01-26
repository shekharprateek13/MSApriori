package minsup.msapriori.model;

import java.util.LinkedHashSet;
import java.util.Set;

import minsup.msapriori.utils.GsonUtil;

public class FrequentItemSet  implements Cloneable{

	private Set<Item> frequentItemSet;
	private Double frequentItemSetSupportCount;
	private int frequentItemSetCount;
	private int tailCount;
	
	public FrequentItemSet() {
		super();
	}
	
	public FrequentItemSet(Set<Item> frequentItemSet, Double frequentItemSetSupportCount, int frequentItemSetCount) {
		super();
		this.frequentItemSet = frequentItemSet;
		this.frequentItemSetSupportCount = frequentItemSetSupportCount;
		this.frequentItemSetCount = frequentItemSetCount;
	}
	
	public Set<Item> getFrequentItemSet() {
		return frequentItemSet;
	}

	public void setFrequentItemSet(Set<Item> frequentItemSet) {
		this.frequentItemSet = frequentItemSet;
	}

	public Double getFrequentItemSetSupportCount() {
		return frequentItemSetSupportCount;
	}

	public void setFrequentItemSetSupportCount(Double frequentItemSetSupportCount) {
		this.frequentItemSetSupportCount = frequentItemSetSupportCount;
	}

	public int getFrequentItemSetCount() {
		return frequentItemSetCount;
	}

	public void setFrequentItemSetCount(int frequentItemSetCount) {
		this.frequentItemSetCount = frequentItemSetCount;
	}
	
	public int getTailCount() {
		return tailCount;
	}

	public void setTailCount(int tailCount) {
		this.tailCount = tailCount;
	}

	public FrequentItemSet getClone(){
		return this.clone();
	}
	
	public Set<String> getItemNameSet(){
		Set<String> itemNameSet = new LinkedHashSet<String>();
		for(Item item:this.frequentItemSet){
			itemNameSet.add(item.getItemName());
		}
		return itemNameSet;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((frequentItemSet == null) ? 0 : frequentItemSet.hashCode());
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
		FrequentItemSet other = (FrequentItemSet) obj;
		if (frequentItemSet == null) {
			if (other.frequentItemSet != null)
				return false;
		} else if (!frequentItemSet.equals(other.frequentItemSet))
			return false;
		return true;
	}

	@Override
	protected FrequentItemSet clone(){
		FrequentItemSet clonedFrequentItemSet = null;
		try{
			clonedFrequentItemSet = (FrequentItemSet)super.clone();
			Set<Item> frequentItems = new LinkedHashSet<Item>();
			for(Item item:this.getFrequentItemSet()){
				frequentItems.add(item);
			}
			clonedFrequentItemSet.setFrequentItemSet(frequentItems);
		}catch(CloneNotSupportedException e){
			e.printStackTrace();
		}
		return clonedFrequentItemSet;
	}

	@Override
	public String toString(){
		return GsonUtil.getGson().toJson(this);
	}
}

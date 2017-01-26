package minsup.msapriori.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minsup.msapriori.model.FrequentItemSet;
import minsup.msapriori.model.Item;

public class MSApriori {

	private static List<Set<Item>> TRANSACTION_DATA;
	private static Set<Item> ITEM_SET;
	private static Double SUPPORT_DIFFERENCE_CONSTRAINT;
	private static List<String> MUST_NOT_HAVE_ITEM_CONSTRAINT;
	private static List<String> MUST_HAVE_ITEM_CONSTRAINT;
	private Set<Set<String>> MUST_NOT_HAVE_ITEM_PAIRS = new HashSet<>();

	private List<Set<Item>> readTransactionData(File inputFile) throws FileNotFoundException, IOException{
		List<Set<Item>> tempTransactionData = new ArrayList<Set<Item>>();
		String line;
		try{
			FileReader fileReader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while((line = bufferedReader.readLine()) != null){
				parseLine(line, tempTransactionData);
			}
			bufferedReader.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("File not found: '" + inputFile + "'");                
			ex.printStackTrace();
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + inputFile + "'");                  
			ex.printStackTrace();
		}

		MSApriori.TRANSACTION_DATA = tempTransactionData;
		return MSApriori.TRANSACTION_DATA;
	}

	private static void parseLine(String line, List<Set<Item>> tempTransactionData){
		line = line.substring(1, line.length()-1);
		Set<Item> tempSet = new HashSet<>();
		String[] tokens = line.split(",");
		for(int i=0; i<tokens.length; i++){
			tempSet.add(new Item(tokens[i].trim(), 0, 0d, 0d));
		}
		tempTransactionData.add(tempSet);
	}

	private static void createItemSet(String filePath){
		Set<Item> tempItemSet = new HashSet<Item>();
		try{
			File parameterFile = new File(filePath);

			if(!parameterFile.exists()) return;

			FileReader fReader = new FileReader(parameterFile);
			BufferedReader bReader = new BufferedReader(fReader);
			String parameterLine = bReader.readLine();
			while(parameterLine != null){
				if(parameterLine.contains("MIS")){
					String[] tempArray = parameterLine.split("=");
					if(tempArray.length == 2){
						String itemName = tempArray[0].trim().replaceAll("\\D+","");
						Double minSupportValue = Double.parseDouble(tempArray[1].trim());
						Item tempItem = new Item(itemName,0,minSupportValue,null);
						tempItemSet.add(tempItem);
					}
				}else if(parameterLine.contains("SDC")){
					MSApriori.SUPPORT_DIFFERENCE_CONSTRAINT = Double.parseDouble(parameterLine.replaceAll("[^0-9\\.]+","")); 
				}else if(parameterLine.contains("cannot_be_together")){
					String itemNameString = parameterLine.replaceAll("[^0-9,]","");
					String[] itemNameArray = itemNameString.split(",");
					MSApriori.MUST_NOT_HAVE_ITEM_CONSTRAINT = Arrays.asList(itemNameArray);
				}else if(parameterLine.contains("must-have")){
					String tempItemLabels = parameterLine.trim().replaceAll(" or ",",").replaceAll("[^0-9,]","");
					MSApriori.MUST_HAVE_ITEM_CONSTRAINT = Arrays.asList(tempItemLabels.trim().split(","));
				}
				parameterLine = bReader.readLine();
			}
			bReader.close();
			fReader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		MSApriori.ITEM_SET = tempItemSet;
	}

	// generate pairs of cannot_be_together pairs and return a set of pairs
	private Set<Set<String>> createPairsSet(List<String> constraintList){
		Set<Set<String>> pairsSet = new HashSet<>();
		Set<String> tempSet = new HashSet<>();
		for(int i=0; i<constraintList.size()-1; i++){
			for(int j=i+1; j<constraintList.size(); j++){
				tempSet.add(constraintList.get(i)); 
				tempSet.add(constraintList.get(j)); 
				pairsSet.add(new HashSet<String>(tempSet));
				tempSet.clear();
			}
		}
		return pairsSet;
	}

	private List<FrequentItemSet> applyMustNotConstraint(List<FrequentItemSet> finalFrequentItemsList){
		Set<FrequentItemSet> frequentItemsListAfterMustNot = new LinkedHashSet<>();
		for(FrequentItemSet frequentItemSet : finalFrequentItemsList){
			Set<String> tempItemNameSet = frequentItemSet.getItemNameSet();
			int count = 0;
			for(Set<String> itemPairs : this.MUST_NOT_HAVE_ITEM_PAIRS){
				if(tempItemNameSet.containsAll(itemPairs)){
					count++;
				}
			}

			if(count == 0){
				frequentItemsListAfterMustNot.add(frequentItemSet);
			}
		}
		return new LinkedList<FrequentItemSet>(frequentItemsListAfterMustNot);

	}

	private List<FrequentItemSet> applyMustHaveConstraint(List<FrequentItemSet> frequentItemSetList){
		List<FrequentItemSet> listAfterConstraintApplied = new LinkedList<FrequentItemSet>();
		for(FrequentItemSet frequentItemSet:frequentItemSetList){
			Set<Item> tempSet = frequentItemSet.getFrequentItemSet();
			int count = 0;
			for(Item item:tempSet){
				if(MSApriori.MUST_HAVE_ITEM_CONSTRAINT.contains(item.getItemName())){
					count++;
					break;
				}
			}
			if(count > 0){
				listAfterConstraintApplied.add(frequentItemSet);
			}	
		}
		return listAfterConstraintApplied;
	}

	private Set<Item> sortItemsOnMISValue(Set<Item> itemSet){
		List<Item> tempList = new ArrayList<Item>(itemSet); 
		Collections.sort(tempList, new Comparator<Item>(){
			@Override
			public int compare(Item item1, Item item2) {
				if(item1.getMinimumSupportValue() > item2.getMinimumSupportValue())
					return 1;
				else if(item1.getMinimumSupportValue() == item2.getMinimumSupportValue())
					return 0;
				else 
					return -1;
			}
		});
		return new LinkedHashSet<Item>(tempList);
	}

	private List<Set<Item>> getSubsets(List<Set<Item>> resultingSubsets, List<Item> inputItemSet, Set<Item> frequentItemSubset, int subsetSize) {
		if(frequentItemSubset!=null && frequentItemSubset.size() == subsetSize){
			resultingSubsets.add(frequentItemSubset);
		}else{
			for(int i = 0;i < inputItemSet.size(); i++){
				Set<Item> tempSet = new LinkedHashSet<Item>(frequentItemSubset);
				tempSet.add(inputItemSet.get(i));
				getSubsets(resultingSubsets,inputItemSet.subList(i+1, inputItemSet.size()),tempSet,subsetSize);
			}
		}
		return resultingSubsets;
	}

	private List<Set<Item>> getFrequentItemSet(List<FrequentItemSet> inputList){
		List<Set<Item>> frequentItemSetList = new LinkedList<Set<Item>>();
		for(FrequentItemSet itemSet: inputList){
			frequentItemSetList.add(itemSet.getFrequentItemSet());
		}
		return frequentItemSetList;
	}

	private void setFrequentItemSetTailCount(List<FrequentItemSet> frequentItemSet){

		for(Set<Item> txnItemSet:MSApriori.TRANSACTION_DATA){
			for(FrequentItemSet frequentItems:frequentItemSet){
				Set<Item> tempSet = frequentItems.getFrequentItemSet();
				if(tempSet.size() > 1){
					List<Item> tempList = Arrays.asList(tempSet.toArray(new Item[tempSet.size()]));
					tempList = tempList.subList(1,tempList.size());
					if(txnItemSet.containsAll(tempList)){
						frequentItems.setTailCount(frequentItems.getTailCount() + 1);
					}
				}	
			}
		}
	}

	public Set<Item> initPass(Set<Item> sortedItemSet,List<Set<Item>> transactionData){
		Set<Item> itemSetAfterInitPass = new LinkedHashSet<Item>();
		int totalNoOfTxn = transactionData.size();
		//1. Count the number of occurrences of each item in transaction data set and also set the actual support count of the item.
		for(Item item:sortedItemSet){
			for(Set<Item> txnItemSet:transactionData){
				if(txnItemSet.contains(item)){
					item.setItemCount(item.getItemCount() + 1);
				}
			}
			item.setActualSupportValue((item.getItemCount()*1.0)/totalNoOfTxn);
		}

		Item itemWithMinSupport = null;
		for(Item item:sortedItemSet){
			if(itemSetAfterInitPass.isEmpty() && (item.getActualSupportValue() >= item.getMinimumSupportValue())){
				itemWithMinSupport = item;
				itemSetAfterInitPass.add(item);
			}else{
				if(!itemSetAfterInitPass.isEmpty() && item.getActualSupportValue() > itemWithMinSupport.getMinimumSupportValue()){
					itemSetAfterInitPass.add(item);
				}
			}
		}
		System.out.println("Items After Init Pass: "+itemSetAfterInitPass);
		return itemSetAfterInitPass;
	}

	public List<FrequentItemSet> level2CandidateGen(Set<Item> itemSetAfterInitPass,Double supportDiffConstraint){
		List<FrequentItemSet> frequent2ItemSetList = new LinkedList<FrequentItemSet>();
		Item[] itemArray = itemSetAfterInitPass.toArray(new Item[itemSetAfterInitPass.size()]);

		for(int i = 0;i<itemSetAfterInitPass.size()-1;i++){
			Item initItem = itemArray[i];
			if(initItem.getActualSupportValue() >= initItem.getMinimumSupportValue()){
				for(int j = i+1;j<itemSetAfterInitPass.size();j++){
					Item postInitItem = itemArray[j];
					double supportDiff = postInitItem.getActualSupportValue() - initItem.getActualSupportValue();
					supportDiff = (supportDiff < 0)?(-1*supportDiff):supportDiff;
					if((postInitItem.getActualSupportValue() >= initItem.getMinimumSupportValue()) 
							&& supportDiff <= supportDiffConstraint){
						Set<Item> tempSet = new LinkedHashSet<Item>();
						tempSet.add(initItem);
						tempSet.add(postInitItem);

						FrequentItemSet frequent2ItemSet = new FrequentItemSet();
						frequent2ItemSet.setFrequentItemSet(tempSet);
						frequent2ItemSetList.add(frequent2ItemSet);
					}
				}
			}	
		}
		return frequent2ItemSetList;
	}

	public List<FrequentItemSet> MSCandidateGen(List<FrequentItemSet> inputFrequentItemSetList,Double supportDiffConstraint){
		List<FrequentItemSet> frequentItemSetList = new LinkedList<FrequentItemSet>();
		List<Set<Item>> fK1FrequentItemSet = this.getFrequentItemSet(inputFrequentItemSetList);

		for(int i = 0;i < inputFrequentItemSetList.size()-1;i++){
			FrequentItemSet f1 = inputFrequentItemSetList.get(i);
			Item[] tempF1Array = f1.getFrequentItemSet().toArray(new Item[f1.getFrequentItemSet().size()]);
			List<Item> tempF1ItemList = Arrays.asList(tempF1Array).subList(0,tempF1Array.length-1);
			Item lastF1Item = tempF1Array[tempF1Array.length-1];

			for(int j = i+1;j<inputFrequentItemSetList.size();j++){
				FrequentItemSet f2 = inputFrequentItemSetList.get(j);
				Item[] tempF2Array = f2.getFrequentItemSet().toArray(new Item[f2.getFrequentItemSet().size()]);
				List<Item> tempF2ItemList = Arrays.asList(tempF2Array).subList(0,tempF2Array.length-1);
				Item lastF2Item = tempF2Array[tempF2Array.length-1];

				double supportDiff = lastF2Item.getActualSupportValue() - lastF1Item.getActualSupportValue();
				supportDiff = (supportDiff < 0)?(-1*supportDiff):supportDiff;

				if(tempF1ItemList.equals(tempF2ItemList) && (lastF2Item.getMinimumSupportValue() > lastF1Item.getMinimumSupportValue()) 
						&& supportDiff <= supportDiffConstraint){

					FrequentItemSet tempFreqItemSet = f1.getClone();
					tempFreqItemSet.getFrequentItemSet().add(lastF2Item);
					tempFreqItemSet.setFrequentItemSetCount(0);
					frequentItemSetList.add(tempFreqItemSet);

					//Create a List of Items from Set<Item> present in tempFreqItemSet....
					List<Item> tempFrequentItemList = new LinkedList<Item>();
					tempFrequentItemList.addAll(tempFreqItemSet.getFrequentItemSet());

					List<Set<Item>> subsets = this.getSubsets(new ArrayList<Set<Item>>(), tempFrequentItemList,
							new LinkedHashSet<Item>(), tempFreqItemSet.getFrequentItemSet().size() - 1);

					Item c1 =  tempF1Array[0];
					Item c2 = tempF1Array[1];
					for(Set<Item> subset:subsets){
						if(subset.contains(c1) || (c1.getMinimumSupportValue() == c2.getMinimumSupportValue())){
							if(!(fK1FrequentItemSet.contains(subset))){
								frequentItemSetList.remove(tempFreqItemSet);
							}
						}
					}
				}
			}
		}
		return frequentItemSetList;
	}

	public void displayFrequentItems(List<FrequentItemSet> frequentItemSetList){
		Map<Integer,List<FrequentItemSet>> frequentItemSetSizeMap = new LinkedHashMap<Integer,List<FrequentItemSet>>();

		for(FrequentItemSet frequentItemSet:frequentItemSetList){
			Set<Item> tempSet = frequentItemSet.getFrequentItemSet();
			Integer setSize = tempSet.size();

			if(frequentItemSetSizeMap.containsKey(setSize)){
				List<FrequentItemSet> tempList = frequentItemSetSizeMap.get(setSize);
				tempList.add(frequentItemSet);
			}else{
				List<FrequentItemSet> tempList = new LinkedList<FrequentItemSet>();
				tempList.add(frequentItemSet);
				frequentItemSetSizeMap.put(setSize, tempList);
			}
		}

		for(Integer freqItemSize: frequentItemSetSizeMap.keySet()){
			System.out.println("\nFrequent "+freqItemSize+"-itemsets");
			List<FrequentItemSet> tempList = frequentItemSetSizeMap.get(freqItemSize);
			for(FrequentItemSet frequentItem : tempList){
				System.out.println("\t"+frequentItem.getFrequentItemSetCount() + " : " + frequentItem.getFrequentItemSet());
				System.out.println("Tailcount = "+frequentItem.getTailCount());
			}
			System.out.println("\n\tTotal number of frequent "+freqItemSize+"-itemsets = "+tempList.size());
		}
	}

	public static void main(String...args) throws FileNotFoundException, IOException{
		MSApriori msAprioriInstance = new MSApriori();

		//Provide path to input-data.txt in transactionsInput
		String transactionsInput = "/home/prateek/workspace/minsup/src/main/resources/input-data.txt";
		transactionsInput = transactionsInput.replace("\\", "/");
		List<Set<Item>> transactionData = msAprioriInstance.readTransactionData(new File(transactionsInput));

		//Provide path to parameter-file.txt in parameterInput
		String parameterInput = "/home/prateek/workspace/minsup/src/main/resources/parameter-file.txt";
		parameterInput = parameterInput.replace("\\", "/");
		MSApriori.createItemSet(parameterInput);

		Set<Item> itemSet = MSApriori.ITEM_SET;
		Double supportDiffConstraint = MSApriori.SUPPORT_DIFFERENCE_CONSTRAINT;
		msAprioriInstance.MUST_NOT_HAVE_ITEM_PAIRS = msAprioriInstance.createPairsSet(MSApriori.MUST_NOT_HAVE_ITEM_CONSTRAINT);
		int totalNoOfTxn = transactionData.size();

		System.out.println("Transaction Data Set: "+transactionData);
		System.out.println("Item Constraints");
		System.out.println("Must Have Constraints: "+MSApriori.MUST_HAVE_ITEM_CONSTRAINT);
		System.out.println("Must Not Have Constraints: "+MSApriori.MUST_NOT_HAVE_ITEM_CONSTRAINT.toString());
		System.out.println("Must Not Have Constraints in Pairs:"+msAprioriInstance.MUST_NOT_HAVE_ITEM_PAIRS.toString());

		List<FrequentItemSet> finalFrequentItemsList = new LinkedList<FrequentItemSet>();
		List<FrequentItemSet> frequentItemsListAfterMustNot = new LinkedList<>();

		//Sort the items in Item set ascending order of MIS values 
		Set<Item> sortedItemSet = msAprioriInstance.sortItemsOnMISValue(itemSet);
		System.out.println("Sorted Item Set: \n"+sortedItemSet+"\n\n");
		Set<Item> itemSetAfterInitPass = msAprioriInstance.initPass(sortedItemSet, transactionData);

		//Get the individual Frequent Items
		List<FrequentItemSet> frequentItemSetList = new LinkedList<FrequentItemSet>();
		for(Item item:itemSetAfterInitPass){
			if(item.getActualSupportValue() >= item.getMinimumSupportValue()){
				FrequentItemSet frequentItemSet = new FrequentItemSet();
				Set<Item> tempSet = new HashSet<Item>();
				tempSet.add(item);
				frequentItemSet.setFrequentItemSet(tempSet);
				frequentItemSet.setFrequentItemSetCount(item.getItemCount());
				frequentItemSet.setFrequentItemSetSupportCount(item.getActualSupportValue());
				frequentItemSet.setTailCount(item.getItemCount());
				frequentItemSetList.add(frequentItemSet);
			}
		}
		finalFrequentItemsList.addAll(frequentItemSetList);

		for(int k = 2;!(frequentItemSetList.isEmpty());k++){
			List<FrequentItemSet> eligibleFrequentItemsList = null;
			if(k==2){
				eligibleFrequentItemsList = msAprioriInstance.level2CandidateGen(itemSetAfterInitPass, supportDiffConstraint);
			}else{
				eligibleFrequentItemsList = msAprioriInstance.MSCandidateGen(frequentItemSetList, supportDiffConstraint);
			}

			//Calculate the count of each item set generated...
			if(!eligibleFrequentItemsList.isEmpty()){
				for(Set<Item> txnItemSet:transactionData){
					for(FrequentItemSet eligibleFrequentItems:eligibleFrequentItemsList){
						if(txnItemSet.containsAll(eligibleFrequentItems.getFrequentItemSet())){
							eligibleFrequentItems.setFrequentItemSetCount(eligibleFrequentItems.getFrequentItemSetCount() + 1);
						}
					}
				}
			}

			//Calculate the actual Frequent Item Set...
			frequentItemSetList = new LinkedList<FrequentItemSet>();
			for(FrequentItemSet eligibleFrequentItemSet : eligibleFrequentItemsList){
				Set<Item> tempSet = eligibleFrequentItemSet.getFrequentItemSet();
				Item firstCandidate = tempSet.iterator().next();

				double actualSupportCount =  (eligibleFrequentItemSet.getFrequentItemSetCount()*1.0)/totalNoOfTxn;

				if(actualSupportCount >= firstCandidate.getMinimumSupportValue()){
					eligibleFrequentItemSet.setFrequentItemSetSupportCount(actualSupportCount);
					frequentItemSetList.add(eligibleFrequentItemSet);
				}
			}
			if(!frequentItemSetList.isEmpty()){
				finalFrequentItemsList.addAll(frequentItemSetList);
			}
		}

		//SET TAIL COUNT
		msAprioriInstance.setFrequentItemSetTailCount(finalFrequentItemsList);

		//DISPLAY FREQUENT ITEMS
		//		System.out.println("\n#### Frequent Items without any Item Constraints ####");
		//		msAprioriInstance.displayFrequentItems(finalFrequentItemsList);

		// Apply cannot_be_together constraint
		frequentItemsListAfterMustNot = msAprioriInstance.applyMustNotConstraint(finalFrequentItemsList);

		//		System.out.println("\n#### Frequent Items with cannot_be_together Item Constraints ####");
		//		msAprioriInstance.displayFrequentItems(frequentItemsListAfterMustNot);


		//DISPLAY FREQUENT ITEMS AFTER APPLYING MUST HAVE CONSTRAINT
		System.out.println("\n#### Frequent Items with must-have and cannot_be_together Item Constraints.....");
		List<FrequentItemSet> afterMustHaveConstraint = msAprioriInstance.applyMustHaveConstraint(frequentItemsListAfterMustNot);
		msAprioriInstance.displayFrequentItems(afterMustHaveConstraint);
	}
}
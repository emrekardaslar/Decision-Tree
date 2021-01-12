package decTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class Table {
	ArrayList<ArrayList<String>> elements = new ArrayList<ArrayList<String>>();
	Hashtable<Integer,String> removed = new Hashtable<Integer,String>();
	String[] columns = {"Class", "age", "menopause", "tumor-size", "inv-nodes", "node-caps", "deg-malig", "breast", "brest-quad", "irradiat"};
	ArrayList<String> attributes = new ArrayList<String>();
	boolean rootNode=true;
	
	public Table(File file) throws FileNotFoundException {
		Scanner sc = new Scanner(file);
		int lineNumber = 1;
		
	    for(String text:columns) {
	    	attributes.add(text);
	    }
	
		while (sc.hasNextLine()) {	
			String line = sc.nextLine();
			ArrayList<String> array = new ArrayList<String>();
			Collections.addAll(array, line.split(","));
			
			if (line.contains("?")) {			
				int missingData = findMissingData(line);
				removed.put(lineNumber, attributes.get(missingData));
				lineNumber++;
				continue;
			}
			lineNumber++;
			elements.add(array);
		}
		sc.close();
		
		//printTable();
		//writeFile();
		//printRemoved();
		drawTree(attributes,elements,"");
	
	}
	
	public void formatBranch(String prefix, String value, boolean root) {
		if (root) {
			System.out.println("    " + value);
		}
		else {
			System.out.println(prefix + "|---" + value);
		}
	}
	
	public void drawTree(ArrayList<String> attributes, ArrayList<ArrayList<String>> elements, String prefix) {
		Hashtable<String,Integer> decisions = new Hashtable<String, Integer>();
		float maxGain = 0;
		int maxGainIndex = 0;
		ArrayList<String> maxAttrValues = new ArrayList<String>();
		ArrayList<String> attributeValues = new ArrayList<String>();
		float entropy = findEntropy(elements, decisions);
		if (rootNode)
			System.out.println("Gains to determine the root:");
		for (int i=1; i<attributes.size(); i++) {
			attributeValues.clear();
			float gain = findGain(i,entropy, decisions, elements, attributeValues);
			if (gain > maxGain) {
				maxGain = gain;
				maxGainIndex = i;
				maxAttrValues = new ArrayList<String>(attributeValues);
			}
			if (rootNode)
				System.out.println(i + ". " + attributes.get(i) + ": " + gain);
			
			if (i==attributes.size()-1 && rootNode == true) {
				rootNode = false;
				System.out.println("-------------------------------");
				System.out.println("\nDecision Tree: \n");
			}
		}
		
		if (maxGainIndex > 0) {
			int i;
			boolean root = prefix.equals("");
			formatBranch(prefix, attributes.get(maxGainIndex), root);
			for (i=0; i<maxAttrValues.size(); i++) {
				String p = prefix + "    ";
				formatBranch(p, maxAttrValues.get(i), false);
				if (i == maxAttrValues.size()-1) {
					p = p + "    ";
				}
				else {
					p = p + "|   ";
				}
				ArrayList<ArrayList<String>> newElements = new ArrayList<ArrayList<String>>();
				for (int j=0; j<elements.size(); j++) {
					if (elements.get(j).get(maxGainIndex).equals(maxAttrValues.get(i))) {
						newElements.add(new ArrayList<String>());
						for (int k=0; k<elements.get(j).size(); k++) {
							if (k != maxGainIndex) {
								newElements.get(newElements.size()-1).add(elements.get(j).get(k));	
							}
						}
					}
				}
				
				ArrayList<String> newAttributes = new ArrayList<String>();
				for (int j=0; j<attributes.size();j++) {
					if (j != maxGainIndex) {
						newAttributes.add(attributes.get(j));
					}
				}
				drawTree(newAttributes, newElements, p);
			}
		}	
		
		if (maxGainIndex == 0) {
			formatBranch(prefix, elements.get(maxGainIndex).get(0), false);
		}
	}
	
	public float findGain(int index, float entropy, Hashtable<String,Integer> decisions, ArrayList<ArrayList<String>> elements, ArrayList<String> attrValues) {
		float gain = 0;
		Hashtable<String,Hashtable<String,Integer>> attrs = new Hashtable<String, Hashtable<String,Integer>>();
		Hashtable<String,Integer> attr_counts = new Hashtable<String,Integer>();
		
		for (int i=0; i<elements.size(); i++) {
			String decision = elements.get(i).get(0);
			String attr = elements.get(i).get(index);
			
			if(attrs.get(attr) == null) {
				attrs.put(attr, new Hashtable<String,Integer>());
				attr_counts.put(attr, 1);
				attrValues.add(attr);
			}
			else
				attr_counts.put(attr, attr_counts.get(attr) + 1);

			if (attrs.get(attr).get(decision) == null)
				attrs.get(attr).put(decision, 1);
			else
				attrs.get(attr).put(decision, attrs.get(attr).get(decision) + 1);
		}
		
		Set<String> keysAttrs = attrs.keySet();
		float g = 0;
		for (String keyAttrs: keysAttrs) {
			float e = 0;
			Set<String> keysDecisions = attrs.get(keyAttrs).keySet();
			for (String keysDecision: keysDecisions) {
				float ex = (float)attrs.get(keyAttrs).get(keysDecision) / (float)attr_counts.get(keyAttrs);
				ex = - ex * log2(ex);
				e = e + ex;
			}
			g = g + ((float)attr_counts.get(keyAttrs) / (float) elements.size()) * e;
		}
		
		gain = entropy - g;
		
		return gain;
	}
	
	public float findEntropy(ArrayList<ArrayList<String>> elements, Hashtable<String,Integer> decisions) {
		float entropy = 0;
		
		for (int i=0; i<elements.size(); i++) {
			String decision = elements.get(i).get(0);
			if(decisions.get(decision) == null) {
				decisions.put(decision,1);
			}
			else {
				decisions.put(decision,decisions.get(decision)+1);
			}
		}
		
		Set<String> keys = decisions.keySet();
		for (String s: keys) {
			float t = ((float)decisions.get(s) / elements.size());
			//System.out.println(decisions.get(s));
			entropy = entropy -t * this.log2(t);
		}
						
		return entropy;
		
	}
	
	
	public void printTable() {
		int lineNumber = 1;
		for (int i=0; i<elements.size(); i++) {
			System.out.print(lineNumber + ". ");
			for (int j=0; j<elements.get(i).size(); j++) {
				System.out.print(elements.get(i).get(j) + " ");
			}
			lineNumber++;
			System.out.println();
		}
	}
	
	public void printRemoved() {
		System.out.println("Silinen satirlar ve ozellikler: ");
		TreeMap<Integer, String> tmap = new TreeMap<Integer, String>(removed);
		System.out.println(tmap);
	}
		
	public int findMissingData(String line) {
		String[] values = line.split(",");
		for (int i=0; i<values.length; i++) {
			if (values[i].equals("?")) {
				return i;
			}
		}
		return -1;
	}
	
	public void writeFile() {
		try {
			FileWriter writer = new FileWriter("src\\decTree\\breast-cancer.data");
		for (int i=0; i<elements.size(); i++) {
			for (int j=0; j<elements.get(i).size(); j++) {
					writer.write(elements.get(i).get(j));
					if (j < elements.get(i).size()-1) {
						writer.write(",");
					}
				} 
			writer.write("\n");
			}
		writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public float log2(float N) {
        float res = (float)(Math.log(N) / Math.log(2)); 
        return res; 
    } 
}

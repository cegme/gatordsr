package edu.ufl.cise.util;

import java.util.LinkedList;
import java.util.List;

import edu.ufl.cise.pipeline.Entity;
import edu.ufl.cise.pipeline.Preprocessor;

public class NameOrderGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		Preprocessor
				.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json");
		List<Entity> l = Preprocessor.entity_list();
		for (Entity e : l) {
			if (e.entity_type().equals("PER")) {

				namePermutation(e.alias().get(0));

			}
		}

		// String name = "William Cohan";
		// System.out.println(namePermutation(name));
		
		addSpace();

	}
	
	private static void addSpace() {
		String s = "SdaddDsdsKol";
		String so = s.replaceAll("([a-z])([A-Z])", "$1 $2");
		
	System.out.println(so);	
//		Pattern p = Pattern.compile("([a-z])([A-Z])");
//		String input = "6 example input 4";
//		Matcher m = p.matcher(input);
//		while (m.find()) {
//		    // replace first number with "number" and second number with the first
//		    String ouput = m.replaceFirst("number $2$1");
//		}
//				
	}

	/**
	 * e.g. String name = "William Cohan";
	 * 
	 * @param name
	 * @return
	 */
	public static List<String> namePermutation(String name) {
//		System.out.println("|"+name+"|");
		LinkedList<String> list = new LinkedList<String>();
		String[] nameArr = name.split(" ");
		if (nameArr.length == 2) {
			String fn = nameArr[0];

			String ln = nameArr[1];

			list.add(fn + " " + ln);
			list.add(fn.charAt(0) + ". " + ln);
			list.add(ln + ", " + fn);
			list.add(ln + ", " + fn.charAt(0) + ".");
//			for (String string : list) {
//				System.out.print(string + ", ");
//			}
		//	System.out.println();
		} else {
			list.add(name);
		}
		return list;
	}

	private static String s(String s) {
		return "\"" + s + "\"";

	}
}

package edu.ufl.cise.util;

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
				if (e.names().size() == 1) {
					if (e.names().get(0).split(" ").length == 2) {

						String[] nameArr = e.names().get(0).split(" ");
						String fn = nameArr[0];
						String ln = nameArr[1];

						System.out.println(s(fn + " " + ln) + " , "
								+ s(fn.charAt(0) + ". " + ln) + " , "
								+ s(ln + ", " + fn) + ", "
								+ s(ln + ", " + fn.charAt(0) + "."));
					}
				}
			}
		}

		// String name = "William Cohan";
		// System.out.println(namePermutation(name));
	}

	/**
	 * e.g. String name = "William Cohan";
	 * 
	 * @param name
	 * @return
	 */
	private static String namePermutation(String name) {
		String[] nameArr = name.split(" ");
		String fn = nameArr[0];
		String ln = nameArr[1];

		return s(fn + " " + ln) + " , " + s(fn.charAt(0) + ". " + ln) + " , "
				+ s(ln + ", " + fn) + ", " + s(ln + ", " + fn.charAt(0) + ".");
	}

	private static String s(String s) {
		return "\"" + s + "\"";

	}
}

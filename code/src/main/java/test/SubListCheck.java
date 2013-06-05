package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SubListCheck {

	static boolean subListCheck(List<String> listParent, List<String> listSub) {
		for (int i = 0; i < listParent.size(); i++) {

			boolean enteredFolLoop = false;
			boolean matched = true;
			for (int j = 0; j < listSub.size() && matched == true; j++) {
				enteredFolLoop = true;
				matched = false;
				if (i + j < listParent.size()) {
					String s1 = listParent.get(i + j);
					String s2 = listSub.get(j);
					if (s1.equals(s2))
						matched = true;
				}
			}
			if (enteredFolLoop && matched == true)
				return true;
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		LinkedList<String> listParent = new LinkedList<String>();
		listParent.add("listParent");
		listParent.add("add");
		listParent.add("LinkedList");
		listParent.add("new");
		listParent.add("boolean");
		listParent.add("new");
		listParent.add("boolean");

		LinkedList<String> listSub = new LinkedList<String>();
		listSub.add("new");
		listSub.add("boolean2");

		String s1 = new String("listParent" + "add" + "LinkedList" + "new" + "boolean" + "new"
				+ "boolean");
		String s2 = new String("new" + "boolean2");

		long t0 = 0;
		t0 = System.nanoTime();
		for (int h = 0; h < 10; h++) {
			int count = 10000000;
			for (int i = 0; i < count; i++) {
				boolean b = subListCheck(listParent, listSub);
				new Boolean(b);
			}
			long t1 = 0;
			t1 = System.nanoTime();
			System.out.println(t1 - t0);

			t0 = System.nanoTime();
			for (int i = 0; i < count; i++) {
				boolean b = s1.contains(s2);
				new Boolean(b);
			}
			t1 = System.nanoTime();
			System.out.println(t1 - t0);
			System.out.println("---");
		}
	}
}

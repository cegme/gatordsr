package edu.ufl.cise.postprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FoundedByLogFix {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Scanner sc = new Scanner(new File("/Users/morteza/zProject/FoundedBy.uniq.txt"));
		while(sc.hasNextLine()){
			String s = sc.nextLine();
			String sArr = s.split("#")[2];//.split(">")[1];
			System.out.println(sArr);
		}
	}

}

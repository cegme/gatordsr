package edu.ufl.cise;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 * 
 */
public class App {
	public static void main( String[] args )
    {
    	for(int j = 0 ; j < 20; j++){
    	String s = "";
    	Random r = new Random();
    	for (int i =0; i < 10000; i++)
    		s = s + r.nextInt();
    	
    	long startTime = System.nanoTime();
    	s.contains("president");
    	long endTime = System.nanoTime();
    	
    	
        System.out.print( endTime - startTime + " " );
        
        Pattern p= Pattern.compile("president");
        startTime = System.nanoTime();
        p.matcher(s).find();
        endTime = System.nanoTime();
        System.out.println( endTime - startTime );
    	}
    	
    }
}

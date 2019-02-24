/*
 * This class is written by Genevieve Plante-Brisebois 40003112
 * COMP 442
 * WINTER 2019
 * */

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;




public class LexerDriver {
 public static void main (String []arg) {
	 Scanner  scan = new Scanner(System.in);
	Lexer lex = new Lexer();
	 System.out.println("Welcome to the Lexer\n");
	 System.out.println("Please enter the name of the file you wish to put in the lexer? It should be of the format of name.txt");
	 String input;
	 boolean keepGoing = true;
	 try {
	 while(keepGoing) {
		 input = scan.next();
		 ArrayList <Token> tokens = new ArrayList<Token> (5);
		 File file = new File (input);
		 lex.lexer(file);
		 System.out.println("lexer done. look in your files. \n");
		 boolean next = true;
		 while(next) {
		 System.out.println("Do you wish to use the lexer on another file? yes/no");
		 input = scan.next();
		 
		 if(input.compareTo("yes")==0) {
			 next = false;
		 }else if (input.compareTo("no")==0) {
			 System.out.println("Exiting program");
			 next = false;
			 keepGoing = false;
			 
		 }else {
			 System.out.println("Please enter a valid answer");
		 }
			 
		 }
		 System.out.println("What is the name of the file?");
		 
		 
	 }
	 System.exit(0);
	 }catch (Exception e) {
		 System.out.println(e);
	 }
 }
}

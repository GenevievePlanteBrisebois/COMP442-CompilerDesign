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
	 Parser parse = new Parser();
	 SemanticAnalyser sem_analysis = new SemanticAnalyser();
	 Node<Token> root;
	 //first level of test tree
	 
	 Node <Token> root1 = new Node<Token> ("prog");
	 Node <Token> classList = new Node<Token> ("classList");
	 Node <Token> funcList = new Node<Token> ("funcList");
	 Location loc = new Location(19);
	 Token m =  new Token("keyword", "main", loc);
	 Node <Token> main = new Node<Token> (m, "terminal");
	 Node <Token> funcBody = new Node<Token> ("funcBody");
	 Token e = new Token("punctuation", ";", loc);
	 Node <Token> end = new Node <Token> (e, "terminal");
	 
	 root1.addChild(classList);
	root1.addChild(funcList);
	 root1.addChild(main);
	 root1.addChild(funcBody);
	 root1.addChild(end);
	 
	 
	 
	 //adding some component to it
	
	Node<Token> cl = new Node<Token>("classDecl");
	 classList.addChild(cl);
	 
	 Token c = new Token("keyword", "class", loc);
	 Token i = new Token("id", "test1", loc);
	 
	 Node <Token> class_test = new Node<Token>(c, "class");
		cl.addChild(class_test);
	 Node <Token> c_id = new Node<Token>(i, "terminal");
	 	cl.addChild(c_id);
	 Node <Token> v_decl = new Node<Token> ("varDecl");
	 
	 SemanticAnalyser sym = new SemanticAnalyser();
	// ArrayList<Symbol_Table> table = sym.create_table(root1);
		
	// String t = table.toString();
	 
	// System.out.println(t);
	 
 
	 
	 System.out.println("Welcome to the COMP 442 Compiler \n\nPlease enter the name of the file that you wish to compile. The format should be \"name\"\n");
	 
	boolean is_true = true;
	String input="";
	
	while(is_true) {
			input = scan.next();
	    	  File file = new File(input+".txt");
	        
	          
	         boolean exists = file.exists();
	          
	         if(exists==false)
	        	 System.out.println("\nPlease enter a valid file name. The file you requested does not exist. \n");
	         else 
	        	 is_true = false;
	      
	}
	 
	 //root = parse.parser(input);
	 sym.sem_analysis(input);
	// sem_analysis.sem_analysis(input);
 
 }
 
}

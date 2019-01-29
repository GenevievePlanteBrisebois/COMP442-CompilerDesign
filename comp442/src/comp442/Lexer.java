/*
 * program written by Genevieve Plante-Brisebois 40003112
 * Written in the context of the COMP442 winter 2019
 * This class is to make the lexer which is the first step to the compiler.
 * */

package comp442;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Lexer {
	ArrayList <Token> tokens;
/*
 * This section of the program is going to be functions that verify if the tokens are of a 
 * certain type.  * the value returned by the functions will be a boolean. In the case the
 * boolean of a function returns * true then the data will be assigned to a token of that
 * type. The default case of the boolean in * the functions is false so that only a 
 * positive match will change it to true and that it will stop the possibility of an error 
 * because of variable not instantiated. 
 * 
 * The logic of each DFA will be used in the boolean functions in order to identify the tokens.
 * There will be error handling in the case that none of the functions give a proper return which
 *  allows to id the tokens. 
 * */	
	
	//strings that will be used to create the patterns. the patterns will be used in the functions. 
	//the patterns take in regular expressions and then work with them. I am using the drfinitions 
	//from the lexical specifications. 
	private static final String KEYWORDS_PATTERN = "\\b if \\b | \\b then \\b | \\b else \\b | \\b for \\b| \\b integer \\b | \\b class \\b | \\b float \\b | \\b read \\b | \\b return \\b| \\b write \\b | \\b main \\b"; 
	private static final String LETTER_PATTERN ="[a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z]|[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z]]" ;
	private static final String DIGIT_PATTERN = "[0,1,2,3,4,5,6,7,8,9]";
	private static final String NONZERO_PATTERN = "[1,2,3,4,5,6,7,8,9]";
	
	//create the pattern objects for the set patterns:
	Pattern keywords = Pattern.compile(KEYWORDS_PATTERN);
	Pattern letter = Pattern.compile(LETTER_PATTERN);
	Pattern digit = Pattern.compile(DIGIT_PATTERN);
	Pattern nonzero = Pattern.compile(NONZERO_PATTERN);
	
	//now that we have the most used pattern that are used as subunits in all other aspects of the
	//lexer we will be able to use the matcher to see if it corresponds to their token definition
	
	
//ids if is a keyword token
public boolean isKeyword(){
	boolean result=false;
	
	
	return result;
}
//ids if is a letter token
public boolean isLetter(){
boolean result=false;
	
	
	return result;
}
//ids if is a digit token
public boolean isDigit(){
boolean result=false;
	
	
	return result;
}
//ids if is a nonzero
public boolean isNonzero(){
boolean result=false;
	
	
	return result;
}

//ids if is a ID
public boolean isID(){
boolean result=false;
	
	
	return result;
}
//ids if is an integer
public boolean isinteger(){
boolean result=false;
	
	
	return result;
}

//ids if is a fraction
public boolean isFraction(){
boolean result=false;
	
	
	return result;
}

//ids if is a float
public boolean isFloat(){
boolean result=false;
	
	
	return result;
}

//ids if is an alphanum
public boolean isAlphanum(){
boolean result=false;
	
	
	return result;
}

//ids if is an operator
public boolean isOperator(){
boolean result=false;
	
	
	return result;
}

//ids if is a punctuation
public boolean isPunctuation(){
boolean result=false;
	
	
	return result;
}

//if if comment
public boolean isComment() {
boolean result=false;
	
	
	return result;
}



/*
 * a class to create the tokens. Tokens will have a type and a data associated to it. 
 * 
 * */

public class Token{
	String token_type;
	String data;
	
	public Token() {
		token_type="";
		data = "";
		
	}
	
	public Token(String type, String d) {
		token_type = type;
		data =d;
	}
	
	public String getType() {
		return token_type;
	}
	
	public String getData() {
		return data;
		
	}
	//gives token type and data associated to it
	public String toString() {
		
		return "Type: "+token_type + " data: " + data;
	}
}
}

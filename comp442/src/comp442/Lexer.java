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
	
	//tokens will be stored in an arraylist. tokens consist of type and data
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
	private static final String LETTER_PATTERN ="[a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z]|[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z]" ;
	private static final String DIGIT_PATTERN = "[0,1,2,3,4,5,6,7,8,9]";
	private static final String NONZERO_PATTERN = "[1,2,3,4,5,6,7,8,9]";
	private static final String ALPHANUM_PATTERN = "[0,1,2,3,4,5,6,7,8,9]|[a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z]|[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z]|_";
	//create the pattern objects for the set patterns:
	Pattern keywords = Pattern.compile(KEYWORDS_PATTERN);
	Pattern letter = Pattern.compile(LETTER_PATTERN);
	Pattern digit = Pattern.compile(DIGIT_PATTERN);
	Pattern nonzero = Pattern.compile(NONZERO_PATTERN);
	Pattern alphanum = Pattern.compile(ALPHANUM_PATTERN);
	//now that we have the most used pattern that are used as subunits in all other aspects of the
	//lexer we will be able to use the matcher to see if it corresponds to their token definition
	
//ids if is a keyword token
	/*
	 * The input is a string which is the sequence to analyse. if the sequence of char 
	 * is a keyword it will return true. 
	 * */
public boolean isKeyword(String key){
	boolean result=false;
	Matcher keywords_match = keywords.matcher(key);
	result = keywords_match.matches();
	return result;
}
//ids if is a letter token
public boolean isLetter(String input){
boolean result=false;
	Matcher letter_match = letter.matcher(input);
	result = letter_match.matches();	
	return result;
}
//ids if is a digit token
public boolean isDigit(String input){
boolean result=false;
	
	Matcher digit_match = digit.matcher(input);
	result = digit_match.matches();
	return result;
}
//ids if is a nonzero
public boolean isNonzero(String input){
boolean result=false;
	Matcher nonzero_match = nonzero.matcher(input);
	result = nonzero_match.matches();
	return result;
}

//ids if is a ID
//ID requires a little more work. 
public boolean isID(String input){
boolean result=false;
	int length = input.length();
	
	//verify the first char of the input is a letter if not result is false and all other processes stop
	if(isLetter(input.valueOf((input.charAt(0))))==false) {
		
		result = false;
	}
	//in the case that the result from the first char is that it is a letter
	else {
		for (int i=1;i<= length;i++) {
			String character = input.valueOf(input.charAt(i));
			
			if(isAlphanum(character)==false)
			{
				return result = false; //exit function and return false not an id
			}
			
			
		}//as we exited the loop it means all remaining char are alphanum. input is an id
		result = true;
		
	}
	
	
	return result;
}
//ids if is an integer
public boolean isinteger(String input){
	boolean result=false;
	int length = input.length();
	String character;
	for (int i=0;i<length;i++) {
		character = input.valueOf(input.charAt(i));
		
		//case first is not a non zero digit
		if(i==0 && isNonzero(character)==false) {
			return result;	
		}else if (i>0 && isDigit(character)==false) {
			return result;
		}	
	}
	//as we exite the loop of status verification the input is a integer
	result = true;	
	return result;
}

//ids if is a fraction
public boolean isFraction(String input){
	boolean result=false;
	int length = input.length();
	String character;
	
	for (int i=0;i<length;i++) {
		character =  input.valueOf(input.charAt(i));
		if(i==0 && character != ".") {
			return result;			
		}
		//checking if the chars between 1and before last are digits
		else if (i>0 && i<length-1 && isDigit(character)==false) {
			return result;
		}
		//checking if the last char is a nonzero
		else if(i==length-1 && isNonzero(character)==false) {
			return result;
		}
		
	}
	//verification loop done and passed
	result = true;
	return result;
}

//ids if is a float
public boolean isFloat(String input){
boolean result=false;
	
	
	return result;
}

//ids if is an alphanum
public boolean isAlphanum(String input){
boolean result=false;
	Matcher alphanum_match = alphanum.matcher(input);
	result = alphanum_match.matches();	
	return result;
}

//ids if is an operator
public boolean isOperator(String input){
	boolean result=false;
	int length = input.length();
	
	//no operator has more than 2 char so immediately if length is >2 its not an operator or at least not a valid one
	if(length>2)
		return result;
	
	//now cheching the other factors i.e.the operator symbols. as it is a short list we will do a big if with not equal
	else if(input != "==" || input !="<>" || input !="<" || input !=">" || input !="<=" || input !=">=" || input !="+" || input !="-" || input !="*"||input !="/"||input !="="|| input !="&&" || input !="||"||input !="!") {
		return result;
	}
	//if we are still in the function it passed the test return true
	result =true;	
	return result;
}

//ids if is a punctuation
public boolean isPunctuation(String input){
	boolean result=false;
	int length = input.length();
	String character;
	
	//no punctuation is longer than 2 so if length >2
	if (length >2)
		return result;
	//make an exhaustive list of the punctuation markers
	else if(input !=";"||input !=":"||input !="::"||input !="."||input !=","||input !="("||input !=")"||input !="["||input !="]"||input !="{"||input !="}") {
	return result;
	}
	//if still in function we passed the test
	result = true;	
	return result;
}

/*for the comments there are two possible functions. if the start of a comment block has been found then
 * the program will have to look for the closing block symbol. it will allow it to know when a
 * block opens or closes or if it is just a line of comment (//)
 * 
 * */
public boolean isCommentBlockOpen(String input) {
boolean result=false;
	int length = input.length();
	
	if(length!=2)
		return result;
	else if (input!="/*")
		return result;
	
	//as we passed the test comment section has started or ended
	result =true;
	return result;
}

public boolean isCommentBlockClosed(String input) {
boolean result=false;
	int length = input.length();
	
	if(length!=2)
		return result;
	else if (input !="*/")
		return result;
	
	//as we passed the test comment section has started or ended
	result =true;
	return result;
}

public boolean isCommentLine(String input) {
	boolean result=false;
	int length = input.length();
	
	if(length!=2)
		return result;
	else if (input !="//")
		return result;
	
	//as we passed the test comment section has started or ended
	result =true;
	return result;
	
}



/*
 * a class to create the tokens. Tokens will have a type and a data associated to it. 
 * they are going to be used in the parent class in order to tokenize the input.
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

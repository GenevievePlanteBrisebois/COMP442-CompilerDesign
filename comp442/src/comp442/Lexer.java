/* program written by Genevieve Plante-Brisebois 40003112
 * Written in the context of the COMP442 winter 2019
 * This class is to make the lexer which is the first step to the compiler.
 * */

package comp442;
import java.io.*;
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
	private static final String WHITESPACE_PATTERN = "\\s";
	
	
	
	//create the pattern objects for the set patterns:
	Pattern keywords = Pattern.compile(KEYWORDS_PATTERN);
	Pattern letter = Pattern.compile(LETTER_PATTERN);
	Pattern digit = Pattern.compile(DIGIT_PATTERN);
	Pattern nonzero = Pattern.compile(NONZERO_PATTERN);
	Pattern alphanum = Pattern.compile(ALPHANUM_PATTERN);
	Pattern whitespace = Pattern.compile(WHITESPACE_PATTERN);
	//now that we have the most used pattern that are used as subunits in all other aspects of the
	//lexer we will be able to use the matcher to see if it corresponds to their token definition
	
	//create the token types that are possible 
	
	public static enum TokenType{
		LETTER, DIGIT, NONZERO, ALPHANUM, KEYWORD, INTEGER, FRACTION, FLOAT, PUNCTUATION, COMMENT, OPERATOR;
		
	}
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
public boolean isInteger(String input){
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
		else if (input == ".0") {
			result = true;
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
	int length = input.length();
	String character;
	//most simple test, looks at if its an integer 
	//(which takes into account the digits). if it is it is not a float
	//makes us skip the big process if it is already not a float
	if(isInteger(input)==true) {
		return result;
	}
	
	//if the input is a fraction it is automatically not a float
	if (isFraction(input)==true)
		return result;
	//if the first char is a pointm even if not a fraction (see above test) then it means that the input is still invalid for the float token
	
	if (input.valueOf(input.charAt(0))==".")
		return result;
	//find where the . is and break the string into two new strings
	//also test if there are multiple . and or e (which makes the string invalid)
	//finds if there is an operator symbol in the string
	int dot = 0;
	int e = 0;
	int plus =0;
	int minus =0;
	
	/*
	 * this is also testing to see if there is more than one occurance of a specific character. in the case that the character is
	 * repeated, it automatically invalidates the input string as a token of type float. 
	 * */
	for (int i=0;i<length;i++) {
		character =  input.valueOf(input.charAt(i));
		if(character =="." && dot ==0 ) 
			dot =i;
		else if (character == "." && dot !=0)
			return result;
		else if (character == "e" && e!=0)
			return result;		
		else if (character =="e" && e==0)
			e = i;
		else if (character == "+" && plus ==0)
			plus=i;
		else if (character =="-" && minus==0)
			minus =i;
		else if (character == "+" && plus !=0)
			return result;
		else if (character == "-" && minus!=0)
			return result;
	}
	//test for the placement of the + and - symbols
	
	
	if(plus != 0 && e+1!= plus)
		return result;
	if(minus!=0 && e+1 != minus)
		return result;
	
	
	//now we have the index of the dot we can create the two strings
	
	String part1 = "";
	String part2 = "";
	
	for (int i=0;i<dot;i++) {
		character =  input.valueOf(input.charAt(i));
		part1+=character;
	}
	for (int i=dot;i<length;i++) {
		character =  input.valueOf(input.charAt(i));
		part2+=character;
	}
	
	//verify if the part1 is an integer
	if(isInteger(part1)==false)
		return result;
	
	if (e==0) {
		if(isFraction(part2)==false)
			return result;		
	}
	
	else if (e!=0 && plus!=0) {
		//make a part 3
		
		String part3 ="";
		
		for (int i=dot;i<e;i++) {
			character =  input.valueOf(input.charAt(i));
			part2+=character;
		}
		for (int i=e+2;i<length;i++) {
			character =  input.valueOf(input.charAt(i));
			part3+=character;
		}
		
		if(isFraction(part2)==false)
			return result;
		if(isInteger(part3)==false)
			return result;}
	
	else if (e!=0 && minus !=0) {
		//make a part 3
		
		String part3 ="";
		
		for (int i=dot;i<e;i++) {
			character =  input.valueOf(input.charAt(i));
			part2+=character;
		}
		for (int i=e+1;i<length;i++) {
			character =  input.valueOf(input.charAt(i));
			part3+=character;
		}
		
		if(isFraction(part2)==false)
			return result;
		if(isInteger(part3)==false)
			return result;
	}else if (e!=0 ) {
		//make a part 3
		
		String part3 ="";
		
		for (int i=dot;i<e;i++) {
			character =  input.valueOf(input.charAt(i));
			part2+=character;
		}
		for (int i=e+1;i<length;i++) {
			character =  input.valueOf(input.charAt(i));
			part3+=character;
		}
		
		if(isFraction(part2)==false)
			return result;
		if(isInteger(part3)==false)
			return result;
		
	}
		
	result = true;	
	
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
 * now that we have the modules for the regular expressions we can look into gathering the input bits and 
 * using the modules we created according to the lexical specification in order to do the tokenization.
 * */
//lexer function
public ArrayList <Token> lexer(String input) {
	String in = input;
	String part;
	
	int len = input.length();
	int marker = 0;
	
	while (marker<len) {
		
		
		
		
		
		
	}
	
	
	return tokens;
}






public String invalidNumber(String input) {
	String message = "Invalid number : " + input;
	
	
}


/*
 * nested class in order to keep track of the line at which we are at so that when there is an error 
 * we can do to string and output where the error was. 
 * */
public class Location{
	
	int line;
	
	public Location(int line) {
		this.line  = line;
	}
	public int getLine() {
		return line;
	}
	
	public String toString() {
		return "Location line :" +line;
	}
}



/*
 * a class to create the tokens. Tokens will have a type and a data associated to it. 
 * they are going to be used in the parent class in order to tokenize the input.
 * 
 * when a token does not have a value the data string should be the empty string
 * */

public class Token{
	String token_type;
	String data;
	Location location;
	public Token() {
		token_type="";
		data = "";
		
	}
	
	public Token(String type, String d, Location loc) {
		token_type = type;
		data =d;
		location = loc;
	}
	public int getLocation() {
		
		return location.getLine();
	}
	public String getType() {
		return token_type;
	}
	
	public String getData() {
		return data;
		
	}
	//gives token type and data associated to it
	public String toString() {
		
		return "Type: "+token_type + " data: " + data + location.toString();
	}
}
}

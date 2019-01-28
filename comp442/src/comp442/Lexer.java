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
//ids if is a keyword token
	/*
	 * list of the keywords:
	 * 
	 * */
isKeyword(){}
//ids if is a letter token
isLetter(){}
//ids if is a digit token
isDigit(){}
//ids if is a nonzero
isNonzero(){}

//ids if is a ID
isID(){}
//ids if is an integer
isinteger(){}

//ids if is a fraction
isFraction(){}

//ids if is a float
isFloat(){}

//ids if is an alphanum
isAlphanum(){}

//ids if is an operator
isOperator(){}

//ids if is a punctuation
isPunctuation(){}



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
	
	
}
}

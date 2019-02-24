/* program written by Genevieve Plante-Brisebois 40003112
 * Written in the context of the COMP442 winter 2019
 * This class is to make the lexer which is the first step to the compiler.
 * */


import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.sun.org.apache.xpath.internal.operations.Bool;


public class Lexer {
	
	//tokens will be stored in an arraylist. tokens consist of type and data
	ArrayList <Token> tokens = new ArrayList <Token>(20);
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
	//the patterns take in regular expressions and then work with them. I am using the definitions 
	//from the lexical specifications. 
	private static final String KEYWORDS_PATTERN = "if|then|else|for|integer|class|float|read|return|write|main"; 
	private static final String LETTER_PATTERN ="[a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z]|[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z]" ;
	private static final String DIGIT_PATTERN = "[0,1,2,3,4,5,6,7,8,9]";
	private static final String NONZERO_PATTERN = "[1,2,3,4,5,6,7,8,9]";
	private static final String ALPHANUM_PATTERN = "[0,1,2,3,4,5,6,7,8,9]|[a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z]|[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z]|_";
	private static final String WHITESPACE_PATTERN = "\\s";
	
	//public static enum TokenType{
		// KEYWORD, INTEGER,  FLOAT, PUNCTUATION, COMMENT, OPERATOR;
		
	//}
	
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
	
	
//ids if is a keyword token
	/*
	 * The input is a string which is the sequence to analyse. if the sequence of char 
	 * is a keyword it will return true. 
	 * */
public boolean isKeyword(String key){
	boolean result=false;
	Matcher keywords_match = keywords.matcher(key);
	result = keywords_match.matches();
	//int x=0;
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
	if(isLetter(String.valueOf((input.charAt(0))))==false) {
		
		result = false;
	}
	//in the case that the result from the first char is that it is a letter
	else {
		for (int i=1;i< length;i++) {
			String character = String.valueOf(input.charAt(i));
			
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
		character = String.valueOf(input.charAt(i));
		
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
		character =  String.valueOf(input.charAt(i));
		if(i==0 && character.compareTo(".") != 0) {
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
	
	if (String.valueOf(input.charAt(0)).compareTo(".")==0)
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
		character =  String.valueOf(input.charAt(i));
		if(character.compareTo(".") ==0 && dot ==0 ) 
			dot =i;
		else if (character.compareTo(".") == 0 && dot !=0)
			return result;
		else if (character.compareTo("e") == 0 && e!=0)
			return result;		
		else if (character.compareTo("e") ==0 && e==0)
			e = i;
		else if (character.compareTo("+") == 0 && plus ==0)
			plus=i;
		else if (character.compareTo("-") ==0 && minus==0)
			minus =i;
		else if (character.compareTo("+") == 0 && plus !=0)
			return result;
		else if (character.compareTo("-") == 0 && minus!=0)
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
		character =  String.valueOf(input.charAt(i));
		part1+=character;
	}
	for (int i=dot;i<length;i++) {
		character =  String.valueOf(input.charAt(i));
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
			character =  String.valueOf(input.charAt(i));
			part2+=character;
		}
		for (int i=e+2;i<length;i++) {
			character =  String.valueOf(input.charAt(i));
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
			character =  String.valueOf(input.charAt(i));
			part2+=character;
		}
		for (int i=e+1;i<length;i++) {
			character =  String.valueOf(input.charAt(i));
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
			character =  String.valueOf(input.charAt(i));
			part2+=character;
		}
		for (int i=e+1;i<length;i++) {
			character =  String.valueOf(input.charAt(i));
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
	if(length>3)
		return result;
	
	//now cheching the other factors i.e.the operator symbols. as it is a short list we will do a big if with not equal
	else if(input.compareTo("==") !=0  && input.compareTo("<>") !=0 && input.compareTo("<") !=0 && input.compareTo(">") !=0 && input.compareTo("<=") !=0 && input.compareTo(">=") !=0 && input.compareTo("+") !=0 && input.compareTo("-") !=0 && input.compareTo("*") !=0&&input.compareTo("/") !=0 && input.compareTo("=") !=0 && input.compareTo("&&") !=0 && input.compareTo("||") !=0&&input.compareTo("!") !=0) {
		return result;
	}
	//if we are still in the function it passed the test return true
	else if(input.compareTo("==") ==0  || input.compareTo("<>") ==0|| input.compareTo("<") ==0 || input.compareTo(">") ==0 || input.compareTo("<=") ==0 || input.compareTo(">=") ==0 || input.compareTo("+") ==0 || input.compareTo("-") ==0 || input.compareTo("*") ==0||input.compareTo("/") ==0 || input.compareTo("=") ==0 || input.compareTo("&&") ==0 || input.compareTo("||") ==0||input.compareTo("!") ==0) {
		
	result =true;	
	return result;}else
		return result;
}

//ids if is a punctuation
public boolean isPunctuation(String input){
	boolean result=false;
	int length = input.length();
	//String character;
	
	//no punctuation is longer than 2 so if length >2
	if (length >2)
		return result;
	//make an exhaustive list of the punctuation markers
	else if(input.compareTo(";") !=0&&input.compareTo(":") !=0&&input.compareTo("::") !=0&&input.compareTo(".") !=0&&input.compareTo(",") !=0&&input.compareTo("(") !=0&&input.compareTo(")") !=0&&input.compareTo(")") !=0&&input.compareTo("]") !=0&&input.compareTo("{") !=0&&input.compareTo("}") !=0) {
	return result;
	}
	//if still in function we passed the test
	else if(input.compareTo(";") ==0||input.compareTo(":") ==0||input.compareTo("::") ==0||input.compareTo(".") ==0||input.compareTo(",") ==0||input.compareTo("(") ==0||input.compareTo(")") ==0||input.compareTo(")") ==0||input.compareTo("]") ==0||input.compareTo("{") ==0||input.compareTo("}") ==0) {
		result = true;	
		return result;
		}else
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
	else if (input.compareTo("/*")==0) {
		result =true;
		return result;
	}
		
	
	//as we passed the test comment section has started or ended
	
	return result;
}

public boolean isCommentBlockClosed(String input) {
boolean result=false;
	int length = input.length();
	
	if(length!=2)
		return result;
	else if (input.compareTo("*/") ==0)
		result =true;
	
	//as we passed the test comment section has started or ended
	
	return result;
}

public boolean isCommentLine(String input) {
	boolean result=false;
	int length = input.length();
	
	if(length!=2)
		return result;
	else if (input.compareTo("//")==0)
		result =true;
	
	//as we passed the test comment section has started or ended
	
	return result;
	
}

/*
 * now that we have the modules for the regular expressions we can look into gathering the input bits and 
 * using the modules we created according to the lexical specification in order to do the tokenization.
 * */
//lexer function
public ArrayList <Token> lexer(File input) {
	BufferedReader reader;
	BufferedWriter write_token;
	BufferedWriter write_error;
	try {
		
		//setting the input and output Files for the process and opening the streams for input andoutput. 
		String inputName = input.getName();
		String tokenOutputFileName = inputName + "_tokens.txt";
		String errorOutputFileName = inputName + "_errors.txt";
		File tokenOutput = new File(tokenOutputFileName);
		File errorOutput = new File(errorOutputFileName);
		reader = new BufferedReader(new FileReader(input));
		write_token = new BufferedWriter(new FileWriter(tokenOutput));
		write_error = new BufferedWriter(new FileWriter (errorOutput));
		
		
		
		
		
		//make nested while loop. not the most efficient design but ensures that only one line at a time is read. 
		int line_count =0;
		Location loc = new Location();
		String char_line = reader.readLine();
		
		//each time a line is done being tokenized and read it will come back to the beginning and 
		//start with the next line. 
		int marker = 0;
		while (char_line !=null) {
			
		//
		//int len = char_line.length();
		loc.setLocation(line_count);
		String part = String.valueOf(char_line.charAt(0));
		
		//int len_part;
		String [] is_token = isToken(part);
		//while loop to do the verification of which token we are looking at. 
		
		while (marker<char_line.length()){
		Token token;
		boolean isTrue =true;	
			//
		while(isTrue) {
			//verify is the part is a token
			if(part.compareTo(" ")==0 && marker!=char_line.length()-1) {
				marker++;
				part = String.valueOf(char_line.charAt(marker));
			}else if (part.compareTo(" ")==0 && marker==char_line.length()-1) {
				break;
			}
			is_token = isToken(part);
			//ifnot a token, we verify up to 3 more chars (ex: floats might need up to 3 chars in order to be valid again
			//moment it becomes valid again:ex add one more and then valid then we go out of this mini loop n back to reality
			if (marker<char_line.length()) {
			if(is_token[0].compareTo("false")==0) {
				String temp = part;
				if (marker<char_line.length()) {
				for (int i=0;i<3;i++) {
					if(marker!=char_line.length()-1) {
						String el = String.valueOf(char_line.charAt(marker+1));
						temp+=el;
					}
					
					is_token = isToken(temp);
					//if the token does get valid after one more character we adjust the part and we 
					//go back to the main loop
					if(is_token[0].compareTo("true")==0) {
						part = temp;
						marker++;
						break;
					}
					//break the loop in the case that the characters are still invalid after three addition
					else if (is_token[0].compareTo("false")==0&& i==2) {
						isTrue = false;
						marker = marker-2;
						break;
					}else if (is_token[0].compareTo("false")==0&& i==1) {
						isTrue = false;
						marker = marker-1;
						break;
					}else if (is_token[0].compareTo("false")==0&& i==0) {
						isTrue = false;
						break;
					}
					marker++;
				}
				
				}
				
				}
					//if its the end of the line and the token is not valid it will not enter the loop so that means there will 
				//be an error message displayed later. 
				
				
			}
			if(isTrue==false)
				break;
			//since we are still in the loop it means that we can add an element to the part (go to next char)
			//increment marker
			marker+=1;
			int length = char_line.length();
			//got get next char and add it to the string
			 if (marker == char_line.length()) {
				isTrue=false;
				break;
			}
			String element = String.valueOf(char_line.charAt(marker));
			part += element;
		}
		
		//we maintain the marker at its current position because the next part will start at this index.
		//now we must take out the last char that we added to the part since it is the part that made the token invalid
		
		
				
				
				
				//now that we exited the loop it means that we need to take away one char from the part and then create the token
				String temp = "";
				if(part.length() == 1 && marker == char_line.length()) {
					part = String.valueOf(char_line.charAt(marker-1));
					//int testing=0;
					//char testing1 = char_line.charAt(testing);
					//marker++;
				}
				else if (part.length()==1 ) {
					
					part = String.valueOf(char_line.charAt(marker));
					marker++;
				}
				
				else 
				
				{
				for(int i=0;i<part.length()-1;i++) {
					temp += String.valueOf(part.charAt(i));
				}
				part = temp;
				}
				is_token = isToken(part);
				//creating the token that the isToken variable tells us is creatable
				if(is_token[1].compareTo("punctuation")==0) {
					//int length = part.length();
					//int index = 0;
					String token_type = "punctuation";
					
					token = new Token(token_type, part, loc);
					write_token.write(token_type );
					write_token.flush();
					write_token.write(" ");
					write_token.flush();
					tokens.add(token);
					
					
				}else if (is_token[1].compareTo("ID")==0) {
					//int length = part.length();
					//int index=0;
					String token_type = "ID";
					
					token = new Token(token_type, part, loc);
					write_token.write(token_type);
					write_token.flush();
					write_token.write(" ");
					write_token.flush();
					tokens.add(token);
				}else if (is_token[1].compareTo("keyword")==0) {
					//int length = part.length();
					//int index =0;
					String token_type = "keyword";
					
					token = new Token (token_type, part, loc);
					write_token.write(token_type);
					write_token.flush();
					write_token.write(" ");
					write_token.flush();
					tokens.add(token);
					int x=0;
					x=x+x;
				}else if (is_token[1].compareTo("float")==0) {
					//int length = part.length();
					//int index = 0;
					String token_type = "float";
					
					token = new Token(token_type, part, loc);
					write_token.write(token_type);
					write_token.write(" ");
					write_token.flush();
					tokens.add(token);
				}else if (is_token[1].compareTo("integer")==0) {
					//int length = part.length();
					//int index = 0;
					String token_type = "integer";
					
					token = new Token(token_type, part, loc);
					write_token.write(token_type);
					write_token.flush();
					write_token.write(" ");
					write_token.flush();
					tokens.add(token);
				}else if (is_token[1].compareTo("operator")==0) {
					//int length = part.length();
					//int index=0;
					String token_type = "operator";
					
					token = new Token(token_type, part,loc);
					write_token.write(token_type);
					write_token.flush();
					write_token.write(" ");
					write_token.flush();
					tokens.add(token);
				}
				// we have to get the rest of the line and put it in the comment and output to file
				
				else if (is_token[1].compareTo("commentLine")==0) {
					int index_start = marker-2;
					int index = 0;
					String token_input = "";
					String token_type = "comment";
					
					//getting the line to put in comment, as itmight not be at the beginning of the line we use the marker as a way to pinpoint the begginning of the comment
					
					for (int i=index_start; i <char_line.length();i++) {
						token_input+=String.valueOf(char_line.charAt(i));
					}
					
					//int token_length = token_type.length();
					part=token_input;
					marker=char_line.length()-1;
					token = new Token("comment", token_input, loc);
					//token stream to the output file
					write_token.write(token_type);
					write_token.write(" ");
					write_error.flush();
					tokens.add(token);
				}
				//we are making only the open comment because the moment we finish counting the open comment tokens, or clo comment tokens, 
				//we will decrease or increase the counter and then we will put everything in one token
				else if (is_token[1].compareTo("commentOpen")==0) {
					//int open_counter =1;
					
					//now we have to parse the text until we find open_counter down to zero
					
					
						
						String [] com = comment_lexer(input, loc, marker);
						
						if(com[0].compareTo("null")==0) {
							
						}else {
							//get the value of the new marker and location of where we are at
							marker = Integer.valueOf(com[0]);
							line_count = Integer.valueOf(com[1]);
							part = com[2];
							
							token =  new Token ("comment", part, loc);
							tokens.add(token);
							write_token.write("comment");
							write_error.flush();
						}					
				}
				
				//if ever the content is still not a valid token then we have an error
				//output to console and output to external file
				else if (is_token[0].compareTo("false")==0 && part.compareTo(" ")==0) {
					break;					
				}
				
				
				
				
				else {
					//write to file the error message
					String errorM = "Token invalid. File: "+ inputName + " content: " + part + "Location line: " + loc;

					System.out.println(errorM);
					
					write_error.write(errorM);
					write_error.flush();
					write_error.newLine();
				}
		if(marker!=char_line.length())
		part = String.valueOf(char_line.charAt(marker));		
		}
		//increase the line count as we are done reading this line and taking in the next line. 
		
		line_count +=1;
		loc.setLocation(line_count);
		//reset the marker to 0 as we are changing line
		marker=0;
		char_line = reader.readLine();
		//go to a new line in the token stream
		write_token.newLine();
		
		}
		//closing the readers/writers	
		reader.close();
		write_token.close();
		write_error.close();
	}
	catch(Exception e) {
		System.out.println(e);
	}
	
	
	return tokens;
	
}


@SuppressWarnings("finally")
public String [] comment_lexer(File input, Location loc, int marker) {
	String [] result = {"marker", "location", "part"};
	
	try {
		BufferedReader reader;
	//	BufferedWriter write_token;
		//BufferedWriter write_error;
		
		//PrintWriter write_token;
		//PrintWriter write_error;
		//setting the input and output Files for the process and opening the streams for input andoutput. 
	
	reader = new BufferedReader(new FileReader(input));
	String line = reader.readLine();
	int initial_loc = loc.getLine();
	int counter_c =1;
	int location = loc.getLine();
	String part = "";
	//getme to the line in the file that we got the comment at
	for (int i = 0;i<initial_loc;i++) {
		
		line = reader.readLine();
	}
	
	boolean isTrue=true;
	int marker2 = marker;
	while(isTrue) {
		String [] is_token;
		boolean second_loop = true;
		part=String.valueOf(line.charAt(marker2));
		int mark = marker2;
		while(second_loop) {
		
		
		
		//encouters of new open or close comment
		
		if (String.valueOf(line.charAt(mark)).compareTo("*")==0)
		{
			part = String.valueOf(line.charAt(mark));
			part +=String.valueOf(line.charAt(mark+1));
			mark++;
		}
		else if (String.valueOf(line.charAt(mark)).compareTo("/")==0){
			part = String.valueOf(line.charAt(mark));
			part +=String.valueOf(line.charAt(mark+1));
			mark++;
		}	
		is_token = isToken(part);
		//checking if the part is a token or not to open or close the comment section
		if(is_token[1].compareTo("commentOpen")==0) {
			mark++;
			counter_c++;
		}
		else if(is_token[1].compareTo("commentClose")==0) {
			counter_c--;
		}
		if (counter_c ==0) {
			mark++;
			marker2=mark;
			second_loop = false;
			isTrue = false;
			break;
		}
		
		if(mark!=line.length()-1) {
		mark++;
		part+=String.valueOf(line.charAt(mark));
		}
		}
		if(mark== line.length()-1) {
		line = reader.readLine();
		location++;
		}
	}
	
	//go get the content of the comment now that we know when it starts (marker) and ends (marker2)
	
	isTrue=true;
	int locationtemp=initial_loc;
	int temp_marker=marker;
	for (int i = 0;i<initial_loc;i++) {		
		line = reader.readLine();
	}
	part=String.valueOf(line.charAt(temp_marker));
	while(isTrue) {
		if(temp_marker==marker2 && locationtemp==location) {
			isTrue=false;
		}
		else if (temp_marker== line.length()-1) {
			line = reader.readLine();
			locationtemp++;
			temp_marker=0;
			part+=" ";
		}else {
		temp_marker++;
		part+=String.valueOf(line.charAt(temp_marker));
		
		}
	}
	
	
	//int location = loc.getLine();
	result [1] = String.valueOf(location);
	result [0] = String.valueOf(marker2);
	result[2] = part;
	reader.close();
	//return result;
	}catch (Exception e) {
		
		System.out.println(e);
	}finally{
		
		if(result[0].compareTo("marker")==0) {
			result[0] = String.valueOf(marker);
			result[1] = "null";
		}
		return result;
	}
}



/*
 * This method is going to return a String array, in the first index there will be true or false
 * in the second index, there will be a null string if there is a false and if true it will tell which 
 * type of token this token identifies to. 
 * 
 * This method is going to be used by the lexer class in order to know if the string is a vlid token ornot
 * 
 */
public String [] isToken(String input) {
	String [] is_token = {"false",""};
	
	String first = String.valueOf(input.charAt(0));
	
	//if the first character is a letter than it is either a keyword or an ID
	if(isLetter(first)==true) {
		if(isKeyword(input)==true) {
			is_token[0] = "true";
			is_token[1] = "keyword";
			return is_token;
		}else if (isID(input)==true) {
			is_token[0] = "true";
			is_token[1] = "ID";
			return is_token;
		}
		else {
			is_token[0]="false";
			return is_token;
		}
		
	}
	//if the first character is a digit, than the only option is to be either an integer or a float
	else if (isDigit(first)==true) {
		if(isFloat(input)==true) {
			is_token[0]="true";
			is_token[1] = "float";
			return is_token;
			
		}//case it is an integer
		else if (isInteger(input)==true) {
			is_token[0]="true";
			is_token[1]="integer";
			return is_token;	
		}else {
			is_token[0]="false";
			return is_token;
		}
		
	}
	//if not one of those two cases we enter in the realm of the punctionation, operator or comment 
	else {
		if(input.compareTo(" ") == 0)
			return is_token;
		if(isCommentBlockOpen(input)==true) {
			is_token[0]="true";
			is_token[1]="commentOpen";
			return is_token;
		}
		else if (isCommentBlockClosed(input)==true) {
			is_token[0] = "true";
			is_token[1] = "commentClose";
			return is_token;
		}else if (isCommentLine(input)==true) {
			is_token[0]="true";
			is_token[1] = "commentLine";
			return is_token;
			
		}else if (isOperator(input)==true) {
			is_token[0]="true";
			is_token[1]="operator";
			return is_token;
			
		}else if (isPunctuation(input)==true) {
			is_token[0] = "true";
			is_token[1] = "punctuation";
			return is_token;
		}
		//in this case fit no valid token category
		else {
			is_token[0]="false";
			return is_token;
		}
		
	}
}




//error messages methods

/*
 * on advice of prof not making special cases, just sending error message if tokens are not valid. 
 * 
 */
}


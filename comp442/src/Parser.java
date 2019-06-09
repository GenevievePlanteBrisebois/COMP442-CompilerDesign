
/*
 * Class written by Genevieve Plante-Brisebois 40003112
 * Code written in the context of the Compiler Design Class Winter 2019
 * 
 * This class is going to be the parser of the compiler, used to make the syntactical analysis
 * of the input streams. 
 * 
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.List;

public class Parser {
	// this will allow us to take in the tokens in and make sense of them
	ArrayList<Token> tokens = new ArrayList<Token>();
	ArrayList<String> input = new ArrayList<String>();

	// AST that we are going to build as we pass through the functions.
	Node<Token> AST = new Node<Token>("prog");
	Node <Token> class_list = new Node<Token>("class_list");
	Node <Token> function_list = new Node <Token> ("function_list");
	Node <Token> main = new Node <Token> ("main");
	
	
	
	// this tree is going to be the parsing tree.
	Node<Token> root = new Node<Token>("prog");
	int value_pointer;

	// as it goes we will add the errors detected in this string and then writing
	// this string to a file in the parser.
	String error_stream = "";

	// creating a stack to be able to make the parsing and do the panic mode error
	// handling
	// Stack <Token> syn_errors= new Stack <Token>();

	/*
	 * function to record the start of an error (when it is first detected)
	 */
	public String start_error_rec(Token token) {
		int loc = token.getLocation();
		return "Error start at line :" + token.getLocation() + " with token :" + token.getType() + " " + token.getData()
				+ "\n";
	}

	/*
	 * function to record when we resume the parsing
	 */
	public String end_error_rec(Token token) {
		return "Resume parsing at line :" + token.getLocation() + "with token :" + token.getType() + " "
				+ token.getData() + "\n";
	}

	/*
	 * going to build the first and follow sets of the productions of the grammar.
	 * They are going to help to know which production to call unto when we are
	 * doing the recursive calls.
	 * 
	 * 
	 * The first and follow sets are done using the KFG EDIT tool. It is a tool to
	 * verify if a grammar is LL1 or not. It shows the sets and it also does the
	 * test to make sure the test is valid It also allows to do derivations in order
	 * to make sure that the grammar is not broken once we transform it into LL1.
	 */
	// FIRST SETS
	final List<String> FIRST_ADDOP = Arrays.asList("+", "-", "||");
	final List<String> FIRST_APARAMS = Arrays.asList("EPSILON", "+", "-", "id", "!", "integer", "float", "(");
	final List<String> FIRST_APARAMSTAIL = Arrays.asList(",");
	final List<String> FIRST_ARITHEXPR = Arrays.asList("(", "float", "integer", "!", "id", "+", "-");
	final List<String> FIRST_ARITHEXPRTAIL = Arrays.asList("EPSILON", "+", "-", "||");
	final List<String> FIRST_ARRAYSIZE = Arrays.asList("[");
	final List<String> FIRST_ASSIGNOP = Arrays.asList("=");
	final List<String> FIRST_ASSIGNSTAT = Arrays.asList("id");

	final List<String> FIRST_CLASSDECL = Arrays.asList("class");
	final List<String> FIRST_EXPR = Arrays.asList("(", "float", "integer", "not", "id", "+", "-");
	final List<String> FIRST_EXPRTAIL = Arrays.asList("EPSILON", "==", ">=", ">", "<=", "<", "<>");

	final List<String> FIRST_FACTOR = Arrays.asList("+", "-", "id", "!", "integer", "float", "(");
	final List<String> FIRST_FACTORTAIL = Arrays.asList("id");
	final List<String> FIRST_FACTORTAILTAIL = Arrays.asList("(", "[");
	final List<String> FIRST_FUNCTIONCALL = Arrays.asList("id");
	final List<String> FIRST_FUNCDECL = Arrays.asList("id", "float", "integer");
	final List<String> FIRST_FUNCHEAD = Arrays.asList("id", "float", "integer");
	final List<String> FIRST_FUNCDEF = Arrays.asList("id", "float", "integer");
	final List<String> FIRST_FUNCBODY = Arrays.asList("{");
	final List<String> FIRST_FPARAMS = Arrays.asList("EPSILON", "float", "id", "integer");
	final List<String> FIRST_FPARAMSTAIL = Arrays.asList(",");

	final List<String> FIRST_IDNEST = Arrays.asList("id");
	final List<String> FIRST_IDNESTTAIL = Arrays.asList("(", "[", ".");
	final List<String> FIRST_INDICE = Arrays.asList("[");

	final List<String> FIRST_MULTOP = Arrays.asList("&&", "*", "/");

	final List<String> FIRST_PROG = Arrays.asList("class", "float", "integer", "id", "main");

	final List<String> FIRST_RELOP = Arrays.asList("==", "<>", "<", "<=", ">", ">=");
	final List<String> FIRST_RELEXPR = Arrays.asList("(", "float", "integer", "!", "id", "+", "-");

	final List<String> FIRST_SIGN = Arrays.asList("+", "-");
	final List<String> FIRST_STATEMENT = Arrays.asList("id", "write", "return", "read", "if", "for");
	final List<String> FIRST_STATBLOCK = Arrays.asList("EPSILON", "{", "for", "if", "read", "return", "write", "id");

	final List<String> FIRST_TERM = Arrays.asList("(", "float", "integer", "!", "id", "+", "-");
	final List<String> FIRST_TERMTAIL = Arrays.asList("EPSILON", "*", "/", "&&");
	final List<String> FIRST_TYPE = Arrays.asList("id", "float", "integer");

	final List<String> FIRST_VARDECL = Arrays.asList("id", "float", "integer");
	final List<String> FIRST_VARIABLE = Arrays.asList("id");

	// FOLLOW SETS

	final List<String> FOLLOW_ARITHEXPRTAIL = Arrays.asList(";", ")", ",", "==", ">=", "<=", "<", ">", "<>", "]");
	final List<String> FOLLOW_TERMTAIL = Arrays.asList(";", ")", ",", "==", ">=", "<=", "<", ">", "<>", "]", "+", "-",
			"||");
	final List<String> FOLLOW_FPARAMS = Arrays.asList(")");
	final List<String> FOLLOW_APARAMS = Arrays.asList(")");
	final List <String> FOLLOW_EXPRTAIL = Arrays.asList(";",")", ",");
	final List <String> FOLLOW_STATBLOCK = Arrays.asList(";","else");

	/*
	 * Note on the error handling: will have to be put eventually in a method for
	 * modularity but for the moment done as it goes in each function to insure that
	 * all restrictions are taken into considerations and work as it should but that
	 * would be to do in iteration 2.
	 * 
	 * The current set up allows for all the different error messages that would be
	 * used at the various places in the parsing.
	 */

	// prog -> {classDecl} {funcDef} 'main' funcBody ';'
	/*
	 * This function is the first function of the recursive calls for the parser. It
	 * will allow to build the parsing tree. Later there will be a function in order
	 * to build the AST that will be used in the semantic analysis.
	 */
	public boolean prog() {
		
		//setting up for the AST
		
		
		
		is_comment();
		// we know that the value is of tokens[1] since for sure the first ever value
		// that will be entered in the prog() function is going to be the value
		// of the tokens[0].
		Token lookahead = tokens.get(value_pointer + 1);

		/*
		 * we will append the class declarations and functions to their respective
		 * parents. this is to facilitate the reading of the parsing tree when we will
		 * want to create the AST.
		 */
		Node<Token> classDeclarations = new Node<Token>("classDeclarations");
		Node<Token> functions = new Node<Token>("functions");

		root.addChild(classDeclarations);
		root.addChild(functions);

		// loop to keep going until the look ahead is not "class" anymore
		// error handling in case the current node or lookahead is not part of the
		// possible terms we are looking for.

		if (FIRST_PROG.contains(tokens.get(value_pointer).getData()) == false) {
			error_stream += start_error_rec(tokens.get(value_pointer));
			System.out.println(start_error_rec(tokens.get(value_pointer)));

			value_pointer++;
			error_stream += "Invalid token to to start a program. Must start with either \"class\" , a type, or \"main\". \n";

			// keep incrementing until we can find a value that is valid.
			while (FIRST_PROG.contains(tokens.get(value_pointer).getData()) == false) {
				// syn_errors.push(tokens.get(value_pointer));
				if (value_pointer == tokens.size()||value_pointer == tokens.size()-2) {
					error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
					return false;
				} else

					value_pointer++;
			}
			error_stream += end_error_rec(tokens.get(value_pointer));
			lookahead = tokens.get(value_pointer + 1);
		}

		if (FIRST_CLASSDECL.contains(tokens.get(value_pointer).getData())) {

			while (FIRST_CLASSDECL.contains(tokens.get(value_pointer).getData())) {
				Node<Token> classDecl = new Node<Token>("classDecl");
				// if there is a class declaration, the children will be created and appended to
				// it
				boolean is_true = classDecl(classDecl);
				// link to the root in the case it is true
				if (is_true != true) {
					System.out.println("Not valid class declaration");
					break;
				} else {
					classDeclarations.addChild(classDecl);
				// child_marker++;
				
				lookahead = tokens.get(value_pointer+1);
				}
				if(FIRST_FUNCDEF.contains(tokens.get(value_pointer).getData())) {
					break;
				}
				if(tokens.get(value_pointer).getData().compareTo("main")==0)
					break;
				// error handling
				if (FIRST_CLASSDECL.contains(tokens.get(value_pointer).getData()) == false && FIRST_FUNCDEF.contains(tokens.get(value_pointer).getData()) == false) {
					error_stream += start_error_rec(tokens.get(value_pointer));
					System.out.println(start_error_rec(tokens.get(value_pointer)));

					value_pointer++;
					error_stream += "Invalid token to continue a program. Must continue with either \"class\" , a type, or \"main\". \n";

					// keep incrementing until we can find a value that is valid.
					while (FIRST_CLASSDECL.contains(tokens.get(value_pointer).getData()) == false) {
						// syn_errors.push(tokens.get(value_pointer));
						if (value_pointer == tokens.size()-1) {
							error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
							return false;
						} else
							value_pointer++;

					}

					error_stream += end_error_rec(tokens.get(value_pointer));
					lookahead = tokens.get(value_pointer + 1);
				}

			}
			// again verify for error at this point

		}
		lookahead = tokens.get(value_pointer+1);
		// now check for functions
		if (FIRST_FUNCDECL.contains(tokens.get(value_pointer).getData())) {
			boolean is_true = true;
			// loop to verify for funcDef
			while (is_true) {
				if(tokens.get(value_pointer).getData().compareTo("main")==0)
					break;
				Node<Token> funcDef = new Node<Token>("funcDef");
				is_true = funcDef(funcDef);
				// adding child to tree if there was a function
				if (is_true == true) {
					functions.addChild(funcDef);
					// child_marker++;
					lookahead = tokens.get(value_pointer + 1);
				}

				// LOOKAHEAD CHECK/ERROR CHECK
				if (FIRST_PROG.contains(tokens.get(value_pointer).getData()) == false
						&& tokens.get(value_pointer).getData().compareTo("class") == 0) {
					error_stream += start_error_rec(tokens.get(value_pointer));
					System.out.println(start_error_rec(tokens.get(value_pointer)));
					error_stream += "Invalid token to continue a program. Must continue with either a type, or \"main\". \n";

					// keep incrementing until we can find a value that is valid.
					while (FIRST_PROG.contains(tokens.get(value_pointer).getData()) == false
							|| tokens.get(value_pointer).getData().compareTo("class") == 0) {
						// syn_errors.push(tokens.get(value_pointer));
						if (value_pointer == tokens.size()) {
							error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
							return false;
						} else
							value_pointer++;

					}
					error_stream += end_error_rec(tokens.get(value_pointer));
					lookahead = tokens.get(value_pointer + 1);
				}

			}

		}
		// keep going to the main part of the function
		if (input.get(value_pointer).compareTo("main") == 0) {
			Node<Token> child = new Node<Token>(tokens.get(value_pointer), "terminal");
			root.addChild(child);
			// child_marker++;
			// value_pointer++;
			lookahead = tokens.get(value_pointer + 1);

			// temp = value_pointer;
			// error handling if the lookahead is not part of the func body.

			Node<Token> funcBody = new Node<Token>("funcBody");
			// check lookahead for the function body
			// error check for lookahead
			if (FIRST_FUNCBODY.contains(lookahead.getData()) == false) {
				error_stream += start_error_rec(tokens.get(value_pointer + 1));
				System.out.println(start_error_rec(tokens.get(value_pointer + 1)));

				error_stream += "Invalid token to continue a program. Must continue a \"{\". \n";

				// keep incrementing until we can find a value that is valid.
				while (FIRST_FUNCBODY.contains(lookahead.getData()) == false) {
					// syn_errors.push(tokens.get(value_pointer+1));
					if (value_pointer == tokens.size()) {
						error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}
				// if lookahead is valid it means we move up the pointer
				value_pointer++;
				// going to resume parsing here
				error_stream += end_error_rec(tokens.get(value_pointer));
				lookahead = tokens.get(value_pointer + 1);
			}
			value_pointer++;
			boolean is_true = funcBody(funcBody);
			if (is_true == false) {
				// remove all children of root as it will not be valid anyway (did not reach the
				// end of production, not valid program)
				error_stream += "Function body of main function invalid or no main function body. Error at line: "
						+ tokens.get(value_pointer).getLocation() + "\n";
				return false;
			} else {
				root.addChild(funcBody);
				value_pointer-=1;
				lookahead = tokens.get(value_pointer + 1);

				// check the lookahead to see if valid with the program grammar rules

				if (lookahead.getData().compareTo(";") != 0) {
					error_stream += start_error_rec(lookahead);
					System.out.println(start_error_rec(tokens.get(value_pointer + 1)));

					error_stream += "Invalid token to continue a program. Must continue a \"{\". \n";

					// keep incrementing until we can find a value that is valid.
					while (lookahead.getData().compareTo(";") != 0) {
						// syn_errors.push(tokens.get(value_pointer+1));
						value_pointer++;

						if (value_pointer == tokens.size()) {
							error_stream += "token to complete the program absent. Expecting \";\" in order to finish the program. Character not found. Program failure";
							break;
						} else
							lookahead = tokens.get(value_pointer + 1);
					}
					// do nothing if the value pointer is at max token size
					if (value_pointer != tokens.size()) {
						// if lookahead is valid it means we move up the pointer
						value_pointer++;
						// going to resume parsing here
						error_stream += end_error_rec(tokens.get(value_pointer));
						lookahead = tokens.get(value_pointer + 1);
					}
				}

				if (lookahead.getData().compareTo(";") == 0) {
					value_pointer++;
					Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
					root.addChild(end);
					// value_pointer++;
					// lookahead= tokens.get(value_pointer+1);
					// temp = value_pointer;
					// child_marker++;
					return true;
				} else {
					// root.removeChildren();
					// value_pointer = temp;
					// for(int i=child_marker;i<root.getChildren().size();i++) {
					// root.removeChild(i);
					// }
					return false;
				}

			}

		} else
			return false;
		/*
		 * else { //remove children value_pointer = temp; //error message
		 * error_stream+="Error in parsing. missing main function. Line: "+tokens.get(
		 * value_pointer).getLocation()+"\n"; return false; }
		 */
	}

	// classDecl -> 'class' 'id' [':' 'id' {',' 'id'}] '{' {varDecl} {funcDecl} '}'
	// ';'

	public boolean classDecl(Node<Token> parent) {
		// check if the current value_pointer is a comment. it will automatically
		// increment if it is a comment
		is_comment();
		Token lookahead = tokens.get(value_pointer + 1);
		Token current  =  tokens.get(value_pointer);
		// int counter =0;
		// int child_marker = parent.getChildren().size();
		// int temp = value_pointer;

		// start parsing for classDecl
		// check for 'class'
		if (input.get(value_pointer).compareTo("class") != 0)
			return false;
		else {
			// in case it is the class keyword then we create a node and add it to the
			// parent
			Node<Token> key = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(key);
			lookahead = tokens.get(value_pointer+1);
			//temp = value_pointer;
			// child_marker++;
			// counter++;

			// checking for id

			boolean is_true = true;
			
			while(is_true) {
				if(lookahead.getType().compareTo("id")==0) {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					break;
				}
				
				else {
						
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(lookahead.getType().compareTo("id")!=0) {

						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
						
					}
					error_stream+= end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}				
		}
			
			
			Node<Token> class_id = new Node<Token>(tokens.get(value_pointer), "terminal");
				Token check = tokens.get(value_pointer);
				parent.addChild(class_id);
				lookahead = tokens.get(value_pointer+1);
				
				
				//now we check for :, if not there we skip that part entirely and move to the "{"
				
				
				if(lookahead.getData().compareTo(":")==0) {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					Node<Token> inherit = new Node<Token>(tokens.get(value_pointer), "terminal");
					parent.addChild(inherit);
					
					
					//now we must have at least one id else it will be invalid
					
					is_true = true;
					
					while(is_true) {
						if(lookahead.getType().compareTo("id")==0) {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							break;
						}
						
						
						else {
							error_stream+= start_error_rec(lookahead);
							error_stream+="Expecting an id.\n";
							
							System.out.println(start_error_rec(lookahead));
							
							
							while(lookahead.getType().compareTo("id")!=0) {
								
							if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
								error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
								return false;
							}else {
								value_pointer++;
								lookahead = tokens.get(value_pointer+1);
								
							}		
							}
							
							error_stream+= end_error_rec(lookahead);
							System.out.println(end_error_rec(lookahead));
						}
						
						
					}
					
					Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");

					parent.addChild(id);
					
					
					//now look for any "," id anynumber of times
					while (is_true) {
						boolean resume_normal = false;
						//check for "{"
						//break from the loop and go to process the "{" 
						if(lookahead.getData().compareTo("{")==0)
							break;
						
						//check for ","
						boolean is_true2 = true;
						while(is_true2) {
							
							if(lookahead.getData().compareTo(",")==0) {
								value_pointer++;
								lookahead = tokens.get(value_pointer+1);
								break;
							}else if(lookahead.getData().compareTo("{")==0) {
								resume_normal = true;
								break;
							}
								
							
							
							else {
								error_stream+= start_error_rec(lookahead);
								error_stream+="Expecting a ,\n";
								System.out.println(start_error_rec(lookahead));
								
								
								while(lookahead.getData().compareTo(",")!=0 && lookahead.getData().compareTo("{")!=0) {
									
								if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
									error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
									return false;
								}else {
									value_pointer++;
									lookahead = tokens.get(value_pointer+1);
									
								}
								}
								
								error_stream+= end_error_rec(lookahead);
								System.out.println(end_error_rec(lookahead));
							}
							
							
						}
						if(resume_normal)
							break;
						Node<Token> coma = new Node<Token>(tokens.get(value_pointer), "terminal");
						parent.addChild(coma);
						
						//now check for an id
						
						while(is_true2) {
							if(lookahead.getType().compareTo("id")==0) {
								value_pointer++;
								lookahead = tokens.get(value_pointer+1);
								break;
							}
							
							
							else {
								error_stream+= start_error_rec(lookahead);
								error_stream+="Expecting an id.\n";
								
								System.out.println(start_error_rec(lookahead));
								
								
								while(lookahead.getType().compareTo("id")!=0) {
									
								if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
									error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
									return false;
								}else {
									value_pointer++;
									lookahead = tokens.get(value_pointer+1);
									
								}		
								}
								
								error_stream+= end_error_rec(lookahead);
								System.out.println(end_error_rec(lookahead));
							}		
						}
						
						Node<Token> id1 = new Node<Token>(tokens.get(value_pointer), "terminal");
						parent.addChild(id1);
					}//end , id section
						
					}//end of : optional section
				
				
				//begin section of the "{" part of the
				//check lookahead for "{"
				
				
				is_true = true;
				while(is_true) {
					if(lookahead.getData().compareTo("{")==0) {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						break;
					}
					
					
					else {
						error_stream+= start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						
						while(lookahead.getData().compareTo("{")!=0) {
							
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
						
							
						}
						
						error_stream+= end_error_rec(lookahead);
						System.out.println(end_error_rec(lookahead));
					}
		
				}
				
				Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(open);
				
				
				//now we check if there are vardeclarations
				 is_true = true;
				// check for varDecl
				 //i know backtracking is not supposed to be used but at the moment not sure how to handle that situation in particular. 
				 
				while (is_true) {
					
					int temp = value_pointer;
					boolean test = FIRST_VARDECL.contains(lookahead.getData());
					if(test) {
						Node<Token> varDecl = new Node<Token>("varDecl");
						value_pointer++;
					boolean is_true2 = varDecl(varDecl);
						if(is_true2!=true) {
							//not var decl, might be function decl
							value_pointer = value_pointer-temp;
							is_true = false;
							
						}else {
					parent.addChild(varDecl);
					value_pointer-=1;
					lookahead = tokens.get(value_pointer+1);
						}
				}// end while
					else
						is_true = false;
				}
				//new we check if there is any number of func declarations
					is_true = true;

					while (is_true) {
						
					int	temp = value_pointer;
						
						if(FIRST_FUNCDECL.contains(lookahead.getData())) {
							Node<Token> funcDecl = new Node<Token>("funcDecl");
						boolean is_true2 =funcDecl(funcDecl);
							if(is_true2!=true) {
								//not var decl, might be function decl
								value_pointer = value_pointer-temp;
								is_true = false;
								
							}else {
						parent.addChild(funcDecl);
						value_pointer-=1;
						lookahead = tokens.get(value_pointer+1);
							}
					} else
						is_true = false;
					}//end while
				
				
				//look for the closing of the declaration
				
				is_true = true;
				while(is_true) {
					if(lookahead.getData().compareTo("}")==0) {
						value_pointer++;
						//dont need the lookahead anymore it is the last element of the function
						//lookahead = tokens.get(value_pointer+1);
						break;
					}
					
					
					else {
						error_stream+= start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						
						while(lookahead.getData().compareTo("}")!=0) {
							
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
											}
						
						error_stream+= end_error_rec(lookahead);
						System.out.println(end_error_rec(lookahead));
					}
		
				}
				
				Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(close);
				
				value_pointer++;
				value_pointer++;
				
				return true;
		}//end of class decl	
	}
					
				
				
	// funcDecl -> type 'id' '(' fParams ')' ';'
	public boolean funcDecl(Node<Token> parent) {
		is_comment();
		// int counter =0;
		//int child_marker = parent.getChildren().size();
		//int temp = value_pointer;
		
		Token lookahead = tokens.get(value_pointer+1);
		
		
		Node<Token> type = new Node<Token>("type");
		boolean t = type(type);

		if (t != true) {
			return false;
		} else {
			parent.addChild(type);
			value_pointer-=1;
			
			//temp = value_pointer;
			// value_pointer++;
			// counter++;
			// child_marker++;
			
			
			// check for 'id'
			
			boolean is_true = true;
			
			is_true = true;
			while(is_true) {
				if(lookahead.getType().compareTo("id")==0) {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					break;
				}				
				else {
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					while(lookahead.getType().compareTo("id")!=0) {
						
					if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
						error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
						return false;
					}else {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						
					}
					
						
					}
					
					error_stream+= end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}
				
				
			}//end lookahead verif
			
			Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(id);
			
			
			//check for the "("
			
			is_true = true;
			while(is_true) {
				if(lookahead.getData().compareTo("(")==0) {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					break;
				}
				
				
				else {
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(lookahead.getData().compareTo("(")!=0) {
						
					if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
						error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
						return false;
					}else {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						
					}
					
						
					}
					
					error_stream+= end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}
				
				
			}//end ( verification
			
			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(open);
			
			
			//verify for FPARAMS
			
			is_true = true;
			while(is_true) {
				if(FIRST_FPARAMS.contains(lookahead.getData())) {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					break;
				}
				
				
				else {
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(FIRST_FPARAMS.contains(lookahead.getData())== false) {
						
					if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
						error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
						return false;
					}else {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						
					}
					
						
					}
					
					error_stream+= end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}
				
				
			}
			
			
					Node<Token> fParams = new Node<Token>("fParams");
					boolean param = fParams(fParams);
				
					if(param!=true)
						return false;
					
					parent.addChild(fParams);
					value_pointer-=1;
					lookahead = tokens.get(value_pointer+1);

				

					//check lookahead for ")"
					is_true = true;
					while(is_true) {
						if(lookahead.getData().compareTo(")")==0) {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							break;
						}
						
						
						else {
							error_stream+= start_error_rec(lookahead);
							System.out.println(start_error_rec(lookahead));
							
							
							while(lookahead.getData().compareTo(")")!=0) {
								
							if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
								error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
								return false;
							}else {
								value_pointer++;
								lookahead = tokens.get(value_pointer+1);
								
							}
							
								
							}
							
							error_stream+= end_error_rec(lookahead);
							System.out.println(end_error_rec(lookahead));
						}
						
						
					}//end verid )
					
					Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
					parent.addChild(close);
					
					
					//check foor ;
					
					
					is_true = true;
					while(is_true) {
						if(lookahead.getData().compareTo(";")==0) {
							value_pointer++;
							//lookahead = tokens.get(value_pointer+1);
							break;
						}
						
						
						else {
							error_stream+= start_error_rec(lookahead);
							System.out.println(start_error_rec(lookahead));
							
							
							while(lookahead.getData().compareTo(";")!=0) {
								
							if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
								error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
								return false;
							}else {
								value_pointer++;
								lookahead = tokens.get(value_pointer+1);
								
							}
							
								
							}
							
							error_stream+= end_error_rec(lookahead);
							System.out.println(end_error_rec(lookahead));
						}
						
						
					}//end verid ;
					
					Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
					value_pointer++;
					parent.addChild(end);
					

					return true;

		}

	}

	// funcHead -> type ['id' 'sr'] 'id' '(' fParams ')'
	public boolean funcHead(Node<Token> parent) {
		is_comment();
		// int counter=0;
		Token lookahead = tokens.get(value_pointer+1);
		
		Node<Token> type = new Node<Token>("type");
		//boolean t = type(type);
		parent.addChild(type);
		value_pointer-=1;
		
		//check for id
		boolean is_true = true;
		is_true = true;
		while(is_true) {
			if(lookahead.getType().compareTo("id")==0) {
				value_pointer+=2;
				Token check = tokens.get(value_pointer);
				lookahead = tokens.get(value_pointer+1);
				break;
			}				
			else {
				error_stream+= start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));
				
				while(lookahead.getType().compareTo("id")!=0) {
					
				if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
					error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
					return false;
				}else {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					
				}		
				}
				
				error_stream+= end_error_rec(lookahead);
				System.out.println(end_error_rec(lookahead));
			}
		}//end lookahead verif
		
		Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");
		parent.addChild(id);
		//value
		
		//check for ::
			if(lookahead.getData().compareTo("::")==0) {
				value_pointer++;
				Node<Token> sr = new Node<Token>(tokens.get(value_pointer), "terminal");
				lookahead = tokens.get(value_pointer+1);
				parent.addChild(sr);
				//check for id
				
				is_true = true;
				while(is_true) {
					if(lookahead.getType().compareTo("id")==0) {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						break;
					}				
					else {
						error_stream+= start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						
						while(lookahead.getType().compareTo("id")!=0) {
							
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
						
							
						}
						
						error_stream+= end_error_rec(lookahead);
						System.out.println(end_error_rec(lookahead));
					}
					
					
				}//end lookahead verif
				
				Node<Token> id2 = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(id2);
				
			}
			
		
		
		//check (fparams)
		
		
		
		//check for the "("
		
		is_true = true;
		while(is_true) {
			if(lookahead.getData().compareTo("(")==0) {
				value_pointer++;
				lookahead = tokens.get(value_pointer+1);
				break;
			}
			
			
			else {
				error_stream+= start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));
				
				
				while(lookahead.getData().compareTo("(")!=0) {
					
				if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
					error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
					return false;
				}else {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					
				}
				
					
				}
				
				error_stream+= end_error_rec(lookahead);
				System.out.println(end_error_rec(lookahead));
			}
			
			
		}//end ( verification
		
		Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
		parent.addChild(open);
		
		
		//verify for FPARAMS
		
		is_true = true;
		while(is_true) {
			if(FIRST_FPARAMS.contains(lookahead.getData())) {
				value_pointer++;
				lookahead = tokens.get(value_pointer+1);
				break;
			}
			
			
			else {
				error_stream+= start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));
				
				
				while(FIRST_FPARAMS.contains(lookahead.getData())== false) {
					
				if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
					error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
					return false;
				}else {
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					
				}
				
					
				}
				
				error_stream+= end_error_rec(lookahead);
				System.out.println(end_error_rec(lookahead));
			}
			
			
		}
		
		
				Node<Token> fParams = new Node<Token>("fParams");
				boolean param = fParams(fParams);
			
				if(param!=true)
					return false;
				
				parent.addChild(fParams);
				value_pointer-=1;
				lookahead = tokens.get(value_pointer+1);

			

				//check lookahead for ")"
				is_true = true;
				while(is_true) {
					if(lookahead.getData().compareTo(")")==0) {
						value_pointer++;
						//lookahead = tokens.get(value_pointer+1);
						break;
					}
					
					
					else {
						error_stream+= start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						
						
						while(lookahead.getData().compareTo(")")!=0) {
							
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
						
							
						}
						
						error_stream+= end_error_rec(lookahead);
						System.out.println(end_error_rec(lookahead));
					}
					
					
				}//end verid )
				
				Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(close);
				value_pointer++;
				return true;
				//end method
		
	}

	// funcDef -> funcHead funcBody ';'
	public boolean funcDef(Node<Token> parent) {
		is_comment();
		Token lookahead = tokens.get(value_pointer+1);
		
		
		// check function head
		Node<Token> fh = new Node<Token>("funcHead");
		boolean f = funcHead(fh);

		if (f != true)
			return false;
		else {
			parent.addChild(fh);
			value_pointer-=1;
			lookahead = tokens.get(value_pointer+1);
			
			//lookahead verification for function body
			
			boolean is_true = true;
			
			is_true = true;
			while(is_true) {
				if(FIRST_FUNCBODY.contains(lookahead.getData()) && tokens.get(value_pointer).getData().compareTo("main")!=0) {
					value_pointer++;
					break;
				}
				
				
				else {
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(FIRST_FUNCBODY.contains(lookahead.getData())== false) {
						
					if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
						error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
						return false;
					}else {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						
					}
					
						
					}
					
					error_stream+= end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}
				
				
			}

			// check function body

			Node<Token> fb = new Node<Token>("funcBody");
			f = funcBody(fb);

			if (f != true) {
				return false;
			} else {
				parent.addChild(fb);
				value_pointer-=1;
				lookahead = tokens.get(value_pointer+1);
				
				//lookahead check for the ;
				is_true = true;
				while(is_true) {
					if(lookahead.getData().compareTo(";")==0) {
						value_pointer++;
						break;
					}
					
					
					else {
						error_stream+= start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						
						
						while(lookahead.getData().compareTo(";")!=0) {
							
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
						
							
						}
						
						error_stream+= end_error_rec(lookahead);
						System.out.println(end_error_rec(lookahead));
					}
					
					
				}
					Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
					parent.addChild(end);
					value_pointer++;
					return true;
			}
		}
	}

	// funcBody -> '{' {varDecl} {statement} '}'
	public boolean funcBody(Node<Token> parent) {
		is_comment();
		// int counter=0;
		int child_marker = parent.getChildren().size();
		int temp = value_pointer;
		Token current = tokens.get(value_pointer);
		// check '{'

		if (input.get(value_pointer).compareTo("{") != 0) {
			return false;
		} else {
			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(open);
			value_pointer++;
			// counter++;
			// child_marker++;
			// temp = value_pointer;

			// check for {varDec}
			boolean is_true = true;

			while (is_true) {
				Node<Token> varDecl = new Node<Token>("varDecl");
				is_true = varDecl(varDecl);
				current = tokens.get(value_pointer);
				if (is_true == true) {
					parent.addChild(varDecl);
					// value_pointer++;
					// child_marker++;
					// counter++;
				}

			} // end while
				// check for {statement}
			is_true = true;

			while (is_true) {
				Node<Token> statement = new Node<Token>("statement");
				
				is_true = statement(statement);

				if (is_true == true) {
					parent.addChild(statement);
					// child_marker++;
					// value_pointer++;
					// counter++;
				}

			} // end While
			//value_pointer++;
			// check for '}'
			current = tokens.get(value_pointer);
			if (tokens.get(value_pointer).getData().compareTo("}") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				parent.addChild(close);
				// child_marker++;
				return true;
			}
		}

	}

	// varDecl -> type 'id' {arraySize} ';'
	public boolean varDecl(Node<Token> parent) {
		is_comment();
		// int counter =0;
		int temp = value_pointer;
		int child_marker = parent.getChildren().size();
		// check type
		Node<Token> type = new Node<Token>("type");
		boolean t = type(type);

		if (t != true) {
			value_pointer = temp;
			return false;
		} else {
			parent.addChild(type);
			// value_pointer++;
			// child_marker++;
			// counter++;
			// check for 'id'
			if (tokens.get(value_pointer).getType().compareTo("id") != 0) {
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				value_pointer = temp;
				return false;
			} else {
				Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				// counter++;
				parent.addChild(id);
				// child_marker++;
				// check for {arraySize}
				boolean is_true = true;
				while (is_true) {
					boolean a;
					Node<Token> arr = new Node<Token>("arraySize");
					boolean b = false;
					if(FIRST_ARRAYSIZE.contains(input.get(value_pointer)))
						b = true;
					

					if (b != true) {
						is_true = false;
					} else {
						a = arraySize(arr);
						parent.addChild(arr);
						//value_pointer++;
						// counter++;
						// child_marker++;
					}

				} // end while

				// check for ';'

				if (input.get(value_pointer).compareTo(";") != 0) {
					for (int i = child_marker; i < parent.getChildren().size(); i++) {
						parent.removeChild(i);
					}
					value_pointer = temp;
					return false;
				} else {
					Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
					parent.addChild(end);
					value_pointer++;
					return true;

				}

			}
		}
	}

	//assignStat ';' 
	//| 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'
	// | 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'
	// | 'read' '(' variable ')' ';' 
	// | 'write' '(' expr ')' ';' 
	// | 'return' '(' expr ')' ';'
	public boolean statement(Node<Token> parent) {
		is_comment();
		// int counter=0;
		int child_marker = parent.getChildren().size();
		int temp = value_pointer;
		// check assignstat
		//String start = input.get(value_pointer);
		Token current = tokens.get(value_pointer);
		Node<Token> as = new Node<Token>("assignStat");
		
		//boolean a = assignStat(as);
		current = tokens.get(value_pointer);
		if (FIRST_ASSIGNSTAT.contains(current.getType())) {
			boolean a = assignStat(as);
			parent.addChild(as);
			// child_marker++;
			// value_pointer++;
			// counter++;

			// check ';'

			if (input.get(value_pointer).compareTo(";") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> child = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(child);
				// child_marker++;
				value_pointer++;
				return true;
			}

		}
		// check 'if'
		else if (current.getData().compareTo("if") == 0) {
			Node<Token> key = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(key);
			value_pointer++;
			// child_marker++;
			// counter++;

			// check for '('
			if (input.get(value_pointer).compareTo("(") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				// counter++;
				// child_marker++;
				parent.addChild(open);

				// check for expr
				Node<Token> expr = new Node<Token>("expr");
				boolean v = expr(expr);

				if (v != true) {
					value_pointer = temp;
					for (int i = child_marker; i < parent.getChildren().size(); i++) {
						parent.removeChild(i);
					}
					return false;
				} else {
					parent.addChild(expr);
					// value_pointer++;
					// counter++;
					// child_marker++;
					// check ')'

					if (input.get(value_pointer).compareTo(")") != 0) {
						value_pointer = temp;
						for (int i = child_marker; i < parent.getChildren().size(); i++) {
							parent.removeChild(i);
						}
						return false;
					} else {
						Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
						value_pointer++;
						// counter++;
						parent.addChild(close);
						// child_marker++;
						// check for 'then'
						if (input.get(value_pointer).compareTo("then") != 0) {
							for (int i = child_marker; i < parent.getChildren().size(); i++) {
								parent.removeChild(i);
							}
							value_pointer = temp;
							return false;
						} else {
							Node<Token> key2 = new Node<Token>(tokens.get(value_pointer), "terminal");
							parent.addChild(key2);
							value_pointer++;
							// counter++;
							// child_marker++;

							// check for statBlock
							Node<Token> statB = new Node<Token>("statBlock");
							boolean s = statBlock(statB);

							if (s != true) {
								for (int i = child_marker; i < parent.getChildren().size(); i++) {
									parent.removeChild(i);
								}
								value_pointer = temp;
								return false;
							} else {
								parent.addChild(statB);
								// child_marker++;
								// value_pointer++;
								// counter++;

								// check for 'else'

								if (input.get(value_pointer).compareTo("else") != 0) {
									for (int i = child_marker; i < parent.getChildren().size(); i++) {
										parent.removeChild(i);
									}
									value_pointer = temp;
									return false;
								} else {
									Node<Token> key3 = new Node<Token>(tokens.get(value_pointer), "terminal");
									parent.addChild(key3);
									value_pointer++;
									// counter++;
									// child_marker++;

									// check for statBlock
									Node<Token> statb = new Node<Token>("statBlock");
									s = statBlock(statb);

									if (s != true) {
										for (int i = child_marker; i < parent.getChildren().size(); i++) {
											parent.removeChild(i);
										}
										value_pointer = temp;
										return false;
									} else {
										parent.addChild(statb);
										// value_pointer++;
										// counter++;
										// child_marker++;
										// check for ';'

										if (input.get(value_pointer).compareTo(";") != 0) {
											value_pointer = temp;
											for (int i = child_marker; i < parent.getChildren().size(); i++) {
												parent.removeChild(i);
											}
											return false;
										} else {
											Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
											value_pointer++;
											// child_marker++;
											// counter++;
											parent.addChild(end);
											return true;
										}
									}
								}

							}
						}
					}
				}
			}
		}
		// check 'for'
		else if (current.getData().compareTo("for") == 0) {
			Node<Token> key = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(key);
			value_pointer++;
			// counter++;
			// child_marker++;
			// check for '('

			if (input.get(value_pointer).compareTo("(") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				// counter++;
				parent.addChild(open);
				// child_marker++;
				Node<Token> type = new Node<Token>("type");

				boolean t = type(type);

				if (t != true) {
					for (int i = child_marker; i < parent.getChildren().size(); i++) {
						parent.removeChild(i);
					}
					value_pointer = temp;
					return false;

				} else {
					parent.addChild(type);
					// value_pointer++;
					// counter++;
					// child_marker++;
					// check for 'id'

					if (input.get(value_pointer).toLowerCase().compareTo("id") != 0) {
						for (int i = child_marker; i < parent.getChildren().size(); i++) {
							parent.removeChild(i);
						}
						value_pointer = temp;
						return false;
					} else {
						Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");
						value_pointer++;
						// counter++;
						// child_marker++;
						parent.addChild(id);

						Node<Token> ao = new Node<Token>("assignOp");

						boolean a = assignOp(ao);

						if (a != true) {
							for (int i = child_marker; i < parent.getChildren().size(); i++) {
								parent.removeChild(i);
							}
							value_pointer = temp;
							return false;

						} else {
							parent.addChild(ao);
							// value_pointer++;
							// counter++;
							// child_marker++;

							// check for expr
							Node<Token> expr = new Node<Token>("expr");
							boolean v = expr(expr);

							if (v != true) {
								value_pointer = temp;
								for (int i = child_marker; i < parent.getChildren().size(); i++) {
									parent.removeChild(i);
								}
								return false;
							} else {
								parent.addChild(expr);
								// value_pointer++;
								// counter++;
								// child_marker++;
								// check for ';'
								if (input.get(value_pointer).compareTo(";") != 0) {
									value_pointer = temp;
									for (int i = child_marker; i < parent.getChildren().size(); i++) {
										parent.removeChild(i);
									}
									return false;
								} else {
									Node<Token> mid = new Node<Token>(tokens.get(value_pointer), "terminal");
									value_pointer++;
									// counter++;
									parent.addChild(mid);
									// child_marker++;
									// check for relExpr

									Node<Token> relexpr = new Node<Token>("relExpr");
									v = relExpr(relexpr);

									if (v != true) {
										value_pointer = temp;
										for (int i = child_marker; i < parent.getChildren().size(); i++) {
											parent.removeChild(i);
										}
										return false;
									} else {
										parent.addChild(relexpr);
										// value_pointer++;
										// counter++;
										// child_marker++;
										// check for ';'

										if (input.get(value_pointer).compareTo(";") != 0) {
											value_pointer = temp;
											for (int i = child_marker; i < parent.getChildren().size(); i++) {
												parent.removeChild(i);
											}
											return false;
										} else {
											Node<Token> last_for = new Node<Token>(tokens.get(value_pointer),
													"terminal");
											value_pointer++;
											// counter++;
											parent.addChild(last_for);
											// child_marker++;
											// check for assignStat
											Node<Token> ast = new Node<Token>("assignStat");
											v = assignStat(ast);

											if (v != true) {
												value_pointer = temp;
												for (int i = child_marker; i < parent.getChildren().size(); i++) {
													parent.removeChild(i);
												}
												return false;
											} else {
												parent.addChild(ast);
												// value_pointer++;
												// counter++;
												// child_marker++;
												// check for ')'

												if (input.get(value_pointer).compareTo(")") != 0) {
													value_pointer = temp;
													for (int i = child_marker; i < parent.getChildren().size(); i++) {
														parent.removeChild(i);
													}
													return false;
												} else {
													Node<Token> close = new Node<Token>(tokens.get(value_pointer),
															"terminal");
													value_pointer++;
													// counter++;
													parent.addChild(close);
													// child_marker++;
													// check for statBlock

													Node<Token> statb = new Node<Token>("statBlock");
													v = statBlock(statb);

													if (v != true) {
														value_pointer = temp;
														for (int i = child_marker; i < parent.getChildren()
																.size(); i++) {
															parent.removeChild(i);
														}
														return false;
													} else {
														parent.addChild(statb);
														// value_pointer++;
														// counter++;
														// child_marker++;

														if (input.get(value_pointer).compareTo(";") != 0) {
															value_pointer = temp;
															for (int i = child_marker; i < parent.getChildren()
																	.size(); i++) {
																parent.removeChild(i);
															}
															return false;
														} else {
															Node<Token> end = new Node<Token>(tokens.get(value_pointer),
																	"terminal");
															value_pointer++;
															// counter++;
															parent.addChild(end);
															return true;
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		// check 'read'
		else if (current.getData().compareTo("read") == 0) {
			Node<Token> key = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(key);
			value_pointer++;
			// counter++;
			// child_marker++;
			// check for '('
			if (input.get(value_pointer).compareTo("(") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				// counter++;
				parent.addChild(open);
				// child_marker++;
				// check for variable
				Node<Token> variable = new Node<Token>("variable");
				boolean v = variable(variable);

				if (v != true) {
					value_pointer = temp;
					for (int i = child_marker; i < parent.getChildren().size(); i++) {
						parent.removeChild(i);
					}
					return false;
				} else {
					parent.addChild(variable);
					// value_pointer++;
					// counter++;
					// child_marker++;
					// check ')'

					if (input.get(value_pointer).compareTo(")") != 0) {
						value_pointer = temp;
						for (int i = child_marker; i < parent.getChildren().size(); i++) {
							parent.removeChild(i);
						}
						return false;
					} else {
						Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
						value_pointer++;
						// counter++;
						parent.addChild(close);
						// child_marker++;
						// check for end
						if (input.get(value_pointer).compareTo(";") != 0) {
							value_pointer = temp;
							for (int i = child_marker; i < parent.getChildren().size(); i++) {
								parent.removeChild(i);
							}
							return false;
						} else {
							Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
							value_pointer++;
							// counter++;
							parent.addChild(end);
							return true;
						}
					}
				}
			}

		}
		// check 'write'
		else if (current.getData().compareTo("write") == 0) {
			Node<Token> key = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(key);
			value_pointer++;
			// child_marker++;
			// counter++;

			// check for '('
			if (input.get(value_pointer).compareTo("(") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				// counter++;
				parent.addChild(open);
				// child_marker++;
				// check for expr
				Node<Token> expr = new Node<Token>("expr");
				boolean v = expr(expr);

				if (v != true) {
					value_pointer = temp;
					for (int i = child_marker; i < parent.getChildren().size(); i++) {
						parent.removeChild(i);
					}
					return false;
				} else {
					parent.addChild(expr);
					// value_pointer++;
					// counter++;
					// child_marker++;
					// check ')'

					if (input.get(value_pointer).compareTo(")") != 0) {
						value_pointer = temp;
						for (int i = child_marker; i < parent.getChildren().size(); i++) {
							parent.removeChild(i);
						}
						return false;
					} else {
						Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
						value_pointer++;
						// counter++;
						parent.addChild(close);
						// child_marker++;
						// check for end
						if (input.get(value_pointer).compareTo(";") != 0) {
							value_pointer = temp;
							for (int i = child_marker; i < parent.getChildren().size(); i++) {
								parent.removeChild(i);
							}
							return false;
						} else {
							Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
							value_pointer++;
							// counter++;
							parent.addChild(end);
							return true;
						}
					}
				}
			}

		}
		// check 'return'
		else if (current.getData().compareTo("return") == 0) {
			Node<Token> key = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(key);
			value_pointer++;
			// counter++;
			// child_marker++;

			// check for '('
			if (input.get(value_pointer).compareTo("(") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				// child_marker++;
				// counter++;
				parent.addChild(open);

				// check for expr
				Node<Token> expr = new Node<Token>("expr");
				boolean v = expr(expr);
				current = tokens.get(value_pointer);
				value_pointer=value_pointer-1;

				if (v != true) {
					value_pointer = temp;
					for (int i = child_marker; i < parent.getChildren().size(); i++) {
						parent.removeChild(i);
					}
					return false;
				} else {
					parent.addChild(expr);
					current = tokens.get(value_pointer);
					// value_pointer++;
					// counter++;
					// child_marker++;
					// check ')'

					if (tokens.get(value_pointer+1).getData().compareTo(")") != 0) {
						value_pointer = temp;
						for (int i = child_marker; i < parent.getChildren().size(); i++) {
							parent.removeChild(i);
						}
						System.out.println("statement invalid");
						return false;
					} else {
						Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
						value_pointer++;
						Token lookahead = tokens.get(value_pointer+1);
						// counter++;
						parent.addChild(close);
						value_pointer++;
						// child_marker++;
						// check for end
						if (tokens.get(value_pointer).getData().compareTo(";") != 0) {
							value_pointer = temp;
							for (int i = child_marker; i < parent.getChildren().size(); i++) {
								parent.removeChild(i);
							}
							return false;
						} else {
							Node<Token> end = new Node<Token>(tokens.get(value_pointer), "terminal");
							value_pointer++;
							current = tokens.get(value_pointer);
							// counter++;
							parent.addChild(end);
							return true;
						}
					}
				}
			}

		} else
			return false;

	}

	// assignStat -> variable assignOp expr
	public boolean assignStat(Node<Token> parent) {
		is_comment();
		Token lookahead = tokens.get(value_pointer+1);
		
		
		// int counter=0;
		//int temp = value_pointer;
		//int child_marker = parent.getChildren().size();
		
		
		
		// check for variable

		Node<Token> var = new Node<Token>("variable");

		boolean v = variable(var);
		if (v != true) {
			return false;
		} else {
			parent.addChild(var);
			value_pointer-=1;
			lookahead = tokens.get(value_pointer+1);
			
			
			// check for assignOp

			boolean is_true = true;
			while(is_true) {
				if(FIRST_ASSIGNOP.contains(lookahead.getData())) {
					value_pointer++;
					
					break;
				}
				
				
				else {
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(FIRST_ASSIGNOP.contains(lookahead.getData())==false) {
						
					if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
						error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
						return false;
					}else {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						
					}
					
						
					}
					
					error_stream+= end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}
				
				
			}
			
			
			Node<Token> ao = new Node<Token>("assignOp");
			boolean a = assignOp(ao);
			
			if (a != true) {
				
				return false;
			} else {
				parent.addChild(ao);
				value_pointer-=1;
				lookahead = tokens.get(value_pointer+1);
				
				// check for expr
				is_true = true;
				while(is_true) {
					if(FIRST_EXPR.contains(lookahead.getType())|| FIRST_EXPR.contains(lookahead.getData())) {
						value_pointer++;
						
						break;
					}
					
					
					else {
						error_stream+= start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						
						
						while(FIRST_EXPR.contains(lookahead.getData())==false || FIRST_EXPR.contains(lookahead.getType())) {
							
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
						
							
						}
						
						error_stream+= end_error_rec(lookahead);
						System.out.println(end_error_rec(lookahead));
					}
					
					
				}
				Node<Token> ex = new Node<Token>("expr");
				boolean e = expr(ex);

				if (e != true) {
					
					return false;
				} else {
					parent.addChild(ex);
					// value_pointer++;
					return true;
				}
			}

		}
	}

	// '{' {statement} '}' | statement | EPSILON
	public boolean statBlock(Node<Token> parent) {
		is_comment();
		// int counter =0;
		int temp = value_pointer;
		int child_marker = parent.getChildren().size();
		
		Token lookahead = tokens.get(value_pointer+1);
		
		//check for epsilon aka check the case of FOLLOW
		if(FOLLOW_STATBLOCK.contains(input.get(value_pointer))) {
			value_pointer++;
			return true;
		}
		
		
		// check for '{'
		if (input.get(value_pointer).compareTo("{") == 0) {
			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(open);
			lookahead = tokens.get(value_pointer+1);
			// counter++;
			// child_marker++;

			boolean is_true = true;

			
			while (is_true) {
				//Node<Token> stat = new Node<Token>("statement");
				//boolean is_true2 = funcDef(stat);
				// adding child to tree if there was a function
				//if (is_true == true) {
				//	parent.addChild(stat);
					// child_marker++;
					//lookahead = tokens.get(value_pointer + 1);
				//}
				if(lookahead.getData().compareTo("}")==0) {
					value_pointer++;
					break;
				}

				// LOOKAHEAD CHECK/ERROR CHECK
				if (FIRST_STATEMENT.contains(lookahead.getData()) == false) {
					error_stream += start_error_rec(tokens.get(value_pointer));
					System.out.println(start_error_rec(tokens.get(value_pointer)));
					error_stream += "Invalid token to continue a program. Must continue with either a type, or \"main\". \n";

					// keep incrementing until we can find a value that is valid.
					while (FIRST_STATEMENT.contains(lookahead.getData()) == false) {
						// syn_errors.push(tokens.get(value_pointer));
						if (value_pointer == tokens.size()||value_pointer == tokens.size()-2) {
							error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
							return false;
						} else
							value_pointer++;

					}
					error_stream += end_error_rec(tokens.get(value_pointer));
					lookahead = tokens.get(value_pointer + 1);
				}else {
					
					Node<Token> state = new Node <Token> ("statement");
					boolean s = statement(state);
					
					if(s!=true)
						return false;
					else {
						value_pointer-=1;
						lookahead = tokens.get(value_pointer+1);
					}
					
					
				}

			}
			
			if (input.get(value_pointer).compareTo("}") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				parent.addChild(close);
				return true;
			}
		}
		// checking if there is only one statement
		// when we call the function it will be adding to the node so we
		else if (FIRST_STATEMENT.contains(input.get(value_pointer))) {
			Node <Token> stat = new Node<Token>("statement");
			boolean s = statement(stat);
			if(s!=true)
				return false;
			else {
				parent.addChild(stat);
				return true;
			}
			
			
			
		}
		// if none of these cases, return false
		else {
			return false;
		}

	}

	// arithExpr exprTail
	public boolean expr(Node<Token> parent) {
		is_comment();
		// int counter =0;
		Token lookahead = tokens.get(value_pointer + 1);

		// int temp = value_pointer;
		// int child_marker = parent.getChildren().size();
		Node<Token> ari = new Node<Token>("arithExpr");
		boolean a = arithExpr(ari);
		value_pointer -= 1;
		lookahead = tokens.get(value_pointer + 1);
		// checkk lookahead to make sure we can do the arith exp

		if (a != true)
			return false;

		// check for the tail now

		parent.addChild(ari);
		
		
		// value_pointer++;
		// counter++;

		// check exprTail
		boolean is_true =true;
		boolean test = FOLLOW_EXPRTAIL.contains(lookahead.getData());
		while (is_true) {
			// check for the follow property
			if (FOLLOW_EXPRTAIL.contains(lookahead.getData())) {
				// if contains follow then fParams = epsilon
				value_pointer++;
				lookahead = tokens.get(value_pointer+1);
				return true;
			}

			// then check for the first
			
			else if (FIRST_EXPRTAIL.contains(lookahead.getData())) {
				value_pointer++;
				lookahead = tokens.get(value_pointer+1);
				break;
			} else {
				error_stream += start_error_rec(lookahead);
				// increment the elements until we have a valid entry with the parsing.
				while(FOLLOW_EXPRTAIL.contains(lookahead.getData())== false && FIRST_EXPRTAIL.contains(lookahead.getData())== false)
				if (value_pointer == tokens.size() || value_pointer == tokens.size() - 1) {
					error_stream += "Cannot continue parsing for the moment as stream has reached the end and no valid token has been entered.";
				} else {
					value_pointer++;
					lookahead = tokens.get(value_pointer + 1);
				}
				error_stream += end_error_rec(tokens.get(value_pointer));

				
			}

		}
		Node<Token> tail = new Node<Token>("exprTail");
		a = exprTail(tail);

		if (a != true) {
			return false;
		} else {
			parent.addChild(tail);
			// value_pointer++;
			return true;
		}

	

	}
	//relOp arithExpr | EPSILON
	public boolean exprTail(Node<Token> parent) {
		is_comment();
		// int counter =0;
		//int temp = value_pointer;
		
		Token lookahead = tokens.get(value_pointer+1);
		
		
		Node<Token> r = new Node<Token>("relOp");
		if (relOp(r)) {
			parent.addChild(r);
			value_pointer-=1;
			lookahead = tokens.get(value_pointer+1);
			
			// counter++;

			//check lookahead for arithexpr
			
			
			boolean is_true = true;
			while(is_true) {
				if(FIRST_ARITHEXPR.contains(lookahead.getData())) {
				value_pointer++;
				lookahead = tokens.get(value_pointer+1);
				break;
					
				}else {
					
					error_stream +=start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					while(FIRST_ARITHEXPR.contains(lookahead.getData())== false) {
						
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);
							
						}
						
					}
					error_stream+=end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
					
				}	
			}
			
			Node<Token> ar = new Node<Token>("arithExpr");
			boolean ari = arithExpr(ar);
			if (ari == true) {
				parent.addChild(ar);
				return true;
			} else {
				return false;
			}

		} else
			return false;

	}

	// arithExpr relOp arithExpr
	public boolean relExpr(Node<Token> parent) {
		is_comment();
		// int counter =0;
		//int temp = value_pointer;
		Token lookahead = tokens.get(value_pointer+1);
		// check for arithExpr
		Node<Token> ari = new Node<Token>("arithExpr");
		boolean a = arithExpr(ari);

		if (a != true) {
			return false;
		} else {
			parent.addChild(ari);
			value_pointer-=1;
			lookahead = tokens.get(value_pointer+1);
			// value_pointer++;
			// counter++;

			// check from relOp with lookahead
			
			boolean is_true = true;
			
			while(is_true) {
				if(FIRST_RELOP.contains(lookahead.getData())) {
					//value_pointer becomes the lookahead. 
					value_pointer++;
					break;
				}else {
					error_stream +=start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					while(FIRST_RELOP.contains(lookahead.getData())== false) {
						
						if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
							error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
							return false;
						}else {
							value_pointer++;
							lookahead = tokens.get(value_pointer+1);							
						}	
					}
					
					error_stream +=end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				
				}				
			}
			
			Node<Token> rel = new Node<Token>("relOp");
			a = relOp(rel);
			//normally should not happen as we checked with the lookahead that this production should be used. 
			if (a != true) {
				//parent.removeChild(ari);
				//value_pointer = temp;
				return false;
			} else {
				parent.addChild(rel);
				value_pointer-=1;
				lookahead = tokens.get(value_pointer+1);
				// value_pointer++;
				// counter++;

				// check for arithExpr
				
				is_true = true;
				
				while(is_true) {
					if(FIRST_ARITHEXPR.contains(lookahead.getData())) {
						//value_pointer becomes the lookahead. 
						value_pointer++;
						break;
					}else {
						error_stream +=start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						
						while(FIRST_ARITHEXPR.contains(lookahead.getData())== false) {
							
							if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
								error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
								return false;
							}else {
								value_pointer++;
								lookahead = tokens.get(value_pointer+1);							
							}	
						}
						
						error_stream +=end_error_rec(lookahead);
						System.out.println(end_error_rec(lookahead));
					
					}				
				}
				
			Node<Token> ari2 = new Node<Token>("arithExpr");
				a = arithExpr(ari2);

				if (a != true) {
					
					return false;
				} else {
					parent.addChild(ari2);
					// value_pointer++;
					return true;
				}
			}
		}

	}

	// term arithExprTail
	public boolean arithExpr(Node<Token> parent) {
		is_comment();
		// int counter=0;
		int temp = value_pointer;
		Node<Token> term = new Node<Token>("term");
		boolean t = term(term);
		if (t != true) {
			return false;
		} else {
			parent.addChild(term);
			// value_pointer++;
			// counter++;

			// check for arithExprTail
			Node<Token> tail = new Node<Token>("arithExprTail");

			t = arithExprTail(tail);

			if (t != true) {
				parent.removeChild(term);
				value_pointer = temp;
				return false;
			} else {
				parent.addChild(tail);
				// value_pointer++;
				return true;
			}

		}
	}

	// addOp term arithExprTail| EPSILON
	public boolean arithExprTail(Node<Token> parent) {
		is_comment();
		Token lookahead = tokens.get(value_pointer+1);
		Token current = tokens.get(value_pointer);
		// int counter=0;
		int temp = value_pointer;
		if(FIRST_ADDOP.contains(current.getData())) {
		Node<Token> op = new Node<Token>("addOP");
		
		boolean a = addOp(op);
		if (a) {
			parent.addChild(op);
			// counter++;

			Node<Token> t = new Node<Token>("term");
			if (term(t)) {
				parent.addChild(t);
				// counter++;

				Node<Token> ar = new Node<Token>("arithExprTail");
				if (arithExprTail(ar)) {
					parent.addChild(ar);
					return true;
				} else {
					parent.removeChild(t);
					parent.removeChild(op);
					value_pointer = temp;
					return false;
				}

			} else {
				parent.removeChild(op);
				value_pointer = temp;
				return false;
			}

		} else return false;
		
		}
		//as we have epsilon, then if the next token does not match it only means that we
		//are moving on to the next one in the grammar
		else
			return true;

	}

	// '+' | '-'
	public boolean sign(Node<Token> parent) {
		is_comment();

		if (input.get(value_pointer).compareTo("+") == 0 || input.get(value_pointer).compareTo("-") == 0) {
			Node<Token> sign = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(sign);
			if (value_pointer != tokens.size())
				value_pointer++;
			else
				System.out.println("end of token stream");
			return true;

		} else
			return false;
	}

	// factorTail | 'integer'| 'float'| '(' arithExpr ')' | 'not' factor | sign
	// factor

	public boolean factor(Node<Token> parent) {
		is_comment();
		// int counter =0;
		int temp = value_pointer;
		Node<Token> first = new Node<Token>("factorTail");
		Node<Token> second = new Node<Token>("sign");
		Token lookahead = tokens.get(value_pointer+1);
		String in = input.get(value_pointer);
		Token current = tokens.get(value_pointer);
		// check tail
		if(FIRST_FACTORTAIL.contains(current.getType())||FIRST_FACTORTAIL.contains(current.getData())) {
		boolean t= factorTail(first);
		if(t) {	
		parent.addChild(first);
			return true;
		}else
			return false;
		}
		// check int
		else if (tokens.get(value_pointer).getType().compareTo("integer") == 0) {
			Node<Token> integer = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(integer);
			value_pointer++;
			return true;
		}
		// check float
		else if (in.compareTo("float") == 0) {
			Node<Token> fl = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(fl);
			value_pointer++;
			return true;
		}
		// check '('
		else if (in.compareTo("(") == 0) {
			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");

			parent.addChild(open);
			value_pointer++;
			// counter ++;

			Node<Token> ar = new Node<Token>("arithExpr");

			if (arithExpr(ar)) {
				parent.addChild(ar);
				// value_pointer++;
				// counter++;
				return true;

			} else {
				value_pointer = temp;
				parent.removeChild(open);
				return false;
			}

		}
		// check 'not'
		else if (in.compareTo("!") == 0) {
			Node<Token> not = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(not);
			value_pointer++;
			// counter++;

			Node<Token> fact = new Node<Token>("factor");

			if (factor(fact)) {
				parent.addChild(fact);
				// value_pointer++;

				return true;
			} else {

				parent.removeChild(fact);
				value_pointer = temp;
				return false;
			}
		}
		// check sign
		else if (FIRST_SIGN.contains(current.getData())&&(FIRST_FACTOR.contains(lookahead.getData())||FIRST_FACTOR.contains(lookahead.getType()))) {
			
			boolean t=sign(second);
			parent.addChild(second);
			// value_pointer++; -> the pointer is already upgraded when we did the function
			// call earlier
			// counter++;

			Node<Token> fact = new Node<Token>("factor");

			if (factor(fact)) {
				parent.addChild(fact);
				// value_pointer++;

				return true;
			} else {

				parent.removeChild(fact);
				value_pointer = temp;
				return false;
			}
		} else
			return false;

	}

	public boolean factorTail(Node<Token> parent) {
		is_comment();
		int child_marker = parent.getChildren().size();
		// int counter=0;
		Token lookahead = tokens.get(value_pointer+1);
		int temp = value_pointer;
		// {idnest}

		boolean is_true = true;
		if(FIRST_IDNEST.contains(lookahead.getType())) {
		while (is_true) {
			Node<Token> idnest = new Node<Token>("idnest");

			if (idnest(idnest)) {
				parent.addChild(idnest);
				// counter++;

			} else
				is_true = false;
		}
		}
		// 'id'
		if (tokens.get(value_pointer).getType().compareTo("id") != 0) {
			// reset the value_pointer so that once we restart recursive parsing we can
			// restart from the beginning (where we started before we process)
			for (int i = child_marker; i < parent.getChildren().size(); i++) {
				parent.removeChild(i);
			}
			value_pointer = temp;
			return false;
		} else {
			// adding id to tree
			Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");

			parent.addChild(id);
			value_pointer++;
			// counter++;

			// check for factor tailtail

			Node<Token> tail = new Node<Token>("factorTailTail");

			boolean check = factorTailTail(tail);

			if (check) {
				parent.addChild(tail);
				// counter++;
				return true;
			} else {
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				value_pointer = temp;
				return false;
			}
		}
	}

	public boolean factorTailTail(Node<Token> parent) {
		is_comment();
		// int counter=0;
		int temp = value_pointer;
		Token lookahead = tokens.get(value_pointer+1);
		Token current = tokens.get(value_pointer);
		// int child_marker = parent.getChildren().size();
		Node<Token> id = new Node<Token>("idnest");
		if(FIRST_IDNEST.contains(current.getType())) {
		boolean t = idnest(id);
			if (t) {
			parent.addChild(id);

			boolean is_true = true;

			while (is_true) {
				Node<Token> id2 = new Node<Token>("idnest");

				if (idnest(id2)) {
					parent.addChild(id2);
				} else
					is_true = false;

			}

			return true;
		}
		else
			return false;
		} else if (input.get(value_pointer).compareTo("(") == 0) {

			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
			value_pointer++;
			// counter++;

			Node<Token> ap = new Node<Token>("aParams");

			if (aParams(ap)) {
				parent.addChild(ap);
				// counter++;
				current = tokens.get(value_pointer);
				if (input.get(value_pointer).compareTo(")") == 0) {
					Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
					parent.addChild(close);
					value_pointer++;
					// counter++;
					return true;
				} else {
					parent.removeChild(ap);
					parent.removeChild(open);
					value_pointer = temp;
					return false;
				}

			} else {
				value_pointer = temp;
				parent.removeChild(open);
				return false;
			}

		} 
		//as we have a {indice} it means we might have 0 times an indice such that the tail is still true. 
		else
			return true;

	}

	public boolean term(Node<Token> parent) {
		is_comment();
		Token lookahead = tokens.get(value_pointer + 1);

		// int temp = value_pointer;
		// int counter=0;

		// check the first character for the term.
		boolean is_true = true;
		while (is_true) {
			if (FIRST_FACTOR.contains(tokens.get(value_pointer).getData())||FIRST_FACTOR.contains(tokens.get(value_pointer).getType())) {
				break;
			} else {
				error_stream += start_error_rec(tokens.get(value_pointer));
				System.out.println(start_error_rec(tokens.get(value_pointer)));

				while (FIRST_FACTOR.contains(tokens.get(value_pointer).getData()) == false) {

					if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}

				error_stream += end_error_rec(tokens.get(value_pointer));
				System.out.println(end_error_rec(tokens.get(value_pointer)));
			}
		}

		Node<Token> fa = new Node<Token>("factor");
		boolean fac = factor(fa);
		value_pointer -= 1;
		lookahead = tokens.get(value_pointer + 1);
		if (fac != true)
			return false;

		parent.addChild(fa);
		// counter++;
		Node<Token> tail = new Node<Token>("termTail");

		// check lookahead for termtail
		// as there is epsilon we need to check first for follow then the terms in the
		// first. (follow in case epsilon is there, first in case we do have a tail)

		is_true = true;

		while (is_true) {
			if (FOLLOW_TERMTAIL.contains(lookahead.getData())) {
				// increment the value pointer for the next function
				value_pointer++;
				return true;
			}

			if (FIRST_TERMTAIL.contains(lookahead.getData())) {
				value_pointer++;
				lookahead = tokens.get(value_pointer + 1);
				break;
			} else {
				error_stream += start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));

				while (FOLLOW_TERMTAIL.contains(lookahead.getData()) == false
						&& FIRST_TERMTAIL.contains(lookahead.getData())) {
					if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}

				error_stream += end_error_rec(lookahead);
				System.out.println(end_error_rec(lookahead));

			}
		}

		boolean term = termTail(tail);

		if (term == true) {
			parent.addChild(tail);
			return true;
		} else
			return false;

	}

	// multOp factor termTail | EPSILON
	public boolean termTail(Node<Token> parent) {
		is_comment();
		Token lookahead = tokens.get(value_pointer + 1);

		// int counter =0;
		// int temp = value_pointer;
		Node<Token> op = new Node<Token>("multOP");
		//for epsilon
		if(FOLLOW_TERMTAIL.contains(lookahead.getData())) {
			return true;
		}
		// this function is not called unless multOp is there as first of multop is the
		// same as first termtail but for term tail we add epsilon

		boolean mu = multOp(op);
		if (mu == false)
			return false;
		parent.addChild(op);
		value_pointer -= 1;
		lookahead = tokens.get(value_pointer + 1);

		Node<Token> fac = new Node<Token>("factor");

		// check lookahead for factor

		boolean is_true = true;

		while (is_true) {
			if (FIRST_FACTOR.contains(lookahead.getData())||FIRST_FACTOR.contains(lookahead.getType())) {
				// increment the value pointer for the next function
				value_pointer++;
				lookahead = tokens.get(value_pointer + 1);
				//return true;
			}

			else {
				error_stream += start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));

				while (FIRST_FACTOR.contains(lookahead.getData())) {
					if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}

				error_stream += end_error_rec(lookahead);
				System.out.println(end_error_rec(lookahead));

			}
		}

		boolean fa = factor(fac);

		if (fa == false)
			return false;

		parent.addChild(fac);
		value_pointer -= 1;
		lookahead = tokens.get(value_pointer + 1);

		Node<Token> term_tail = new Node<Token>("termTail");

		// check for termtail symbols in lookahead
		is_true = true;

		while (is_true) {
			if (FOLLOW_TERMTAIL.contains(lookahead.getData())) {
				// increment the value pointer for the next function
				value_pointer++;
				return true;
			}

			if (FIRST_TERMTAIL.contains(lookahead.getData())) {
				value_pointer++;
				lookahead = tokens.get(value_pointer + 1);
				break;
			} else {
				error_stream += start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));

				while (FOLLOW_TERMTAIL.contains(lookahead.getData()) == false
						&& FIRST_TERMTAIL.contains(lookahead.getData())) {
					if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}

				error_stream += end_error_rec(lookahead);
				System.out.println(end_error_rec(lookahead));

			}
		}

		boolean term = termTail(term_tail);

		if (term == true) {
			parent.addChild(term_tail);
			return true;
		} else
			return false;

	}

	// variable -> {idnest} 'id' {indice}
	public boolean variable(Node<Token> parent) {
		is_comment();
		int temp = value_pointer;
		Token lookahead = tokens.get(value_pointer+1);
		int child_marker = parent.getChildren().size();
		//Token lookahead = tokens.get(value_pointer+1);
		
		// int counter=0;

		// {idnest}

		
		if (tokens.get(value_pointer).getType().compareTo("id") == 0) {
			// we know we start with id no matter what, now we check with

		} else
			return false;

		boolean is_true = true;
		if(FIRST_IDNESTTAIL.contains(lookahead.getData())) {
		while (is_true) {
			Node<Token> idnest = new Node<Token>("idnest");

			if (idnest(idnest)) {
				parent.addChild(idnest);
				child_marker++;
				// counter++;

			} else {
				is_true = false;
				value_pointer=temp;
				int size = parent.getChildren().size();
				for(int i=0;i<child_marker;i++) {
				parent.removeChild(size-i);
				}
			}
		}
		}

		// 'id'
		if (tokens.get(value_pointer).getType().compareTo("id") != 0) {
			// reset the value_pointer so that once we restart recursive parsing we can
			// restart from the beginning (where we started before we process)
			for (int i = child_marker; i < parent.getChildren().size(); i++) {
				parent.removeChild(i);
			}
			value_pointer = temp;
			return false;
		} else {
			// adding id to tree
			Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");

			parent.addChild(id);
			value_pointer++;
			// counter++;
			if(FIRST_INDICE.contains(tokens.get(value_pointer).getData())) {
			is_true = true;

			while (is_true) {
				Node<Token> indice = new Node<Token>("indice");

				if (indice(indice)) {
					parent.addChild(indice);
					// counter++;

				} else
					is_true = false;
			}
			
			}

			return true;
		}

	}

	public boolean functionCall(Node<Token> parent) {
		is_comment();
		// int counter=0;
		int temp = value_pointer;
		Token lookahead = tokens.get(value_pointer+1);
		int child_marker = parent.getChildren().size();
		// {idnest}

		boolean is_true = true;
		
		
		while (is_true) {
			Node<Token> idnest = new Node<Token>("idnest");

			if (idnest(idnest)) {
				parent.addChild(idnest);
				// counter++;

			} else
				is_true = false;
		}

		// 'id'
		if (input.get(value_pointer).toLowerCase().compareTo("id") != 0) {
			// reset the value_pointer so that once we restart recursive parsing we can
			// restart from the beginning (where we started before we process)
			for (int i = child_marker; i < parent.getChildren().size(); i++) {
				parent.removeChild(i);
			}
			value_pointer = temp;
			return false;
		} else {
			// adding id to tree
			Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");

			parent.addChild(id);
			value_pointer++;
			// counter++;

			// '('

			if (input.get(value_pointer).compareTo("(") != 0) {
				value_pointer = temp;
				for (int i = child_marker; i < parent.getChildren().size(); i++) {
					parent.removeChild(i);
				}
				return false;
			} else {
				Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
				value_pointer++;
				// counter++;
				parent.addChild(open);

				// check for fParams
				Node<Token> aParams = new Node<Token>("aParams");
				boolean param = fParams(aParams);

				if (param != true) {
					value_pointer = temp;
					for (int i = child_marker; i < parent.getChildren().size(); i++) {
						parent.removeChild(i);
					}
					return false;
				} else {
					parent.addChild(aParams);
					// counter++;

					// check ')'

					if (input.get(value_pointer).compareTo(")") != 0) {
						value_pointer = temp;
						for (int i = child_marker; i < parent.getChildren().size(); i++) {
							parent.removeChild(i);
						}
						return false;
					} else {
						Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
						value_pointer++;
						// counter++;
						parent.addChild(close);
						return true;
					}

				}
			}
		}

	}
//idnest -> 'id' idnestTail
	public boolean idnest(Node<Token> parent) {
		is_comment();
		// int counter =0;
		Token lookahead = tokens.get(value_pointer+1);
		
		
		
		int temp = value_pointer;
		//String in = input.get(value_pointer);

		
			Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(id);
			
			//check the lookahead
			
			boolean is_true = true;
			while(is_true) {
				if(FIRST_IDNESTTAIL.contains(lookahead.getData())) {
					value_pointer++;
					break;
				}
				
				
				else {
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(FIRST_IDNESTTAIL.contains(lookahead.getData())== false) {
						
					if(value_pointer == tokens.size()-2 || value_pointer == tokens.size()-1) {
						error_stream+="Parsing error. Reaching end of token stream and no corresponding syntax.\n";
						return false;
					}else {
						value_pointer++;
						lookahead = tokens.get(value_pointer+1);
						
					}			
					}
					
					error_stream+= end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}
				
				
			}
			// counter++;

			Node<Token> tail = new Node<Token>("idnestTail");

			boolean i = idnestTail(tail);

			if (i == true) {
				parent.addChild(tail);
				// value_pointer++;
				return true;

			} else {

			//parent.removeChild(id);
				value_pointer = temp;
				return false;
			}

		} 

	// {indice} '.' | '(' aParams ')' '.'
	public boolean idnestTail(Node<Token> parent) {
		is_comment();
		// int counter =0;
		//int temp = value_pointer;
		//int child_marker = parent.getChildren().size();
		
		Token lookahead = tokens.get(value_pointer+1);

		//check which LHS we should use. (indice or a params. )'		
		boolean is_true = true;
		boolean indice = false;
		boolean param = false;
		if (lookahead.getData().compareTo("=")==0) 
			return false;
		
		while(is_true) {
			//if there is no indice, then the tail is only the "."
			if(input.get(value_pointer).compareTo(".")==0) {
				Node<Token> point = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(point);
				value_pointer++;
				return true;
			}
			
			else if(FIRST_INDICE.contains(input.get(value_pointer))) {
				indice = true;
				//we are on the first value, if we want to be able to call onto indice(param) then we need to keep the pointer there
				break;				
			}
			//aoarams LHS
			else if (input.get(value_pointer).compareTo("[")==0) {
				param = true;
				break;
				
			}
			//neither, which means that we are going to keep skipping until we find either ".", first indice or first
			else {
				error_stream+= start_error_rec(tokens.get(value_pointer));
				System.out.println(start_error_rec(tokens.get(value_pointer)));
				
				
				while(input.get(value_pointer).compareTo(".")!=0 && FIRST_INDICE.contains(input.get(value_pointer))==false && input.get(value_pointer).compareTo("(")!=0)
				{
					
					if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}
				error_stream+=end_error_rec(tokens.get(value_pointer));
				System.out.println(end_error_rec(tokens.get(value_pointer)));		
			}
		}
		
		
		if(indice == true) {
		Node<Token> ind = new Node<Token>("indice");
		boolean i = indice(ind);
		value_pointer-=1;
		lookahead = tokens.get(value_pointer+1);
		
		
			
		//now keep checking for indice or "." 
		
		if(i!=true)
			return false;
		parent.addChild(ind);
		
		is_true = true;
		while (is_true) {
			if(FIRST_INDICE.contains(lookahead.getData())) {
				Node<Token> in = new Node<Token>("indice");
				boolean indice_ = indice(in);
				
				
				if(indice_!=true)
					return false;
				
				parent.addChild(in);
				
				value_pointer-=1;
				lookahead = tokens.get(value_pointer+1);
			}else if (lookahead.getData().compareTo(".")==0) {
				Node<Token> point = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(point);
				value_pointer++;
				return true;
			}else if (lookahead.getData().compareTo("=")==0) 
				return false;
			
			//if lookahead is neither, we skip until we either get another indice or we get the end of function "."
			else {
				error_stream+= start_error_rec(tokens.get(value_pointer));
				System.out.println(start_error_rec(tokens.get(value_pointer)));
				
				
				while(lookahead.getData().compareTo(".")!=0 && FIRST_INDICE.contains(lookahead.getData())==false)
				{
					
					if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}
				error_stream+=end_error_rec(tokens.get(value_pointer));
				System.out.println(end_error_rec(tokens.get(value_pointer)));		
				is_true = true;
				}
			}
				return  true;
				
		}else if (param == true) {
			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(open);
			
			lookahead = tokens.get(value_pointer+1);
			
			
			//now we check with lookahead on if conditions for aparams are met
			
			
			is_true = true;
			
			while(is_true) {
				//if the follow is present, then there is no aparms, but epsilon. 
				if(FOLLOW_APARAMS.contains(lookahead.getData())) {
					Node<Token> close = new Node<Token>(lookahead, "terminal");
					value_pointer++;
					lookahead = tokens.get(value_pointer+1);
					// counter++;
					parent.addChild(close);
					is_true = false;
					
				}
				else if (FIRST_APARAMS.contains(lookahead.getData())) {
					value_pointer++;
					
					Node<Token> aparams = new Node<Token>("aParams");
					boolean i = aParams(aparams);
					
					if(i!=true)
						return false;
					
					parent.addChild(aparams);
					value_pointer-=1;
					lookahead = tokens.get(value_pointer+1);
					
				}
				else{
					
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(FOLLOW_APARAMS.contains(lookahead.getData())==false && FIRST_APARAMS.contains(lookahead.getData())==false)
					{
						
						if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
							error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
							return false;
						} else {
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
						}
					}
					error_stream+=end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));		
					
					}
					
			}
				
			
			is_true = true;
			
			
			//check lookahead for the "."
			
			while(is_true) {
				//if there is no indice, then the tail is only the "."
				if(lookahead.getData().compareTo(".")==0) {
					
					value_pointer++;
					break;
				}
				else {
					error_stream+= start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					
					
					while(lookahead.getData().compareTo(".")!=0)
					{
						
						if (value_pointer == tokens.size() - 2 || value_pointer == tokens.size() - 1) {
							error_stream += "Parsing can go no further. End of token stream reached and no match to a valid syntax.";
							return false;
						} else {
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
						}
					}
					error_stream+=end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));		
				}
			}
			
			Node<Token> point = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(point);
			
			return true;
			
	//		else if (lookahead.getData().compareTo(".")==0) {
	//			Node<Token> point = new Node<Token>(tokens.get(value_pointer), "terminal");
	//			parent.addChild(point);
	//			value_pointer++;
	//			return true;	
		//	}
			
			
			
		}else
			return false;

	}

	public boolean indice(Node<Token> parent) {
		is_comment();
		// int counter=0;
		// int temp = parent.getChildren().size();
		// String in = input.get(value_pointer);

		Token lookahead = tokens.get(value_pointer + 1);

		// String in = input.get(value_pointer);

		if (input.get(value_pointer).compareTo("[") == 0) {
			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(open);

			// value_pointer++;
			// counter++;

			// check for 'integer'
			// check lookahead for integer

			boolean is_true = true;

			while (is_true) {
				if (FIRST_ARITHEXPR.contains(lookahead.getData()) || FIRST_ARITHEXPR.contains(lookahead.getType())) {
					// is_true = false;
					value_pointer++;
					lookahead = tokens.get(value_pointer + 1);

					break;
				} else {
					error_stream += start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));

					while (FIRST_ARITHEXPR.contains(lookahead.getData()) == false) {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}

					error_stream += end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));

					// point to the value of the lookahead to use it in the parsing
					// value_pointer++;
					// lookahead = tokens.get(value_pointer+1);
				}

			}

			Node<Token> arith = new Node<Token>("arithExpr");
			if (arithExpr(arith)) {
				parent.addChild(arith);
				value_pointer -= 1;
				lookahead = tokens.get(value_pointer + 1);
			} else
				return false;
			// counter++;

			// check for ']' for the lookahead

			is_true = true;
			while (is_true) {
				if (lookahead.getData().compareTo("]") == 0) {
					value_pointer++;
					break;
				}

				else {
					error_stream += start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));

					while (lookahead.getData().compareTo("]") != 0) {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);

					}

					error_stream += end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}

			}

			Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(close);
			value_pointer++;
			return true;

		} else
			return false;
	}

	/*
	 * if (in.compareTo("[") == 0) { Node<Token> open = new
	 * Node<Token>(tokens.get(value_pointer), "terminal"); parent.addChild(open);
	 * 
	 * value_pointer++; // counter++;
	 * 
	 * // check for arithExpr Node<Token> tail = new Node<Token>("arithExprTail");
	 * 
	 * boolean t = arithExprTail(tail);
	 * 
	 * if (t != true) { parent.removeChild(open); value_pointer = temp; return
	 * false; } else { parent.addChild(tail); value_pointer++; // counter++;
	 * 
	 * // check for "]"
	 * 
	 * in = input.get(value_pointer);
	 * 
	 * if (in.compareTo("]") == 0) { Node<Token> close = new
	 * Node<Token>(tokens.get(value_pointer), "terminal"); parent.addChild(close);
	 * value_pointer++; return true;
	 * 
	 * } else { parent.removeChild(open); parent.removeChild(tail); value_pointer =
	 * temp; return false; }
	 * 
	 * }
	 * 
	 * } else { return false; } }
	 */

	public boolean arraySize(Node<Token> parent) {
		is_comment();
//		int counter =0;
		// int temp = value_pointer;
		Token lookahead = tokens.get(value_pointer + 1);

		// String in = input.get(value_pointer);

		if (input.get(value_pointer).compareTo("[") == 0) {
			Node<Token> open = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(open);

			// value_pointer++;
			// counter++;

			// check for 'integer'
			// check lookahead for integer

			boolean is_true = true;

			while (is_true) {
				if (lookahead.getType().compareTo("integer") == 0) {
					// is_true = false;
					value_pointer++;
					lookahead = tokens.get(value_pointer + 1);

					break;
				} else {
					error_stream += start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));

					while (lookahead.getType().compareTo("integer") != 0) {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}

					error_stream += end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));

					// point to the value of the lookahead to use it in the parsing
					// value_pointer++;
					// lookahead = tokens.get(value_pointer+1);
				}

			}

			Node<Token> type = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(type);

			// counter++;

			// check for ']' for the lookahead

			is_true = true;
			while (is_true) {
				if (lookahead.getData().compareTo("]") == 0) {
					value_pointer++;
					break;
				}

				else {
					error_stream += start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));

					while (lookahead.getData().compareTo("]") != 0) {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);

					}

					error_stream += end_error_rec(lookahead);
					System.out.println(end_error_rec(lookahead));
				}

			}
			Node<Token> close = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(close);
			value_pointer++;
			return true;

		} else
			return false;
	}

	// type -> 'integer' | 'float' | 'id'
	public boolean type(Node<Token> parent) {
		is_comment();
		String in = input.get(value_pointer);

		if (in.compareTo("integer") == 0 || in.compareTo("float") == 0 || in.compareTo("id") == 0) {
			Node<Token> val = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(val);
			if (value_pointer != tokens.size())
				value_pointer++;
			else
				System.out.println("end of token stream");
			return true;
		} else
			return false;
	}
	// fParams -> type 'id' {arraySize} {fParamsTail} | EPSILON

	public boolean fParams(Node<Token> parent) {
		is_comment();
		// int temp = value_pointer;
		Token lookahead = tokens.get(value_pointer + 1);
		// int counter =0;
		boolean is_true = true;
		Node<Token> t = new Node<Token>("type");

		while (is_true) {
			// check for the follow property
			if (FOLLOW_FPARAMS.contains(tokens.get(value_pointer).getData())) {
				// if contains follow then fParams = epsilon
				return true;
			}

			// then check for the first
			else if (FIRST_FPARAMS.contains(tokens.get(value_pointer).getData())) {
				error_stream += end_error_rec(tokens.get(value_pointer));

				break;
			} else {
				error_stream += start_error_rec(tokens.get(value_pointer));
				// increment the elements until we have a valid entry with the parsing.

				if (value_pointer == tokens.size() || value_pointer == tokens.size() - 1) {
					error_stream += "Cannot continue parsing for the moment as stream has reached the end and no valid token has been entered.";
				} else {
					value_pointer++;
					lookahead = tokens.get(value_pointer + 1);
				}
			}

		}

		if (type(t)) {
			parent.addChild(t);
			value_pointer -= 1;
			lookahead = tokens.get(value_pointer + 1);
			// counter++;
			// check look ahead

			while (lookahead.getType().compareTo("id") != 0) {
				error_stream += start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));
				error_stream += "Invalid token to continue a program. Must continue with either expecting an id" + "\n";

				// keep incrementing until we can find a value that is valid.
				while (lookahead.getType().compareTo("id") != 0) {
					// syn_errors.push(tokens.get(value_pointer));

					if (value_pointer == tokens.size() || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer);
					}
				}
				error_stream += end_error_rec(tokens.get(value_pointer));
				lookahead = tokens.get(value_pointer + 1);
			}

			// if (input.get(value_pointer).toLowerCase().compareTo("id") != 0) {
			// parent.removeChild(t);
			// value_pointer = temp;
			// return false;
			// }

			Node<Token> id = new Node<Token>(lookahead, "terminal");
			// value at pointer becomes the lookahead and the lookahead is increased
			value_pointer++;
			lookahead = tokens.get(value_pointer + 1);
			// arent.addChild(id);

			is_true = true;
			// check for {arraySize}
			// check for the follow incase no array size and no other parameters are
			// entered.
			// if follow is there then it means we do not need to go further

			boolean array = false;
			boolean paramsTail = false;
			// if we are still here then we check for the arraysize first
			while (is_true) {
				if (FOLLOW_FPARAMS.contains(lookahead.getData())) {
					value_pointer++;
					lookahead = tokens.get(value_pointer + 1);
					return true;
				} else if (FIRST_ARRAYSIZE.contains(lookahead.getData())) {
					value_pointer++;
					lookahead = tokens.get(value_pointer + 1);
					array = true;
					break;
				}
				// check for the fparams tail
				else if (FIRST_FPARAMSTAIL.contains(lookahead.getData())) {
					paramsTail = true;
					value_pointer++;
					lookahead = tokens.get(value_pointer + 1);
					break;
				}

				// what to do if neither part of arraysize of
				else {
					error_stream += start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					boolean keepgoing = true;
					while (keepgoing) {
						if (FIRST_ARRAYSIZE.contains(lookahead.getData())
								|| FIRST_FPARAMSTAIL.contains(lookahead.getData())
								|| FOLLOW_FPARAMS.contains(lookahead.getData())) {
							error_stream += end_error_rec(lookahead);
							keepgoing = false;
						} else {
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
						}
					}
				}
			}

			// reset the value of the boolean
			is_true = true;

			// array size any amount of times
			if (array = true) {
				while (is_true) {
					Node<Token> as = new Node<Token>("arraySize");
					// as the current value is not the vallue of the lookahead, increase the pointer
					// value_pointer++;
					parent.addChild(as);

					value_pointer -= 1;
					lookahead = tokens.get(value_pointer + 1);

					// check if there is another array size, if follow set or if tail
					boolean inner = true;
					array = false;
					paramsTail = false;

					while (inner) {
						if (FOLLOW_FPARAMS.contains(lookahead.getData())) {
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
							return true;
						} else if (FIRST_ARRAYSIZE.contains(lookahead.getData())) {
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
							array = true;
							break;
						}
						// check for the fparams tail
						else if (FIRST_FPARAMSTAIL.contains(lookahead.getData())) {
							paramsTail = true;
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
							break;
						}

						// what to do if neither part of arraysize of
						else {
							error_stream += start_error_rec(lookahead);
							System.out.println(start_error_rec(lookahead));
							boolean keepgoing = true;
							while (keepgoing) {
								if (FIRST_ARRAYSIZE.contains(lookahead.getData())
										|| FIRST_FPARAMSTAIL.contains(lookahead.getData())
										|| FOLLOW_FPARAMS.contains(lookahead.getData())) {
									error_stream += end_error_rec(lookahead);
									keepgoing = false;
								} else {
									value_pointer++;
									lookahead = tokens.get(value_pointer + 1);
								}
							}
						}
					}

					if (array == true) {
						is_true = true;
					} else if (paramsTail = true)
						is_true = false;
				}

				is_true = true;

				while (is_true) {
					Node<Token> ftail = new Node<Token>("fParamsTail");
					fParamsTail(ftail);
					parent.addChild(ftail);
					value_pointer -= 1;
					lookahead = tokens.get(value_pointer + 1);

					// check for next tail or follow
					boolean inner = true;
					array = false;
					paramsTail = false;

					while (inner) {
						if (FOLLOW_FPARAMS.contains(lookahead.getData())) {
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
							return true;
						}
						// check for the fparams tail
						else if (FIRST_FPARAMSTAIL.contains(lookahead.getData())) {
							paramsTail = true;
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
							break;
						}

						// what to do if neither part of arraysize of
						else {
							error_stream += start_error_rec(lookahead);
							System.out.println(start_error_rec(lookahead));
							boolean keepgoing = true;
							while (keepgoing) {
								if (FIRST_FPARAMSTAIL.contains(lookahead.getData())
										|| FOLLOW_FPARAMS.contains(lookahead.getData())) {
									error_stream += end_error_rec(lookahead);
									keepgoing = false;
								} else {
									value_pointer++;
									lookahead = tokens.get(value_pointer + 1);
								}
							}
						}
					}
				}
				return true;
			}

			// the case that there is no array size but that there is a tail.
			else if (paramsTail == true) {
				is_true = true;

				while (is_true) {
					Node<Token> ftail = new Node<Token>("fParamsTail");
					fParamsTail(ftail);
					parent.addChild(ftail);
					value_pointer -= 1;
					lookahead = tokens.get(value_pointer + 1);

					// check for next tail or follow
					boolean inner = true;
					array = false;
					paramsTail = false;

					while (inner) {
						if (FOLLOW_FPARAMS.contains(lookahead.getData())) {
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
							return true;
						}
						// check for the fparams tail
						else if (FIRST_FPARAMSTAIL.contains(lookahead.getData())) {
							paramsTail = true;
							value_pointer++;
							lookahead = tokens.get(value_pointer + 1);
							break;
						}

						// what to do if neither part of arraysize of
						else {
							error_stream += start_error_rec(lookahead);
							System.out.println(start_error_rec(lookahead));
							boolean keepgoing = true;
							while (keepgoing) {
								if (FIRST_FPARAMSTAIL.contains(lookahead.getData())
										|| FOLLOW_FPARAMS.contains(lookahead.getData())) {
									error_stream += end_error_rec(lookahead);
									keepgoing = false;
								} else {
									value_pointer++;
									lookahead = tokens.get(value_pointer + 1);
								}
							}
						}
					}
				}
				return true;
			} else
				return false;
		} else
			return false;

	}

	// aParams -> expr {aParamsTail} | EPSILON
	public boolean aParams(Node<Token> parent) {
		is_comment();
		Token lookahead = tokens.get(value_pointer + 1);

		// int counter=0;
		// int temp = value_pointer;
		Node<Token> exp = new Node<Token>("expr");

		boolean ex = expr(exp);

		if (ex) {
			parent.addChild(exp);
			// as we just exited a function, the next token to look at is the one of the
			// value pointer and as such for simplicities sake
			// we are going to put the "lookahead" as the current value of the pointer.

			lookahead = tokens.get(value_pointer);

			// check for either follow value or for a x number of time the value of a params
			// tail
			boolean is_true = true;

			while (is_true) {

				if (FOLLOW_APARAMS.contains(lookahead.getData())) {
					// AS WE ARE GETTING A MEMBER OF THE FOLLOW SET THEN WE END THE THINGS HERE ARE
					// WE ARE NOW IN THE FOLLOW SET.
				//	value_pointer++;
					return true;
				}

				// if not a member of follow nor arraysize
				else if (FIRST_APARAMSTAIL.contains(lookahead.getData()) == false
						&& FOLLOW_APARAMS.contains(lookahead.getData()) == false) {
					error_stream += start_error_rec(lookahead);
					System.out.println(start_error_rec(lookahead));
					error_stream += "Invalid token to continue a program. Must continue with either expecting one of the following symbols "
							+ FIRST_APARAMSTAIL.toString() + FOLLOW_APARAMS.toString() + "\n";

					// keep incrementing until we can find a value that is valid.
					while (FIRST_APARAMSTAIL.contains(lookahead.getData()) == false
							&& FOLLOW_APARAMS.contains(lookahead.getData()) == false) {
						// syn_errors.push(tokens.get(value_pointer));

						if (value_pointer == tokens.size() || value_pointer == tokens.size() - 1) {
							error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
							return false;
						} else if (FOLLOW_APARAMS.contains(tokens.get(value_pointer + 1).getData())) {
							value_pointer++;
							return true;
						} else {
							value_pointer++;
							lookahead = tokens.get(value_pointer);
						}
					}
					error_stream += end_error_rec(tokens.get(value_pointer));
					lookahead = tokens.get(value_pointer + 1);
				}

				Node<Token> aP = new Node<Token>("aParamsTail");
				boolean a = aParamsTail(aP);
				if (a) {
					parent.addChild(aP);
				} else {
					parent.removeChild(exp);
					// value_pointer = temp;
					is_true = false;
				}
			}
			return true;
		} else {
			return true;
		}
	}

	// fParamsTail -> ',' type 'id' {arraySize}
	public boolean fParamsTail(Node<Token> parent) {
		is_comment();
		// int counter=0;
		// int temp = value_pointer;
		// int child_marker=parent.getChildren().size();
		String in = input.get(value_pointer);
		Token lookahead = tokens.get(value_pointer + 1);

		// check that first var is appropriate for this function. (double check)
		if (in.compareTo(",") == 0) {
			Node<Token> coma = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(coma);
			// value_pointer++;
			// child_marker++;
			// counter++;
			// temp ++;

			// LOOKAHEAD CHECK
			if (FIRST_TYPE.contains(lookahead.getData()) == false) {
				error_stream += start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));
				error_stream += "Invalid token to continue a program. Must continue with either expecting one of the following symbols "
						+ FIRST_EXPR.toString() + "\n";

				// keep incrementing until we can find a value that is valid.
				while (FIRST_TYPE.contains(lookahead.getData()) == false) {
					// syn_errors.push(tokens.get(value_pointer));

					if (value_pointer == tokens.size() || value_pointer == tokens.size() - 1) {
						error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}
				error_stream += end_error_rec(tokens.get(value_pointer));
				lookahead = tokens.get(value_pointer + 1);
			}

			Node<Token> type = new Node<Token>("type");
			boolean t = type(type);

			if (t != true) {
				// parent.removeChild(coma);
				// value_pointer = temp;
				return t;
			} else {
				parent.addChild(type);
				lookahead = tokens.get(value_pointer + 1);

				// value_pointer++;
				// child_marker++;;

				// check for an id
				if (tokens.get(value_pointer).getType().compareTo("id") != 0) {
					error_stream += start_error_rec(tokens.get(value_pointer));
					System.out.println(start_error_rec(tokens.get(value_pointer)));
					error_stream += "Invalid token to continue a program. Must continue with either expecting one of the following symbols "
							+ FIRST_EXPR.toString() + "\n";

					// keep incrementing until we can find a value that is valid.
					while (tokens.get(value_pointer).getType().compareTo("id") != 0) {
						// syn_errors.push(tokens.get(value_pointer));

						if (value_pointer == tokens.size()) {
							error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
							return false;
						} else
							value_pointer++;
					}
					error_stream += end_error_rec(tokens.get(value_pointer));
					lookahead = tokens.get(value_pointer + 1);
				}

				Node<Token> id = new Node<Token>(tokens.get(value_pointer), "terminal");
				parent.addChild(id);
				// value_pointer++;
				// child_marker++;
				// temp = value_pointer;

				// check for arraySize from 0 to n times
				// if array 0 times

				boolean is_true = true;
				while (is_true) {

					if (FOLLOW_FPARAMS.contains(lookahead.getData())) {
						// AS WE ARE GETTING A MEMBER OF THE FOLLOW SET THEN WE END THE THINGS HERE ARE
						// WE ARE NOW IN THE FOLLOW SET.
						value_pointer++;
						return true;
					}

					// if not a member of follow nor arraysize
					else if (FIRST_ARRAYSIZE.contains(lookahead.getData()) == false
							&& FOLLOW_FPARAMS.contains(lookahead.getData()) == false) {
						error_stream += start_error_rec(lookahead);
						System.out.println(start_error_rec(lookahead));
						error_stream += "Invalid token to continue a program. Must continue with either expecting one of the following symbols "
								+ FIRST_ARRAYSIZE.toString() + FOLLOW_FPARAMS.toString() + "\n";

						// keep incrementing until we can find a value that is valid.
						while (FIRST_ARRAYSIZE.contains(lookahead.getData()) == false
								&& FOLLOW_FPARAMS.contains(lookahead.getData()) == false) {
							// syn_errors.push(tokens.get(value_pointer));

							if (value_pointer == tokens.size() || value_pointer == tokens.size() - 1) {
								error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
								return false;
							} else if (FOLLOW_FPARAMS.contains(tokens.get(value_pointer + 2).getData())) {
								value_pointer += 2;
								return true;
							} else {
								value_pointer++;
								lookahead = tokens.get(value_pointer + 1);

							}
						}
						error_stream += end_error_rec(tokens.get(value_pointer));
						lookahead = tokens.get(value_pointer + 1);
					}

					Node<Token> arr = new Node<Token>("arraySize");

					boolean as = arraySize(arr);

					if (as != true)
						is_true = false;
					else {
						parent.addChild(arr);
						// lookahead = tokens.get(value_pointer+1);
						// child_marker++;
						// value_pointer++;
						// counter++;
					}

				} // end while
				return true;

			}

		} else
			return false;

	}

	// ',' expr
	public boolean aParamsTail(Node<Token> parent) {
		is_comment();
		Token lookahead = tokens.get(value_pointer + 1);

		// int counter =0;
		// int child_marker = parent.getChildren().size();
		// int temp = value_pointer;

		/*
		 * return false if the first value is not valid. should not occur but it puts a
		 * second layer of security to what needs to be done. Does not change the
		 * complexity of the program at it is an opperation that would occur maximum
		 * once.
		 */
		if (input.get(value_pointer).compareTo(",") != 0) {
			return false;
		} else {
			Node<Token> coma = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(coma);

			// LOOKAHEAD CHECK/ERROR CHECK
			if (FIRST_EXPR.contains(lookahead.getData()) == false) {
				error_stream += start_error_rec(lookahead);
				System.out.println(start_error_rec(lookahead));
				error_stream += "Invalid token to continue a program. Must continue with either expecting one of the following symbols "
						+ FIRST_EXPR.toString() + "\n";

				// keep incrementing until we can find a value that is valid.
				while (FIRST_EXPR.contains(lookahead.getData()) == false) {
					// syn_errors.push(tokens.get(value_pointer));

					if (value_pointer == tokens.size()) {
						error_stream += "Parsing reached the end without successfull syntax. Parsing remains erronous and program non compilable.\n";
						return false;
					} else {
						value_pointer++;
						lookahead = tokens.get(value_pointer + 1);
					}
				}
				error_stream += end_error_rec(tokens.get(value_pointer));
				lookahead = tokens.get(value_pointer + 1);
			}

			// counter++;
			// child_marker++;

			// check expr
			Node<Token> expr = new Node<Token>("expr");
			boolean e = expr(expr);
			/*
			 * if (e != true) { for (int i = child_marker; i < parent.getChildren().size();
			 * i++) { parent.removeChild(i); } value_pointer = temp; return false; } else {
			 * parent.addChild(expr); // value_pointer++; // counter++; // temp =
			 * value_pointer; return true; }
			 */

			return e;
		}

	}

	// assignOp -> "="
	public boolean assignOp(Node<Token> parent) {
		is_comment();
		if (input.get(value_pointer).compareTo("=") == 0) {
			Node<Token> assign = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(assign);
			if (value_pointer != tokens.size())
				value_pointer++;
			else
				System.out.println("end of token stream");
			return true;
		} else
			return false;
	}

	// relOp -> 'eq' | 'neq' | 'lt' | 'gt' | 'leq' | 'geq'
	public boolean relOp(Node<Token> parent) {
		is_comment();

		if (input.get(value_pointer).compareTo("==") == 0 || input.get(value_pointer).compareTo("<>") == 0
				|| input.get(value_pointer).compareTo("<") == 0 || input.get(value_pointer).compareTo(">") == 0
				|| input.get(value_pointer).compareTo("<=") == 0 || input.get(value_pointer).compareTo(">=") == 0) {
			Node<Token> rel = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(rel);
			if (value_pointer != tokens.size())
				value_pointer++;
			else
				System.out.println("end of token stream");
			return true;
		} else
			return false;
	}

	// addOp -> '+' | '-' | 'or'
	public boolean addOp(Node<Token> parent) {
		is_comment();

		if (input.get(value_pointer).compareTo("+") == 0 || input.get(value_pointer).compareTo("-") == 0
				|| input.get(value_pointer).compareTo("" + "||") == 0) {
			Node<Token> op = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(op);
			if (value_pointer != tokens.size())
				value_pointer++;
			else
				System.out.println("end of token stream");
			return true;
		} else
			return false;
	}

	// multOp -> '*' | '/' | 'and'
	public boolean multOp(Node<Token> parent) {
		is_comment();
		// boolean result = false;
		String input_data = input.get(value_pointer);
		if (input_data.compareTo("*") == 0 || input_data.compareTo("/") == 0 || input_data.compareTo("&&") == 0)
		// result = true;
		{
			Node<Token> multOp = new Node<Token>(tokens.get(value_pointer), "terminal");
			parent.addChild(multOp);

			if (value_pointer != tokens.size())
				value_pointer++;
			else
				System.out.println("end of token stream");
			return true;
		}
		//
		else
			return false;

	}

	/*
	 * if the value of the token is a comment, then the token will be skipped by
	 * incrementing the value of the pointer.
	 */
	public void is_comment() {
		// boolean result = false;
		boolean is_true = false;
		// to make sure that it keeps checking for comments
		while (is_true == false) {
			String d = input.get(value_pointer);
			if (d.compareTo("comment") == 0) {
				value_pointer++;
				// result = true;
			} else
				is_true = true;

		}

		// return result;
	}

	
	public Node<Token> to_AST(Node<Token> parse_tree) {
		//transforming the parse tree into an AST. 
		//going to start traversing and checking for the elements as shown in the grammar, however, the elements will be 
		//getting a few modifications on how the tree will look like. 
		
		//this process is going to be recursive
		
		
		//we need to see all the children of the tree
		List <Node<Token>> children = parse_tree.getChildren();
		int size = children.size();
		
		
		
		if(children.get(0).getProduction().compareTo("classDeclarations")==0 && children.get(0).getChildren()!=null)
			AST.addChild(class_list);
		if(children.get(1).getProduction().compareTo("functions")==0 && children.get(1).getChildren()!=null)
			AST.addChild(function_list);
		if(children.get(2).getData().getData().compareTo("main")==0)	
			AST.addChild(main);
		
		for(int i=0;i<size;i++) {
			
			//get the children one by one and check their child
			Node <Token> current = children.get(i);
			List<Node<Token>> child = current.getChildren();
			if(child!=null) {
				
				
				
				
				
				
			}
			//if the children list is null it means its a terminal production. 
			else {
				//set their production to what their parent is
				current.setProduction(current.getParent().getProduction());
				
			}
		
			
			
			
			
		}
		return AST;		
	}

	/*
	 * function in order to format the input string of the lexer into something that
	 * will allow the and allow to build a proper AST
	 */

	public ArrayList<String> format_input(ArrayList<Token> list) {
		ArrayList<String> input = new ArrayList<String>();

		int size = list.size();

		for (int i = 0; i < size; i++) {
			String element = list.get(i).getType();
			String data = list.get(i).getData();
			if (element.compareTo("comment") == 0 || element.compareTo("id") == 0 || element.compareTo("float") == 0
					|| element.compareTo("integer") == 0) {
				input.add(element);
			} else {
				input.add(data);
			}
			// end for loop
		}
		return input;
	}

	// will return a node list which represents the parse tree once the file has
	// been checked.
	public Node<Token> parser(String filename) {

		BufferedWriter errors;
		BufferedWriter AST_out;
		BufferedWriter parse_tree;
		try {
			// setting the output to file
			String errorOutputFileName = filename + "_Syntactical_errors.txt";
			String astOutputFileName = filename + "_AST.txt";
			String parse_treeOutputFileName = filename + "_parse_tree.txt";

			File errorOutput = new File(errorOutputFileName);
			File ast_out = new File(astOutputFileName);
			File parse_out = new File(parse_treeOutputFileName);

			errors = new BufferedWriter(new FileWriter(errorOutput));
			AST_out = new BufferedWriter(new FileWriter(ast_out));
			parse_tree = new BufferedWriter(new FileWriter(parse_out));

			
			
			// make sure filename has .txt format
			File in = new File(filename + ".txt");
			// setting the starting value of the pointer.
			// the pointer will allow to
			value_pointer = 0;
			//AST ast = new AST();
			
			/*
			 * getting the token stream from the lexer. The stream is in the .txt file, but
			 * it is also stored in this arraylist of tokens as it is easier to go get
			 * information from the arraylist, the arraylist is used in priority.
			 */
			Lexer lex = new Lexer();
			tokens = lex.lexer(in);
			input = format_input(tokens);

			/*
			 * once we have the arraylist we can start parsing . we actually only need to
			 * call on the first method, as it will call every other method in a recursive
			 * manner. the input for it is the token arraylist, as the methods will be going
			 * through, they will add to the class variable parse_tree which will be
			 * returned at the end of the parsing.
			 */

			// return the list at the end of the parser
			//int si = tokens.size();
			
			if (prog() == true) {
				System.out.println("Program valid");
			} else {
				System.out.println("Program invalid");
			}

			// writting errors to file
			errors.write(error_stream);
			errors.flush();
			errors.close();
			// writing the parse tree to file
			parse_tree.write(root.toString());
			parse_tree.flush();
			parse_tree.close();
			// changing to AST to be able to use it during the next phase
			AST = to_AST(root);

			AST_out.write(AST.toString());
			AST_out.flush();
			AST_out.close();
			
			
		}
		// catch errors that might occur
		catch (Exception e) {
			System.out.println(e);
		}
		// the root will have the parse tree
		//return AST;
		return root;
	}

}

/*
 * class written by Genevieve Plante-Brisebois 40003112
 * COMP442 winter 2019
 * 
 * this class uses the ast from the parser and the symbol table data structure in order to 
 * make the symbol tables and the semantic analysis. 
 */

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class SemanticAnalyser {
	//creating the global scope table and then we can append to it the other tables as we go
	ArrayList <Symbol_Table> global = new ArrayList<Symbol_Table>();
	Node<Token> root;
	
	String filename;
	String error_stream = "";
	//----------------------------------------------------------------------------------------------------------------
	//error messages
	
	/*
	 * generation of 
	 */
	
	/*
	 * generation of duplicate error message
	 */
	private String duplicateValueError(String name, String value_a, String value_b) {
		return "Error. Value " + name + " has already been used. The previous value of "+ value_a + " will be replaced by the value " + value_b; 
	}
	
	
	/*
	 * generation of error message for the type mismatch
	 */
	private String typeMismatch(String type_a, String type_b) {
		return "Error. Type mismatch. " + type_a + " cannot be used with "+ type_b+"\n";
	}
	/*
	 * method to do type checking and to be used in the class. 
	 */
	private boolean typeCheck(Node<Token> a, Node <Token> b) {
		if (a.getData().getData()==b.getData().getData())
			return true;
		else
			return false;
	}
	private boolean typeCheck(String type, Node<Token> node) {
		String temp = node.getData().getType();
		
		if(temp.compareTo(type)==0)
			return true;
		else 
			return false;
	}
	
	//----------------------------------------------------------------------------------------------------------------
	
	
	
	
	//method to see if a symbol is already in the table 
	
	public boolean isDuplicate(String name, ArrayList<Symbol_Table> table) {
		boolean duplicate = false;
		int size = table.size();
		
		int index = 0;
		
		while(index<size) {
			if(table.get(index).getName().compareTo("name")==0) {
				duplicate =true;
				return duplicate;
			}
			index++;
		}
		
		return duplicate;
	}
	
	//method to get the name of a production/scope
	//if the variable does not have an id the name will be null
	public String getName(List<Node<Token>> list, Node<Token> parent) {
		
		
		String name = null;
		
		int size = list.size();
		//looked at the production that have an id in their production that is used to identify them
		if(parent.getProduction().compareTo("factor")==0||parent.getProduction().compareTo("fParams")==0||parent.getProduction().compareTo("classDecl")==0|| parent.getProduction().compareTo("funcDecl")==0||parent.getProduction().compareTo("varDecl")==0||parent.getProduction().compareTo("variable")==0) {
			
			int index=0;
			//go through the children and find the id so that we can name the element in the table
			while(index<size) {
				String type = list.get(index).getData().getType().toLowerCase();
				if (type.compareTo("id")==0) {
					name = list.get(index).getData().getData();
					return name;
				}
				index++;
			}
			
		}
		return name;	
	}
	
	//method to get the index of an element by their name
	
	private int getIndex(String name, ArrayList <Symbol_Table> table) {
		int index =0;
		
		int size  = table.size();
		
		int i=0;
		while(i<size) {
			if (table.get(i).getName().compareTo(name)==0) {
				index = i;
				break;
			}
			i++;
			
		}
		
		return index;
		
		
	}
	
	
	//method to get the parameters of a funtion from the tree
	

	public ArrayList<String> getFParams(Node<Token> parent) {
		ArrayList<String> param = new ArrayList<String>();
		
		List<Node<Token>> childrenList = parent.getChildren();
		int index=0;
		while(index<childrenList.size()) {
			Node<Token> node = childrenList.get(index);
			//go get type
			if(node.getProduction().compareTo("type")==0) {
				String type=null;
				
				List<Node<Token>> child = node.getChildren();
				Node<Token> c = child.get(0);
				type = c.getData().getType();
				param.add(type);
				
			}else if(node.getProduction().compareTo("arraySize")==0) {
				List<Node<Token>> child = node.getChildren();
				Node<Token> c = child.get(1);
				 
				String arr="["+c.getData().getData()+"]";
				param.add(arr);
				
			}
			
			
			index++;
		}
		return param;
		
		
	}
	
	
	//method to create one table of symbols
	
	public ArrayList<Symbol_Table> create_table(Node<Token> parent){
		
		BufferedWriter write_semerror;
		ArrayList <Symbol_Table> table = new ArrayList<Symbol_Table>();
		List <Node<Token>> children = parent.getChildren();
		String prod;
		String name;
		String kind;
		String type =null;
		ArrayList<Symbol_Table> link;
		Node <Token> node;
		
		
		try {
			String errorOutputFileName = filename + "_errors.txt";
			File errorOutput = new File(errorOutputFileName);
			write_semerror = new BufferedWriter(new FileWriter(errorOutput));
			
		if(children!= null) {
			int index = 0;
			while(index<children.size()) {
				node = children.get(index);
				prod = node.getProduction();
				
			//start by getting the tokens from the tree
			if(prod.compareTo("classDecl")==0) {
				//in a class declaration the second child is the id
				List <Node<Token>> node_child =node.getChildren(); 
				name = node_child.get(1).getData().getData();
				kind = "class";
				type = null;
				Token  data= node_child.get(1).getData();
				//creates recursive filling of the symbol table
				link = create_table(node);
				Symbol_Table class_el = new Symbol_Table(name, kind, type, link, data);
				//if (isDuplicate(name, table)==true)
					//throws   SemanticExceptions{
					//throw new SemanticExceptions("Duplicate variable");
				//};
				//else
				table.add(class_el);
			}
			else if (prod.compareTo("funcDecl")==0) {
				List <Node<Token>> node_child =node.getChildren(); 
				name = node_child.get(1).getData().getData();
				kind = "function";
				ArrayList<String> param = getFParams(node);
				int ind = 0;
				
				//build the array to a string
				while(ind<param.size()) {
					type+=param.get(ind);
					type+=" ";
					ind++;
				}
				
				
				//creates recursive filling of the symbol table
				Token data = node_child.get(1).getData();
				link = create_table(node);
				Symbol_Table func_el = new Symbol_Table(name, kind, type, link, data);
				table.add(func_el);
			}
			else if (prod.compareTo("funcBody")==0) {
				List <Node<Token>> node_child =node.getChildren(); 
				
				
				name = node_child.get(1).getData().getData();
				kind = "function";
				ArrayList<String> param = getFParams(node);
				int ind = 0;
				
				//build the array to a string
				while(ind<param.size()) {
					type+=param.get(ind);
					type+=" ";
					ind++;
				}
				
				Token data = node_child.get(1).getData();
				//creates recursive filling of the symbol table
				link = create_table(node);
				Symbol_Table func_el = new Symbol_Table(name, kind, type, link, data);
				table.add(func_el);
			}
			else if (prod.compareTo("funcHead")==0) {
				List <Node<Token>> node_child =node.getChildren(); 
				name = node_child.get(1).getData().getData();
				kind = "function";
				ArrayList<String> param = getFParams(node);
				int ind = 0;
				
				//build the array to a string
				while(ind<param.size()) {
					type+=param.get(ind);
					type+=" ";
					ind++;
				}
				
				Token data = node_child.get(1).getData();
				//creates recursive filling of the symbol table
				link = create_table(node);
				Symbol_Table func_el = new Symbol_Table(name, kind, type, link, data);
				table.add(func_el);
			}
			else if (prod.compareTo("varDecl")==0) {
				List <Node<Token>> node_child =node.getChildren(); 
				name = node_child.get(1).getData().getData();
				if (isDuplicate(name, table)) {
					//get the index of the entry with the current variable of that name
					int dup = getIndex(name, table);
					
					String init_data = table.get(dup).getToken().getData();
					kind = "variable";
					type = node_child.get(0).getData().getType();
					//if it is a variable it does not have another scope underneath and as it is a duplicate then we reset values
					link = null;
					Token data = node_child.get(1).getData();
					table.get(dup).setKind(kind);
					table.get(dup).setToken(data);
					table.get(dup).setSymbolTable(link);
					table.get(dup).setType(type);
					String new_data = data.getData();
					String error = duplicateValueError(name, init_data, new_data );
					
					
					//write error to file
					write_semerror.write(error + "\n");
					write_semerror.flush();
					
					
				}else {
				
				kind = "variable";
				
				type = node_child.get(0).getData().getType();
				//if it is a variable it does not have another scope underneath
				link = null;
				Token data = node_child.get(1).getData();
				Symbol_Table var_el = new Symbol_Table(name, kind, type, link, data);
				table.add(var_el);
				
				}
				
			}
		//	else if (prod.compareTo("assignStat")==0)
			//{
				//List <Node<Token>> node_child =node.getChildren(); 
	//			
		//		Node<Token> var = node_child.get(0);
			//	int in = getIndex(var.getData().getData(), table);
				//Symbol_Table element = table.get(in);
		//		String type_var = element.getType();
	//			
			//	String type_result;
				
			//	if(type_var.compareTo("type_result")!=0) {
					
				//	error_stream+= typeMismatch(type_var,type_result);
					
			//	}
				
				
		//	}
			index++;
			}
		}
		}
		
		catch(Exception e){
			System.out.println(e);
		}
		
		
		return table;
	}
	
	

	//method takes the root of the ast and we will be building the table by going through the tree in pre order traversal
	public ArrayList <Symbol_Table> sem_analysis(String filename) {
		
		this.filename = filename;
		//create the parser
		Parser parse = new Parser();
		//creating the tree
		root = parse.parser(this.filename);
		//create the tables, all tables will be created recursively with the method
		global = create_table(root);
		
		
		BufferedWriter write_symtbl;
		BufferedWriter error;
		try {
			String errorout = filename+"_semantic_errors.txt";
			String tableOutputFileName = filename + "_symtbl.txt";
			File tableOutput = new File(tableOutputFileName);
			File erroroutput = new File(errorout);
			write_symtbl = new BufferedWriter(new FileWriter (tableOutput));			
			error  = new BufferedWriter(new FileWriter(erroroutput));
			
			error.write(error_stream);
			error.flush();
			
			
			
			int size = global.size();
			int i=0;
			write_symtbl.write("size of table: "+size);
			//write the tables to a file 
			while(i<size) {
			write_symtbl.write(global.get(i).toString());
			write_symtbl.flush();	
			i++;
			
			
			}
			
		
			
			//now that we have the tables, we should be able to do the semantic analysis. 
			
			
			
			
		}
		
		catch(Exception e) {
			System.out.println(e);
		}
		return global;
	}
	
}

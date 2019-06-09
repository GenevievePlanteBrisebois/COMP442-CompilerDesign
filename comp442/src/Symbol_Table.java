/*
 * class created by Genevieve Plante-Brisebois 40003112
 * This class if going to be used in order to create the symbol tables. 
 * 
 * the next step later will be to use another class to  use the result of this class to be able to make the semantic analysis
 * 
 * This class reads the syntax tree produced by the parser and then uses the preorder traversal class in order to read the tree and create the 
 * 
 */

import java.util.ArrayList;
//import java.util.List;


public class Symbol_Table {
	//id
	String name = null;
	//type of production/token
	String kind =null;
	//function/class parameters type
	String type =null;
	//symbol table of the local scope of that particular symbol
	ArrayList<Symbol_Table>  link = null;
	//memory aalocation
	int memory ;
	//token
	Token data;
	
	public Symbol_Table() {
		name =null;
		kind =null;
		type =null;
		link = null;	
		
	}
	
	public Symbol_Table(String name, String kind, String type, ArrayList<Symbol_Table> link, Token a) {
		this.name = name;
		this.kind = kind;
		this.type =type;
		this.link = link;
		data = a;
		
	}
	
	public String getName() {
		return name;
	}
	
	public String getKind() {
		return kind;
	}
	
	public String getType() {
		return type;
	}
	
	public ArrayList<Symbol_Table> getLink() {
		return link;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setSymbolTable(ArrayList<Symbol_Table> table) {
		link = table;
	}
	
	public void setMemoryAddress(int mem) {
		memory = mem;
	}
	
	public int getMemoryAddress() {
		return memory;
	}
	
	
	public void setToken(Token data) {
		this.data=data;
	}
	
	public Token getToken() {
		return data;
	}
	
	public String toString() {
		String output =null;
		
		output+= "name: " + name +"\nkind: " + kind + "\ntype: "+type+ "\nToken: "+data.toString()+"\nMemory location: "+memory+ "\nlink: ";
		
		if(link!= null) {
			int index =0;
			int size = link.size();
			
			while(index<size) {
				link.get(index).toString();
				index++;
			}
		}
		
		return output;
	}
	
}

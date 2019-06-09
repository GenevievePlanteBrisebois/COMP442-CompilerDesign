/*
 * class written by Genevieve Plante-Brisebois 40003112
 * Winter 2019 COMP 442
 * 
 * This class will be used in order to produce the parse tree that the parser produces. 
 *
 *The code is to be used with tokens. 
 *
 *This has been used as a reference for the code of this class. Some code has been used as is. 
 *
 *https://stackoverflow.com/questions/19330731/tree-implementation-in-java-root-parents-and-children
 *
 */


import java.util.ArrayList;
import java.util.List;

public class Node <E> {
	// each node will have a production that it will be associated to (class, program, statement, variable, etc)
	// it will also contain the token with full information (token from lexical analysis)
	// a parent node, if it is not a root
	// a list of nodes that will then lead to the children associated with this 
 private List<Node<E>> children = new ArrayList<Node<E>>();
 private Node<E> parent = null;
 private Token data = null;
 //this is to indicate which production it is. if it is a terminal symbol, then production will be put as terminal as it is a 
 //terminal symbol and it then moves to the next input symbol
 private String production = null;
 
 // allows to create a node that will tell us that is the production used. 
 public Node(String prod) {
	 production = prod;
 }
 public Node(Token data_input, String prod) {
	 data = data_input;
	 production = prod;
 }
 
 public Node (Token input_data, Node<E> node_parent, String prod) {
	 parent = node_parent;
	 data = input_data;
	 production = prod;
 }
 
 public void setParent(Node <E> node_parent) {
	 parent = node_parent;
 }
 public Node <E> getParent(){
	 return this.parent;
 }
 public void setChild(List<Node <E>> child_node)
 {
	children = child_node;
	
	int size = children.size();
	
	for(int i=0;i<size;i++) {
		children.get(i).setParent(this);
	}
	
 }
 public List<Node<E>> getChildren(){
	 return children;
 }
public void addChild(Token input_data, String prod) {
	Node <E> child = new Node<E>(input_data, prod);
	children.add(child);
	child.setParent(this);	
	
}

public void addChild(Node<E> child) {
	this.children.add(child);
	child.setParent(this);
	
}
public void setData(Token input_data) {
	data=input_data;
}
public void setProduction(String prod) {
	production = prod;
}
public String getProduction() {
	return production;
}


public Token getData() {
	return data;
}

public boolean isRoot() {
	boolean result  = false;
	if (this.parent == null)
			result = true;
	return result;
	}

public boolean isLeaf() {
	boolean result = false;
	if (children.size()==0)
		result = true;
	return result;
	
}
public void removeChild(Node <E> child) {
	children.remove(child);
}

public void removeParent() {
	parent.removeChild(this);
	parent = null;
}
//takes off all children of node
public void removeChildren() {
	int size = children.size();
	for(int i=0;i<size;i++) {
		children.remove(i);
	}
	//children =null;
}
public void removeChild(int index) {
	children.remove(index);
}

public String toString() {
	String out ="";
	if(parent!=null) {
		if(data!=null) {
			out =  "\nParent data: " + parent.getProduction() + " Current node token: " + data.toString() + "Production: " + production + "\n Children: " ;
			if(children!=null) {
			int size = children.size();
			
			for (int i=0;i<size;i++) {
				out += children.get(i).toString();
			}
			}
		}else {
	out =  "\nParent data: " + parent.getProduction() + "Production: " + production + "\n Children: " ;
	if(children!=null) {
	int size = children.size();
	
	for (int i=0;i<size;i++) {
		out += children.get(i).toString();
	}
	
	}
	}
		}else {
			if(data!=null) {
				out =  "\nParent data: None Current node token: " + data.toString() + "Production: " + production + "\n Children: " ;
				if(children!=null) {
				int size = children.size();
				
				for (int i=0;i<size;i++) {
					out += children.get(i).toString();
				}
				}
			}else {
		out =  "\nParent data: None Current node token: None Production: " + production + "\n Children: " ;
		if(children!=null) {
		int size = children.size();
		
		for (int i=0;i<size;i++) {
			out += children.get(i).toString();
		}
		}
	}}
	return out;
}
}

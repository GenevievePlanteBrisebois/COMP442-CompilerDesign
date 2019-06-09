/*
 * class written by Genevieve Plante-Brisebois 40003112
 * code written in the context of COMP442 winter 2019 compiler design
 * 
 * 
 * This class is going to take in the symbol tables and from it generate code that is going to be able to be
 * processed by the moon VM. 
 * 
 * This process is going with a tag based approach. it will allow to do basic
 * functionality
 * 
 */

import java.util.ArrayList;
import java.io.*;
import java.nio.ByteBuffer;



public class CodeGeneration {
	//max number of bits to put in each memory location
	int bit_limit= 32;
	
	String file_name="";
	
	//setting the symbol table that we receive from the semantic analyser
	
	ArrayList <Symbol_Table> global = new ArrayList<Symbol_Table>();
	/*
	BufferedWriter write_code;
	
	
	try {
		//setting up the file writing to be able to write the assembly code that will later be 
		//the moon vm
		String code_gen_output = file_name + "_code.txt";
		File code_gen = new File(code_gen_output);
		write_code = new BufferedWriter(new FileWriter(code_gen));
		
		
		
		
	}
	catch(Exception e) {
		
	}
	*/
	
	
	//memory allocation
	//only allocate memory to the class variables first, and then do the operations 
	//asked in the program
	
	//functions
	
	/*
	 * function to put a string to byte with a limit of 4 byte (32 bits)
	 * in an 4 byte format (32 bits)
	 */
	
	public String StringToByte(String a) {
		
		//int max = 4;
		byte [] by = new byte[4];
		String out ="";
		
		by = a.getBytes();
		
		for(int i=0; i<4;i++) {
			out += Integer.toBinaryString(by[i]);
		}
		
		return out;
		
	}
	/*
	 * this will allow to get the floats to be put to binary 32 bit format
	 * 
	 */
	public String FloatToByte(String a) {
		float in = Float.parseFloat(a);
		
		byte [] by = new byte[4];
		
		String out ="";
		
		by = ByteBuffer.allocate(4).putFloat(in).array();
		
		for(int i=0; i<4;i++) {
			out += Integer.toBinaryString(by[i]);
		}
		
		return out;
	}
	//	UNFINISHED METHOD
	public String intToByte(String a) {
		int in = Integer.parseInt(a);
		
		byte [] by = new byte[4];
		String out ="";
		
		//by = in.toByteArray();
		return out;
		
	}
	
	//
	//this function will output a string corresponding to the memory allocation of a symbol table
	public String allocate_memory_program(ArrayList<Symbol_Table> table) {
		//final string that represents the memory allocation commands
		String memory = "";
		int max = 15;
		//read the symbol table to get the variables
		
		int size = table.size();
		int i = 0;
		
		while(i<max) {
			//only get the variable
			if (table.get(i).getKind().compareTo("variable")==0) {
				//set memory to current register as we just started the program
				table.get(i).setMemoryAddress(i);
				
				String type = table.get(i).getType();
				String data ="";
				String content = table.get(i).getToken().getData();
				String register = "r"+i;
				if(type.compareTo("integer")==0) {
					data = intToByte(content);
					memory+= "sb "+data+"("+register+"),"+register+"\n";
					
				}
				else if (type.compareTo("float")==0) {
					data = FloatToByte(content);
					memory+= "sb "+data+"("+register+"),"+register+"\n";
					
				}
				else if (type.compareTo("id")==0) {
					data = StringToByte(content);
					memory+= "sb "+data+"("+register+"),"+register+"\n";
					
				}
				
			}
			
			i++;
			//case of overflow, as we only have 15 registers and each only can take data from one variable
			if (i==max && size >=max) {
				BufferedWriter write_code_error;
				System.out.println("Memory overflow at line:" + table.get(i).getToken().getLocation());
				
				try {
					//setting up the file writing to be able to write the assembly code that will later be 
					//the moon vm
					String code_gen_output = file_name + "_code_memory allocation_error.txt";
					File code_gen = new File(code_gen_output);
					write_code_error = new BufferedWriter(new FileWriter(code_gen));
					
					write_code_error.write("Memory overflow, at line: " + table.get(i).getToken().getLocation());
					
					
				}
				catch(Exception e) {
					
				}
				
			}
		}
		
		
		return memory;
	}
	
}

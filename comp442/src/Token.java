
	/*Created by Genevieve Plante-Brisebois 40003112
	 * COMP442 Compiler Design WInter 2019
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
		
		return "Type: "+token_type + " data: " + data + " " +location.toString();
	}
}
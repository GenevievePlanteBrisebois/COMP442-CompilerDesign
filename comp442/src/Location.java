	/*
	 * Created by Genevieve Plante-Brisebois 40003112
	 * COMP442 Compiler Design WInter 2019
	 * class in order to keep track of the line at which we are at so that when there is an error 
	 * we can do to string and output where the error was. 
	 * */
	public class Location{
		
		int line;
		
		//default constructor
		public  Location() {
			line = 0;
		}
		public Location(int line) {
			this.line  = line;
		}
		public int getLine() {
			return line;
		}
		public void setLocation(int i) {
			line = i;
		}
		public String toString() {
			return "Location line :" +line;
		}
	}
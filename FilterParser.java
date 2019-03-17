package writer;

public class FilterParser {

	private String operation;
	public String parsedString = "";

	public FilterParser(String operation) {
		this.operation = operation;
	}
	
	public String createString(String[] queries) {
		//handles the "and" case
		if(this.operation.equalsIgnoreCase("and")) {
			this.parsedString += "(&";
			for(String s : queries) {
				this.parsedString += "(" + s + ")";
			}
			this.parsedString += ")";
		}
		//handles the "or" case
		else if(this.operation.equalsIgnoreCase("or")) {
			this.parsedString += "(|";
			for(String s : queries) {
				this.parsedString += "(" + s + ")";
			}
			this.parsedString += ")";
		}
		//handles the "eq" case
		else if(this.operation.equalsIgnoreCase("eq")) {
			for(String s : queries) {
				this.parsedString += "(" + s + ")";
			}
		}
		//if user input an incorrect filter word
		else {
			return "INCORRECT QUERY";
		}
		return this.parsedString;
	}
}

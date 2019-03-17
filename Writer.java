package writer;

import java.io.File;
import java.io.IOException;

import com.unboundid.ldif.LDIFWriter;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPTestUtils;

public class Writer {
	
	private static final int NUM_ARGS = 5;
	private static final int NUM_ARGS_MAX = 6;

	/**
	 * Writes an LDIF file from an LDAP server given a set of user-specified
	 * filters.
	 * 
	 * @param args[0] the address of the host for the LDAP
	 * @param args[1] the associated port
	 * @param args[2] the bindDN or section of the directory to start the
	 * 		query
	 * @param args[3] the password (not always used) used for authentication
	 * @param args[4] the name of the file to which the output will be written
	 * @param args[5] filter string ("and", "or", or "eq")
	 * <br>
	 * 		If left blank and there are no more arguments after this, the
	 * 		filter will default to an empty filter "(objectclass=*)".
	 * <br>
	 * <br>
	 *		1.	"and" allows filters for intersecting attribute equalities
	 *			(e.g.): "and", "ou=animals", "ou=cats" for args 5 through 7
	 * <br>
	 *		2.	"or" allows filters for a union of attributes equalities
	 *			(e.g.): "or", "ou=humans", "ou=reptiles" for args 5 through 7
	 * <br>
	 *		3.	"eq" is used for filters that specify a certain equality
	 *			(e.g.): "eq", "uid=John" for args 5 and 6
	 *
	 *@param args[6] specifying string(s) for the previously declared filter
	 */
	public static void main(String[] args) {
		
		if(args.length < NUM_ARGS) {
			System.out.println("Incorrect input format. Correct format is: "
					+ "address, port, bindDN, password, output filename, "
					+ "filter ... (defaults to no filter).");
			return;
		}
		
		String address = args[0];
		int port = Integer.parseInt(args[1]);
		String bindDN = args[2];
		String password = args[3];
		String filename = args[4];
		String filter = "";
		
		LDAPStruct struct = new LDAPStruct(address, port, bindDN, password);
		
		//if filter word is specified ("or", "and", or "eq")
		if(args.length > NUM_ARGS_MAX) {
			//FilterParser constructor handles unexpected inputs
			FilterParser parser = new FilterParser(args[5]);
			
			String[] queries = new String[args.length - NUM_ARGS_MAX];
			int count = 0;
			//get the specified identifier strings for the filter
			for(int i = NUM_ARGS_MAX; i < args.length; i++) {
				queries[count] = args[i];
				count++;
			}
			
			//create the parsed string for the filter
			filter = parser.createString(queries);
		}
		//if no filter is specified, return entire tree
		else {
			filter = "(objectClass=*)";
		}

		if(!struct.connectToServer()) {
			return;
		}
		
		/*if(!struct.bind()) {
			return;
		}*/
		
		System.out.println("Querying: \"" + filter + "\"...");
		
		Filter f1;
		try {
			//create filter object with parsed filter constructor string
			f1 = Filter.create(filter);
			
			SearchRequest req = new SearchRequest(bindDN, SearchScope.SUB, f1,
					"*");
		
			SearchResult res;
		
			try {
				//search given our specified request
				res = struct.getConnection().search(req);

				File output = new File(filename);
				LDIFWriter w;

				try {
					w = new LDIFWriter(output);
					//write out all matching search results
					for(SearchResultEntry entry : res.getSearchEntries()) {
						w.writeEntry(entry);
					}
					//close writer
					w.close();
					//if no search results were found
					if(res.getSearchEntries().isEmpty()) {
						System.out.println("\nNo search results found.");
						
					}
					else {
						System.out.println("\nSuccessfully wrote to file "
								+ "\"" + filename + "\".");
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
	
			catch (LDAPSearchException noResult) {
				System.out.println("Failed to find result with specified "
						+ "attributes.");
				noResult.printStackTrace();
			}
			//close connection with LDAP server
			struct.getConnection().close();
			}
		
		catch (LDAPException e2) {
			System.out.println("First argument of filter should be \"add\", "
					+ "\"or\", or \"eq\".");
			e2.printStackTrace();
		}
		
	}

}

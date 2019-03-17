package writer;

import java.io.File;
import java.io.IOException;

import com.unboundid.ldif.LDIFWriter;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.LDAPTestUtils;

public class LDAPStruct {
	
	private String address;
	private int port;
	private String bindDN;
	private String password;
	private LDAPConnection c;

	public LDAPStruct(String address, int port, String bindDN, String password) {
		
		this.address = address;
		this.port = port;
		this.bindDN = bindDN;
		this.password = password;
	}
	
	//connects to an LDAP server at given address and port
	public boolean connectToServer() {
		
		this.c = new LDAPConnection();
		try {
			this.c.connect(this.address, this.port, 15000);
		} catch (LDAPException e1) {
			System.out.println("Failed to connect to server.");
			e1.printStackTrace();
			return false;
		}
		System.out.println("Successfully connected to " 
				+ this.c.getConnectedAddress() + " at port "
				+ this.c.getConnectedPort() + ".");
		return true;
	}
	
	
	//authenticate and binds to a specified bindDN in the directory
	public boolean bind() {
		
		try {
			BindResult bindResult = this.c.bind(this.bindDN, this.password);
		} catch (LDAPException e1) {
			System.out.println("Failed to bind to directory.");
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	//NEEDS REVISING
	public boolean writeEntries(File output, SearchResult result) {
		
		LDIFWriter w;
		try {
			w = new LDIFWriter(output);
			int count = 0;
			for(SearchResultEntry entry : result.getSearchEntries()) {
				w.writeEntry(entry);
				count++;
			}
			
			System.out.println("Successfully wrote " + (count) 
				+ " entries to file " + output.toString());
			
		} catch (IOException e) {
			System.out.println("Write to file " + output.toString() + " failed.");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public SearchResultEntry searchEntry(String dn) {
		SearchResultEntry search;
		try {
			search = this.c.getEntry(dn);
			System.out.println(search.toString());
			return search;
		} catch (LDAPException e) {
			System.out.println("Failed to retrieve search result.");
			e.printStackTrace();
			return null;
		}
	}
	
	public LDAPConnection getConnection() {
		return this.c;
	}
}

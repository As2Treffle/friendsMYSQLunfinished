package de.TomDalton.Datenbank;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;


public class Db {
	public Connection connection;
	private String host, database, username, password;
	private int port;
    
    public Db(String phost, int pport, String pdatabase, String pusername, String ppassword) {
	    host = phost;
	    port = pport;
	    database = pdatabase;
	    username = pusername;
	    password = ppassword;
	    try {
			openConnection();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void closeCon() {
    	try {
			connection.close();
			if (connection.isClosed()) {
				System.out.println("[Friends] Verbindung zur Datenbank getrennt.");
			} else {
				System.out.println("[Friends] Verbindung zur Datenbank konnte nicht getrennt werden.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
    
    public void openConnection() throws SQLException, ClassNotFoundException {
	    if (connection != null && !connection.isClosed()) {
	        return;
	    }
	 
	    synchronized (this) {
	        if (connection != null && !connection.isClosed()) {
	            return;
	        } 
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
	    }
	}

}

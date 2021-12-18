package es.uvigo.esei.dai.hybridserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class XSDDBDAO implements XSDDAO {

	Connection connection;
	String DB_URL, DB_PASSWORD, DB_USER;

	public XSDDBDAO (String DB_URL, String DB_PASSWORD, String DB_USER) {
		this.DB_URL = DB_URL;
		this.DB_PASSWORD = DB_PASSWORD;
		this.DB_USER = DB_USER;
	}

	@Override
	public boolean contains(String uuid) {
		
		boolean found = false;

		String query = "SELECT `uuid` FROM XSD WHERE `uuid`=?";
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				
				statement.setString(1, uuid);
				
				try (ResultSet result = statement.executeQuery()) {
					result.next();
					try {
						if (result.getString("uuid").equals(uuid))
							found = true;
					} catch (SQLException e) {
						found = false;
					}
				}
	
			} 
		
		}catch (SQLException e) {
			throw new RuntimeException(e);
		}
	
		return found;
	}

	@Override
	public String getContent(String uuid) {

		String toRet = "";

		String query = "SELECT `content` FROM XSD WHERE `uuid`=?";
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				
				statement.setString(1, uuid);
				
				try (ResultSet result = statement.executeQuery()) {
					result.next();
					toRet = result.getString("content");
				}
	
			} 
		
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	
		return toRet;

	}

	@Override
	public void add(String uuid, String xsd) {

		String query = "INSERT INTO XSD (uuid, content) " + "VALUES (?, ?)";
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

			try (PreparedStatement statement = connection.prepareStatement(query)) {

				statement.setString(1, uuid);
				statement.setString(2, xsd);

				if (statement.executeUpdate() != 1)
					throw new SQLException("Error adding line.");

			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(String uuid) {
		
		String query = "DELETE FROM XSD WHERE `uuid`=?";
		
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			try (PreparedStatement statement = connection.prepareStatement(query)) {

				statement.setString(1, uuid);

				if (statement.executeUpdate() != 1)
					throw new SQLException("Error deleting line.");

			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Set<String> list() {
		
		Set<String> lista= new HashSet<String>();
		String query = "SELECT uuid FROM XSD";
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			
			try (PreparedStatement statement = connection.prepareStatement(query)) {

				try(ResultSet result= statement.executeQuery()){
					
					while(result.next()) {
						lista.add(result.getString("uuid"));
					}
				}

			}
			
		}catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return lista;
	}

}

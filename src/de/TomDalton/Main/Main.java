package de.TomDalton.Main;

import java.sql.ResultSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import de.TomDalton.Commands.Friends;
import de.TomDalton.Datenbank.Db;
import de.TomDalton.Main.ListenerClass;

public class Main extends JavaPlugin {
	public static FileConfiguration config;
	private Db db;
	public static Connection con;
	
	@Override
	public void onEnable() {
		try {
			createConfig();
			createListener();
			openCon();
			loadPlayerList();
			new Friends(this);
			System.out.println("[Friends] Plugin geladen!");
		}catch(Exception e) {
			System.out.println("[Friends] Fehler: "+e);
		}
	}

	@Override
	public void onDisable() {
		con = null;
		db.closeCon();
	}
	
	public void insert(UUID uuid) {
		Player p = Bukkit.getPlayer(uuid);
		Statement statement;
		try {
			statement = (Statement) con.createStatement();
			statement.executeUpdate("Insert into players(name,uuid,ip) values('" + p.getDisplayName() + "','" + p.getUniqueId().toString() +"','" + p.getAddress().toString() +"')");
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("[Friends] Fehler beim Insert: " + e);
		}
	}

	public void loadPlayerList() {
		String query = "SELECT uuid FROM players";
	    Statement st;
	    try {
	    	st = (Statement) con.createStatement();
	    	ResultSet rs = st.executeQuery(query);
	    	while (rs.next()) {
	    		String uuids = rs.getString("uuid");
	    		ListenerClass.players.add(UUID.fromString(uuids));
	    	}
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public void openCon() {
		String host = config.getString("host");
	    int port = config.getInt("port");
	    String database = config.getString("database");
	    String username = config.getString("username");
	    String password = config.getString("password");
	    db = new Db(host, port, database, username, password);
	    con = db.connection;
	}
	
	public void createConfig() {
		this.saveDefaultConfig();
		config = this.getConfig();
		config.addDefault("damage", 5);
		config.options().copyDefaults(true);
		saveConfig();
	}
	
	
	public void createListener() {
		Bukkit.getServer().getPluginManager().registerEvents(new ListenerClass(), this);
	}
}

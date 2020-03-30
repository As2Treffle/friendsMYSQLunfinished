package de.TomDalton.Commands;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mysql.jdbc.Statement;

import de.TomDalton.Main.ListenerClass;
import de.TomDalton.Main.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Friends implements CommandExecutor{
	
	public Main plugin;
	public static Inventory menu = Bukkit.createInventory(null, 9, "§6Freunde §0- §8Hauptmenü");
	public static Inventory all_players = Bukkit.createInventory(null, 36, "§6Freunde §0- §8Alle Spieler");
	
	public Friends(Main plugin){
		
		menu = Bukkit.createInventory(null, 9, "§6Freunde §0- §8Hauptmenü");
		{
	    	ItemStack schliessen = new ItemStack(Material.BARRIER, 1);
	    	ItemMeta schliessenMeta = schliessen.getItemMeta();
	    	schliessenMeta.setDisplayName("Schließen");
	    	schliessenMeta.setLore(Arrays.asList("Menü schließen.."));
	    	schliessen.setItemMeta(schliessenMeta);
	    	
	    	/**
	    	ItemStack geben = new ItemStack(Material.SKULL_ITEM, 1);
	    	ItemMeta gebenMeta = geben.getItemMeta();
	    	gebenMeta.setDisplayName("Alle Spieler");
	    	gebenMeta.setLore(Arrays.asList("alle Spieler anzeigen.."));
	    	geben.setItemMeta(gebenMeta);*/
	    	
	    	ItemStack friends = new ItemStack(Material.BLAZE_POWDER, 1);
	    	ItemMeta friendsmeta = friends.getItemMeta();
	    	friendsmeta.setDisplayName("Freunde");
	    	friendsmeta.setLore(Arrays.asList("Freundesliste anzeigen"));
	    	friends.setItemMeta(friendsmeta);
	    	
	    	ItemStack reqs = new ItemStack(Material.MAGMA_CREAM, 1);
	    	ItemMeta reqsMeta = reqs.getItemMeta();
	    	reqsMeta.setDisplayName("Freundesanfragen");
	    	reqsMeta.setLore(Arrays.asList("Anfragen anzeigen"));
	    	reqs.setItemMeta(reqsMeta);
	    	
	            //menu.setItem(0, new ItemStack(geben));
	            menu.setItem(8, new ItemStack(schliessen));
	            menu.setItem(3, new ItemStack(friends));
	            menu.setItem(5, new ItemStack(reqs));
		}
		
		this.plugin = plugin;
		plugin.getCommand("friends").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = null;
		if(!(sender instanceof Player)) {
			return true;
		}else {
			p = (Player) sender;
			if(args.length!=0) {
				if(!args[0].equals("")) {
					if(args[0].equals("add")) {
						if(args.length == 2) {
							addfriend(p,args[1]);
						}else {
							p.sendMessage("§f[§6Friends§f] Du musst einen gültigen Spielernamen angeben!");
						}
					}
					if(args[0].equals("accept")) {
						if(args.length == 2) {
							accept(p,args[1]);
						}
					}
					if(args[0].equals("decline")) {
						if(args.length == 2) {
							decline(p,args[1]);
						}
					}
					if(args[0].equals("getitem")) {
						ItemStack renamed = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
						Player target = p;
						SkullMeta skull = (SkullMeta) renamed.getItemMeta();
				        skull.setDisplayName("Freunde");
				        ArrayList<String> lore = new ArrayList<String>();
				        lore.add("Freunde");
				        skull.setLore(lore);
				        skull.setOwningPlayer(target);
				        renamed.setItemMeta(skull);
				        p.getInventory().setItemInMainHand(new ItemStack(renamed));
					}
				}
			}else {
				openInv(p);
			}
		}
		return false;
	}
	
	public void openInv(Player p) {
		p.openInventory(menu);
	}
	
	private void decline(Player receiver, String psender) {
		try {
			Player sender = Bukkit.getPlayer(psender);
			Statement statement;
			try {
				statement = (Statement) Main.con.createStatement();
				statement.executeUpdate("DELETE FROM request WHERE from_id,to_id,status FROM request WHERE to_id='" + receiver.getUniqueId().toString() + "' AND from_id='" + sender.getUniqueId().toString() + "' AND status='" + 0 + "'");
				receiver.sendMessage("§f[§6Friends§f] Du hast die Anfrage von "+sender.getDisplayName()+" abgelehnt!");
			}catch(Exception e) {
				e.printStackTrace();
				receiver.sendMessage("§f[§6Friends§f] Fehler beim Ablehnen. Kontaktiere einen Administrator!");
			}
		}catch(Exception e) {
			
		}
		
	}

	private void accept(Player receiver, String psender) {
		try {
			Player sender = Bukkit.getPlayer(psender);
			String receiver_dbid = "";
			String sender_dbid = "";
			String friends_receiver = "";
			String friends_sender = "";
			
			//Sender DBID
			String query = "SELECT id FROM players WHERE uuid='" + sender.getUniqueId().toString() + "'";
		    Statement st;
		    try {
		    	st = (Statement) Main.con.createStatement();
		    	ResultSet rs = st.executeQuery(query);
		    	if(rs.next()) {
		    		sender_dbid = ""+rs.getInt("id");
		    	}
		    }catch(Exception e) {
		    	
		    }
		    
		  //Receiver DBID
		    query = "SELECT id FROM players WHERE uuid='" + receiver.getUniqueId().toString() + "'";
		    try {
		    	st = (Statement) Main.con.createStatement();
		    	ResultSet rs = st.executeQuery(query);
		    	if(rs.next()) {
		    		receiver_dbid = ""+rs.getInt("id");
		    	}
		    }catch(Exception e) {
		    	
		    }
			query = "SELECT from_id,to_id,status FROM request WHERE to_id='" + receiver.getUniqueId().toString() + "' AND from_id='" + sender.getUniqueId().toString() + "' AND status='" + 0 + "'";
		    try {
		    	st = (Statement) Main.con.createStatement();
		    	ResultSet rs = st.executeQuery(query);
		    	if(rs.next()) {
		    		//setze Wert auf 1 = true zum annehmen
		    		query = "UPDATE request SET status=1 WHERE to_id='" + receiver.getUniqueId().toString() + "' AND from_id='" + sender.getUniqueId().toString() + "' AND status='" + 0 + "'";
				    try {
				    	st = (Statement) Main.con.createStatement();
				    	st.executeUpdate(query);
				    	//aktuelle freundesliste des receivers
				    	query = "SELECT player_id,friend_ids FROM friends WHERE player_id='" + receiver.getUniqueId().toString() + "'";
					    try {
					    	st = (Statement) Main.con.createStatement();
					    	rs = st.executeQuery(query);
					    	if(rs.next()) {
					    		friends_receiver = rs.getString("friend_ids");
					    	}
					    }catch(Exception e) {
					    	
					    }
				    	//freundesliste des receivers aktualisieren
					    friends_receiver = friends_receiver+","+sender_dbid;
				    	query = "UPDATE friends SET friend_ids='" + friends_receiver + "' WHERE player_id='" + receiver.getUniqueId().toString() + "'";
					    try {
					    	st = (Statement) Main.con.createStatement();
					    	st.executeUpdate(query);
					    }catch(Exception e) {
					    	
					    }
					    //aktuelle freundesliste des senders
				    	query = "SELECT player_id,friend_ids FROM friends WHERE player_id='" + sender.getUniqueId().toString() + "'";
					    try {
					    	st = (Statement) Main.con.createStatement();
					    	rs = st.executeQuery(query);
					    	if(rs.next()) {
					    		friends_sender = rs.getString("friend_ids");
					    	}
					    }catch(Exception e) {
					    	
					    }
				    	//freundesliste des senders aktualisieren
					    friends_sender = friends_sender+","+receiver_dbid;
				    	query = "UPDATE friends SET friend_ids='" + friends_receiver + "' WHERE player_id='" + receiver.getUniqueId().toString() + "'";
					    try {
					    	st = (Statement) Main.con.createStatement();
					    	st.executeUpdate(query);
					    	if(sender.isOnline()) {
					    		sender.sendMessage("§f[§6Friends§f] "+ receiver.getDisplayName() +" hat deine Anfrage angenommen!");
					    	}
					    	receiver.sendMessage("§f[§6Friends§f] Du hast die Anfrage von "+ sender.getDisplayName() +" angenommen!");
					    }catch(Exception e) {
					    	
					    }
				    }catch(Exception e) {
				    	
				    }
		    	}else {
		    		receiver.sendMessage("§f[§6Friends§f] Du hast keine Anfrage von diesem Spieler!");
		    	}
		    }catch(Exception e) {
		    	
		    }
		}catch(Exception e) {
			receiver.sendMessage("§f[§6Friends§f] Du musst einen gültigen Spielernamen angeben!");
		}
	}

	public void addfriend(Player p,String pTarget) {
		try {
			Player target = Bukkit.getPlayer(pTarget);
			String target_dbid = "";
			String[] sender_freunde = null;
			//Abrufen der Spielerdaten
			//Empfänger DBID
			String query = "SELECT id FROM players WHERE uuid='" + target.getUniqueId().toString() + "'";
		    Statement st;
		    try {
		    	st = (Statement) Main.con.createStatement();
		    	ResultSet rs = st.executeQuery(query);
		    	if(rs.next()) {
		    		target_dbid = ""+rs.getInt("id");
		    	}
		    }catch(Exception e) {
		    	
		    }
			//Freundesliste des Senders abrufen
			query = "SELECT friend_ids FROM friends WHERE player_id='" + p.getUniqueId().toString() + "'";
		    try {
		    	st = (Statement) Main.con.createStatement();
		    	ResultSet rs = st.executeQuery(query);
		    	if(rs.next()) {
		    		//hat Freunde
		    		sender_freunde = rs.getString("friend_ids").split(",");
		    		//wenn person nicht mit ziel befreundet ist
		    		if(!contains(sender_freunde,target_dbid+"")) {
				    	//Check ob bereits Anfrage existiert
						query = "SELECT from_id,to_id FROM request WHERE from_id='" + p.getUniqueId().toString() + "' AND to_id='" + target.getUniqueId().toString() + "'";
					    try {
					    	st = (Statement) Main.con.createStatement();
					    	rs = st.executeQuery(query);
					    	if(rs.next()) {
					    		//Anfrage existiert
					    		p.sendMessage("§f[§6Friends§f] Du hast "+target.getDisplayName()+" bereits eine Anfrage geschickt!");
					    	}else {
					    		//Erstelle Anfrage
					    		Statement statement;
					    		try {
					    			statement = (Statement) Main.con.createStatement();
					    			statement.executeUpdate("Insert into request(from_id,to_id) values('" + p.getUniqueId().toString() + "','" + target.getUniqueId().toString() +"')");
					    			sendMsg(p,target);
					    		}catch(Exception e) {
					    			e.printStackTrace();
					    			System.out.println("[Friends] Fehler beim Insert der Freundesanfragen: " + e);
					    		}
					    	}
					    }catch(Exception e) {
					    	e.printStackTrace();
					    }
				    }else {
				    	p.sendMessage("§f[§6Friends§f] Du bist mit dieser Person bereits befreundet!");
				    }
		    	}else {
		    		//hat keine Freunde
		    		Statement statement;
		    		try {
		    			statement = (Statement) Main.con.createStatement();
		    			statement.executeUpdate("Insert into friends(player_id,friend_ids) values('" + p.getUniqueId().toString() + "','" + "0" +"')");
		    			statement = (Statement) Main.con.createStatement();
		    			statement.executeUpdate("Insert into request(from_id,to_id) values('" + p.getUniqueId().toString() + "','" + target.getUniqueId().toString() +"')");
		    			sendMsg(p,target);
		    		}catch(Exception e) {
		    			e.printStackTrace();
		    			System.out.println("[Friends] Fehler beim Erstellen einer Freundesliste: " + e);
		    		}
		    	}
		    }catch(Exception e) {
		    	
		    }
		}catch(Exception e) {
			p.sendMessage("§f[§6Friends§f] Du musst einen gültigen Spielernamen angeben!");
		}
		
	}
	
	private void sendMsg(Player sender,Player target) {
		sender.sendMessage("§f[§6Friends§f] Du hast "+target.getDisplayName()+" eine Anfrage geschickt!");
		if(target.isOnline()) {
			TextComponent accept = new TextComponent( "§a§lAnnehmen" );
			accept.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/friends accept "+sender.getDisplayName() ) );
			accept.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Anfrage annehmen" ).create() ) );
			TextComponent decline = new TextComponent( "§c§lAblehnen" );
			decline.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/friends decline "+sender.getDisplayName() ) );
			decline.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Anfrage ablehnen" ).create() ) );
			
			target.sendMessage("§f[§6Friends§f] Du hast eine neue Freundschaftsanfrage von " + sender.getDisplayName());
			target.spigot().sendMessage(accept);
			target.spigot().sendMessage(decline);
		}
	}
	
	private boolean contains(String[] array,String string) {
		boolean contains=false;
		for(String asd : array) {
			if(asd.equals(string)) {
				return true;
			}
		}
		return contains;
	}

	public void insert(Player p) {
		Statement statement;
		try {
			statement = (Statement) Main.con.createStatement();
			statement.executeUpdate("Insert into players(name,uuid,ip) values('" + p.getDisplayName() + "','" + p.getUniqueId().toString() +"','" + p.getAddress().toString() +"')");
		}catch(Exception ee) {
			ee.printStackTrace();
			System.out.println("[Friends] Fehler beim Insert: " + ee);
		}
	}
	
}

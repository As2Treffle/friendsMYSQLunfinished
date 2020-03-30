package de.TomDalton.Main;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mysql.jdbc.Statement;

import de.TomDalton.Commands.Friends;

public class ListenerClass implements org.bukkit.event.Listener{

	public static ArrayList<UUID> players;
	
	public ListenerClass() {
		players = new ArrayList<UUID>();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		try {
			if(e.getAction() == Action.RIGHT_CLICK_AIR && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Freunde")) {
				e.getPlayer().performCommand("friends");
			}
		}catch(Exception er) {
			
		}
		
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(!players.contains(e.getPlayer().getUniqueId())) {
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
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory requests = Bukkit.createInventory(null, 36, "§6Freunde §0- §8Anfragen");
		Inventory friends = Bukkit.createInventory(null, 36, "§6Freunde §0- §8Freunde");
		Inventory profile = null;
		Inventory request_profile = null;
		Player lastClickedFriend = null;
		Player lastClickedSender = null;
		ArrayList<String> list = new ArrayList<String>();
		for(Player all:Bukkit.getServer().getOnlinePlayers()){
			list.add(all.getName());
		}
    	Player player = (Player) event.getWhoClicked();
    	ItemStack clicked = event.getCurrentItem();
    	Inventory inventory = event.getInventory();
    	if (inventory.getName().equals(Friends.menu.getName())) {
    		event.setCancelled(true);
    		if (clicked == null) {
    			event.setCancelled(true);
    		} else if (clicked.getType() == Material.SKULL_ITEM) {
    			player.closeInventory();
    			Friends.all_players.clear();
    			loadAllPlayers(player,list);
    			player.openInventory(Friends.all_players);
    		} else if (clicked.getType() == Material.BLAZE_POWDER) {
    			friends.clear();
    			friends = loadFriends(player,friends);
    			player.openInventory(friends);
    		} else if (clicked.getType() == Material.MAGMA_CREAM) {
    			requests = loadReqs(player,requests);
    			player.openInventory(requests);
    		}else if (clicked.getType() == Material.BARRIER) {
    			player.closeInventory();
    		}
    	}else if (inventory.getName().equals(requests.getName())) {
    		event.setCancelled(true);
    		if(clicked.getType() == Material.SKULL_ITEM) {
    			Player sender = Bukkit.getPlayer(clicked.getItemMeta().getDisplayName());
    			request_profile = getRequestProfile(sender);
    			lastClickedSender = sender;
    			player.openInventory(request_profile);
    		}
    	}else if (inventory.getName().equals(Friends.all_players.getName())) {
    		event.setCancelled(true);
    	}else if (inventory.getName().equals(friends.getName())) {
    		event.setCancelled(true);
    		if(clicked.getType() == Material.SKULL_ITEM && !clicked.getItemMeta().getDisplayName().equals("Niemand da..")) {
    			Player friend = Bukkit.getPlayer(clicked.getItemMeta().getDisplayName());
    			profile = getProfile(friend);
    			lastClickedFriend = friend;
    			player.openInventory(profile);
    		}
    	}else if(inventory.getName().contains("§6Freunde §0- §8Profil von ")) {
    		String[] f_tr = inventory.getName().split(" ");
    		lastClickedFriend = Bukkit.getPlayer(f_tr[4]);
    		if (clicked.getType() == Material.ARMOR_STAND) {
    			player.closeInventory();
    			player.sendMessage("§f[§6Friends§f] WiP");
    		} else if (clicked.getType() == Material.ARROW) {
    			player.closeInventory();
    			player.sendMessage("§f[§6Friends§f] WiP");
    		} else if (clicked.getType() == Material.BARRIER) {
    			player.closeInventory();
    		}
    	}else if(inventory.getName().contains("§6Freunde §0- §8Anfrage von ")) {
    		String[] f_tr = inventory.getName().split(" ");
    		lastClickedSender = Bukkit.getPlayer(f_tr[4]);
    		if (clicked.getType() == Material.REDSTONE) {
    			player.closeInventory();
    			player.performCommand("friends decline "+lastClickedSender.getDisplayName());
    		} else if (clicked.getType() == Material.EMERALD) {
    			player.closeInventory();
    			player.performCommand("friends accept "+lastClickedSender.getDisplayName());
    		} else if (clicked.getType() == Material.BARRIER) {
    			player.closeInventory();
    		}
    	}
	}

	private void loadAllPlayers(Player p, ArrayList<String> list) {
		for(int i=0;i<list.size(); i++) {
			ItemStack renamed = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
			String ziel = list.get(i);
			Player target = Bukkit.getPlayer(ziel);
			SkullMeta skull = (SkullMeta) renamed.getItemMeta();
	        skull.setDisplayName(target.getName());
	        ArrayList<String> lore = new ArrayList<String>();
	        lore.add(p.getDisplayName());
	        skull.setLore(lore);
	        skull.setOwningPlayer(target);
	        renamed.setItemMeta(skull);
			Friends.all_players.setItem(i, new ItemStack(renamed));
		}
	}
	
	private Inventory loadReqs(Player p, Inventory reqss) {
		Inventory reqs = reqss;
		String query = "SELECT from_id,to_id FROM request WHERE to_id='" + p.getUniqueId().toString() + "' AND status='" + 0 + "'";
	    Statement st;
	    try {
	    	st = (Statement) Main.con.createStatement();
	    	ResultSet rs = st.executeQuery(query);
	    	int count=0;
	    	while(rs.next()) {
	    		Player sender = Bukkit.getPlayer(UUID.fromString(rs.getString("from_id")));
		    	ItemStack renamed = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		    	SkullMeta skull = (SkullMeta) renamed.getItemMeta();
		    	skull.setDisplayName(sender.getName());
		    	ArrayList<String> lore = new ArrayList<String>();
		    	lore.add(p.getDisplayName());
		    	skull.setLore(lore);
		    	skull.setOwningPlayer(sender);
		    	renamed.setItemMeta(skull);
		    	reqs.setItem(count, new ItemStack(renamed));
		    }
	    }catch(Exception e) {
	    	
	    }
	    return reqs;
	}
	
	private Inventory loadFriends(Player p, Inventory pFriends) {
		Inventory friends = pFriends;
		String friend_ids = "";
		String query = "SELECT friend_ids FROM friends WHERE player_id='" + p.getUniqueId().toString() + "'";
	    Statement st;
	    try {
	    	st = (Statement) Main.con.createStatement();
	    	ResultSet rs = st.executeQuery(query);
	    	if(rs.next()) {
	    		friend_ids = ""+rs.getString("friend_ids");
	    		if(!friend_ids.equals("0")) {
	    			String[] friendss = friend_ids.split(",");
		    		//UUID von Freunden laden
		    		UUID[] freunde = new UUID[friendss.length];
		    		for(int i=1; i<friendss.length; i++) {
		    			query = "SELECT uuid FROM players WHERE id='" + Integer.parseInt(friendss[i]) + "'";
		    		    try {
		    		    	st = (Statement) Main.con.createStatement();
		    		    	rs = st.executeQuery(query);
		    		    	if(rs.next()) {
		    		    		freunde[i] = UUID.fromString(rs.getString("uuid"));
		    		    	}
		    		    }catch(Exception e) {
		    		    	
		    		    }
		    		}
		    		for(int i=1;i<freunde.length;i++) {
		    			Player target = Bukkit.getPlayer(freunde[i]);
		    			ItemStack renamed = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		    			SkullMeta skull = (SkullMeta) renamed.getItemMeta();
		    	        skull.setDisplayName(target.getDisplayName());
		    	        ArrayList<String> lore = new ArrayList<String>();
		    	        lore.add(p.getDisplayName());
		    	        skull.setLore(lore);
		    	        skull.setOwningPlayer(target);
		    	        renamed.setItemMeta(skull);
		    	        friends.setItem(i, new ItemStack(renamed));
		    		}
	    		}else {
		    		//keine Freunde
		    		ItemStack renamed = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	    			SkullMeta skull = (SkullMeta) renamed.getItemMeta();
	    	        skull.setDisplayName("Niemand da..");
	    	        ArrayList<String> lore = new ArrayList<String>();
	    	        lore.add("Du hast leider noch keine Freunde :(");
	    	        skull.setLore(lore);
	    	        renamed.setItemMeta(skull);
	    	        friends.setItem(0, new ItemStack(renamed));
		    	}
	    	}
	    }catch(Exception e) {
	    	
	    }
	    return friends;
	}

	private Inventory getRequestProfile(Player sender) {
		Inventory request_profile = null;
		String status = "";
		if(sender.isOnline()) {
			status = "§a§l(Online)";
		}else {
			status = "§c§l(Offline)";
		}
		
		request_profile = Bukkit.createInventory(null, 9, "§6Freunde §0- §8Anfrage von "+sender.getDisplayName() + " "+status);
		{
	    	ItemStack schliessen = new ItemStack(Material.BARRIER, 1);
	    	ItemMeta schliessenMeta = schliessen.getItemMeta();
	    	schliessenMeta.setDisplayName("Schließen");
	    	schliessenMeta.setLore(Arrays.asList("Menü schließen.."));
	    	schliessen.setItemMeta(schliessenMeta);
	    	
	    	ItemStack infos = new ItemStack(Material.EMERALD, 1);
	    	ItemMeta infosmeta = infos.getItemMeta();
	    	infosmeta.setDisplayName("Annehmen");
	    	infosmeta.setLore(Arrays.asList("Testfunktion"));
	    	infos.setItemMeta(infosmeta);
	    	
	    	ItemStack test = new ItemStack(Material.REDSTONE, 1);
	    	ItemMeta testMeta = test.getItemMeta();
	    	testMeta.setDisplayName("Ablehnen");
	    	testMeta.setLore(Arrays.asList("Testfunktion"));
	    	test.setItemMeta(testMeta);
	    	
	    	request_profile.setItem(8, new ItemStack(schliessen));
	    	request_profile.setItem(3, new ItemStack(infos));
	    	request_profile.setItem(5, new ItemStack(test));
		}
		
		return request_profile;
	}
	
	private Inventory getProfile(Player friend) {
		Inventory profile = null;
		String status = "";
		if(friend.isOnline()) {
			status = "§a§l(Online)";
		}else {
			status = "§c§l(Offline)";
		}
		
		profile = Bukkit.createInventory(null, 9, "§6Freunde §0- §8Profil von "+friend.getDisplayName() + " "+status);
		{
	    	ItemStack schliessen = new ItemStack(Material.BARRIER, 1);
	    	ItemMeta schliessenMeta = schliessen.getItemMeta();
	    	schliessenMeta.setDisplayName("Schließen");
	    	schliessenMeta.setLore(Arrays.asList("Menü schließen.."));
	    	schliessen.setItemMeta(schliessenMeta);
	    	
	    	ItemStack infos = new ItemStack(Material.ARMOR_STAND, 1);
	    	ItemMeta infosmeta = infos.getItemMeta();
	    	infosmeta.setDisplayName("Infos");
	    	infosmeta.setLore(Arrays.asList("Testfunktion"));
	    	infos.setItemMeta(infosmeta);
	    	
	    	ItemStack test = new ItemStack(Material.ARROW, 1);
	    	ItemMeta testMeta = test.getItemMeta();
	    	testMeta.setDisplayName("Testfunktion");
	    	testMeta.setLore(Arrays.asList("Testfunktion"));
	    	test.setItemMeta(testMeta);
	    	
	    	profile.setItem(8, new ItemStack(schliessen));
	    	profile.setItem(3, new ItemStack(infos));
	    	profile.setItem(5, new ItemStack(test));
		}
		
		return profile;
	}
}
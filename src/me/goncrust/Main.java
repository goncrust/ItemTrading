package me.goncrust;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	EventListener listener = new EventListener();
	
	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage("Ready for trades!");
		getServer().getPluginManager().registerEvents(listener, this);
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage("Closing trades...");
	}
	
	
	//animaçao de comer quando entras
	//dropas no reload se tiver na mao
}

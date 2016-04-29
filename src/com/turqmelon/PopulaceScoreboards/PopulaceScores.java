package com.turqmelon.PopulaceScoreboards;


import com.turqmelon.Populace.Events.Resident.ResidentJoinTownEvent;
import com.turqmelon.Populace.Events.Resident.ResidentLeaveTownEvent;
import com.turqmelon.Populace.Events.Town.TownCreationEvent;
import com.turqmelon.Populace.Resident.Resident;
import com.turqmelon.Populace.Resident.ResidentManager;
import com.turqmelon.Populace.Town.Town;
import com.turqmelon.Populace.Town.TownManager;
import com.turqmelon.Populace.Town.TownRank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/******************************************************************************
 * Copyright (c) 2016.  Written by Devon "Turqmelon": http://turqmelon.com    *
 * For more information, see LICENSE.TXT.                                     *
 ******************************************************************************/

public class PopulaceScores extends JavaPlugin implements Listener {

    private static Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private static Map<UUID, Long> idstorage = new HashMap<>();
    private static long nextID = 1;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(ResidentJoinTownEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onJoin(ResidentLeaveTownEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onEvent(TownCreationEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onEvent(PlayerJoinEvent event){
        new BukkitRunnable(){
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    public static long assignID(Town town){
        if (idstorage.containsKey(town.getUuid())){
            return idstorage.get(town.getUuid());
        }
        long thisid = nextID;
        idstorage.put(town.getUuid(), thisid);
        PopulaceScores.nextID++;
        return thisid;
    }

    public static void updateScoreboard(Player player) {

        Scoreboard sb;
        if (scoreboards.containsKey(player.getUniqueId())){
            sb = scoreboards.get(player.getUniqueId());;
        }
        else{
            sb = Bukkit.getScoreboardManager().getNewScoreboard();
            scoreboards.put(player.getUniqueId(), sb);
        }

        for(Town town : TownManager.getTowns()){
            long id = assignID(town);
            for(TownRank rank : TownRank.values()){
                Team team;
                String teamName = id + ":" + rank.getPermissionLevel();
                if (sb.getTeam(teamName) == null){
                    team = sb.registerNewTeam(teamName);
                }
                else{
                    team = sb.getTeam(teamName);
                }
                if (team.getDisplayName() != null && team.getDisplayName().startsWith("updated"))continue;
                team.setAllowFriendlyFire(true);
                team.setCanSeeFriendlyInvisibles(false);
                team.setDisplayName("updated" + id);
                team.setPrefix(rank.getPrefix());
                String townName = town.getName();
                if (townName.length() > 13){
                    townName = townName.substring(0, 12);
                }
                townName = " [" + townName + "]";
                team.setSuffix(townName);
            }

        }
        for(Player p : Bukkit.getOnlinePlayers()){
            Resident resident = ResidentManager.getResident(p);
            if (resident==null)continue;
            String memberOf = null;
            if (resident.getTown() != null){
                long id = assignID(resident.getTown());
                TownRank rank = resident.getTown().getRank(resident);
                String teamName = id+ ":" + rank.getPermissionLevel();
                Team team = sb.getTeam(teamName);
                if (team != null && !team.hasEntry(p.getName())){
                    team.addEntry(p.getName());
                }
                if (team != null){
                    memberOf = team.getName();
                }
            }
            for(Team team : sb.getTeams()){
                if (memberOf!=null&&team.getName().equals(memberOf))continue;
                if (team.hasEntry(p.getName())){
                    team.removeEntry(p.getName());
                }
            }

        }

        player.setScoreboard(sb);

    }

}

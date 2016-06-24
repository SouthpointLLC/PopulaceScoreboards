package com.turqmelon.PopulaceScoreboards;


import com.turqmelon.Populace.Events.Resident.ResidentJoinTownEvent;
import com.turqmelon.Populace.Events.Resident.ResidentLeaveTownEvent;
import com.turqmelon.Populace.Events.Resident.ResidentPrefixUpdatedEvent;
import com.turqmelon.Populace.Events.Resident.ResidentRankChangedEvent;
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
    private static Map<String, Integer> prefixIndex = new HashMap<>();
    private static long nextID = 1;

    public static long assignID(Town town) {
        if (idstorage.containsKey(town.getUuid())) {
            return idstorage.get(town.getUuid());
        }
        long thisid = nextID;
        idstorage.put(town.getUuid(), thisid);
        PopulaceScores.nextID++;
        return thisid;
    }

    private static boolean isIndexTaken(int index) {
        for (int i : prefixIndex.values()) {
            if (i == index) {
                return true;
            }
        }
        return false;
    }

    public static void updateScoreboard(Player player) {

        Scoreboard sb;
        if (scoreboards.containsKey(player.getUniqueId())) {
            sb = scoreboards.get(player.getUniqueId());
        } else {
            sb = Bukkit.getScoreboardManager().getNewScoreboard();
            scoreboards.put(player.getUniqueId(), sb);
        }

        for (Town town : TownManager.getTowns()) {
            long id = assignID(town);
            Map<String, String> possibilities = new HashMap<>();
            int startID = 0;
            for (TownRank rank : TownRank.values()) {
                possibilities.put(rank.getPermissionLevel() + "", rank.getPrefix());
                int newStartID = rank.getPermissionLevel() + 1;
                if (newStartID > startID) {
                    startID = newStartID;
                }
            }
            for (Resident resident : town.getResidents().keySet()) {
                if (resident.getPrefix() != null) {
                    if (!prefixIndex.containsKey(resident.getPrefix())) {
                        while (isIndexTaken(startID)) {
                            startID++;
                        }
                        prefixIndex.put(resident.getPrefix(), startID);
                        startID++;
                    }
                    int index = prefixIndex.get(resident.getPrefix());
                    possibilities.put(index + "", "§6" + resident.getPrefix() + " ");
                }
            }

            for (String i : possibilities.keySet()) {
                String prefix = possibilities.get(i);
                Team team;
                String teamName = id + ":" + i;
                if (sb.getTeam(teamName) == null) {
                    team = sb.registerNewTeam(teamName);
                } else {
                    team = sb.getTeam(teamName);
                }
                if (team.getDisplayName() != null && team.getDisplayName().startsWith("updated")) continue;
                team.setAllowFriendlyFire(true);
                team.setCanSeeFriendlyInvisibles(false);
                team.setDisplayName("updated" + id);
                team.setPrefix(prefix + "§f");
                String townName = town.getName();
                if (townName.length() > 11) {
                    townName = townName.substring(0, 10);
                }
                townName = "§7 [" + townName + "]";
                team.setSuffix(townName);
            }

        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            Resident resident = ResidentManager.getResident(p);
            if (resident == null) continue;
            String memberOf = null;
            if (resident.getTown() != null) {
                long id = assignID(resident.getTown());
                Team team = null;
                if (resident.getPrefix() != null && prefixIndex.containsKey(resident.getPrefix())) {
                    int index = prefixIndex.get(resident.getPrefix());
                    String teamName = id + ":" + index;
                    team = sb.getTeam(teamName);
                } else {
                    TownRank rank = resident.getTown().getRank(resident);
                    String teamName = id + ":" + rank.getPermissionLevel();
                    team = sb.getTeam(teamName);
                }
                if (team != null && !team.hasEntry(p.getName())) {
                    team.addEntry(p.getName());
                }
                if (team != null) {
                    memberOf = team.getName();
                }
            }
            for (Team team : sb.getTeams()) {
                if (memberOf != null && team.getName().equals(memberOf)) continue;
                if (team.hasEntry(p.getName())) {
                    team.removeEntry(p.getName());
                }
            }

        }

        player.setScoreboard(sb);

    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onEvent(ResidentRankChangedEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onEvent(ResidentPrefixUpdatedEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onEvent(ResidentJoinTownEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onEvent(ResidentLeaveTownEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onEvent(TownCreationEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

    @EventHandler
    public void onEvent(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(PopulaceScores::updateScoreboard);
            }
        }.runTaskLater(this, 10L);
    }

}


package id.server.deathhistory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class DeathHistory extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("DeathHistory berhasil diaktifkan!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        String path = "players." + victim.getUniqueId();
        String cause = getDeathCause(victim);
        String time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                .format(new Date());

        String entry = time + " | " + cause
                + " | Dunia: " + victim.getWorld().getName();

        getConfig().set(path + ".name", victim.getName());

        List<String> history = new ArrayList<>(
                getConfig().getStringList(path + ".history")
        );

        history.add(0, entry);
        getConfig().set(path + ".history", history);
        getConfig().set(path + ".totalDeaths", history.size());

        saveConfig();
    }

    private String getDeathCause(Player victim) {
        Player killer = victim.getKiller();

        if (killer != null) {
            return "Dibunuh oleh player " + killer.getName();
        }

        EntityDamageEvent damage = victim.getLastDamageCause();

        if (damage instanceof EntityDamageByEntityEvent entityDamage) {
            Entity damager = entityDamage.getDamager();

            if (damager instanceof Projectile projectile) {
                ProjectileSource shooter = projectile.getShooter();

                if (shooter instanceof Player player) {
                    return "Dibunuh oleh player " + player.getName();
                }

                if (shooter instanceof LivingEntity mob) {
                    return "Dibunuh oleh mob "
                            + formatName(mob.getType().name());
                }
            }

            if (damager instanceof LivingEntity mob) {
                return "Dibunuh oleh mob "
                        + formatName(mob.getType().name());
            }

            return "Dibunuh oleh "
                    + formatName(damager.getType().name());
        }

        if (damage == null) {
            return "Penyebab tidak diketahui";
        }

        return switch (damage.getCause()) {
            case FALL -> "Jatuh";
            case FIRE, FIRE_TICK -> "Terbakar";
            case LAVA -> "Lava";
            case DROWNING -> "Tenggelam";
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> "Ledakan";
            case VOID -> "Jatuh ke void";
            case SUFFOCATION -> "Terjepit";
            case STARVATION -> "Kelaparan";
            case POISON -> "Racun";
            case MAGIC -> "Efek sihir";
            case FREEZE -> "Membeku";
            case LIGHTNING -> "Petir";
            case CONTACT -> "Terkena duri atau blok berbahaya";
            case HOT_FLOOR -> "Lantai panas";
            case CRAMMING -> "Tertindih entitas";
            case DRYOUT -> "Kehabisan air";
            default -> "Penyebab: "
                    + formatName(damage.getCause().name());
        };
    }

    private String formatName(String name) {
        String result = name.toLowerCase(Locale.ROOT)
                .replace('_', ' ');
        return result.substring(0, 1).toUpperCase(Locale.ROOT)
                + result.substring(1);
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!command.getName().equalsIgnoreCase("riwayat")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini hanya untuk pemain.");
            return true;
        }

        if (args.length > 0) {
            player.sendMessage(ChatColor.YELLOW + "Gunakan /riwayat");
            return true;
        }

        UUID uuid = player.getUniqueId();
        String path = "players." + uuid;
        List<String> history = getConfig()
                .getStringList(path + ".history");

        player.sendMessage(ChatColor.GOLD + "===== RIWAYAT KEMATIAN =====");
        player.sendMessage(ChatColor.GRAY + "Total tercatat: "
                + history.size());

        if (history.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW
                    + "Belum ada riwayat kematian yang tercatat.");
            return true;
        }

        for (int i = 0; i < history.size(); i++) {
            player.sendMessage(ChatColor.YELLOW + "#" + (i + 1)
                    + " " + ChatColor.WHITE + history.get(i));
        }

        return true;
    }
    }
      

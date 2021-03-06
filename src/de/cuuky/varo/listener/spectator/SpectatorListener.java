package de.cuuky.varo.listener.spectator;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import de.cuuky.cfw.utils.listener.EntityDamageByEntityUtil;
import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.configuration.configurations.language.languages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.stats.stat.PlayerState;
import de.cuuky.varo.game.state.GameState;
import de.cuuky.varo.vanish.Vanish;

public class SpectatorListener implements Listener {

    private static final int BLOCK_INTERACT_DISTANCE = 60;

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entityDamager = event.getDamager();
        Entity entityDamaged = event.getEntity();

        if (cancelEvent(event.getEntity())) {
            if (entityDamager instanceof Projectile) {
                if (((Projectile) entityDamager).getShooter() instanceof Player) {
                    Projectile projectile = (Projectile) entityDamager;

                    Player shooter = (Player) projectile.getShooter();
                    Player damaged = (Player) entityDamaged;

                    if (Vanish.getVanish((Player) entityDamaged) != null) {
                        damaged.teleport(entityDamaged.getLocation().add(0, 5, 0));

                        Projectile newProjectile = (Projectile) projectile.getWorld().spawnEntity(projectile.getLocation(), projectile.getType());
                        newProjectile.setShooter(shooter);
                        newProjectile.setVelocity(projectile.getVelocity());
                        newProjectile.setBounce(projectile.doesBounce());

                        event.setCancelled(true);
                        projectile.remove();
                    }
                }
            }
        }

        if (!event.isCancelled()) {
            Player damager = new EntityDamageByEntityUtil(event).getDamager();
            if (damager != null && VaroPlayer.getPlayer(damager).getStats().isSpectator())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        this.checkBlockEvent(event, event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.checkBlockEvent(event, event.getPlayer());
    }

    @EventHandler
    public void onProjectile(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player)
            this.checkBlockEvent(event, (Player) event.getEntity().getShooter());
    }

    private void checkBlockEvent(Cancellable event, Player player) {
        if (!event.isCancelled() && Main.getVaroGame().getGameState() == GameState.STARTED && VaroPlayer.getPlayer(player).getStats().isSpectator()) {
            for (Entity entity : player.getNearbyEntities(BLOCK_INTERACT_DISTANCE, BLOCK_INTERACT_DISTANCE, BLOCK_INTERACT_DISTANCE)) {
                if (!(entity instanceof Player))
                    continue;

                VaroPlayer vp = VaroPlayer.getPlayer((Player) entity);
                if (!vp.getStats().isSpectator()) {
                    event.setCancelled(true);
                    player.sendMessage(Main.getPrefix() + "You can't interact with the world in close proximity to another player!");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof ExperienceOrb && event.getTarget() instanceof Player) {
            VaroPlayer vp = VaroPlayer.getPlayer((Player) event.getTarget());
            if (vp.getStats().isSpectator())
                event.setTarget(null);
        }

        if (Main.getVaroGame().getGameState() == GameState.LOBBY)
            event.setCancelled(true);

        if (cancelEvent(event.getTarget()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLose(FoodLevelChangeEvent event) {
        if (cancelEvent(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onHealthLose(EntityDamageEvent event) {
        if (cancelEvent(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (Main.getVaroGame().getGameState() == GameState.LOBBY && !event.getPlayer().isOp() || cancelEvent(event.getPlayer()))
            event.setCancelled(true);
        else this.checkBlockEvent(event, event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        VaroPlayer vp = VaroPlayer.getPlayer(event.getPlayer());
        this.checkBlockEvent(event, event.getPlayer());
        if (!Main.getVaroGame().hasStarted() && event.getPlayer().getGameMode() != GameMode.CREATIVE)
            event.setCancelled(true);

        if (vp.isInProtection() && (!vp.isAdminIgnore() && vp.getStats().getState() != PlayerState.SPECTATOR))
            event.setCancelled(true);

        if (cancelEvent(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryMove(InventoryDragEvent event) {
        if (cancelEvent(event.getWhoClicked()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerPickupItemEvent event) {
        if (Main.getVaroGame().getGameState() == GameState.LOBBY && !event.getPlayer().isOp() || cancelEvent(event.getPlayer()))
            event.setCancelled(true);
        else this.checkBlockEvent(event, event.getPlayer());
    }

    @EventHandler
    public void onItemPickup(PlayerDropItemEvent event) {
        if (Main.getVaroGame().getGameState() == GameState.LOBBY && !event.getPlayer().isOp() || cancelEvent(event.getPlayer()))
            event.setCancelled(true);
        else this.checkBlockEvent(event, event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.getPlayer().isOp())
            if (cancelEvent(event.getPlayer()))
                if (event.getTo().getY() < ConfigSetting.MINIMAL_SPECTATOR_HEIGHT.getValueAsInt()) {
                    Location tp = event.getFrom();
                    tp.setY(ConfigSetting.MINIMAL_SPECTATOR_HEIGHT.getValueAsInt());
                    event.setTo(tp);
                    VaroPlayer vp = VaroPlayer.getPlayer(event.getPlayer());
                    vp.sendMessage(ConfigMessages.NOPERMISSION_NO_LOWER_FLIGHT, vp);
                }
        return;

    }

    private static boolean cancelEvent(Entity interact) {
        if (!(interact instanceof Player))
            return false;

        Player player = (Player) interact;

        if (Vanish.getVanish(player) == null || player.getGameMode() != GameMode.ADVENTURE)
            return false;

        return true;
    }
}
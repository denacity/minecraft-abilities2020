package com.gmail.mattdiamond98.coronacraft.abilities.Wizard.Thaumaturge;

import com.gmail.mattdiamond98.coronacraft.CoronaCraft;
import com.gmail.mattdiamond98.coronacraft.abilities.Ability;
import com.gmail.mattdiamond98.coronacraft.abilities.Wizard.WizardStyle;
import com.gmail.mattdiamond98.coronacraft.abilities.Wizard.WizardUtil;
import com.gmail.mattdiamond98.coronacraft.util.AbilityUtil;
import com.tommytony.war.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Deluge extends WizardStyle {

    public static final int MANA_COST = 3;

    public static final int COOLDOWN_ABILITY_TICKS = 1;

    public Deluge() {
        super("Deluge", new String[]{
                        "Deluge",
                        String.format("%d Mana", MANA_COST),
                        "",
                        "Left click with wand to cast"
                },
                new Ability("Deluge", Material.POTION) {
                    @EventHandler
                    public void potionSplash(PotionSplashEvent event) {
                        if (event.getEntity().getShooter() instanceof Player && event.getEntity().getEffects().isEmpty()) {
                            event.setCancelled(true);
                            Player p = (Player) event.getEntity().getShooter();
                            Team team = Team.getTeamByPlayerName(p.getName());
                            event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), 1, 1, 1).stream()
                                    .filter(entity -> entity instanceof Player)
                                    .map(entity -> (Player) entity)
                                    .filter(member -> Team.getTeamByPlayerName(member.getName()) != null)
                                    .filter(member -> !Team.getTeamByPlayerName(member.getName()).equals(team))
                                    .filter(member -> !AbilityUtil.inSpawn(member)).forEach(hit -> {
                                        hit.damage(6, p);
                                        hit.getWorld().playSound(hit.getLocation(), Sound.ENTITY_DROWNED_HURT_WATER, 0.5F, 0.5F);
                                        hit.getWorld().playEffect(hit.getEyeLocation(), Effect.STEP_SOUND, Material.WATER);
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(CoronaCraft.instance, () -> {
                                            hit.setVelocity(event.getEntity().getLocation().getDirection().setY(0.1).normalize().multiply(0.3));
                                        }, 2);
                                    }

                            );
                        }
                    }
                });
        ability.getStyles().add(this);
    }

    @Override
    public int execute(Player p, Object... args) {
        if (WizardUtil.handleManaCostAndCooldown(p, this, MANA_COST, true)) {
            p.setLevel(p.getLevel() - MANA_COST);
            CoronaCraft.setCooldown(p, getItem(), COOLDOWN_ABILITY_TICKS);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BOAT_PADDLE_WATER, 0.5F, 0.5F);
            final ThrownPotion projectile = p.launchProjectile(ThrownPotion.class);
            p.getWorld().playEffect(p.getEyeLocation(), Effect.STEP_SOUND, Material.WATER);
            projectile.setVelocity(projectile.getVelocity().add(p.getLocation().getDirection().normalize().multiply(2)));
            new BukkitRunnable() {
                @Override
                public void run() {
                    projectile.getWorld().spawnParticle(Particle.WATER_SPLASH, projectile.getLocation(), 10, 1, 1, 1);
                    if (projectile.isDead()) {
                        cancel();
                    }
                }
            }.runTaskTimer(CoronaCraft.instance, 1, 2);
        }
        return 0;
    }

}

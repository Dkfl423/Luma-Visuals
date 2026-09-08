package dev.luma.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import java.util.Random;

/** Client-local state. No server messages, entity scans, files or external connections. */
public final class VisualState {
    private final Random random = new Random();
    private LivingEntity target;
    private long targetSeen;
    private long lastSparks;
    private Object levelIdentity;
    private boolean wasOnGround;
    private boolean sampled;
    private double previousX, previousZ;
    private int ticks, particlesThisTick;
    public double speed;

    private static long now() { return System.nanoTime() / 1_000_000L; }
    public void tick(Minecraft client) {
        particlesThisTick = 0;
        if (client.player == null || client.level == null) { clear(); return; }
        if (client.level != levelIdentity) { clear(); levelIdentity = client.level; }
        var player = client.player;
        if (client.isPaused()) return;
        ticks++;
        if (sampled) {
            double traveled = Math.hypot(player.getX() - previousX, player.getZ() - previousZ);
            speed = traveled < 8 ? traveled * 20 : 0; // Do not count teleports as movement speed.
        }
        previousX = player.getX(); previousZ = player.getZ();
        if (client.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && visible(client, living)) {
            target = living; targetSeen = now();
        }
        if (target != null && (now() - targetSeen > 1200 || !visible(client, target))) target = null;
        LumaConfig cfg = LumaVisualsClient.config;
        if (!cfg.reducedMotion && !player.isSpectator() && !player.getAbilities().flying) {
            if (sampled && wasOnGround && !player.onGround() && player.getDeltaMovement().y > .1 && cfg.jumpRing) {
                int count = Math.min(36, cfg.particleCount * 2);
                for (int i = 0; i < count; i++) {
                    double a = Math.PI * 2 * i / count;
                    dust(client, player.getX() + Math.cos(a) * .64, player.getY() + .02,
                            player.getZ() + Math.sin(a) * .64, .7f);
                }
            }
            if (sampled && cfg.trail && ticks % 3 == 0 && speed > .7 && speed < 30 && player.onGround()) {
                dust(client, player.getX() + (random.nextDouble() - .5) * .25,
                        player.getY() + .12, player.getZ() + (random.nextDouble() - .5) * .25, .55f);
            }
        }
        wasOnGround = player.onGround(); sampled = true;
    }
    public void attacked(Minecraft client, Entity entity) {
        if (!(entity instanceof LivingEntity living) || !visible(client, living)) return;
        target = living; targetSeen = now();
        LumaConfig cfg = LumaVisualsClient.config;
        // The callback is an attack attempt, NOT proof that server damage occurred.
        if (!cfg.attackSparks || cfg.reducedMotion || now() - lastSparks < 120) return;
        lastSparks = now();
        for (int i = 0; i < cfg.particleCount; i++) {
            dust(client, living.getX() + (random.nextDouble() - .5) * (living.getBbWidth() + .5),
                    living.getY() + .2 + random.nextDouble() * living.getBbHeight(),
                    living.getZ() + (random.nextDouble() - .5) * (living.getBbWidth() + .5), .7f);
        }
    }
    private boolean visible(Minecraft client, LivingEntity living) {
        return client.player != null && living != client.player && living.isAlive() && !living.isRemoved()
                && living.level() == client.level && !living.isInvisibleTo(client.player)
                && client.player.distanceTo(living) <= 6 && client.player.hasLineOfSight(living);
    }
    public LivingEntity target(Minecraft client) { return target != null && visible(client, target) ? target : null; }
    private void dust(Minecraft client, double x, double y, double z, float size) {
        if (client.level == null || particlesThisTick >= 48) return;
        particlesThisTick++;
        client.level.addParticle(new DustParticleOptions(LumaVisualsClient.config.accent() & 0xFFFFFF, size),
                x, y, z, 0, .008, 0);
    }
    private void clear() {
        target = null; levelIdentity = null; sampled = false; speed = 0; ticks = 0;
        lastSparks = 0; targetSeen = 0; particlesThisTick = 0;
    }
}

package com.v1mod.host;

import com.v1mod.Fx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

/** Die vier aktiven Faehigkeiten. 0 = Dash, 1 = Schockwelle, 2 = Overdrive, 3 = Blink. */
public final class HostAbilities {
    private static final int[] BASE_COOLDOWN = {80, 160, 1800, 120};

    public static void use(ServerPlayer p, int id) {
        if (!HostData.isHost(p) || id < 0 || id >= HostData.ABILITIES) return;
        ServerLevel level = p.serverLevel();
        long now = level.getGameTime();
        long end = HostData.cooldownEnd(p, id);
        if (now < end) {
            p.displayClientMessage(Component.translatable("msg.v1mod.cooldown", String.format("%.1f", (end - now) / 20.0)), true);
            return;
        }
        int lvl = HostData.level(p);
        switch (id) {
            case 0 -> dash(p, level, lvl);
            case 1 -> shock(p, level, lvl);
            case 2 -> overdrive(p, level, lvl, now);
            case 3 -> { if (!blink(p, level, lvl)) return; }
            default -> { return; }
        }
        int cd = Math.max(20, (int) (BASE_COOLDOWN[id] * (1.0 - 0.05 * (lvl - 1))));
        HostData.setCooldown(p, id, now + cd);
        HostData.sync(p);
    }

    private static void dash(ServerPlayer p, ServerLevel level, int lvl) {
        Vec3 look = p.getLookAngle();
        double speed = 2.2 + 0.15 * lvl;
        p.setDeltaMovement(look.x * speed, Math.max(look.y * speed * 0.6, 0.15), look.z * speed);
        p.hurtMarked = true;
        p.fallDistance = 0;
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12, 4, true, false, false));
        AABB box = p.getBoundingBox().expandTowards(look.scale(7)).inflate(1.5);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == p || !e.isAlive()) continue;
            Vec3 dir = e.position().subtract(p.position());
            if (dir.lengthSqr() < 1.0E-4 || dir.normalize().dot(look) < 0.6) continue;
            DamageSource ds = level.damageSources().playerAttack(p);
            e.hurt(ds, 6.0F + 2.0F * lvl);
            e.push(look.x * 1.3, 0.45, look.z * 1.3);
            e.hurtMarked = true;
        }
        for (int i = 0; i < 12; i++) {
            Vec3 pt = p.position().add(look.scale(i * 0.6)).add(0, 1, 0);
            level.sendParticles(ParticleTypes.SONIC_BOOM, pt.x, pt.y, pt.z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y, pt.z, 3, 0.2, 0.2, 0.2, 0.1);
        }
        Fx.play(level, p.position(), "entity.ender_dragon.flap", 1.0F, 1.5F);
    }

    private static void shock(ServerPlayer p, ServerLevel level, int lvl) {
        V1Combat.shockwave(level, p, p.position(), 6.0 + 0.5 * lvl, 10.0F + 2.0F * lvl, 1.6);
        Fx.play(level, p.position(), "entity.generic.explode", 1.2F, 0.8F);
    }

    private static void overdrive(ServerPlayer p, ServerLevel level, int lvl, long now) {
        HostData.setOverdriveEnd(p, now + (15 + lvl) * 20L);
        HostEvents.applyPowers(p);
        Fx.play(level, p.position(), "entity.wither.spawn", 0.8F, 1.6F);
        p.displayClientMessage(Component.translatable("msg.v1mod.overdrive_on"), true);
    }

    private static boolean blink(ServerPlayer p, ServerLevel level, int lvl) {
        double max = 12 + lvl * 1.5;
        Vec3 look = p.getLookAngle();
        Vec3 eye = p.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(max)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double dist = hit.getType() == HitResult.Type.MISS ? max : Math.max(0, eye.distanceTo(hit.getLocation()) - 0.7);
        Vec3 from = p.position();
        for (double d = dist; d >= 1.0; d -= 0.5) {
            if (level.noCollision(p, p.getBoundingBox().move(look.scale(d)))) {
                Vec3 to = from.add(look.scale(d));
                level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1, from.z, 40, 0.3, 0.6, 0.3, 0.5);
                p.teleportTo(to.x, to.y, to.z);
                p.fallDistance = 0;
                level.sendParticles(ParticleTypes.PORTAL, to.x, to.y + 1, to.z, 40, 0.3, 0.6, 0.3, 0.5);
                Fx.play(level, to, "entity.enderman.teleport", 1.0F, 1.2F);
                return true;
            }
        }
        p.displayClientMessage(Component.translatable("msg.v1mod.blink_blocked"), true);
        return false;
    }

    private HostAbilities() {}
}

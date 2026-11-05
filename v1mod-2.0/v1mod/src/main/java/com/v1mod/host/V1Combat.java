package com.v1mod.host;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class V1Combat {

    /** Druckwelle: Schaden + Rueckstoss in einem Radius, mit Partikelring. */
    public static void shockwave(ServerLevel level, LivingEntity src, Vec3 center, double radius, float damage, double knockback) {
        AABB box = new AABB(center, center).inflate(radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == src || !e.isAlive() || e instanceof ArmorStand) continue;
            if (e instanceof TamableAnimal t && src instanceof Player sp && t.isOwnedBy(sp)) continue;
            if (e instanceof Player ep && src instanceof Player sp && !sp.canHarmPlayer(ep)) continue;
            double d = e.position().distanceTo(center);
            if (d > radius) continue;
            float scaled = (float) (damage * (1.0 - d / (radius * 1.4)));
            DamageSource ds = src instanceof Player p ? level.damageSources().playerAttack(p) : level.damageSources().mobAttack(src);
            e.hurt(ds, scaled);
            Vec3 dir = e.position().subtract(center).multiply(1, 0, 1);
            dir = dir.lengthSqr() < 1.0E-4 ? Vec3.ZERO : dir.normalize();
            e.push(dir.x * knockback, 0.45 + 0.1 * knockback, dir.z * knockback);
            e.hurtMarked = true;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.2, center.z, 3, radius * 0.25, 0.1, radius * 0.25, 0.0);
        int points = (int) (radius * 8);
        for (int i = 0; i < points; i++) {
            double a = (Math.PI * 2 * i) / points;
            level.sendParticles(ParticleTypes.CLOUD, center.x + Math.cos(a) * radius * 0.8, center.y + 0.15, center.z + Math.sin(a) * radius * 0.8, 1, 0, 0.05, 0, 0.02);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x + Math.cos(a) * radius * 0.5, center.y + 0.3, center.z + Math.sin(a) * radius * 0.5, 2, 0.2, 0.3, 0.2, 0.1);
            }
        }
    }

    private V1Combat() {}
}

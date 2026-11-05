package com.v1mod.entity;

import com.v1mod.Fx;
import com.v1mod.ModRegistry;
import com.v1mod.host.HostData;
import com.v1mod.host.V1Combat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Geworfener V1 Speer. Hosts: kehrt von selbst zurueck. Setzt Gegner in Brand. */
public class V1SpearEntity extends AbstractArrow {
    private boolean dealtDamage;

    public V1SpearEntity(EntityType<? extends V1SpearEntity> type, Level level) {
        super(type, level);
    }

    public V1SpearEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModRegistry.SPEAR_ENTITY.get(), owner, level, stack, null);
        this.pickup = AbstractArrow.Pickup.ALLOWED;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModRegistry.V1_SPEAR.get());
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        Entity owner = this.getOwner();
        boolean returns = owner instanceof ServerPlayer sp && HostData.isHost(sp);
        if (returns && owner.isAlive() && (this.dealtDamage || this.isNoPhysics())) {
            this.setNoPhysics(true);
            Vec3 toOwner = owner.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.075, this.getZ());
            if (this.level().isClientSide) {
                this.yOld = this.getY();
            }
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92).add(toOwner.normalize().scale(0.35)));
        }
        if (this.level().isClientSide && !this.inGround) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
        super.tick();
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return this.dealtDamage ? null : super.findHitEntity(start, end);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().trident(this, owner == null ? this : owner);
        if (this.level() instanceof ServerLevel sl) {
            if (target.hurt(source, 16.0F)) {
                target.igniteForSeconds(8);
                if (owner instanceof LivingEntity living) {
                    V1Combat.shockwave(sl, living, target.position(), 3.0, 6.0F, 0.6);
                }
            }
        }
        this.dealtDamage = true;
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        Fx.play(this.level(), this.position(), "item.trident.hit", 1.0F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("DealtDamage", this.dealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.dealtDamage = tag.getBoolean("DealtDamage");
    }
}

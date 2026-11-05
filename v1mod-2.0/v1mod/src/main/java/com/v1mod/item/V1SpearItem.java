package com.v1mod.item;

import com.v1mod.Fx;
import com.v1mod.entity.V1SpearEntity;
import com.v1mod.host.V1Combat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/** Schwert + Speer + Mace in einem. */
public class V1SpearItem extends Item {
    public V1SpearItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers attributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 11.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(com.v1mod.V1Mod.MODID_RL("spear_reach"), 1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    // ---------- Speer: halten und loslassen = werfen ----------
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        int used = getUseDuration(stack) - timeLeft;
        if (used < 10) return;
        if (!level.isClientSide) {
            V1SpearEntity spear = new V1SpearEntity(level, player, stack.copyWithCount(1));
            spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.2F, 1.0F);
            if (player.hasInfiniteMaterials()) {
                spear.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(spear);
            Fx.play(level, player.position(), "item.trident.throw", 1.0F, 1.0F);
            if (!player.hasInfiniteMaterials()) {
                player.getInventory().removeItem(stack);
            }
        }
    }

    // ---------- Schwert ----------
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ability == ItemAbilities.SWORD_SWEEP || ability == ItemAbilities.SWORD_DIG;
    }

    // ---------- Mace: Extraschaden beim Fallen ----------
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        if (source.getEntity() instanceof Player p && p.fallDistance > 1.5F && !p.isFallFlying()) {
            float fd = p.fallDistance;
            float bonus = fd <= 3 ? 6.0F * fd : (fd <= 8 ? 18.0F + 3.0F * (fd - 3) : 33.0F + 1.5F * (fd - 8));
            return bonus;
        }
        return 0.0F;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.igniteForSeconds(6);
        if (attacker instanceof Player p && p.fallDistance > 1.5F && !p.onGround() && p.level() instanceof ServerLevel sl) {
            V1Combat.shockwave(sl, p, target.position(), 4.5, 10.0F, 1.2);
            Fx.play(sl, target.position(), "entity.generic.explode", 1.0F, 1.2F);
            p.resetFallDistance();
        }
        return true;
    }
}

package com.v1mod.host;

import com.v1mod.Fx;
import com.v1mod.ModRegistry;
import com.v1mod.V1Mod;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = V1Mod.MODID)
public final class HostEvents {

    // ------------------------------------------------------------------ Host werden / abgeben
    public static void makeHost(ServerPlayer p, boolean announce) {
        HostData.setHost(p, true);
        grantGear(p);
        applyPowers(p);
        HostData.sync(p);
        if (announce) {
            ServerLevel level = p.serverLevel();
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(p.getX(), p.getY(), p.getZ());
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 25));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("msg.v1mod.host_title")));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("msg.v1mod.host_sub")));
            level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 1.0, 0.6, 0.15);
            Fx.play(level, p.position(), "entity.player.levelup", 1.0F, 0.6F);
        }
    }

    public static void removeHost(ServerPlayer p) {
        HostData.setHost(p, false);
        for (Holder<MobEffect> e : POWER_EFFECTS) {
            p.removeEffect(e);
        }
        p.removeEffect(MobEffects.GLOWING);
        if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModRegistry.V1_BLOCK_ITEM.get())) {
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }
        p.getInventory().clearOrCountMatchingItems(s -> s.is(ModRegistry.V1_SPEAR.get()), -1, p.inventoryMenu.getCraftSlots());
        HostData.sync(p);
    }

    /** Block (mit Fluch der Bindung) auf den Kopf und Speer ins Inventar, falls noch nicht vorhanden. */
    public static void grantGear(ServerPlayer p) {
        ItemStack head = p.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ModRegistry.V1_BLOCK_ITEM.get())) {
            if (!head.isEmpty()) {
                p.getInventory().placeItemBackInInventory(head.copy());
            }
            ItemStack block = new ItemStack(ModRegistry.V1_BLOCK_ITEM.get());
            Holder<Enchantment> binding = p.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE);
            block.enchant(binding, 1);
            p.setItemSlot(EquipmentSlot.HEAD, block);
        }
        giveSpear(p);
    }

    public static boolean giveSpear(ServerPlayer p) {
        boolean has = p.getInventory().hasAnyMatching(s -> s.is(ModRegistry.V1_SPEAR.get()));
        if (has) return false;
        p.getInventory().placeItemBackInInventory(new ItemStack(ModRegistry.V1_SPEAR.get()));
        return true;
    }

    // ------------------------------------------------------------------ Dauer-Kraefte
    private static final java.util.List<Holder<MobEffect>> POWER_EFFECTS = java.util.List.of(
            MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_RESISTANCE, MobEffects.REGENERATION,
            MobEffects.JUMP, MobEffects.DIG_SPEED, MobEffects.HEALTH_BOOST, MobEffects.NIGHT_VISION,
            MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING);

    private static void add(ServerPlayer p, Holder<MobEffect> effect, int amp, int duration) {
        p.addEffect(new MobEffectInstance(effect, duration, amp, true, false, true));
    }

    public static void applyPowers(ServerPlayer p) {
        int lvl = HostData.level(p);
        boolean od = HostData.overdriveActive(p);
        add(p, MobEffects.DAMAGE_BOOST, 1 + lvl / 3 + (od ? 2 : 0), 220);
        add(p, MobEffects.MOVEMENT_SPEED, 1 + lvl / 5 + (od ? 1 : 0), 220);
        add(p, MobEffects.DAMAGE_RESISTANCE, Math.min(3, 1 + lvl / 4), 220);
        add(p, MobEffects.REGENERATION, lvl >= 5 ? 2 : 1, 220);
        add(p, MobEffects.JUMP, 1 + lvl / 4, 220);
        add(p, MobEffects.DIG_SPEED, 1 + lvl / 3, 220);
        add(p, MobEffects.HEALTH_BOOST, 1 + lvl / 2, 220);
        add(p, MobEffects.NIGHT_VISION, 0, 440);
        add(p, MobEffects.FIRE_RESISTANCE, 0, 220);
        add(p, MobEffects.WATER_BREATHING, 0, 220);
        if (lvl >= 3 && p.getFoodData().getFoodLevel() < 20) {
            p.getFoodData().setFoodLevel(20);
        }
        if (od) {
            p.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false, false));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !HostData.isHost(p)) return;
        ServerLevel level = p.serverLevel();
        boolean od = HostData.overdriveActive(p);
        if (od && p.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1.0, p.getZ(), 4, 0.4, 0.8, 0.4, 0.15);
        }
        if (HostData.overdriveFlag(p) && !od) {
            HostData.clearOverdriveFlag(p);
            p.removeEffect(MobEffects.GLOWING);
            p.displayClientMessage(Component.translatable("msg.v1mod.overdrive_off"), true);
            Fx.play(level, p.position(), "block.beacon.deactivate", 1.0F, 1.0F);
            applyPowers(p);
            HostData.sync(p);
        }
        if (p.tickCount % 20 == 0) {
            applyPowers(p);
        }
        if (p.tickCount % 40 == 0 && p.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            grantGear(p);
        }
    }

    // ------------------------------------------------------------------ Kampf
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource src = event.getSource();
        if (src.is(DamageTypes.THORNS)) return;

        if (src.getEntity() instanceof ServerPlayer atk && HostData.isHost(atk)) {
            int lvl = HostData.level(atk);
            float mult = 1.0F + 0.10F * (lvl - 1);
            if (HostData.overdriveActive(atk)) mult *= 1.75F;
            event.setAmount(event.getAmount() * mult);
            if (lvl >= 4) {
                atk.heal(event.getAmount() * 0.08F); // Lebensraub
            }
        }
        if (event.getEntity() instanceof ServerPlayer victim && HostData.isHost(victim)) {
            int lvl = HostData.level(victim);
            if (HostData.overdriveActive(victim)) {
                event.setAmount(event.getAmount() * 0.5F);
            }
            if (lvl >= 6 && src.getEntity() instanceof LivingEntity attacker && attacker != victim) {
                attacker.hurt(victim.damageSources().thorns(victim), event.getAmount() * 0.35F); // Dornen-Aura
            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !HostData.isHost(p)) return;
        float dist = event.getDistance();
        event.setDamageMultiplier(0.0F);
        if (dist >= 4.0F) {
            // Impact-Landung
            float dmg = Math.min(40.0F, dist * 1.5F);
            V1Combat.shockwave(p.serverLevel(), p, p.position(), Math.min(8.0, 3.0 + dist * 0.3), dmg, 1.0);
            Fx.play(p.serverLevel(), p.position(), "entity.generic.explode", 1.0F, 1.0F);
        }
    }

    // ------------------------------------------------------------------ XP / Level
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer p) || !HostData.isHost(p) || victim == p) return;
        int xp;
        if (victim instanceof Player) {
            xp = 25;
        } else if (victim.getType().is(Tags.EntityTypes.BOSSES)) {
            xp = 300;
        } else if (victim instanceof Enemy) {
            xp = Math.max(4, (int) (victim.getMaxHealth() / 2));
        } else {
            xp = 1;
        }
        int before = HostData.level(p);
        boolean up = HostData.addXp(p, xp);
        if (up) {
            int now = HostData.level(p);
            p.displayClientMessage(Component.translatable("msg.v1mod.levelup", now), false);
            p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 15));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("msg.v1mod.levelup_title", now)));
            Fx.play(p.serverLevel(), p.position(), "entity.player.levelup", 1.0F, 1.0F);
            p.serverLevel().sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 40, 0.5, 0.9, 0.5, 0.1);
            applyPowers(p);
        }
        HostData.sync(p);
    }

    // ------------------------------------------------------------------ Tod / Respawn
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && HostData.isHost(p)) {
            event.getDrops().removeIf((ItemEntity e) -> e.getItem().is(ModRegistry.V1_SPEAR.get()) || e.getItem().is(ModRegistry.V1_BLOCK_ITEM.get()));
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        HostData.copy(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            if (HostData.isHost(p)) {
                HostData.clearOverdriveFlag(p);
                grantGear(p);
                applyPowers(p);
            }
            HostData.sync(p);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            HostData.sync(p);
        }
    }

    private HostEvents() {}
}

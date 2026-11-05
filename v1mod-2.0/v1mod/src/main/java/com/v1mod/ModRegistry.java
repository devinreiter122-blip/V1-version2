package com.v1mod;

import com.v1mod.block.V1Block;
import com.v1mod.entity.V1SpearEntity;
import com.v1mod.item.V1SpearItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(V1Mod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(V1Mod.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, V1Mod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, V1Mod.MODID);

    public static final DeferredBlock<V1Block> V1_BLOCK = BLOCKS.registerBlock("v1_block", V1Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 15)
                    .sound(SoundType.AMETHYST));

    public static final DeferredItem<BlockItem> V1_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("v1_block", V1_BLOCK,
            new Item.Properties().rarity(Rarity.EPIC));

    public static final DeferredItem<V1SpearItem> V1_SPEAR = ITEMS.registerItem("v1_spear", V1SpearItem::new,
            new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).attributes(V1SpearItem.attributes()));

    public static final DeferredHolder<EntityType<?>, EntityType<V1SpearEntity>> SPEAR_ENTITY = ENTITIES.register("v1_spear",
            () -> EntityType.Builder.<V1SpearEntity>of(V1SpearEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("v1_spear"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.v1mod"))
                    .icon(() -> new ItemStack(V1_BLOCK.get()))
                    .displayItems((params, output) -> {
                        output.accept(V1_BLOCK.get());
                        output.accept(V1_SPEAR.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        TABS.register(bus);
    }

    private ModRegistry() {}
}

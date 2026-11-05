package com.v1mod;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(V1Mod.MODID)
public class V1Mod {
    public static final String MODID = "v1mod";

    public static ResourceLocation MODID_RL(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public V1Mod(IEventBus modBus) {
        ModRegistry.register(modBus);
    }
}

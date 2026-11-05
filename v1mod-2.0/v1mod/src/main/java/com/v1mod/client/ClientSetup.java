package com.v1mod.client;

import com.v1mod.ModRegistry;
import com.v1mod.V1Mod;
import com.v1mod.network.AbilityPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class ClientSetup {
    private static final String CAT = "key.categories.v1mod";
    /** 0 Dash (R), 1 Schockwelle (G), 2 Overdrive (V), 3 Blink (B) */
    public static final KeyMapping[] KEYS = {
            new KeyMapping("key.v1mod.dash", GLFW.GLFW_KEY_R, CAT),
            new KeyMapping("key.v1mod.shock", GLFW.GLFW_KEY_G, CAT),
            new KeyMapping("key.v1mod.overdrive", GLFW.GLFW_KEY_V, CAT),
            new KeyMapping("key.v1mod.blink", GLFW.GLFW_KEY_B, CAT)
    };

    @EventBusSubscriber(modid = V1Mod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent e) {
            for (KeyMapping k : KEYS) e.register(k);
        }

        @SubscribeEvent
        public static void hud(RegisterGuiLayersEvent e) {
            e.registerAboveAll(V1Mod.MODID_RL("hud"), V1Hud::render);
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
            e.registerEntityRenderer(ModRegistry.SPEAR_ENTITY.get(), V1SpearRenderer::new);
        }
    }

    @EventBusSubscriber(modid = V1Mod.MODID, value = Dist.CLIENT)
    public static final class GameBus {
        @SubscribeEvent
        public static void tick(ClientTickEvent.Post e) {
            if (Minecraft.getInstance().player == null || !ClientState.host) return;
            for (int i = 0; i < KEYS.length; i++) {
                while (KEYS[i].consumeClick()) {
                    PacketDistributor.sendToServer(new AbilityPayload(i));
                }
            }
        }
    }

    private ClientSetup() {}
}

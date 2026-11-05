package com.v1mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class ClientHooks {
    public static void openTerminal(BlockPos pos) {
        Minecraft.getInstance().setScreen(new V1InputScreen(pos));
    }

    private ClientHooks() {}
}

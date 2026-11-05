package com.v1mod.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import com.v1mod.host.HostData;

public final class V1Hud {
    private static final String[] NAMES = {"dash", "shock", "overdrive", "blink"};
    private static final int[] BASE_CD = {80, 160, 1800, 120};

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!ClientState.host || mc.player == null || mc.level == null || mc.options.hideGui) return;
        long now = mc.level.getGameTime();
        int x = 8;
        int y = g.guiHeight() - 78;
        boolean od = now < ClientState.overdriveEnd;
        g.fill(x - 4, y - 4, x + 128, y + 70, 0x99000000);

        Component title = Component.translatable("hud.v1mod.title", ClientState.level);
        g.drawString(mc.font, title, x, y, od ? 0xFFFFE070 : 0xFFC080FF, true);

        int need = HostData.xpNeeded(ClientState.level);
        boolean max = ClientState.level >= HostData.MAX_LEVEL;
        float frac = max ? 1.0F : Math.min(1.0F, ClientState.xp / (float) need);
        g.fill(x, y + 12, x + 120, y + 16, 0xFF2A1040);
        g.fill(x, y + 12, x + (int) (120 * frac), y + 16, 0xFFB070FF);

        for (int i = 0; i < 4; i++) {
            int ry = y + 22 + i * 12;
            long left = ClientState.cooldownEnd[i] - now;
            boolean ready = left <= 0;
            String key = ClientSetup.KEYS[i].getTranslatedKeyMessage().getString();
            Component name = Component.translatable("ability.v1mod." + NAMES[i]);
            g.drawString(mc.font, "[" + key + "] ", x, ry, 0xFF9090A0, false);
            g.drawString(mc.font, name, x + 24, ry, ready ? 0xFFFFFFFF : 0xFF808080, false);
            int bx = x + 84;
            g.fill(bx, ry + 2, bx + 36, ry + 6, 0xFF2A1040);
            float f = ready ? 1.0F : 1.0F - Math.min(1.0F, left / (float) BASE_CD[i]);
            g.fill(bx, ry + 2, bx + (int) (36 * f), ry + 6, ready ? 0xFF70FF90 : 0xFFFF8040);
        }
        if (od) {
            g.drawString(mc.font, Component.translatable("hud.v1mod.overdrive", (ClientState.overdriveEnd - now) / 20), x, y + 70 - 10, 0xFFFFE070, true);
        }
    }

    private V1Hud() {}
}

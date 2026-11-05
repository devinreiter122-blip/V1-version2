package com.v1mod.client;

import com.v1mod.network.SyncPayload;
import net.minecraft.client.Minecraft;

/** Client-Spiegel der Host-Daten (wird vom Server per SyncPayload gefuettert). */
public final class ClientState {
    public static boolean host;
    public static int level = 1;
    public static int xp;
    public static final long[] cooldownEnd = new long[4];
    public static long overdriveEnd;

    public static void apply(SyncPayload p) {
        long now = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        host = p.host();
        level = p.level();
        xp = p.xp();
        for (int i = 0; i < 4; i++) cooldownEnd[i] = now + p.cooldowns()[i];
        overdriveEnd = p.overdrive() > 0 ? now + p.overdrive() : 0L;
    }

    private ClientState() {}
}

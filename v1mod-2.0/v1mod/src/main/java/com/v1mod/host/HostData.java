package com.v1mod.host;

import com.v1mod.network.SyncPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/** Alle Host-Daten liegen im persistenten Spieler-NBT (ueberlebt Tod, Neustart, Dimensionswechsel). */
public final class HostData {
    public static final String ROOT = "v1mod";
    public static final int MAX_LEVEL = 10;
    public static final int ABILITIES = 4;

    private static CompoundTag tag(Player p) {
        CompoundTag pd = p.getPersistentData();
        if (!pd.contains(ROOT, 10)) {
            pd.put(ROOT, new CompoundTag());
        }
        return pd.getCompound(ROOT);
    }

    public static boolean isHost(Player p) { return tag(p).getBoolean("host"); }
    public static void setHost(Player p, boolean v) {
        CompoundTag t = tag(p);
        t.putBoolean("host", v);
        if (v && t.getInt("level") < 1) t.putInt("level", 1);
    }

    public static int level(Player p) { return Math.max(1, tag(p).getInt("level")); }
    public static void setLevel(Player p, int lvl) {
        tag(p).putInt("level", Math.max(1, Math.min(MAX_LEVEL, lvl)));
        tag(p).putInt("xp", 0);
    }
    public static int xp(Player p) { return tag(p).getInt("xp"); }
    public static int xpNeeded(int level) { return 100 * level; }

    /** Gibt true zurueck, wenn ein Level-Up passiert ist. */
    public static boolean addXp(Player p, int amount) {
        CompoundTag t = tag(p);
        int lvl = level(p);
        if (lvl >= MAX_LEVEL) return false;
        int xp = t.getInt("xp") + amount;
        boolean up = false;
        while (lvl < MAX_LEVEL && xp >= xpNeeded(lvl)) {
            xp -= xpNeeded(lvl);
            lvl++;
            up = true;
        }
        t.putInt("level", lvl);
        t.putInt("xp", lvl >= MAX_LEVEL ? 0 : xp);
        return up;
    }

    // ---- Cooldowns (Spielzeit, ab der die Faehigkeit wieder bereit ist) ----
    public static long cooldownEnd(Player p, int ability) { return tag(p).getLong("cd" + ability); }
    public static void setCooldown(Player p, int ability, long endTime) { tag(p).putLong("cd" + ability, endTime); }

    public static long overdriveEnd(Player p) { return tag(p).getLong("overEnd"); }
    public static void setOverdriveEnd(Player p, long t) { tag(p).putLong("overEnd", t); tag(p).putBoolean("overFlag", true); }
    public static boolean overdriveFlag(Player p) { return tag(p).getBoolean("overFlag"); }
    public static void clearOverdriveFlag(Player p) { tag(p).putBoolean("overFlag", false); }
    public static boolean overdriveActive(Player p) {
        return overdriveFlag(p) && p.level().getGameTime() < overdriveEnd(p);
    }

    public static void copy(Player from, Player to) {
        to.getPersistentData().put(ROOT, tag(from).copy());
    }

    public static void sync(ServerPlayer p) {
        long now = p.level().getGameTime();
        int[] cds = new int[ABILITIES];
        for (int i = 0; i < ABILITIES; i++) {
            cds[i] = (int) Math.max(0, cooldownEnd(p, i) - now);
        }
        int over = overdriveActive(p) ? (int) (overdriveEnd(p) - now) : 0;
        PacketDistributor.sendToPlayer(p, new SyncPayload(isHost(p), level(p), xp(p), cds, over));
    }

    private HostData() {}
}

package com.v1mod.host;

import com.v1mod.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/** Verarbeitet, was im V1-Eingabefenster getippt wird. */
public final class Terminal {

    public static void handle(ServerPlayer p, BlockPos pos, String raw) {
        if (raw == null || raw.length() > 64) return;
        String t = raw.trim().toLowerCase(Locale.ROOT);
        if (t.startsWith("/")) t = t.substring(1).trim();
        if (!t.startsWith("v1")) {
            p.displayClientMessage(Component.translatable("msg.v1mod.unknown"), true);
            return;
        }
        String sub = t.substring(2).trim();
        switch (sub) {
            case "host" -> host(p, pos);
            case "status" -> status(p);
            case "spear" -> spear(p);
            case "help", "" -> p.sendSystemMessage(Component.translatable("msg.v1mod.help"));
            default -> p.displayClientMessage(Component.translatable("msg.v1mod.unknown"), true);
        }
    }

    private static void host(ServerPlayer p, BlockPos pos) {
        if (HostData.isHost(p)) {
            p.displayClientMessage(Component.translatable("msg.v1mod.already_host"), true);
            return;
        }
        ServerLevel level = p.serverLevel();
        if (p.distanceToSqr(Vec3.atCenterOf(pos)) > 100 || !level.getBlockState(pos).is(ModRegistry.V1_BLOCK.get())) {
            p.displayClientMessage(Component.translatable("msg.v1mod.no_block"), true);
            return;
        }
        level.removeBlock(pos, false);
        HostEvents.makeHost(p, true);
    }

    public static void status(ServerPlayer p) {
        if (!HostData.isHost(p)) {
            p.sendSystemMessage(Component.translatable("msg.v1mod.not_host"));
            return;
        }
        int lvl = HostData.level(p);
        p.sendSystemMessage(Component.translatable("msg.v1mod.status", lvl, HostData.MAX_LEVEL,
                HostData.xp(p), lvl >= HostData.MAX_LEVEL ? "MAX" : String.valueOf(HostData.xpNeeded(lvl))));
    }

    public static void spear(ServerPlayer p) {
        if (!HostData.isHost(p)) {
            p.sendSystemMessage(Component.translatable("msg.v1mod.not_host"));
            return;
        }
        if (HostEvents.giveSpear(p)) {
            p.displayClientMessage(Component.translatable("msg.v1mod.spear_given"), true);
        } else {
            p.displayClientMessage(Component.translatable("msg.v1mod.spear_have"), true);
        }
    }

    private Terminal() {}
}

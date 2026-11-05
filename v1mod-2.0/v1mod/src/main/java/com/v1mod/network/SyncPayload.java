package com.v1mod.network;

import com.v1mod.V1Mod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> Client: Host-Status fuer HUD und Tasten. Restzeiten in Ticks. */
public record SyncPayload(boolean host, int level, int xp, int[] cooldowns, int overdrive) implements CustomPacketPayload {
    public static final Type<SyncPayload> TYPE = new Type<>(V1Mod.MODID_RL("sync"));
    public static final StreamCodec<FriendlyByteBuf, SyncPayload> CODEC = CustomPacketPayload.codec(SyncPayload::write, SyncPayload::new);

    public SyncPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(),
                new int[]{buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()}, buf.readVarInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(host);
        buf.writeVarInt(level);
        buf.writeVarInt(xp);
        for (int i = 0; i < 4; i++) buf.writeVarInt(cooldowns[i]);
        buf.writeVarInt(overdrive);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

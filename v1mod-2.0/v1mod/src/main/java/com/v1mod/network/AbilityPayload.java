package com.v1mod.network;

import com.v1mod.V1Mod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> Server: Faehigkeit ausloesen (0 Dash, 1 Schockwelle, 2 Overdrive, 3 Blink). */
public record AbilityPayload(int id) implements CustomPacketPayload {
    public static final Type<AbilityPayload> TYPE = new Type<>(V1Mod.MODID_RL("ability"));
    public static final StreamCodec<ByteBuf, AbilityPayload> CODEC = ByteBufCodecs.VAR_INT.map(AbilityPayload::new, AbilityPayload::id);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

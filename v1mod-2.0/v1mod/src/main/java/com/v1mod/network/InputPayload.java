package com.v1mod.network;

import com.v1mod.V1Mod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> Server: Text aus dem V1-Eingabefenster. */
public record InputPayload(BlockPos pos, String text) implements CustomPacketPayload {
    public static final Type<InputPayload> TYPE = new Type<>(V1Mod.MODID_RL("input"));
    public static final StreamCodec<ByteBuf, InputPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, InputPayload::pos,
            ByteBufCodecs.stringUtf8(64), InputPayload::text,
            InputPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

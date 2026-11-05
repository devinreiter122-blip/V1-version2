package com.v1mod.network;

import com.v1mod.V1Mod;
import com.v1mod.client.ClientState;
import com.v1mod.host.HostAbilities;
import com.v1mod.host.Terminal;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = V1Mod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Net {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("2");
        r.playToClient(SyncPayload.TYPE, SyncPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientState.apply(payload)));
        r.playToServer(AbilityPayload.TYPE, AbilityPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer sp) HostAbilities.use(sp, payload.id());
                }));
        r.playToServer(InputPayload.TYPE, InputPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer sp) Terminal.handle(sp, payload.pos(), payload.text());
                }));
    }

    private Net() {}
}

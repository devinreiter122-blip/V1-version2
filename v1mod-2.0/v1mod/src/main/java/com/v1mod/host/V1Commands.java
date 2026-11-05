package com.v1mod.host;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.v1mod.V1Mod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = V1Mod.MODID)
public final class V1Commands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("v1")
                .then(Commands.literal("status").executes(c -> { Terminal.status(c.getSource().getPlayerOrException()); return 1; }))
                .then(Commands.literal("spear").executes(c -> { Terminal.spear(c.getSource().getPlayerOrException()); return 1; }))
                .then(Commands.literal("host").requires(s -> s.hasPermission(2))
                        .executes(c -> { HostEvents.makeHost(c.getSource().getPlayerOrException(), true); return 1; })
                        .then(Commands.argument("targets", EntityArgument.players()).executes(c -> {
                            int n = 0;
                            for (ServerPlayer p : EntityArgument.getPlayers(c, "targets")) { HostEvents.makeHost(p, true); n++; }
                            return n;
                        })))
                .then(Commands.literal("unhost").requires(s -> s.hasPermission(2))
                        .executes(c -> { HostEvents.removeHost(c.getSource().getPlayerOrException()); return 1; })
                        .then(Commands.argument("targets", EntityArgument.players()).executes(c -> {
                            int n = 0;
                            for (ServerPlayer p : EntityArgument.getPlayers(c, "targets")) { HostEvents.removeHost(p); n++; }
                            return n;
                        })))
                .then(Commands.literal("level").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, HostData.MAX_LEVEL)).executes(c -> setLevel(c))))
        );
    }

    private static int setLevel(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        if (!HostData.isHost(p)) {
            c.getSource().sendFailure(Component.translatable("msg.v1mod.not_host"));
            return 0;
        }
        HostData.setLevel(p, IntegerArgumentType.getInteger(c, "level"));
        HostEvents.applyPowers(p);
        HostData.sync(p);
        Terminal.status(p);
        return 1;
    }

    private V1Commands() {}
}

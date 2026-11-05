package com.v1mod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Kleine Helfer fuer Sounds (per Registry-ID, damit es unabhaengig von Holder/SoundEvent-Feldern ist). */
public final class Fx {
    public static SoundEvent sound(String id) {
        SoundEvent e = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.withDefaultNamespace(id));
        return e;
    }

    public static void play(Level level, Vec3 pos, String id, float volume, float pitch) {
        SoundEvent e = sound(id);
        if (e != null) {
            level.playSound(null, pos.x, pos.y, pos.z, e, SoundSource.PLAYERS, volume, pitch);
        }
    }

    private Fx() {}
}

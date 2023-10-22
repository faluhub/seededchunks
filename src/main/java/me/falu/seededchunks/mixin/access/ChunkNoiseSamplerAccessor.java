package me.falu.seededchunks.mixin.access;

import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkNoiseSampler.class)
public interface ChunkNoiseSamplerAccessor {
    @Mutable @Accessor("aquiferSampler") void setAquiferSampler(AquiferSampler aquiferSampler);
}

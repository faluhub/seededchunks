package me.falu.seededchunks.mixin;

import me.falu.seededchunks.OffsetAquiferSampler;
import me.falu.seededchunks.SeededChunks;
import me.falu.seededchunks.mixin.access.ChunkNoiseSamplerAccessor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkNoiseSampler.class)
public class ChunkNoiseSamplerMixin {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void createSwappedChunk(Chunk chunk, NoiseConfig noiseConfig, DensityFunctionTypes.Beardifying beardifying, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender, CallbackInfoReturnable<ChunkNoiseSampler> cir) {
        GenerationShapeConfig genShapeConfig = chunkGeneratorSettings.generationShapeConfig().trimHeight(chunk);
        int cellCount = 16 / genShapeConfig.horizontalCellBlockCount();
        ChunkPos original = chunk.getPos();
        ChunkPos pos = SeededChunks.randomize(original);
        ChunkNoiseSampler sampler = new ChunkNoiseSampler(cellCount, noiseConfig, pos.getStartX(), pos.getStartZ(), genShapeConfig, beardifying, chunkGeneratorSettings, fluidLevelSampler, blender);
        ((ChunkNoiseSamplerAccessor) sampler).setAquiferSampler(new OffsetAquiferSampler(sampler.getAquiferSampler(), pos.getStartX() - original.getStartX(), pos.getStartZ() - original.getStartZ()));
        cir.setReturnValue(sampler);
    }
}

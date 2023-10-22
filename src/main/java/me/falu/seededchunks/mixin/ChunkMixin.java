package me.falu.seededchunks.mixin;

import me.falu.seededchunks.SeededChunks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Shadow public abstract ChunkPos getPos();
    @Shadow public abstract HeightLimitView getHeightLimitView();
    @Shadow public abstract ChunkSection getSection(int yIndex);

    @Inject(method = "populateBiomes", at = @At("HEAD"), cancellable = true)
    private void applyChunkOffset(BiomeSupplier biomeSupplier, MultiNoiseUtil.MultiNoiseSampler sampler, CallbackInfo ci) {
        ChunkPos pos = SeededChunks.randomize(this.getPos());
        int biomeX = BiomeCoords.fromBlock(pos.getStartX());
        int biomeZ = BiomeCoords.fromBlock(pos.getStartZ());
        HeightLimitView heightLimitView = this.getHeightLimitView();
        for (int biomeY = heightLimitView.getBottomSectionCoord(); biomeY < heightLimitView.getTopSectionCoord(); biomeY++) {
            this.getSection(((HeightLimitView) this).sectionCoordToIndex(biomeY)).populateBiomes(biomeSupplier, sampler, biomeX, BiomeCoords.fromChunk(biomeY), biomeZ);
        }
        ci.cancel();
    }
}

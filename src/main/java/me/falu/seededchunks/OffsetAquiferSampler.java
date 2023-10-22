package me.falu.seededchunks;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;

public class OffsetAquiferSampler implements AquiferSampler {
    private final AquiferSampler sampler;
    private final int offsetX;
    private final int offsetZ;

    public OffsetAquiferSampler(AquiferSampler sampler, int offsetX, int offsetZ) {
        this.sampler = sampler;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    @Nullable
    @Override
    public BlockState apply(DensityFunction.NoisePos pos, double density) {
        return this.sampler.apply(new DensityFunction.UnblendedNoisePos(pos.blockX() + this.offsetX, pos.blockY(), pos.blockZ() + this.offsetZ), density);
    }

    @Override
    public boolean needsFluidTick() {
        return this.sampler.needsFluidTick();
    }
}

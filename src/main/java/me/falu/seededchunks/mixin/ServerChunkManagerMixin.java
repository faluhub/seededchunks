package me.falu.seededchunks.mixin;

import me.falu.seededchunks.SeededChunks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Shadow public abstract World getWorld();

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("RETURN"))
    private void swapChunk(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk == null || !create) { return; }
        ServerWorld world = (ServerWorld) this.getWorld();
        if (!SeededChunks.FLIPPED_CHUNKS.contains(chunk.getPos().toLong())) {
            ChunkPos referenceChunkPos = SeededChunks.createNewPos(chunk);
            SeededChunks.FLIPPED_CHUNKS.add(referenceChunkPos.toLong());
            AtomicReference<Chunk> referenceChunk = new AtomicReference<>(chunk);
            MinecraftServer server = world.getServer();
            server.submit(() -> referenceChunk.set(world.getChunk(referenceChunkPos.x, referenceChunkPos.z, ChunkStatus.FULL, true)));
            if (referenceChunk.get() != null) {
                for (int relativeX = 0; relativeX < 16; ++relativeX) {
                    for (int relativeZ = 0; relativeZ < 16; ++relativeZ) {
                        BlockPos.Mutable mutable = new BlockPos.Mutable();
                        BlockPos.Mutable referencePos = new BlockPos.Mutable();
                        int k = (chunk.getPos().getStartX() + relativeX) & 0xF;
                        int l = (chunk.getPos().getStartZ() + relativeZ) & 0xF;
                        int height;
                        try {
                            height = referenceChunk.get().sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, referenceChunkPos.getStartX() + relativeX, referenceChunkPos.getStartZ() + relativeZ) + 1;
                        } catch (NullPointerException ignored) {
                            height = this.getWorld().getSeaLevel();
                        }
                        for (int m = height; m >= 0; --m) {
                            referencePos.set(referenceChunkPos.getStartX() + relativeX, m, referenceChunkPos.getStartZ() + relativeZ);
                            mutable.set(k, m, l);
                            chunk.setBlockState(mutable, referenceChunk.get().getBlockState(referencePos), false);
                        }
                    }
                }
            }
        }
    }
}

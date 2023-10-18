package me.falu.seededchunks.mixin;

import com.mojang.datafixers.util.Either;
import me.falu.seededchunks.SeededChunks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Shadow public abstract World getWorld();
    @Shadow @Final private ServerChunkManager.MainThreadExecutor mainThreadExecutor;
    @Shadow @Nullable protected abstract ChunkHolder getChunkHolder(long pos);

    @Unique
    private void log(String origin, String msg) {
        SeededChunks.log("[" + origin + "] " + msg);
    }

    @Unique
    private void swapChunk(Chunk chunk, String origin) {
        this.mainThreadExecutor.execute(() -> {
            ServerWorld world = (ServerWorld) this.getWorld();
            if (!SeededChunks.FLIPPED_CHUNKS.contains(chunk.getPos().toLong()) && !SeededChunks.BUILT_CHUNKS.contains(chunk.getPos().toLong())) {
                ChunkHolder chunkHolder = this.getChunkHolder(chunk.getPos().toLong());
                if (chunkHolder != null) {
                    ChunkPos referenceChunkPos = SeededChunks.createNewPos(chunk);
                    SeededChunks.FLIPPED_CHUNKS.add(referenceChunkPos.toLong());
                    SeededChunks.BUILT_CHUNKS.add(chunk.getPos().toLong());
                    Chunk referenceChunk = world.getChunk(referenceChunkPos.x, referenceChunkPos.z, ChunkStatus.FULL, true);
                    if (referenceChunk != null) {
                        this.log(origin, "Started building chunk " + referenceChunkPos + " at " + chunk.getPos());
                        for (int relativeX = 0; relativeX < 16; ++relativeX) {
                            for (int relativeZ = 0; relativeZ < 16; ++relativeZ) {
                                BlockPos.Mutable mutable = new BlockPos.Mutable();
                                BlockPos.Mutable referencePos = new BlockPos.Mutable();
                                int k = (chunk.getPos().getStartX() + relativeX) & 0xF;
                                int l = (chunk.getPos().getStartZ() + relativeZ) & 0xF;
                                int height;
                                try {
                                    height = Math.max(
                                            referenceChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, referenceChunkPos.getStartX() + relativeX, referenceChunkPos.getStartZ() + relativeZ) + 1,
                                            chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, k, l)
                                    );
                                } catch (NullPointerException ignored) {
                                    height = this.getWorld().getHeight();
                                }
                                for (int m = height; m >= 0; --m) {
                                    referencePos.set(referenceChunkPos.getStartX() + relativeX, m, referenceChunkPos.getStartZ() + relativeZ);
                                    mutable.set(k, m, l);
                                    BlockState blockState = referenceChunk.getBlockState(referencePos);
                                    try {
                                        chunk.setBlockState(mutable, blockState == null ? Blocks.AIR.getDefaultState() : blockState, false);
                                        chunkHolder.markForBlockUpdate(mutable.getX(), mutable.getY(), mutable.getZ());
                                        chunkHolder.markForLightUpdate(LightType.BLOCK, mutable.getY());
                                        BlockEntity blockEntity = referenceChunk.getBlockEntity(referencePos);
                                        if (blockEntity != null) {
                                            this.log(origin, "Detected block entity at " + referenceChunkPos);
                                            BlockEntity blockEntity1 = blockEntity.getType().instantiate();
                                            if (blockEntity1 != null) {
                                                blockEntity1.setLocation(world, mutable);
                                                chunk.setBlockEntity(mutable, blockEntity1);
                                                this.log(origin, "Placed block entity from " + referenceChunkPos + " in " + chunk.getPos());
                                            }
                                        }
                                    } catch (IndexOutOfBoundsException ignored) {}
                                }
                            }
                        }
                        this.log(origin, "Finished building chunk " + referenceChunkPos + " at " + chunk.getPos());
                    }
                }
            }
        });
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("RETURN"))
    private void interceptChunk1(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk == null) { return; }
        this.swapChunk(chunk, "getChunk");
    }

    @Inject(method = "getWorldChunk", at = @At("RETURN"))
    private void interceptChunk2(int chunkX, int chunkZ, CallbackInfoReturnable<WorldChunk> cir) {
        WorldChunk chunk = cir.getReturnValue();
        if (chunk == null) { return; }
        this.swapChunk(chunk, "getWorldChunk");
    }

    @Inject(method = "getChunkFuture", at = @At("RETURN"))
    private void interceptChunk3(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        cir.getReturnValue().whenCompleteAsync((chunkUnloadedEither, throwable) -> {
            if (throwable == null) {
                chunkUnloadedEither.left().ifPresent(chunk -> this.swapChunk(chunk, "getChunkFuture"));
            } else {
                throw new RuntimeException(throwable);
            }
        });
    }
}

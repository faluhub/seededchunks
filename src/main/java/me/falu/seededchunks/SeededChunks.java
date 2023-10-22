package me.falu.seededchunks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.*;

import java.util.Random;

public class SeededChunks implements ClientModInitializer {
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("seededchunks").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    private static final int BOUND = 50000;

    public static ChunkPos randomizeChunk(ChunkPos pos) {
        Random random = new Random(pos.toLong());
        return new ChunkPos(random.nextInt(-BOUND, BOUND), random.nextInt(-BOUND, BOUND));
    }

    public static BlockPos randomizeBlock(BlockPos pos) {
        ChunkPos original = new ChunkPos(pos);
        int offsetX = pos.getX() - original.getStartX();
        int offsetZ = pos.getZ() - original.getStartZ();
        ChunkPos other = SeededChunks.randomizeChunk(original);
        int x = other.getStartX() + offsetX;
        int z = other.getStartZ() + offsetZ;
        return new BlockPos(x, pos.getY(), z);
    }

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    @Override
    public void onInitializeClient() {
        log("Using " + MOD_NAME + " v" + MOD_VERSION);
    }
}

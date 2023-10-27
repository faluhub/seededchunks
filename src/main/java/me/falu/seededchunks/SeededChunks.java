package me.falu.seededchunks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import org.apache.logging.log4j.*;

import java.util.Random;

public class SeededChunks implements ClientModInitializer {
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("seededchunks").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    private static final int BOUND = 50000;

    public static ChunkPos randomizeChunk(ColumnPos pos) {
        return randomizeChunk(pos.toChunkPos());
    }

    public static ChunkPos randomizeChunk(ChunkPos pos) {
        Random random = new Random(pos.toLong());
        return new ChunkPos(random.nextInt(-BOUND, BOUND), random.nextInt(-BOUND, BOUND));
    }

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    @Override
    public void onInitializeClient() {
        log("Using " + MOD_NAME + " v" + MOD_VERSION);
    }
}

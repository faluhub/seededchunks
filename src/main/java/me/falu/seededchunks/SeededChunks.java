package me.falu.seededchunks;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.*;

import java.util.Random;

public class SeededChunks implements ClientModInitializer {
    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("seededchunks").orElseThrow(RuntimeException::new);
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final String MOD_VERSION = String.valueOf(MOD_CONTAINER.getMetadata().getVersion());
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final LongSet FLIPPED_CHUNKS = new LongOpenHashSet();
    public static final LongSet BUILT_CHUNKS = new LongOpenHashSet();

    public static void log(Object msg) {
        LOGGER.log(Level.INFO, msg);
    }

    @Override
    public void onInitializeClient() {
        log("Using " + MOD_NAME + " v" + MOD_VERSION);
    }

    public static ChunkPos createNewPos(Chunk chunk) {
        return createNewPos(chunk.getPos());
    }

    public static ChunkPos createNewPos(ChunkPos pos) {
        int bounds = 5000;
        Random random = new Random(pos.toLong());
        return new ChunkPos(random.nextInt(-bounds, bounds), random.nextInt(-bounds, bounds));
    }
}

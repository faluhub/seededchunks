package me.falu.seededchunks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;

public class ActualPosCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("actualpos")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player != null) {
                                return execute(context, new ColumnPos(player.getBlockX(), player.getBlockZ()));
                            }
                            return 0;
                        })
                        .then(
                                CommandManager.argument("chunkPos", ColumnPosArgumentType.columnPos())
                                        .executes(context -> execute(context, ColumnPosArgumentType.getColumnPos(context, "chunkPos")))
                        )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context, ColumnPos columnPos) {
        ChunkPos pos = SeededChunks.randomizeChunk(columnPos);
        context.getSource().sendFeedback(() -> Text.literal("Actual pos: [" + pos.x + ", " + pos.z + "] (" + pos.getStartX() + ", " + pos.getStartZ() + ")"), false);
        return 1;
    }
}

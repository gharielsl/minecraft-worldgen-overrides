package com.gharielsl.pwg.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import personthecat.fastnoise.FastNoise;
import personthecat.fastnoise.data.FractalType;
import personthecat.fastnoise.data.NoiseType;

import java.lang.reflect.Method;
import java.nio.file.Path;

public class RuntimeUtil {
    public static WorldAccess currentWorld;
    public static Chunk currentChunk;
    public static MinecraftServer currentServer;
    public static boolean compileStatus = true;
    public static Path tempDir;
    public static Method rawOverride;
    public static Method featureOverride;
    public static Method structureOverride;

    public static float getNoise(int seed, float frequency, float x, float y, float z) {
        final FastNoise generator = FastNoise.builder()
                .seed(seed)
                .type(NoiseType.SIMPLEX)
                .fractal(FractalType.FBM)
                .frequency(frequency)
                .build();
        return generator.getNoise(x, y, z);
    }

    public static float getNoise(int seed, float frequency, float x, float y) {
        final FastNoise generator = FastNoise.builder()
                .seed(seed)
                .type(NoiseType.SIMPLEX)
                .fractal(FractalType.FBM)
                .frequency(frequency)
                .build();
        return generator.getNoise(x, y);
    }

    public static float getNoise(int seed, float frequency, float x) {
        final FastNoise generator = FastNoise.builder()
                .seed(seed)
                .type(NoiseType.SIMPLEX)
                .fractal(FractalType.FBM)
                .frequency(frequency)
                .build();
        return generator.getNoise(x);
    }

    public static int getHeightmap(String type, int x, int z) {
        return currentChunk.getHeightmap(Heightmap.Type.valueOf(type)).get(x, z);
    }

    public static String getBlockState(int x, int y, int z) {
        return currentWorld.getBlockState(new BlockPos(x, y, z)).toString();
    }

    public static String getBlock(int x, int y, int z) {
        return Registries.BLOCK.getId(currentWorld.getBlockState(new BlockPos(x, y, z)).getBlock()).toString();
    }

    public static int getWorldBottom() {
        return currentWorld.getBottomY();
    }

    public static int getWorldTop() {
        return currentWorld.getTopY();
    }

    public static int[] getChunkPos() {
        return new int[] { currentChunk.getPos().x, currentChunk.getPos().z };
    }

    public static String getBiome(int x, int y, int z) {
        return currentWorld.getBiome(new BlockPos(x, y, z)).toString();
    }

    public static boolean setBlockState(int x, int y, int z, String state, int flags) {
        try {
            BlockStateArgument blockStateArgument = BlockStateArgumentType.blockState(CommandRegistryAccess.of(currentServer.getRegistryManager(), FeatureSet.empty())).parse(new StringReader(state));
            return currentWorld.setBlockState(new BlockPos(x, y, z), blockStateArgument.getBlockState(), flags);
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    public static boolean setBlock(int x, int y, int z, String block, int flags) {
        return currentWorld.setBlockState(new BlockPos(x, y, z), Registries.BLOCK.get(new Identifier(block)).getDefaultState(), flags);
    }
}

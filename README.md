## Description
This mod allows complete control over Minecraft's world generation. 

It lets you write java code that will run after each generation step (e.g., raw, feature, structure). 

Using this mod you can:

- Replace any block or block state with another block or block state.
- Create entirely custom terrain generation using a void or flat world as base.
- Customize modded world generation.
- Add decorations based on biome or blocks in the area.
- Anything else.

## RuntimeUtil
RuntimeUtil is a class with static methods you can use to modify the terrain and get useful information.

Methods:
```java
/**
 * Retrieves the full block state at the specified world coordinates.
 *
 * @param x The X-coordinate of the block.
 * @param y The Y-coordinate of the block.
 * @param z The Z-coordinate of the block.
 * @return A string representing the full block state, including properties (e.g., "minecraft:oak_log[axis=y]").
 */
String getBlockState(int x, int y, int z);

/**
 * Retrieves the block ID at the specified world coordinates.
 *
 * @param x The X-coordinate of the block.
 * @param y The Y-coordinate of the block.
 * @param z The Z-coordinate of the block.
 * @return A string representing the block ID (e.g., "minecraft:stone").
 */
String getBlock(int x, int y, int z);

/**
 * Returns the chunk position of the current execution context.
 *
 * @return An array of two integers representing the chunk's X and Z coordinates.
 */
int[] getChunkPos();

/**
 * Retrieves the biome name at the specified world coordinates.
 *
 * @param x The X-coordinate.
 * @param y The Y-coordinate.
 * @param z The Z-coordinate.
 * @return A string representing the biome name (e.g., "minecraft:plains").
 */
String getBiome(int x, int y, int z);

/**
 * Sets the block state at the specified coordinates.
 *
 * @param x The X-coordinate of the block.
 * @param y The Y-coordinate of the block.
 * @param z The Z-coordinate of the block.
 * @param state The full block state string (e.g., "minecraft:oak_log[axis=y]").
 * @param flags Block update flags (e.g., 3 for update + notify clients).
 * @return True if the block state was successfully set, false otherwise.
 */
boolean setBlockState(int x, int y, int z, String state, int flags);

/**
 * Sets a block at the specified coordinates using just the block ID.
 *
 * @param x The X-coordinate of the block.
 * @param y The Y-coordinate of the block.
 * @param z The Z-coordinate of the block.
 * @param block The block ID (e.g., "minecraft:stone").
 * @param flags Block update flags (e.g., 3 for update + notify clients).
 * @return True if the block was successfully set, false otherwise.
 */
boolean setBlock(int x, int y, int z, String block, int flags);

/**
 * Gets the minimum Y-coordinate of the world (bottom build limit).
 *
 * @return The lowest Y-level of the world.
 */
int getWorldBottom();

/**
 * Gets the maximum Y-coordinate of the world (top build limit).
 *
 * @return The highest Y-level of the world.
 */
int getWorldTop();

/**
 * Returns the height at the given X and Z position from the specified heightmap type.
 *
 * @param type The heightmap type (e.g., "WORLD_SURFACE", "OCEAN_FLOOR").
 * @param x The X-coordinate.
 * @param z The Z-coordinate.
 * @return The Y-coordinate value from the heightmap.
 */
int getHeightmap(String type, int x, int z);

/**
 * Generates 1D noise using the specified seed and frequency.
 *
 * @param seed The seed for noise generation.
 * @param frequency The frequency of the noise.
 * @param x The X-coordinate.
 * @return A float value representing the noise at the given coordinate.
 */
float getNoise(int seed, float frequency, float x);

/**
 * Generates 2D noise using the specified seed and frequency.
 *
 * @param seed The seed for noise generation.
 * @param frequency The frequency of the noise.
 * @param x The X-coordinate.
 * @param y The Y-coordinate.
 * @return A float value representing the noise at the given coordinates.
 */
float getNoise(int seed, float frequency, float x, float y);

/**
 * Generates 3D noise using the specified seed and frequency.
 *
 * @param seed The seed for noise generation.
 * @param frequency The frequency of the noise.
 * @param x The X-coordinate.
 * @param y The Y-coordinate.
 * @param z The Z-coordinate.
 * @return A float value representing the noise at the given coordinates.
 */
float getNoise(int seed, float frequency, float x, float y, float z);

```

## Examples

### Remove diorite from the world
```java
import com.gharielsl.pwg.util.RuntimeUtil;

public class RawGenerationOverride {
    public static void override() {
        for (int x = 0; x < 16; x++) {
            for (int y = RuntimeUtil.getWorldBottom(); y < RuntimeUtil.getWorldTop(); y++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = RuntimeUtil.getChunkPos()[0] * 16 + x;
                    int worldZ = RuntimeUtil.getChunkPos()[1] * 16 + z;
                    if (RuntimeUtil.getBlock(worldX, y, worldZ).equals("minecraft:diorite")) {
                        RuntimeUtil.setBlock(worldX, y, worldZ, "minecraft:stone", 3);
                    }
                }
            }
        }
    }
}
```

### Randomize stone (half stone half cobblestone)
```java
import com.gharielsl.pwg.util.RuntimeUtil;
import java.util.Random;

public class RawGenerationOverride {
    public static void override() {
        for (int x = 0; x < 16; x++) {
            for (int y = RuntimeUtil.getWorldBottom(); y < RuntimeUtil.getWorldTop(); y++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = RuntimeUtil.getChunkPos()[0] * 16 + x;
                    int worldZ = RuntimeUtil.getChunkPos()[1] * 16 + z;
                    Random rand = new Random();
                    if (rand.nextBoolean() && RuntimeUtil.getBlock(worldX, y, worldZ).equals("minecraft:stone")) {
                        RuntimeUtil.setBlock(worldX, y, worldZ, rand.nextBoolean() ? "minecraft:cobblestone", 3);
                    }
                }
            }
        }
    }
}
```

### Flood areas
```java
import com.gharielsl.pwg.util.RuntimeUtil;
import java.util.Random;

public class RawGenerationOverride {
    public static void override() {
        for (int x = 0; x < 16; x++) {
            for (int y = RuntimeUtil.getWorldBottom(); y < RuntimeUtil.getWorldTop(); y++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = RuntimeUtil.getChunkPos()[0] * 16 + x;
                    int worldZ = RuntimeUtil.getChunkPos()[1] * 16 + z;
                    if (y < 100 && RuntimeUtil.getBiome(worldX, y, worldZ).equals("minecraft:plains")) {
                        if (RuntimeUtil.getBlock(worldX, y, worldZ).equals("minecraft:air")) {
                            RuntimeUtil.setBlock(worldX, y, worldZ, "minecraft:water", 3);
                        }
                    }
                }
            }
        }
    }
}
```

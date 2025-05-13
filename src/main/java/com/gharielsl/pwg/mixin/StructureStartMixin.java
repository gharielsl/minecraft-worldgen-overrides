package com.gharielsl.pwg.mixin;

import com.gharielsl.pwg.util.RuntimeUtil;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public class StructureStartMixin {
    @Inject(method = "place", at = @At("RETURN"))
    public void place(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
        RuntimeUtil.currentWorld = world;
        RuntimeUtil.currentChunk = world.getChunk(chunkPos.x, chunkPos.z);

        if (RuntimeUtil.structureOverride != null) {
            try {
                RuntimeUtil.structureOverride.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

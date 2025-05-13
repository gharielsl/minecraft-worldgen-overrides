package com.gharielsl.pwg.mixin;

import com.gharielsl.pwg.util.RuntimeUtil;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "generateFeatures", at = @At("HEAD"))
    void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfo ci) {
        RuntimeUtil.currentWorld = world;
        RuntimeUtil.currentChunk = chunk;

        if (RuntimeUtil.rawOverride != null) {
            try {
                RuntimeUtil.rawOverride.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

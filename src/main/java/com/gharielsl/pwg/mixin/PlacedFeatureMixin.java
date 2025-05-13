package com.gharielsl.pwg.mixin;

import com.gharielsl.pwg.util.RuntimeUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacedFeature.class)
public class PlacedFeatureMixin {
    @Inject(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("RETURN"))
    private void generate(StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        RuntimeUtil.currentWorld = world;
        RuntimeUtil.currentChunk = world.getChunk(pos.getX() / 16, pos.getZ() / 16);

        if (RuntimeUtil.featureOverride != null) {
            try {
                RuntimeUtil.featureOverride.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

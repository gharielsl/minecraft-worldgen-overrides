package com.gharielsl.pwg.mixin;

import com.gharielsl.pwg.screen.PWGScreen;
import com.gharielsl.pwg.util.RuntimeUtil;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(targets = { "net.minecraft.client.gui.screen.world.CreateWorldScreen$WorldTab" })
public class CreateWorldScreen$WorldTabMixin extends GridScreenTab {

    public CreateWorldScreen$WorldTabMixin(Text title) {
        super(title);
    }

    @Shadow
    @Final
    private TextFieldWidget seedField;
    @Unique
    private Path tempDir;

    @Unique
    private void createFile(String fileName) {
        File file = tempDir.resolve(fileName).toFile();
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(String.format("""
import com.gharielsl.pwg.util.RuntimeUtil;

/*
--------------------------------------------------------------------
Available methods
String getBlockState(int x, int y, int z);
String getBlock(int x, int y, int z);
int[] getChunkPos();
String getBiome(int x, int y, int z);
boolean setBlockState(int x, int y, int z, String state, int flags);
boolean setBlock(int x, int y, int z, String block, int flags)
int getWorldBottom();
int getWorldTop();
int getHeightmap(String type, int x, int z);
float getNoise(int seed, float frequency, float x);
float getNoise(int seed, float frequency, float x, float y);
float getNoise(int seed, float frequency, float x, float y, float z);
--------------------------------------------------------------------
Example - replace all diorite with stone:
public class %s {
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
More examples at https://github.com/gharielsl/minecraft-worldgen-overrides
 */

public class %s {
    public static void override() {
        
    }
}
                        """, fileName.replace(".java", ""), fileName.replace(".java", "")));
                writer.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private Path getTempDir() {
        if (this.tempDir == null) {
            RuntimeUtil.compileStatus = true;
            try {
                this.tempDir = Files.createTempDirectory("mcworldpwg-");
                createFile("RawGenerationOverride.java");
                createFile("FeatureGenerationOverride.java");
                createFile("StructureGenerationOverride.java");
                RuntimeUtil.tempDir = tempDir;
            } catch (IOException e) {}
        }

        return this.tempDir;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CreateWorldScreen screen, CallbackInfo ci, @Local(ordinal = 0) GridWidget.Adder adder) {
        adder.add(new EmptyWidget(0, 8), 2);

        adder.add((new TextWidget(Text.translatable("text.pwg.title"), MinecraftClient.getInstance().textRenderer)).alignLeft());

        ButtonWidget button = ButtonWidget.builder(
                Text.translatable("text.pwg.overrides"),
                btn -> {
                    MinecraftClient.getInstance().setScreen(new PWGScreen(screen, getTempDir()));
                }).width(seedField.getInnerWidth() + 10).build();

        adder.add(button, 2);
    }
}

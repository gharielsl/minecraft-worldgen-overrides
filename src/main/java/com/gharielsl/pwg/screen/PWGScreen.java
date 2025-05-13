package com.gharielsl.pwg.screen;

import com.gharielsl.pwg.util.RuntimeUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class PWGScreen extends Screen {

    private GridWidget grid;
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    private TabNavigationWidget tabNavigation;
    private CreateWorldScreen createWorldScreen;
    private Path tempDir;
    private String rawText;
    private String featureText;
    private String structureText;
    private boolean ready = false;

    public PWGScreen(CreateWorldScreen createWorldScreen, Path tempDir) {
        super(Text.translatable("text.pwg.overrides"));
        this.createWorldScreen = createWorldScreen;
        this.tempDir = tempDir;
    }

    private String readFile(String file) {
        try {
            String result = Files.readString(tempDir.resolve(file));
            return result.isEmpty() ? "<empty>" : result;
        } catch (IOException e) {
            return "<error reading file>";
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (!ready) {
            return;
        }

        if (!readFile("RawGenerationOverride.java").equals(this.rawText) ||
                !readFile("FeatureGenerationOverride.java").equals(this.featureText) ||
                !readFile("StructureGenerationOverride.java").equals(this.structureText)
        ) {
            RuntimeUtil.compileStatus = true;
            init();
        }
    }

    @Override
    protected void init() {
        super.init();
        ready = false;
        this.clearChildren();
        tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width).tabs(new Tab[]{ new RawGenTab(), new FeatureGenTab(), new StructureGenTab() }).build();
        addDrawableChild(tabNavigation);
        grid = (new GridWidget()).setColumnSpacing(0).setRowSpacing(0);
        GridWidget.Adder adder = this.grid.createAdder(2);

        adder.add(ButtonWidget.builder(Text.translatable("text.pwg.open_file"), (button) -> {
            try {
                Runtime.getRuntime().exec("explorer.exe " + tempDir.toAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).build());

        adder.add(ButtonWidget.builder(Text.translatable("text.pwg.open_examples"), (button) -> {
            Util.getOperatingSystem().open("https://github.com/gharielsl/minecraft-worldgen-overrides");
        }).build());

        adder.add(ButtonWidget.builder(RuntimeUtil.compileStatus ? ScreenTexts.DONE : Text.of("§cFix Errors!"), (button) -> {
            this.close(false);
        }).build());

        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.close();
        }).build());

        this.grid.forEachChild((child) -> {
            child.setNavigationOrder(1);
            this.addDrawableChild(child);
        });

        this.tabNavigation.selectTab(0, false);
        this.initTabNavigation();
        ready = true;
    }

    public void initTabNavigation() {
        if (this.tabNavigation != null && this.grid != null) {
            this.tabNavigation.setWidth(this.width);
            this.tabNavigation.init();
            this.grid.refreshPositions();
            SimplePositioningWidget.setPos(this.grid, 0, this.height - 36, this.width, 36);
            int i = this.tabNavigation.getNavigationFocus().getBottom();
            ScreenRect screenRect = new ScreenRect(0, i, this.width, this.grid.getY() - i);
            this.tabManager.setTabArea(screenRect);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private class RawGenTab extends GridScreenTab {

        public RawGenTab() {
            super(Text.translatable("text.pwg.raw"));
            GridWidget.Adder adder = this.grid.setColumnSpacing(8).setRowSpacing(8).createAdder(1);

            TextWidget fileName = new TextWidget(Text.of("RawGenerationOverride.java"), MinecraftClient.getInstance().textRenderer);
            TextWidget hint = new TextWidget(Text.of("Edit using external file editor"), MinecraftClient.getInstance().textRenderer);

            adder.add(fileName);

            PWGScreen.this.rawText = readFile("RawGenerationOverride.java");

            ScrollableTextWidget textWidget =
                    new ScrollableTextWidget(PWGScreen.this.width / 2 - 64, 64, PWGScreen.this.width - 64,
                            PWGScreen.this.height - 128, Text.of(PWGScreen.this.rawText),
                            MinecraftClient.getInstance().textRenderer);
            adder.add(textWidget);
            adder.add(hint);
        }
    }

    private class FeatureGenTab extends GridScreenTab {

        public FeatureGenTab() {
            super(Text.translatable("text.pwg.feature"));
            GridWidget.Adder adder = this.grid.setColumnSpacing(8).setRowSpacing(8).createAdder(1);

            TextWidget fileName = new TextWidget(Text.of("FeatureGenerationOverride.java"), MinecraftClient.getInstance().textRenderer);
            TextWidget hint = new TextWidget(Text.of("Edit using external file editor"), MinecraftClient.getInstance().textRenderer);

            adder.add(fileName);

            PWGScreen.this.featureText = readFile("FeatureGenerationOverride.java");
            ScrollableTextWidget textWidget =
                    new ScrollableTextWidget(PWGScreen.this.width / 2 - 64, 64, PWGScreen.this.width - 64,
                            PWGScreen.this.height - 128, Text.of(PWGScreen.this.featureText),
                            MinecraftClient.getInstance().textRenderer);
            adder.add(textWidget);
            adder.add(hint);
        }
    }

    private class StructureGenTab extends GridScreenTab {

        public StructureGenTab() {
            super(Text.translatable("text.pwg.structure"));
            GridWidget.Adder adder = this.grid.setColumnSpacing(8).setRowSpacing(8).createAdder(1);

            TextWidget fileName = new TextWidget(Text.of("StructureGenerationOverride.java"), MinecraftClient.getInstance().textRenderer);
            TextWidget hint = new TextWidget(Text.of("Edit using external file editor"), MinecraftClient.getInstance().textRenderer);

            adder.add(fileName);

            PWGScreen.this.structureText = readFile("StructureGenerationOverride.java");
            ScrollableTextWidget textWidget =
                    new ScrollableTextWidget(PWGScreen.this.width / 2 - 64, 64, PWGScreen.this.width - 64,
                            PWGScreen.this.height - 128, Text.of(PWGScreen.this.structureText),
                            MinecraftClient.getInstance().textRenderer);
            adder.add(textWidget);
            adder.add(hint);
        }
    }

    public void close(boolean cancelled) {
        File logs = tempDir.resolve("log.txt").toFile();
        PrintStream out = System.out;
        if (!logs.exists()) {
            try {
                logs.createNewFile();
                out = new PrintStream(logs);;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!cancelled) {
            try {
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
                File jarFile = new File(RuntimeUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                String classpath = jarFile.getAbsolutePath() + File.pathSeparator + System.getProperty("java.class.path");
                Iterable<String> options = Arrays.asList("-classpath", classpath);
                List<File> javaFiles = Arrays.asList(
                        tempDir.resolve("RawGenerationOverride.java").toFile(),
                        tempDir.resolve("FeatureGenerationOverride.java").toFile(),
                        tempDir.resolve("StructureGenerationOverride.java").toFile()
                );

                Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFiles);
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);
                RuntimeUtil.compileStatus = task.call();
                fileManager.close();

                if (RuntimeUtil.compileStatus) {
                    this.close();
                }
            } catch (Error e) {
                e.printStackTrace(out);
                RuntimeUtil.compileStatus = false;
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!RuntimeUtil.compileStatus) {
            init();
        }
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(createWorldScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}

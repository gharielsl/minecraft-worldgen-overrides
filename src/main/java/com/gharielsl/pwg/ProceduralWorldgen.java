package com.gharielsl.pwg;

import com.gharielsl.pwg.util.RuntimeUtil;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ProceduralWorldgen implements ModInitializer {
	public static final String MOD_ID = "procedural-worldgen";

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
			RuntimeUtil.currentServer = server;
			Path worldDir = server.getSavePath(WorldSavePath.ROOT);
			boolean pwgcreated = worldDir.resolve(".pwgcreated").toFile().exists();
			if (!pwgcreated) {
				if (RuntimeUtil.tempDir == null) {
                    try {
                        worldDir.resolve(".pwgcreated").toFile().createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
					File tempDir = RuntimeUtil.tempDir.toFile();
					if (tempDir.exists() && tempDir.isDirectory()) {
						try {
							Files.walk(tempDir.toPath())
									.filter(path -> path.toString().endsWith(".class"))
									.forEach(path -> {
										try {
											Path targetPath = worldDir.resolve(tempDir.toPath().relativize(path).toString());
											Files.createDirectories(targetPath.getParent());
											Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
										} catch (IOException e) {
											e.printStackTrace();
										}
									});
							worldDir.resolve(".pwgcreated").toFile().createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			try {
				ClassLoader classLoader = new URLClassLoader(new URL[]{worldDir.toUri().toURL()}, this.getClass().getClassLoader());

				String[] classNames = {
						"RawGenerationOverride",
						"FeatureGenerationOverride",
						"StructureGenerationOverride"
				};

				for (String className : classNames) {
					Path classFile = worldDir.resolve(className + ".class");
					if (Files.exists(classFile)) {
						try {
							Class<?> clazz = Class.forName(className, true, classLoader);
							Method staticOverride = clazz.getMethod("override");
							if (Modifier.isStatic(staticOverride.getModifiers())) {
								if (className.equals("RawGenerationOverride")) {
									RuntimeUtil.rawOverride = staticOverride;
								} else if (className.equals("FeatureGenerationOverride")) {
									RuntimeUtil.featureOverride = staticOverride;
								} else if (className.equals("StructureGenerationOverride")) {
									RuntimeUtil.structureOverride = staticOverride;
								}
							}
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});
	}
}
package util;

import core.BlockSheet;
import core.SpriteSheet;
import org.joml.Vector4i;
import renderer.Shader;
import renderer.Texture;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetPool {
    private static final Map<String, Shader> shaders = new HashMap<>();
    private static final Texture blockTexture = new Texture();
    private static final Texture entityTexture = new Texture();
    private static final Map<String, SpriteSheet> spritesheets = new HashMap<>();
    private static final Map<String, BlockSheet> blocksheets = new HashMap<>();

    public static Shader getShader(String path) {
        File file = new File(path);
        if (shaders.containsKey(file.getAbsolutePath())) {
            return shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = null;
            try {
                shader = new Shader(path);
                shader.compile();
                shaders.put(file.getAbsolutePath(), shader);
            } catch (IOException e) {
                e.printStackTrace(Logger.getErr());
                Logger.critical("Unable to load shader at '" + path + "'.");
            }
            return shader;
        }
    }

    public static Texture getBlockTexture() {
        return blockTexture;
    }

    public static Vector4i getBlockTexCoords(String name) {
        Vector4i texCoords = blockTexture.getTexCoords(name);
        return texCoords != null ? texCoords : blockTexture.addImage(name, "assets/textures/block/" + name + ".png");
    }

    public static Texture getEntityTexture() {
        return entityTexture;
    }

    public static Vector4i getEntityTexCoords(String name) {
        Vector4i texCoords = entityTexture.getTexCoords(name);
        return texCoords != null ? texCoords : entityTexture.addImage(name, "assets/textures/entity/" + name + ".png");
    }

    public static void addSpriteSheet(String path, SpriteSheet spritesheet) {
        File file = new File(path);
        if (!spritesheets.containsKey(file.getAbsolutePath())) {
            spritesheets.put(file.getAbsolutePath(), spritesheet);
        }
    }

    public static void addBlockSheet(String path, BlockSheet blocksheet) {
        File file = new File(path);
        if (!blocksheets.containsKey(file.getAbsolutePath())) {
            blocksheets.put(file.getAbsolutePath(), blocksheet);
        }
    }

    public static SpriteSheet getSpritesheet(String path) {
        File file = new File(path);
        if (!spritesheets.containsKey(file.getAbsolutePath()))
            Logger.critical("Invalid asset request '" + path + "'.");
        return spritesheets.getOrDefault(file.getAbsolutePath(), null);
    }

    public static BlockSheet getBlocksheet(String path) {
        File file = new File(path);
        if (!blocksheets.containsKey(file.getAbsolutePath()))
            Logger.critical("Invalid asset request '" + path + "'.");
        return blocksheets.getOrDefault(file.getAbsolutePath(), null);
    }
}

package util;

import core.BlockSheet;
import core.Sound;
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
    private static final Map<String, BlockSheet> blocksheets = new HashMap<>();
    private static final Map<String, Sound> sounds = new HashMap<>();

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

    public static Image getBlockImage(String name) {
        Image image = blockTexture.getImage(name);
        return image != null ? image : blockTexture.addImage(name, "assets/textures/block/" + name + ".png");
    }

    public static Texture getEntityTexture() {
        return entityTexture;
    }

    public static Image getEntityImage(String name) {
        Image image = entityTexture.getImage(name);
        return image != null ? image : entityTexture.addImage(name, "assets/textures/entity/" + name + ".png");
    }

    public static void addBlockSheet(String path, BlockSheet blocksheet) {
        File file = new File(path);
        if (!blocksheets.containsKey(file.getAbsolutePath())) {
            blocksheets.put(file.getAbsolutePath(), blocksheet);
        }
    }

    public static BlockSheet getBlocksheet(String path) {
        File file = new File(path);
        if (blocksheets.containsKey(file.getAbsolutePath()))
            return blocksheets.get(file.getAbsolutePath());
        Logger.critical("Invalid block sheet request: '" + path + "'.");
        return null;
    }

    public static Sound getSound(String path) {
        File file = new File(path);
        if (!sounds.containsKey(file.getAbsolutePath())) {
            Sound sound = new Sound(path);
            sounds.put(file.getAbsolutePath(), sound);
            return sound;
        }
        return sounds.get(file.getAbsolutePath());
    }

}

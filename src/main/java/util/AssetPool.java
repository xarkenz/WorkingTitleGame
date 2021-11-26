package util;

import core.BlockSheet;
import core.SpriteSheet;
import renderer.Shader;
import renderer.Texture;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AssetPool {
    private static Map<String, Shader> shaders = new HashMap<>();
    private static Map<String, Texture> textures = new HashMap<>();
    private static Map<String, SpriteSheet> spritesheets = new HashMap<>();
    private static Map<String, BlockSheet> blocksheets = new HashMap<>();

    public static Shader getShader(String resourceName) {
        File file = new File(resourceName);
        if (AssetPool.shaders.containsKey(file.getAbsolutePath())) {
            return AssetPool.shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = new Shader(resourceName);
            shader.compile();
            AssetPool.shaders.put(file.getAbsolutePath(), shader);
            return shader;
        }
    }

    public static Texture getTexture(String resourceName) {
        File file = new File(resourceName);
        if (AssetPool.textures.containsKey(file.getAbsolutePath())) {
            return AssetPool.textures.get(file.getAbsolutePath());
        } else {
            Texture texture = new Texture();
            texture.init(resourceName);
            AssetPool.textures.put(file.getAbsolutePath(), texture);
            return texture;
        }
    }

    public static void addSpriteSheet(String resourceName, SpriteSheet spritesheet) {
        File file = new File(resourceName);
        if (!AssetPool.spritesheets.containsKey(file.getAbsolutePath())) {
            AssetPool.spritesheets.put(file.getAbsolutePath(), spritesheet);
        }
    }

    public static void addBlockSheet(String resourceName, BlockSheet blocksheet) {
        File file = new File(resourceName);
        if (!AssetPool.blocksheets.containsKey(file.getAbsolutePath())) {
            AssetPool.blocksheets.put(file.getAbsolutePath(), blocksheet);
        }
    }

    public static SpriteSheet getSpritesheet(String resourceName) {
        File file = new File(resourceName);
        if (!AssetPool.spritesheets.containsKey(file.getAbsolutePath())) {
            assert false : "Error: Spritesheet '" + resourceName + "' not in AssetPool";
        }
        return AssetPool.spritesheets.getOrDefault(file.getAbsolutePath(), null);
    }

    public static BlockSheet getBlocksheet(String resourceName) {
        File file = new File(resourceName);
        if (!AssetPool.blocksheets.containsKey(file.getAbsolutePath())) {
            assert false : "Error: Blocksheet '" + resourceName + "' not in AssetPool";
        }
        return AssetPool.blocksheets.getOrDefault(file.getAbsolutePath(), null);
    }
}

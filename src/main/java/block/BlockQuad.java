package block;

import org.joml.Vector2f;
import util.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class BlockQuad {

    private static final HashMap<BlockType, List<BlockQuad>> QUADS = new HashMap<>();

    private final BlockType type;
    private final Image image;
    private final int format;
    private final int size;
    private final int pos;
    private int shape;
    private String attribute, value;

    public BlockQuad(BlockType type, Image image, int size, int pos, int format) {
        // Format 0
        this.type = type;
        this.image = image;
        this.size = size;
        this.pos = pos;
        this.format = format;
    }

    public BlockQuad(BlockType type, Image image, int size, int pos, int format, int shape) {
        // Formats 1 and 2
        this.type = type;
        this.image = image;
        this.size = size;
        this.pos = pos;
        this.format = format;
        this.shape = shape;
    }

    public BlockQuad(BlockType type, Image image, int size, int pos, int format, String attribute, String value) {
        // Format 3
        this.type = type;
        this.image = image;
        this.size = size;
        this.pos = pos;
        this.format = format;
        this.attribute = attribute;
        this.value = value;
    }

    public Vector2f[] getTexCoords(double time) {
        return image.getTexCoords(time);
    }

    public int getPos() {
        return pos;
    }

    public int getShape() {
        return shape;
    }

    public int getFormat() {
        return format;
    }

    public BlockType getType() {
        return type;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    public static BlockQuad get(BlockType type, int pos, int shape) {
        for (BlockQuad quad: QUADS.get(type)) {
            if (quad.getPos() == pos && quad.getShape() == shape) {
                return quad;
            }
        }
        return null;
    }

    public static void add(BlockQuad quad) {
        if (!QUADS.containsKey(quad.getType())) {
            QUADS.put(quad.getType(), new ArrayList<>());
        }
        QUADS.get(quad.getType()).add(quad);
    }

}

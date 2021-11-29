package block;

import org.joml.Vector2f;
import renderer.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockQuad {

    private static final HashMap<BlockType, List<BlockQuad>> QUADS = new HashMap<>();

    private final Texture texture;
    private final BlockType type;
    private final Vector2f[] texCoords;
    private final int format;
    private final int size;
    private final int pos;
    private int shape;
    private String attribute, value;

    public BlockQuad(Texture texture, BlockType type, Vector2f[] texCoords, int size, int pos, int format) {
        // Format 0
        this.texture = texture;
        this.type = type;
        this.texCoords = texCoords;
        this.size = size;
        this.pos = pos;
        this.format = format;
    }

    public BlockQuad(Texture texture, BlockType type, Vector2f[] texCoords, int size, int pos, int format, int shape) {
        // Formats 1 and 2
        this.texture = texture;
        this.type = type;
        this.texCoords = texCoords;
        this.size = size;
        this.pos = pos;
        this.format = format;
        this.shape = shape;
    }

    public BlockQuad(Texture texture, BlockType type, Vector2f[] texCoords, int size, int pos, int format, String attribute, String value) {
        // Format 3
        this.texture = texture;
        this.type = type;
        this.texCoords = texCoords;
        this.size = size;
        this.pos = pos;
        this.format = format;
        this.attribute = attribute;
        this.value = value;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public int getPos() {
        return pos;
    }

    public int getShape() {
        return shape;
    }

    public Texture getTexture() {
        return texture;
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

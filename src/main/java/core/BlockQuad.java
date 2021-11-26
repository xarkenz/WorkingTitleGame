package core;

import org.joml.Vector2f;
import renderer.Texture;

public class BlockQuad {

    private Texture texture;
    private String name;
    private Vector2f[] texCoords;
    private int format, size, pos, shape;
    private String attribute, value;

    public BlockQuad(Texture texture, String name, Vector2f[] texCoords, int size, int pos, int format) {
        // Format 0
        this.texture = texture;
        this.name = name;
        this.texCoords = texCoords;
        this.size = size;
        this.pos = pos;
        this.format = format;
    }

    public BlockQuad(Texture texture, String name, Vector2f[] texCoords, int size, int pos, int format, int shape) {
        // Formats 1 and 2
        this.texture = texture;
        this.name = name;
        this.texCoords = texCoords;
        this.size = size;
        this.pos = pos;
        this.format = format;
        this.shape = shape;
    }

    public BlockQuad(Texture texture, String name, Vector2f[] texCoords, int size, int pos, int format, String attribute, String value) {
        // Format 3
        this.texture = texture;
        this.name = name;
        this.texCoords = texCoords;
        this.size = size;
        this.pos = pos;
        this.format = format;
        this.attribute = attribute;
        this.value = value;
    }

    public Vector2f[] getTexCoords() {
        return this.texCoords;
    }

    public int getPos() {
        return this.pos;
    }

    public int getShape() {
        return this.shape;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public int getFormat() {
        return this.format;
    }

    public String getName() {
        return this.name;
    }

    public String getAttribute() {
        return this.attribute;
    }

    public String getValue() {
        return this.value;
    }
}

package components;

import org.joml.Vector2f;
import renderer.Texture;

public class BlockQuad {

    private Texture texture;
    private String name;
    private Vector2f[] texCoords;
    private int width, height, pos, shape;

    public BlockQuad(Texture texture, String name, Vector2f[] texCoords, int width, int height, int pos, int shape) {
        this.texture = texture;
        this.name = name;
        this.texCoords = texCoords;
        this.width = width;
        this.height = height;
        this.pos = pos;
        this.shape = shape;
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

    public String getName() {
        return this.name;
    }
}

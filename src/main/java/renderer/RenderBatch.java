package renderer;

import components.Block;
import components.SpriteRenderer;
import core.Window;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderBatch implements Comparable<RenderBatch> {
    // Vertex
    // ======
    // Pos               Color                          Texture coords   Texture id
    // float, float,     float, float, float, float     float, float     float
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;
    private final int TEX_ID_SIZE = 1;
    private final int ENTITY_ID_SIZE = 1;
    private final int VERTEX_SIZE = POS_SIZE + COLOR_SIZE + TEX_COORDS_SIZE + TEX_ID_SIZE + ENTITY_ID_SIZE;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
    private final int ENTITY_ID_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private Block[] blocks;
    private int numPolygons;
    private boolean hasRoom;
    private float[] vertices;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    private List<Texture> textures;
    private int vaoID, vboID;
    private int maxBatchSize;
    private int zIndex;
    private boolean isSpriteBatch;

    public RenderBatch(int maxBatchSize, int zIndex, boolean isSpriteBatch) {
        this.zIndex = zIndex;
        this.isSpriteBatch = isSpriteBatch;
        if (isSpriteBatch) {
            this.sprites = new SpriteRenderer[maxBatchSize];
        } else {
            this.blocks = new Block[maxBatchSize];
        }
        this.maxBatchSize = maxBatchSize;

        // 4 vertices quads
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        this.numPolygons = 0;
        this.hasRoom = true;
        this.textures = new ArrayList<>();
    }

    public void start() {
        // Generate and bind a Vertex Array Object
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Allocate space for vertices
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Create and upload indices buffer
        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(4, ENTITY_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, ENTITY_ID_OFFSET);
        glEnableVertexAttribArray(4);
    }

    public void addSprite(SpriteRenderer spr) {
        assert this.isSpriteBatch : "Attempted to add a sprite to a block batch";

        // Get index and add renderObject
        int index = this.numPolygons;
        this.sprites[index] = spr;
        this.numPolygons++;

        if (spr.getTexture() != null) {
            if (!textures.contains(spr.getTexture())) {
                textures.add(spr.getTexture());
            }
        }

        // Add properties to local vertices array
        loadSpriteVertexProperties(index);

        if (this.numPolygons >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    public void addBlock(Block block) {
        assert !this.isSpriteBatch : "Attempted to add a block to a sprite batch";

        // Get index and add
        int index = this.numPolygons;
        this.blocks[index] = block;
        this.numPolygons += 4;

        if (block.getTexture() != null) {
            if (!textures.contains(block.getTexture())) {
                textures.add(block.getTexture());
            }
        }

        // Add properties to local vertices array
        loadBlockVertexProperties(index);

        if (this.numPolygons >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    public void render() {
        boolean rebufferData = false;
        for (int i=0; i < numPolygons; i++) {
            if (this.isSpriteBatch) {
                if (sprites[i] != null) {
                    SpriteRenderer spr = sprites[i];
                    if (spr.getDirty()) {
                        loadSpriteVertexProperties(i);
                        spr.setDirty(false);
                        rebufferData = true;
                    }
                }
            } else {
                if (blocks[i] != null) {
                    Block block = blocks[i];
                    if (block.getDirty()) {
                        loadBlockVertexProperties(i);
                        block.setDirty(false);
                        rebufferData = true;
                    }
                }
            }
        }
        if (rebufferData) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        // Use shader
        Shader shader = Renderer.getBoundShader();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());
        for (int i=0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.numPolygons * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        for (Texture texture : textures) {
            texture.unbind();
        }

        shader.detach();
    }

    private void loadSpriteVertexProperties(int index) {
        assert this.isSpriteBatch : "Attempted to load sprite vertices in block batch";

        SpriteRenderer sprite = this.sprites[index];

        // Find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;

        Vector4f color = sprite.getColor();
        Vector2f[] texCoords = sprite.getTexCoords();

        int texId = 0;
        if (sprite.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i).equals(sprite.getTexture())) {
                    texId = i + 1;
                    break;
                }
            }
        }

        boolean isRotated = sprite.gameObject.transform.rotation != 0.0f;
        Matrix4f transformMatrix = new Matrix4f().identity();
        if (isRotated) {
            transformMatrix.translate(sprite.gameObject.transform.position.x, sprite.gameObject.transform.position.y, 0f);
            transformMatrix.rotate((float)Math.toRadians(sprite.gameObject.transform.rotation), 0, 0, 1);
            transformMatrix.scale(sprite.gameObject.transform.scale.x, sprite.gameObject.transform.scale.y, 1f);
        }

        // Add vertex with the appropriate properties
        float xAdd = 1.0f;
        float yAdd = 1.0f;
        for (int i=0; i < 4; i++) {
            if (i == 1) {
                yAdd = 0.0f;
            } else if (i == 2) {
                xAdd = 0.0f;
            } else if (i == 3) {
                yAdd = 1.0f;
            }

            Vector4f currentPos = new Vector4f(sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x),
                    sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y), 0, 1);
            if (isRotated) {
                currentPos = new Vector4f(xAdd, yAdd, 0, 1).mul(transformMatrix);
            }

            // Load position
            vertices[offset] = currentPos.x;
            vertices[offset + 1] = currentPos.y;

            // Load color
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            // Load texture coordinates
            vertices[offset + 6] = texCoords[i].x;
            vertices[offset + 7] = texCoords[i].y;

            // Load texture ID
            vertices[offset + 8] = texId;

            // Load entity ID (+1 distinguishes invalid objects from UID 0)
            vertices[offset + 9] = sprite.gameObject.getUID() + 1;

            offset += VERTEX_SIZE;
        }
    }

    private void loadBlockVertexProperties(int index) {
        assert this.isSpriteBatch : "Attempted to load block vertices in sprite batch";

        Block block = this.blocks[index];

        // Find offset within array (16 vertices per block)
        int offset = index * 4 * VERTEX_SIZE;

        int texId = 0;
        if (block.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i).equals(block.getTexture())) {
                    texId = i + 1;
                    break;
                }
            }
        }

        // Add vertexes with the appropriate properties
        Vector2f[] offsets = {new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f()};

        for (int j=0; j < 4; j++) {
            Vector2f[] texCoords = Block.getQuad(block.getName(), j, block.getShape(j)).getTexCoords();

            switch(j) {
                case 0:
                    offsets[0].set(0.5f, 1f);
                    offsets[1].set(0.5f, 0.5f);
                    offsets[2].set(0f, 0.5f);
                    offsets[3].set(0f, 1f);
                    break;
                case 1:
                    offsets[0].set(1f, 1f);
                    offsets[1].set(1f, 0.5f);
                    offsets[2].set(0.5f, 0.5f);
                    offsets[3].set(0.5f, 1f);
                    break;
                case 2:
                    offsets[0].set(0.5f, 0.5f);
                    offsets[1].set(0.5f, 0f);
                    offsets[2].set(0f, 0f);
                    offsets[3].set(0f, 0.5f);
                    break;
                case 3:
                    offsets[0].set(1f, 0.5f);
                    offsets[1].set(1f, 0f);
                    offsets[2].set(0.5f, 0f);
                    offsets[3].set(0.5f, 0.5f);
                    break;
            }

            for (int i = 0; i < 4; i++) {
                Vector4f currentPos = new Vector4f((block.getPosition().x + offsets[i].x) * 32,
                        (block.getPosition().y + offsets[i].y) * 32, 0, 1);

                // Load position
                vertices[offset] = currentPos.x;
                vertices[offset + 1] = currentPos.y;

                // Load white color
                vertices[offset + 2] = 1f;
                vertices[offset + 3] = 1f;
                vertices[offset + 4] = 1f;
                vertices[offset + 5] = 1f;

                // Load texture coordinates
                vertices[offset + 6] = texCoords[i].x;
                vertices[offset + 7] = texCoords[i].y;

                // Load texture ID
                vertices[offset + 8] = texId;

                // Load UID 0 (block)
                vertices[offset + 9] = 0;

                offset += VERTEX_SIZE;
            }
        }
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle)
        int[] elements = new int[6 * maxBatchSize];
        for (int i=0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }
        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // 3, 2, 0, 0, 2, 1
        // Triangle 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    public boolean getHasRoom() {
        return this.hasRoom;
    }

    public boolean getHasTextureRoom() {
        return this.textures.size() < 8;
    }

    public boolean getHasTexture(Texture tex) {
        return this.textures.contains(tex);
    }

    public boolean getIsSpriteBatch() {
        return this.isSpriteBatch;
    }

    public int getZIndex() {
        return this.zIndex;
    }

    @Override
    public int compareTo(RenderBatch o) {
        return Integer.compare(this.zIndex, o.getZIndex());
    }
}

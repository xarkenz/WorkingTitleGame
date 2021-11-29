package renderer;

import block.BlockQuad;
import block.Chunk;
import component.SpriteRenderer;
import core.Window;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ChunkRenderBatch implements Comparable<ChunkRenderBatch> {
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
    private final int UID_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private Chunk chunk;
    private final Float[] vertices;
    private final int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    private final List<Texture> textures;
    private int vaoID, vboID;
    private final int zIndex;

    public ChunkRenderBatch(int z) {
        zIndex = z;
        //                   |-- # of squares to draw --|- 4 per square -|
        vertices = new Float[Chunk.SIZE * Chunk.SIZE * 4 * 4 * VERTEX_SIZE];

        textures = new ArrayList<>();
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

        glVertexAttribPointer(4, ENTITY_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, UID_OFFSET);
        glEnableVertexAttribArray(4);
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;

        // Load properties in vertex array
        loadChunkVertexProperties();
    }

    public void render() {
        boolean rebufferData = false;

        if (chunk.isDirty()) {
            loadChunkVertexProperties();
            chunk.setClean();
            rebufferData = true;
        }

        if (rebufferData) {
            float[] primitiveVertices = new float[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                if (vertices[i] != null) primitiveVertices[i] = vertices[i];
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, primitiveVertices);
        }

        // Use shader
        Shader shader = Renderer.getBoundShader();
        shader.uploadMat4f("uProjection", Window.getWorld().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getWorld().getCamera().getViewMatrix());
        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, Chunk.SIZE * Chunk.SIZE * 4 * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        for (Texture texture : textures) {
            texture.unbind();
        }

        shader.detach();
    }

    private void loadChunkVertexProperties() {
        if (chunk == null) {
            Arrays.fill(vertices, null);
            return;
        }

        // Ensure all textures are available
        for (int y = 0; y < Chunk.SIZE; y++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                for (int p = 0; p < 4; p++) {
                    BlockQuad quad = chunk.getBlockQuad(x, y, p);
                    if (quad != null && quad.getTexture() != null && !hasTexture(quad.getTexture()) && hasTextureRoom())
                        textures.add(quad.getTexture());
                }
            }
        }

        int offset = 0;

        for (int y = 0; y < Chunk.SIZE; y++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                Vector2f[] offsets = {new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f()};

                for (int p = 0; p < 4; p++) {
                    BlockQuad quad = chunk.getBlockQuad(x, y, p);
                    if (quad == null) continue;

                    int texId = textures.indexOf(quad.getTexture()) + 1;
                    Vector2f[] texCoords = quad.getTexCoords();

                    // Get the offset based on p
                    switch (p) {
                        case 0 -> {
                            offsets[0].set(0.5f, 1);
                            offsets[1].set(0.5f, 0.5f);
                            offsets[2].set(0,    0.5f);
                            offsets[3].set(0,    1);
                        }
                        case 1 -> {
                            offsets[0].set(1,    1);
                            offsets[1].set(1,    0.5f);
                            offsets[2].set(0.5f, 0.5f);
                            offsets[3].set(0.5f, 1);
                        }
                        case 2 -> {
                            offsets[0].set(0.5f, 0.5f);
                            offsets[1].set(0.5f, 0);
                            offsets[2].set(0,    0);
                            offsets[3].set(0,    0.5f);
                        }
                        case 3 -> {
                            offsets[0].set(1,    0.5f);
                            offsets[1].set(1,    0);
                            offsets[2].set(0.5f, 0);
                            offsets[3].set(0.5f, 0.5f);
                        }
                    }

                    float gamma = ((float) x / 16 + (float) y / 16) / 2;

                    // Create the vertices for the quad
                    for (int i = 0; i < 4; i++) {
                        // Vertex position
                        vertices[offset] = chunk.getPosition().x * Chunk.SIZE * 32 + (x + offsets[i].x) * 32;
                        vertices[offset + 1] = chunk.getPosition().y * Chunk.SIZE * 32 + (y + offsets[i].y) * 32;

                        // Vertex color
                        vertices[offset + 2] = gamma;
                        vertices[offset + 3] = gamma;
                        vertices[offset + 4] = gamma;
                        vertices[offset + 5] = 1f;

                        // Texture coordinates
                        vertices[offset + 6] = texCoords[i].x;
                        vertices[offset + 7] = texCoords[i].y;

                        // Texture ID
                        vertices[offset + 8] = (float) texId;

                        // UID 0 (block)
                        vertices[offset + 9] = 0f;

                        offset += VERTEX_SIZE;
                    }
                }
            }
        }
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle)
        int[] elements = new int[Chunk.SIZE * Chunk.SIZE * 4 * 6];
        for (int i = 0; i < Chunk.SIZE * Chunk.SIZE * 4; i++) {
            loadElementIndices(elements, i);
        }
        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // 3, 2, 0, 0, 2, 1
        // Triangle 1
        elements[offsetArrayIndex]     = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    public boolean isEmpty() {
        return chunk == null;
    }

    public boolean hasTextureRoom() {
        return textures.size() < 8;
    }

    public boolean hasTexture(Texture tex) {
        return textures.contains(tex);
    }

    public int getZIndex() {
        return zIndex;
    }

    @Override
    public int compareTo(ChunkRenderBatch o) {
        return Integer.compare(zIndex, o.getZIndex());
    }
}

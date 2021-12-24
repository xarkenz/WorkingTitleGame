package renderer;

import block.BlockQuad;
import block.Chunk;
import core.Window;
import org.joml.Vector2f;
import util.AssetPool;
import util.Settings;

import java.util.Arrays;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ChunkRenderer implements Comparable<ChunkRenderer> {

    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;

    private final int VERTEX_SIZE = POS_SIZE + COLOR_SIZE + TEX_COORDS_SIZE;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;

    private Chunk chunk;
    private final float[] vertices;
    private int vaoID, vboID;
    private final int zIndex;

    public ChunkRenderer(int z) {
        zIndex = z;
        //                   |-- # of squares to draw --|- 4 per square -|
        vertices = new float[Chunk.SIZE * Chunk.SIZE * 4 * 4 * VERTEX_SIZE];
    }

    public void start() {
        // Generate and bind a vertex array
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
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
        loadVertices();
    }

    public void render() {
        if (chunk.isChunkDirty()) {
            loadVertices();
            chunk.setChunkDirty(false);

            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        Shader shader = Renderer.getBoundShader();
        shader.uploadMat4f("uProjection", Window.getWorld().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getWorld().getCamera().getViewMatrix());

        Texture tex = AssetPool.getBlockTexture();
        glActiveTexture(tex.getID());
        tex.bind();

        shader.uploadTexture("uTexture", GL_TEXTURE0 + AssetPool.getBlockTexture().getID());

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, Chunk.SIZE * Chunk.SIZE * 4 * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);

        tex.unbind();
        shader.detach();
    }

    private void loadVertices() {
        if (chunk == null) {
            Arrays.fill(vertices, 0);
            return;
        }

        int offset = 0;
        for (int y = 0; y < Chunk.SIZE; y++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                Vector2f[] offsets = {new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f()};

                for (int p = 0; p < 4; p++) {
                    BlockQuad quad = chunk.getBlockQuad(x, y, p);
                    if (quad == null) {
                        for (int i = 0; i < 4 * VERTEX_SIZE; i++) {
                            vertices[offset + i] = 0;
                        }
                        offset += 4 * VERTEX_SIZE;
                        continue;
                    }

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

                    // Create the vertices for the quad
                    for (int v = 0; v < 4; v++) {
                        // Vertex position
                        vertices[offset] = chunk.getPosition().x * Chunk.SIZE * Settings.BLOCK_SIZE + (x + offsets[v].x) * Settings.BLOCK_SIZE;
                        vertices[offset + 1] = chunk.getPosition().y * Chunk.SIZE * Settings.BLOCK_SIZE + (y + offsets[v].y) * Settings.BLOCK_SIZE;

                        // Vertex color
                        vertices[offset + 2] = 1;
                        vertices[offset + 3] = 1;
                        vertices[offset + 4] = 1;
                        vertices[offset + 5] = 1;

                        // Texture coordinates
                        vertices[offset + 6] = texCoords[v].x;
                        vertices[offset + 7] = texCoords[v].y;

                        offset += VERTEX_SIZE;
                    }
                }
            }
        }
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 indices per triangle, 2 triangles per quad)
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

    public int getZIndex() {
        return zIndex;
    }

    @Override
    public int compareTo(ChunkRenderer o) {
        return Integer.compare(zIndex, o.getZIndex());
    }
}

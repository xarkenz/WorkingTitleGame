package renderer;

import component.SpriteRenderer;
import core.Window;
import entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4i;
import util.AssetPool;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class EntityRenderBatch implements Comparable<EntityRenderBatch> {

    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;
    private final int VERTEX_SIZE = POS_SIZE + COLOR_SIZE + TEX_COORDS_SIZE;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;

    private final int MAX_BATCH_SIZE = 1000;

    private final Entity[] entities;
    private int numElements;
    private final Float[] vertices;

    private int vaoID, vboID;
    private final int zIndex;

    public EntityRenderBatch(int z) {
        zIndex = z;
        entities = new Entity[MAX_BATCH_SIZE];
        vertices = new Float[MAX_BATCH_SIZE * 4 * VERTEX_SIZE];
        numElements = 0;
    }

    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

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

    public boolean add(Entity entity) {
        int index = -1;
        for (int i = 0; i < entities.length; i++) {
            if (entities[i] == null) {
                index = i;
                break;
            }
        }

        if (index == -1 || numElements + entity.getAppearance().numElements() > MAX_BATCH_SIZE) return false;

        entities[index] = entity;
        numElements += entity.getAppearance().numElements();

        loadVertices(index);

        return true;
    }

    public void render() {
        boolean rebuffer = false;
        for (int i = 0; i < entities.length; i++) {
            if (entities[i] != null && entities[i].getDirty()) {
                loadVertices(i);
                entities[i].setDirty(false);
                rebuffer = true;
            }
        }

        if (rebuffer) {
            float[] primitiveVertices = new float[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                if (vertices[i] != null)
                    primitiveVertices[i] = vertices[i];
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, primitiveVertices);
        }

        Shader shader = Renderer.getBoundShader();
        shader.uploadMat4f("uProjection", Window.getWorld().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getWorld().getCamera().getViewMatrix());

        Texture tex = AssetPool.getEntityTexture();
        glActiveTexture(GL_TEXTURE0 + tex.getID());
        tex.bind();

        shader.uploadTexture("uTexture", AssetPool.getEntityTexture().getID());

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, MAX_BATCH_SIZE * 4 * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);

        tex.unbind();
        shader.detach();
    }

    private void loadVertices(int index) {
        Entity entity = entities[index];

        // Find offset within array (4 vertices per quad)
        int offset = index * 4 * VERTEX_SIZE; // WRONG

        Vector4f color = entity.getAppearance().getColor();
        ArrayList<Vector2f[]> texCoords = entity.getAppearance().getTexCoords();
        ArrayList<Vector4i> texPlaces = entity.getAppearance().getTexPlaces();

        for (int i = 0; i < texCoords.size(); i++) {
            Vector2f[] places = {
                    new Vector2f(texPlaces.get(i).x + texPlaces.get(i).z, texPlaces.get(i).y + texPlaces.get(i).w),
                    new Vector2f(texPlaces.get(i).x + texPlaces.get(i).z, texPlaces.get(i).y),
                    new Vector2f(texPlaces.get(i).x, texPlaces.get(i).y),
                    new Vector2f(texPlaces.get(i).x, texPlaces.get(i).y + texPlaces.get(i).w)
            };

            // Create the vertices for the quad
            for (int v = 0; v < 4; v++) {
                // Vertex position
                vertices[offset] = (float) entity.getPosition().x + places[v].x;
                vertices[offset + 1] = (float) entity.getPosition().y + places[v].y;

                // Vertex color
                vertices[offset + 2] = color.x;
                vertices[offset + 3] = color.y;
                vertices[offset + 4] = color.z;
                vertices[offset + 5] = color.w;

                // Texture coordinates
                vertices[offset + 6] = texCoords.get(i)[v].x;
                vertices[offset + 7] = texCoords.get(i)[v].y;

                offset += VERTEX_SIZE;
            }
        }
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 indices per triangle, 2 triangles per quad)
        int[] elements = new int[MAX_BATCH_SIZE * 6];
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
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

    public int getZIndex() {
        return zIndex;
    }

    @Override
    public int compareTo(EntityRenderBatch o) {
        return Integer.compare(zIndex, o.getZIndex());
    }
}

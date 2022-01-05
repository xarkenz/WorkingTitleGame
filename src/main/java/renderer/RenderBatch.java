package renderer;

import core.Window;
import entity.Entity;
import org.joml.*;
import util.AssetPool;
import util.Logger;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderBatch implements Comparable<RenderBatch> {

    private final int MAX_BATCH_SIZE = 1000;

    private final Texture texture;
    private final int zIndex;

    private final float[] vertices;

    private final Vector2f[][] imagePositions;
    private final Vector4f[] imageColors;
    private final Vector2f[][] imageTexCoords;
    private final boolean[] imagesStatic;
    private final boolean[] imagesVisible;
    private final boolean[] imagesDirty;
    private final boolean[] imagesPresent;

    private int vaoID, vboID;

    public RenderBatch(Texture tex, int z) {
        texture = tex;
        zIndex = z;

        vertices = new float[MAX_BATCH_SIZE * 4 * 9];

        imagePositions = new Vector2f[MAX_BATCH_SIZE][4];
        imageColors = new Vector4f[MAX_BATCH_SIZE];
        imageTexCoords = new Vector2f[MAX_BATCH_SIZE][4];
        imagesStatic = new boolean[MAX_BATCH_SIZE];
        imagesVisible = new boolean[MAX_BATCH_SIZE];
        imagesDirty = new boolean[MAX_BATCH_SIZE];
        imagesPresent = new boolean[MAX_BATCH_SIZE];
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

        // Shader: layout (location = 0) in vec3 aPos;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Shader: layout (location = 1) in vec4 aColor;
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 9 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Shader: layout (location = 2) in vec2 aTexCoords;
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // Shader: layout (location = 3) in float aStatic;
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
    }

    public int add(Vector2f[] position, Vector4f color, Vector2f[] texCoords, boolean staticPos, boolean visible) {
        int index = -1;
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            if (!imagesPresent[i]) {
                index = i;
                break;
            }
        }

        if (index == -1) return index;

        imagePositions[index] = position;
        imageColors[index] = color;
        imageTexCoords[index] = texCoords;
        imagesStatic[index] = staticPos;
        imagesVisible[index] = visible;
        imagesDirty[index] = true;
        imagesPresent[index] = true;

        return index;
    }

    public void remove(int index) {
        imagesPresent[index] = false;
        imagesDirty[index] = true;
    }

    public void render() {
        boolean rebuffer = false;
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            if (imagesDirty[i]) {
                loadVertices(i);
                imagesDirty[i] = false;
                rebuffer = true;
            }
        }

        if (rebuffer) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        Shader shader = Renderer.getBoundShader();
        shader.uploadMat4f("uWorldProj", Window.getScene().getCamera().getWorldProjection());
        shader.uploadMat4f("uStaticProj", Window.getScene().getCamera().getStaticProjection());

        glActiveTexture(GL_TEXTURE0 + texture.getID());
        texture.bind();

        shader.uploadTexture("uTexture", texture.getID());

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glDrawElements(GL_TRIANGLES, MAX_BATCH_SIZE * 4 * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glBindVertexArray(0);

        texture.unbind();
        shader.detach();
    }

    private void loadVertices(int index) {
        // Find offset within array (4 vertices per quad)
        int offset = index * 4 * 9;

        // Reset vertex data if invisible or nonexistent
        if (!imagesPresent[index] || !imagesVisible[index]) {
            for (int i = 0; i < 4 * 9; i++) {
                vertices[offset + i] = 0;
            }
            return;
        }

        for (int v = 0; v < 4; v++) {
            // Vertex position
            vertices[offset] = imagePositions[index][v].x;
            vertices[offset + 1] = imagePositions[index][v].y;

            // Vertex color
            vertices[offset + 2] = imageColors[index].x;
            vertices[offset + 3] = imageColors[index].y;
            vertices[offset + 4] = imageColors[index].z;
            vertices[offset + 5] = imageColors[index].w;

            // Texture coordinates
            vertices[offset + 6] = imageTexCoords[index][v].x;
            vertices[offset + 7] = imageTexCoords[index][v].y;

            // Static/World position
            vertices[offset + 8] = imagesStatic[index] ? 1 : 0;

            offset += 9;
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

    public int getTexID() {
        return texture.getID();
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setPosition(int index, Vector2f[] position) {
        imagePositions[index] = position;
        imagesDirty[index] = true;
    }

    public void setColor(int index, Vector4f color) {
        imageColors[index] = color;
        imagesDirty[index] = true;
    }

    public void setTexCoords(int index, Vector2f[] texCoords) {
        imageTexCoords[index] = texCoords;
        imagesDirty[index] = true;
    }

    public void setVisible(int index, boolean visible) {
        imagesVisible[index] = visible;
        imagesDirty[index] = true;
    }

    @Override
    public int compareTo(RenderBatch o) {
        return Integer.compare(zIndex, o.getZIndex());
    }
}

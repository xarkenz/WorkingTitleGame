package renderer;

import core.Window;
import gui.GuiElement;
import util.AssetPool;
import util.Logger;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class GuiRenderBatch implements Comparable<GuiRenderBatch> {

    private final int MAX_BATCH_SIZE = 1000;

    private final GuiElement[] guiElements;
    private int nextIndex;
    private int numElements;
    private final float[] vertices;

    private int vaoID, vboID;
    private final int zIndex;

    public GuiRenderBatch(int z) {
        zIndex = z;
        guiElements = new GuiElement[MAX_BATCH_SIZE];
        vertices = new float[MAX_BATCH_SIZE * 4 * 9];
        nextIndex = 0;
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
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 4, GL_FLOAT, false, 9 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
    }

    public boolean add(GuiElement guiElement) {
        guiElement.setVertexIndex(nextIndex);
        nextIndex = guiElement.loadVertexData(vertices, nextIndex);

        if (nextIndex == -1) {
            guiElement.setVertexIndex(-1);
            return false;
        }

        guiElements[numElements++] = guiElement;
        return true;
    }

    public void render() {
        boolean rebuffer = false;
        for (GuiElement element : guiElements) {
            if (element != null && element.isDirty()) {
                element.loadVertexData(vertices, element.getVertexIndex());
                element.setDirty(false);
                rebuffer = true;
            }
        }

        if (rebuffer) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        Shader shader = Renderer.getBoundShader();
        shader.uploadMat4f("uView", Window.getWorld().getCamera().getViewMatrix());
        shader.uploadMat4f("uProjection", Window.getWorld().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uStaticProjection", Window.getWorld().getCamera().getStaticProjection());

        Texture tex = AssetPool.getGuiTexture();
        glActiveTexture(GL_TEXTURE0 + tex.getID());
        tex.bind();

        shader.uploadTexture("uTexture", tex.getID());

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

        tex.unbind();
        shader.detach();
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
    public int compareTo(GuiRenderBatch o) {
        return Integer.compare(zIndex, o.getZIndex());
    }
}

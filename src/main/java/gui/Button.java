package gui;

import core.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.AssetPool;
import util.Logger;
import util.Settings;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Button extends GuiElement {

    private final Consumer<GuiElement> callback;
    private final Vector3f color;
    private final Vector2f[][] texCoords = new Vector2f[9][4];

    public Button(String name, GuiElement parent, int x, int y, int w, int h, Consumer<GuiElement> callback) {
        super(name, parent, x, y, w, h);
        this.callback = callback;
        this.color = new Vector3f(1, 0, 0);
    }

    @Override
    public void start() {
        Vector2f offset = AssetPool.getGuiImage("buttons").getTexCoords(glfwGetTime())[3];

        Vector2f[] coords = {new Vector2f(offset.x + 8, offset.y), new Vector2f(offset.x + 8, offset.y - 8), new Vector2f(offset.x, offset.y - 8), new Vector2f(offset.x, offset.y)};

        System.arraycopy(coords, 0, texCoords[0], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(8, 0, new Vector2f());
        System.arraycopy(coords, 0, texCoords[1], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(8, 0, new Vector2f());
        System.arraycopy(coords, 0, texCoords[2], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(-16, -8, new Vector2f());
        System.arraycopy(coords, 0, texCoords[3], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(8, 0, new Vector2f());
        System.arraycopy(coords, 0, texCoords[4], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(8, 0, new Vector2f());
        System.arraycopy(coords, 0, texCoords[5], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(-16, -8, new Vector2f());
        System.arraycopy(coords, 0, texCoords[6], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(8, 0, new Vector2f());
        System.arraycopy(coords, 0, texCoords[7], 0, 4);
        for (int i = 0; i < 4; i++) coords[i] = coords[i].add(8, 0, new Vector2f());
        System.arraycopy(coords, 0, texCoords[8], 0, 4);
    }

    @Override
    public void update(float dt) {
        DebugDraw.addRect(new Vector2f(posX, posY), new Vector2f(width, height), color, 0, 1, true);
    }

    @Override
    public void mousePress(int x, int y) {
        color.set(0, 0.7f, 0);
        isDirty = true;
    }

    @Override
    public void mouseDrag(int x, int y) {
        if (wantsMouse(x, y)) color.set(0, 0.7f, 0);
        else color.set(1, 0, 0);
    }

    @Override
    public void mouseRelease(int x, int y) {
        if (wantsMouse(x, y)) {
            callback.accept(this);
        }
        color.set(1, 0, 0);
        isDirty = true;
    }

    @Override
    public int loadVertexData(float[] vertices, int nextIndex) {
        int across = width / 8;
        int down = height / 8;

        if (vertices.length < nextIndex + across * down * 4) {
            //vertices = Arrays.copyOf(vertices, nextIndex + across * down * 4);
            return -1;
        } else if (!visible) {
            return nextIndex + across * down * 4;
        }

        Vector2f[][] mappings = new Vector2f[across * down][4];
        mappings[0] = texCoords[0];
        mappings[across - 1] = texCoords[2];
        mappings[mappings.length - across] = texCoords[6];
        mappings[mappings.length - 1] = texCoords[8];
        for (int x = 1; x < across - 1; x++) {
            mappings[x] = texCoords[1];
            mappings[mappings.length - 1 - x] = texCoords[7];
        }
        for (int y = 1; y < down - 1; y++) {
            mappings[across * y] = texCoords[3];
            for (int x = 1; x < across - 1; x++) {
                mappings[across * y + x] = texCoords[4];
            }
            mappings[across * y + across - 1] = texCoords[5];
        }

        Vector2f[] offsets = {
                new Vector2f(8, 0),
                new Vector2f(8, -8),
                new Vector2f(0, -8),
                new Vector2f(0, 0)
        };

        int i = 0;
        for (float y = posY + height / 2f; y >= posY - height / 2f + 8; y -= 8) {
            for (float x = posX - width / 2f; x <= posX + width / 2f - 8; x += 8) {
                for (int v = 0; v < 4; v++) {
                    // Vertex position
                    vertices[nextIndex] = x + offsets[v].x;
                    vertices[nextIndex + 1] = y + offsets[v].y;

                    // Vertex color
                    vertices[nextIndex + 2] = 1;
                    vertices[nextIndex + 3] = 1;
                    vertices[nextIndex + 4] = 1;
                    vertices[nextIndex + 5] = 1;

                    // Texture coordinates
                    vertices[nextIndex + 6] = mappings[i][v].x + (hovering ? 24 : 0);
                    vertices[nextIndex + 7] = mappings[i][v].y;

                    // Static position
                    vertices[nextIndex + 8] = 1;

                    nextIndex += 9;
                }
                i++;
            }
        }

        return nextIndex;
    }

}

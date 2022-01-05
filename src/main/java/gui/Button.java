package gui;

import renderer.DebugDraw;
import renderer.Renderer;
import util.AssetPool;

import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Button extends GuiElement {

    private final Consumer<GuiElement> callback;
    private final Vector4f color;
    private final Vector2f[][] texCoords = new Vector2f[9][4];
    private int[] batchIndices = null;

    public Button(String name, GuiElement parent, int x, int y, int w, int h, Consumer<GuiElement> callback) {
        super(name, parent, x, y, w, h);
        this.callback = callback;
        this.color = new Vector4f(1, 1, 1, 1);
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

    }

    @Override
    public void mousePress(int x, int y) {
        color.set(0.7f, 0.7f, 0.7f, 1);
        isDirty = true;
    }

    @Override
    public void mouseDrag(int x, int y) {
        if (wantsMouse(x, y)) color.set(0.7f, 0.7f, 0.7f, 1);
        else color.set(1, 1, 1, 1);
    }

    @Override
    public void mouseRelease(int x, int y) {
        if (wantsMouse(x, y)) {
            callback.accept(this);
        }
        color.set(1, 1, 1, 1);
        isDirty = true;
    }

    @Override
    public void updateGraphics(Renderer renderer) {
        int across = width / 8;
        int down = height / 8;

        if (batchIndices != null) {
            for (int index : batchIndices) {
                renderer.removeImage(AssetPool.getGuiTexture().getID(), 0, index);
            }
        }
        batchIndices = new int[across * down];

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

        int index = 0;
        for (float y = posY + height / 2f; y >= posY - height / 2f + 8; y -= 8) {
            for (float x = posX - width / 2f; x <= posX + width / 2f - 8; x += 8) {
                Vector2f[] positions = new Vector2f[4];
                for (int i = 0; i < 4; i++) {
                    positions[i] = offsets[i].add(x, y, new Vector2f());
                }
                batchIndices[index] = renderer.addImage(AssetPool.getGuiTexture(), 0, positions, color, mappings[index], true, visible);
                index++;
            }
        }
    }

}

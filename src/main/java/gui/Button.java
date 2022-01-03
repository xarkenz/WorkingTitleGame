package gui;

import core.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;

import java.util.function.Consumer;

public class Button extends GuiElement {

    private final Consumer<GuiElement> callback;
    private final Vector3f color;

    public Button(String name, GuiElement parent, int x, int y, int w, int h, Consumer<GuiElement> callback) {
        super(name, parent, x, y, w, h);
        this.callback = callback;
        this.color = new Vector3f(1, 0, 0);
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt) {
        DebugDraw.addRect(new Vector2f(posX, posY), new Vector2f(width, height), color, 0, 1, true);
    }

    @Override
    public void mousePress(int x, int y) {
        color.set(0, 0.7f, 0);
    }

    @Override
    public void mouseDrag(int x, int y) {

    }

    @Override
    public void mouseRelease(int x, int y) {
        if (wantsMouse(x, y)) {
            callback.accept(this);
        }
        color.set(1, 0, 0);
    }

    @Override
    public void loadVertexData(float[] vertices) {

    }

}

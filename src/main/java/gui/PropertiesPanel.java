package gui;

import imgui.ImGui;
import core.GameObject;
import core.MouseListener;
import renderer.PickingTexture;
import world.Scene;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class PropertiesPanel {

    private GameObject activeGameObject = null;
    private final PickingTexture pickingTexture;

    private float debounce = 0.2f;

    public PropertiesPanel(PickingTexture pickingTexture) {
        this.pickingTexture = pickingTexture;
    }

    public void update(float dt, Scene currentScene) {
        debounce -= dt;

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
            int x = (int) MouseListener.getScreenX();
            int y = (int) MouseListener.getScreenY();
            int uid = pickingTexture.readPixel(x, y);
            activeGameObject = currentScene.getGameObject(uid);
            debounce = 0.2f;
        }
    }

    public void imGui() {
        if (activeGameObject != null) {
            ImGui.begin("Object Properties");
            activeGameObject.imGui();
            ImGui.end();
        }
    }

    public GameObject getActiveGameObject() {
        return activeGameObject;
    }
}

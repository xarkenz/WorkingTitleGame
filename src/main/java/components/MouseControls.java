package components;

import core.GameObject;
import core.MouseListener;
import core.Window;
import util.Settings;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class MouseControls extends Component {
    GameObject holdingObject = null;

    public void bindObject(GameObject go) {
        this.holdingObject = go;
        Window.getScene().addGameObject(go);
    }

    public void place() {
        this.holdingObject = null;
    }

    @Override
    public void update(float dt) {
        if (holdingObject != null) {
            holdingObject.transform.position.x = (int)(MouseListener.getWorldX() / Settings.GRID_SIZE) * Settings.GRID_SIZE;
            holdingObject.transform.position.y = (int)(MouseListener.getWorldY() / Settings.GRID_SIZE) * Settings.GRID_SIZE;

            if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                place();
            }
        }
    }
}

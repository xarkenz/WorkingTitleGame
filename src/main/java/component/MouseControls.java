package component;

import core.GameObject;
import core.MouseListener;
import util.Settings;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class MouseControls extends Component {
    GameObject holdingObject = null;

    public void bindObject(GameObject go) {
        this.holdingObject = go;
//        Window.getScene().addGameObject(go);
    }

    public void place() {
        this.holdingObject = null;
    }

    @Override
    public void update(float dt) {
        if (holdingObject != null) {
            holdingObject.transform.position.x = (int)(MouseListener.getWorldX() / Settings.BLOCK_SIZE) * Settings.BLOCK_SIZE;
            holdingObject.transform.position.y = (int)(MouseListener.getWorldY() / Settings.BLOCK_SIZE) * Settings.BLOCK_SIZE;

            if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                place();
            }
        }
    }
}

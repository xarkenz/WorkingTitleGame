package component;

import core.Camera;
import core.KeyListener;
import core.MouseListener;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DECIMAL;

public class EditorCamera extends Component {

    private float dragDebounce = 0.032f;

    private final Camera camera;
    private transient Vector2f clickOrigin;
    private boolean reset = false;
    private Vector2f targetPos;

    private float lerpTime = 0;
    private float dragSensitivity = 30;
    private float scrollSensitivity = 0.1f;

    public EditorCamera(Camera camera) {
        this.camera = camera;
        this.targetPos = new Vector2f(camera.position);
    }

    @Override
    public void update(float dt) {
        /*if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE) && dragDebounce > 0) {
            clickOrigin = new Vector2f(MouseListener.getWorldX(), MouseListener.getWorldY());
            dragDebounce -= dt;
            return;
        } else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
            Vector2f mousePos = new Vector2f(MouseListener.getWorldX(), MouseListener.getWorldY());
            Vector2f delta = new Vector2f(mousePos).sub(clickOrigin);
            levelEditorCamera.position.sub(delta.mul(dt).mul(dragSensitivity));
            this.clickOrigin.lerp(mousePos, dt);
        }

        if (dragDebounce <= 0 && !MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
            dragDebounce = 0.032f;
        }*/

        if (MouseListener.getScrollY() != 0) {
            float addValue = (float) Math.pow(Math.abs(MouseListener.getScrollY() * scrollSensitivity), 1 / camera.getZoom());
            addValue *= -Math.signum(MouseListener.getScrollY());
            camera.addZoom(addValue);
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_KP_DECIMAL))
            reset = true;

        if (reset) {
            camera.addZoom((1 - camera.getZoom()) * lerpTime);
            this.lerpTime += 0.2f * dt;

            if (Math.abs(1 - camera.getZoom()) <= 0.01f) {
                this.lerpTime = 0;
                this.camera.setZoom(1.0f);
                reset = false;
            }
        }

        if (!camera.position.equals(targetPos, 0)) {
            if (camera.position.equals(targetPos, 0.5f)) {
                camera.position.set(targetPos);
            } else {
                camera.position.lerp(targetPos, dt * 3);
            }
        }
    }

    public void setTargetPos(float x, float y) {
        this.targetPos.set(x, y);
    }
}

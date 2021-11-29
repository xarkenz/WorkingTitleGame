package component;

import core.Camera;
import core.KeyListener;
import core.MouseListener;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DECIMAL;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class EditorCamera extends Component {

    private float dragDebounce = 0.032f;

    private Camera levelEditorCamera;
    private transient Vector2f clickOrigin;
    private boolean reset = false;
    private Vector2f targetPos;

    private float lerpTime = 0;
    private float dragSensitivity = 30;
    private float scrollSensitivity = 0.1f;

    public EditorCamera(Camera levelEditorCamera) {
        this.levelEditorCamera = levelEditorCamera;
        this.targetPos = new Vector2f(levelEditorCamera.position);
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
            float addValue = (float) Math.pow(Math.abs(MouseListener.getScrollY() * scrollSensitivity), 1 / levelEditorCamera.getZoom());
            addValue *= -Math.signum(MouseListener.getScrollY());

            Vector2f posAdjust = new Vector2f(levelEditorCamera.getProjectionSize().x * addValue / 2, levelEditorCamera.getProjectionSize().y * addValue / 2);

            levelEditorCamera.addZoom(addValue);
            levelEditorCamera.position.sub(posAdjust);
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_KP_DECIMAL))
            reset = true;

        if (reset) {
            levelEditorCamera.addZoom((1 - levelEditorCamera.getZoom()) * lerpTime);
            this.lerpTime += 0.2f * dt;

            if (Math.abs(1 - levelEditorCamera.getZoom()) <= 0.01f) {
                this.lerpTime = 0;
                this.levelEditorCamera.setZoom(1.0f);
                reset = false;
            }
        }

        if (!levelEditorCamera.position.equals(targetPos, 0)) {
            if (levelEditorCamera.position.equals(targetPos, 0.5f)) {
                levelEditorCamera.position.set(targetPos);
            } else {
                levelEditorCamera.position.lerp(targetPos, dt * 3);
            }
        }
    }

    public void setTargetPos(float x, float y) {
        this.targetPos.set(x, y);
    }
}

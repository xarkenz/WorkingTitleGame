package core;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Settings;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {
    private static MouseListener instance;
    private double scrollX, scrollY;
    private double xPos, yPos, lastX, lastY, worldX, worldY, lastWorldX, lastWorldY;
    private final boolean[] buttonStates = new boolean[3];
    private final boolean[] queuedButtonStates = new boolean[3];
    private int mouseButtonsDown = 0;
    private boolean isDragging;

    private Vector2f gameViewportPos = new Vector2f();
    private Vector2f gameViewportSize = new Vector2f();

    private MouseListener() {
        scrollX = 0;
        scrollY = 0;
        xPos = 0;
        yPos = 0;
        lastX = 0;
        lastY = 0;
    }

    public static MouseListener get() {
        if (MouseListener.instance == null) MouseListener.instance = new MouseListener();
        return MouseListener.instance;
    }

    public static void mousePosCallback(long window, double x, double y) {
        if (get().mouseButtonsDown > 0) get().isDragging = true;

        get().lastX = get().xPos;
        get().lastY = get().yPos;
        get().lastWorldX = get().worldX;
        get().lastWorldY = get().worldY;
        get().xPos = x;
        get().yPos = y;

        calcOrtho();
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            get().mouseButtonsDown++;
            if (button < get().buttonStates.length) {
                get().buttonStates[button] = true;
                get().queuedButtonStates[button] = true;
            }
        } else if (action == GLFW_RELEASE) {
            get().mouseButtonsDown--;
            if (button < get().buttonStates.length) {
                get().buttonStates[button] = false;
                get().isDragging = false;
            }
        }
    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        get().scrollX = xOffset;
        get().scrollY = yOffset;
    }

    public static void endFrame() {
        get().scrollX = 0;
        get().scrollY = 0;
        get().lastX = get().xPos;
        get().lastY = get().yPos;
        get().lastWorldX = get().worldX;
        get().lastWorldY = get().worldY;
    }

    public static void calcOrtho() {
        Camera camera = Window.getScene().getCamera();

        float currentX = getX() - get().gameViewportPos.x;
        float currentY = getY() - get().gameViewportPos.y;
        currentX = (currentX / get().gameViewportSize.x) * 2 - 1;
        currentY = -((currentY / get().gameViewportSize.y) * 2 - 1);

        Matrix4f viewProjection = new Matrix4f();
        camera.getInverseView().mul(camera.getInverseProjection(), viewProjection);

        Vector4f tmpX = new Vector4f(currentX, 0, 0, 1);
        Vector4f tmpY = new Vector4f(0, currentY, 0, 1);
        tmpX.mul(viewProjection);
        tmpY.mul(viewProjection);

        get().worldX = tmpX.x;
        get().worldY = tmpY.y;
    }

    public static float getX() {
        return (float) get().xPos;
    }

    public static float getY() {
        return (float) get().yPos;
    }

    public static float getDx() {
        return (float) (get().lastX - get().xPos);
    }

    public static float getDy() {
        return (float) (get().lastY - get().yPos);
    }

    public static float getWorldDx() {
        return (float) (get().lastWorldX - get().worldX);
    }

    public static float getWorldDy() {
        return (float) (get().lastWorldY - get().worldY);
    }

    public static float getScrollX() {
        return (float) get().scrollX;
    }

    public static float getScrollY() {
        return (float) get().scrollY;
    }

    public static boolean isDragging() {
        return get().isDragging;
    }

    public static boolean mouseButtonDown(int button) {
        if (button < get().buttonStates.length) {
            if (get().queuedButtonStates[button]) {
                get().queuedButtonStates[button] = false;
                return true;
            }
            return get().buttonStates[button];
        }
        return false;
    }

    public static float getScreenX() {
        float currentX = getX() - get().gameViewportPos.x;
        currentX = (currentX / get().gameViewportSize.x) * (float) Settings.DISPLAY_WIDTH;
        return currentX;
    }

    public static float getScreenY() {
        float currentY = getY() - get().gameViewportPos.y;
        currentY = (float) Settings.DISPLAY_HEIGHT - (currentY / get().gameViewportSize.y) * (float) Settings.DISPLAY_HEIGHT;
        return currentY;
    }

    public static float getWorldX() {
        MouseListener.calcOrtho();
        return (float) get().worldX;
    }

    public static float getWorldY() {
        MouseListener.calcOrtho();
        return (float) get().worldY;
    }

    public static Vector2f getGameViewportPos() {
        return get().gameViewportPos;
    }

    public static void setGameViewportPos(Vector2f gameViewportPos) {
        get().gameViewportPos.set(gameViewportPos);
    }

    public static Vector2f getGameViewportSize() {
        return get().gameViewportSize;
    }

    public static void setGameViewportSize(Vector2f gameViewportSize) {
        get().gameViewportSize.set(gameViewportSize);
    }

    public static void clearQueue() {
        Arrays.fill(get().queuedButtonStates, false);
    }
}

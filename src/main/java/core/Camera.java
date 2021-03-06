package core;

import org.joml.*;
import util.Settings;

public class Camera {
    private Matrix4f projectionMatrix, viewMatrix, inverseProjection, inverseView;
    public Vector2f position;
    private Vector2f projectionSize = new Vector2f(32 * 40, 32 * 21);
    private float zoom = 0.4f;

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.identity();
        projectionMatrix.ortho(-projectionSize.x * zoom / 2, projectionSize.x * zoom / 2, -projectionSize.y * zoom / 2, projectionSize.y * zoom / 2, 0.0f, 100.0f);
        projectionMatrix.invert(inverseProjection);
    }

    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(position.x, position.y, -1);
        Vector3f cameraUp = new Vector3f(0, 1, 0);

        viewMatrix.identity();
        viewMatrix.lookAt(new Vector3f(position.x, position.y, 20), cameraFront, cameraUp);
        viewMatrix.invert(inverseView);

        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getInverseProjection() {
        return inverseProjection;
    }

    public Matrix4f getWorldProjection() {
        return projectionMatrix.mul(viewMatrix, new Matrix4f());
    }

    public Matrix4f getStaticProjection() {
        Matrix4f staticProjection = new Matrix4f();
        float xOffset = projectionSize.x / Settings.GUI_SCALE;
        float yOffset = projectionSize.y / Settings.GUI_SCALE;
        return staticProjection.ortho(-xOffset, xOffset, -yOffset, yOffset, 0.0f, 100.0f);
    }

    public Matrix4f getInverseView() {
        return inverseView;
    }

    public Vector2f getProjectionSize() {
        return projectionSize;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float value) {
        zoom = value;
    }

    public void addZoom(float value) {
        zoom += value;
    }
}

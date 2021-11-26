package renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Line {
    private Vector2f from;
    private Vector2f to;
    private Vector3f color;
    private int lifetime;
    private boolean bg;

    public Line(Vector2f from, Vector2f to, Vector3f color, int lifetime) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.lifetime = lifetime;
        this.bg = false;
    }

    public Line(Vector2f from, Vector2f to, Vector3f color, int lifetime, boolean isBackground) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.lifetime = lifetime;
        this.bg = isBackground;
    }

    public int beginFrame() {
        this.lifetime--;
        return this.lifetime;
    }

    public Vector2f getFrom() {
        return from;
    }

    public Vector2f getTo() {
        return to;
    }

    public Vector3f getColor() {
        return color;
    }

    public boolean isBackground() {
        return bg;
    }
}

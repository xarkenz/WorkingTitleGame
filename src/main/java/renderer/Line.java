package renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Line {
    private final Vector2f from;
    private final Vector2f to;
    private final Vector3f color;
    private int lifetime;
    private final boolean isBackground;
    private final boolean isStatic;

    public Line(Vector2f from, Vector2f to, Vector3f color, int lifetime, boolean isBackground, boolean isStatic) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.lifetime = lifetime;
        this.isBackground = isBackground;
        this.isStatic = isStatic;
    }

    public int beginFrame() {
        return --lifetime;
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
        return isBackground;
    }

    public boolean isStatic() {
        return isStatic;
    }
}

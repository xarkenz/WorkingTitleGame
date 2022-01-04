package util;

import org.joml.Vector2f;
import org.joml.Vector2i;

public class Image {

    private final Vector2i size;
    private final Vector2i[] pos;
    private final float frameTime;

    public Image(Vector2i size, Vector2i[] pos) {
        this.size = size;
        this.pos = pos;
        this.frameTime = 0.1f;
    }

    public Vector2i getSize() {
        return size;
    }

    public Vector2i[] getPos() {
        return pos;
    }

    public Vector2f[] getTexCoords(double time) {
        int frame = (int) (time % (frameTime * pos.length) / frameTime);
        return new Vector2f[]{
                new Vector2f(pos[frame].x + size.x, pos[frame].y + size.y),
                new Vector2f(pos[frame].x + size.x, pos[frame].y),
                new Vector2f(pos[frame].x, pos[frame].y),
                new Vector2f(pos[frame].x, pos[frame].y + size.y)
        };
    }

}

package renderer;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.ArrayList;
import java.util.Arrays;

public class EntityAppearance {

    private final ArrayList<Vector2f[]> texCoords;
    private final ArrayList<Vector4i> texPlaces;
    private final Vector4f color;
    private boolean flipH;
    private boolean flipV;

    public EntityAppearance() {
        texCoords = new ArrayList<>();
        texPlaces = new ArrayList<>();
        color = new Vector4f(1, 1, 1, 1);
    }

    public int addTexElement(Vector2f[] coords, Vector4i place) {
        texCoords.add(coords);
        texPlaces.add(place);
        return texCoords.size() - 1;
    }

    public ArrayList<Vector2f[]> getTexCoords() {
        ArrayList<Vector2f[]> ret = new ArrayList<>();
        for (Vector2f[] coords : texCoords) {
            if (flipH) coords = new Vector2f[]{coords[3], coords[2], coords[1], coords[0]};
            if (flipV) coords = new Vector2f[]{coords[1], coords[0], coords[3], coords[2]};
            ret.add(coords);
        }
        return ret;
    }

    public ArrayList<Vector4i> getTexPlaces() {
        return texPlaces;
    }

    public int numElements() {
        return texCoords.size();
    }

    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f newColor) {
        color.set(newColor);
    }

    public void setFlipH(boolean flip) {
        flipH = flip;
    }

    public void setFlipV(boolean flip) {
        flipV = flip;
    }

    public boolean isFlipH() {
        return flipH;
    }

    public boolean isFlipV() {
        return flipV;
    }
}

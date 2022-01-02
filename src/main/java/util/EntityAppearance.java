package util;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class EntityAppearance {

    private final ArrayList<Image> images;
    private final ArrayList<Vector4i> places;
    private final ArrayList<Boolean> visibility;
    private final ArrayList<Double> startTimes;
    private final Vector4f color;
    private boolean flipH;
    private boolean flipV;

    public EntityAppearance() {
        images = new ArrayList<>();
        places = new ArrayList<>();
        visibility = new ArrayList<>();
        startTimes = new ArrayList<>();
        color = new Vector4f(1, 1, 1, 1);
    }

    public int addImage(Image image, Vector4i place) {
        images.add(image);
        places.add(place);
        visibility.add(false);
        startTimes.add(0d);
        return images.size() - 1;
    }

    public ArrayList<Vector2f[]> getTexCoords() {
        ArrayList<Vector2f[]> ret = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            if (!visibility.get(i)) continue;
            Vector2f[] texCoords = images.get(i).getTexCoords(glfwGetTime() - startTimes.get(i));
            if (flipH) texCoords = new Vector2f[]{texCoords[3], texCoords[2], texCoords[1], texCoords[0]};
            if (flipV) texCoords = new Vector2f[]{texCoords[1], texCoords[0], texCoords[3], texCoords[2]};
            ret.add(texCoords);
        }
        return ret;
    }

    public ArrayList<Vector4i> getPlaces() {
        return places;
    }

    public int numElements() {
        return images.size();
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

    public void showImage(int index, boolean show) {
        if (!visibility.get(index).equals(show)) startTimes.set(index, glfwGetTime());
        visibility.set(index, show);
    }

}

package util;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class EntityAppearance {

    private final ArrayList<Image> images = new ArrayList<>();
    private final ArrayList<Vector4i> places = new ArrayList<>();
    private final ArrayList<Boolean> visibility = new ArrayList<>();
    private final ArrayList<Double> startTimes = new ArrayList<>();
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private boolean flipH;
    private boolean flipV;

    public EntityAppearance() {

    }

    public int addImage(Image image, Vector4i place) {
        images.add(image);
        places.add(place);
        visibility.add(false);
        startTimes.add(0d);
        return images.size() - 1;
    }

    public Vector2f[] getTexCoords(int index) {
        if (!visibility.get(index)) return null;
        Vector2f[] texCoords = images.get(index).getTexCoords(glfwGetTime() - startTimes.get(index));
        if (flipH) texCoords = new Vector2f[]{texCoords[3], texCoords[2], texCoords[1], texCoords[0]};
        if (flipV) texCoords = new Vector2f[]{texCoords[1], texCoords[0], texCoords[3], texCoords[2]};
        return texCoords;
    }

    public Vector4i getPlace(int index) {
        return places.get(index);
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

    public boolean isFlipH() {
        return flipH;
    }

    public void setFlipH(boolean flip) {
        flipH = flip;
    }

    public boolean isFlipV() {
        return flipV;
    }

    public void setFlipV(boolean flip) {
        flipV = flip;
    }

    public boolean isVisible(int index) {
        return visibility.get(index);
    }

    public void showImage(int index, boolean show) {
        if (!visibility.get(index).equals(show)) startTimes.set(index, glfwGetTime());
        visibility.set(index, show);
    }
}

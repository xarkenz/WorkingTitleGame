package renderer;

import org.joml.Vector2i;
import org.joml.Vector4i;
import org.lwjgl.BufferUtils;
import util.Image;
import util.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private final HashMap<String, Image> images;
    private ByteBuffer atlas;
    private int texID;
    private int width, height;
    private int nextX, nextY;

    public Texture() {
        this(0, 0);
    }

    public Texture(int w, int h) {
        texID = -1;
        width = w;
        height = h;
        nextX = 0;
        nextY = 0;
        images = new HashMap<>();
        atlas = null;
    }

    public void bind() {
        if (texID == -1) texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public Image addImage(String name, String path) {
        // Create buffers to store image data from stbi_load
        IntBuffer imageW = BufferUtils.createIntBuffer(1);
        IntBuffer imageH = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        // Flip images vertically for use in the atlas
        stbi_set_flip_vertically_on_load(true);
        // Load the image from file
        ByteBuffer loaded = stbi_load(path, imageW, imageH, channels, 0);

        if (loaded == null) {
            Logger.critical("Unable to load image '" + path + "': Not found.");
            return null;
        }

        if (channels.get(0) == 4) {
            // Atlas images are stacked vertically
            int newWidth = Math.max(width, imageW.get(0));
            int newHeight = height + imageH.get(0);

            if (atlas == null) atlas = BufferUtils.createByteBuffer(width * height * 4);
            atlas.rewind();
            loaded.rewind();

            // buffer size = width * height * channels
            ByteBuffer newAtlas = BufferUtils.createByteBuffer(newWidth * newHeight * 4);
            for (int row = 0; row < newHeight; row++) {
                byte[] data = new byte[newWidth * 4];
                if (row < height)
                    // First, inject the old atlas data
                    atlas.get(data, 0, width * 4);
                else
                    // Then, inject the new image data
                    loaded.get(data, 0, imageW.get(0) * 4);
                newAtlas.put(data);
            }

            // Update the atlas with the newly created version
            atlas = newAtlas.rewind();
            width = newWidth;
            height = newHeight;
        } else {
            Logger.critical("Unable to load image '" + path + "': Only RGBA images are supported.");
            return null;
        }

        Vector2i[] pos = {new Vector2i(nextX, nextY)};
        int h = imageH.get(0);
        if (h == 192) {
            pos = new Vector2i[]{
                    new Vector2i(nextX, nextY + 160),
                    new Vector2i(nextX, nextY + 128),
                    new Vector2i(nextX, nextY + 96),
                    new Vector2i(nextX, nextY + 64),
                    new Vector2i(nextX, nextY + 32),
                    new Vector2i(nextX, nextY),
            };
            h = 32;
        }
        Image image = new Image(new Vector2i(imageW.get(0), h), pos);
        images.put(name, image);
        nextY += imageH.get(0);

        stbi_image_free(loaded.rewind());

        return image;
    }

    public void upload() {
        // Generate and bind texture on GPU
        bind();

        if (atlas == null) {
            // Interpolate when shrinking and stretching
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // Allocate space on texture, initialize to empty data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);

        } else {
            // Clamp texture when wrapping
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            // Interpolate when shrinking, pixelate when stretching
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            // Allocate space on texture, initialize to atlas data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, atlas.rewind());
        }
    }

    public Image getImage(String name) {
        return images.get(name);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getID() {
        return texID;
    }

}

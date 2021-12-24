package renderer;

import org.joml.Vector4i;
import org.lwjgl.BufferUtils;
import util.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private final HashMap<String, Vector4i> images;
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

    public Vector4i addImage(String name, String path) {
        // Create buffers to store image data from stbi_load
        IntBuffer imageW = BufferUtils.createIntBuffer(1);
        IntBuffer imageH = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        // Flip images vertically for use in the atlas
        stbi_set_flip_vertically_on_load(true);
        // Load the image from file
        ByteBuffer image = stbi_load(path, imageW, imageH, channels, 0);

        if (image == null) {
            Logger.critical("Unable to load image '" + path + "': Not found.");
            return null;
        }

        if (channels.get(0) == 4) {
            // Atlas images are stacked vertically
            int newWidth = Math.max(width, imageW.get(0));
            int newHeight = height + imageH.get(0);

            if (atlas == null) atlas = BufferUtils.createByteBuffer(width * height * 4);
            atlas.rewind();
            image.rewind();

            // buffer size = width * height * channels
            ByteBuffer newAtlas = BufferUtils.createByteBuffer(newWidth * newHeight * 4);
            for (int row = 0; row < newHeight; row++) {
                byte[] data = new byte[newWidth * 4];
                if (row < height)
                    // First, inject the old atlas data
                    atlas.get(data, 0, width * 4);
                else
                    // Then, inject the new image data
                    image.get(data, 0, imageW.get(0) * 4);
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

        Vector4i texCoords = new Vector4i(nextX, nextY, imageW.get(0), imageH.get(0));
        images.put(name, texCoords);
        nextY += imageH.get(0);

        stbi_image_free(image.rewind());

        return texCoords;
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

        /*if (atlas != null) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            atlas.rewind();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int r = atlas.get();
                    int g = atlas.get();
                    int b = atlas.get();
                    int a = atlas.get();
                    r = r < 0 ? r + 256 : r;
                    g = g < 0 ? g + 256 : g;
                    b = b < 0 ? b + 256 : b;
                    a = a < 0 ? a + 256 : a;
                    image.setRGB(x, y, (a << 24) + (r << 16) + (g << 8) + b);
                }
            }
            atlas.rewind();
            try {
                ImageIO.write(image, "png", new File("image.png"));
            } catch (IOException e) {
                Logger.critical(e.toString());
            }
        }*/
    }

    public Vector4i getTexCoords(String name) {
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

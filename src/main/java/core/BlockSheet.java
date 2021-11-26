package core;

import blocks.Block;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joml.Vector2f;
import renderer.Texture;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class BlockSheet {

    private static final String[] FORMATS = {
            "single",
            "connected_quads_simple",
            "connected_quads_layered",
    };
    private static final String[] FORMAT_1_SHAPES = {
            "outward_corners",
            "horizontal",
            "vertical",
            "inward_corners",
            "fill"
    };
    private static final String[] FORMAT_2_SHAPES = {
            "outward_corners",
            "horizontal",
            "vertical",
            "inward_corners",
            "fill_outward_corners",
            "fill_horizontal",
            "fill_vertical",
            "fill_inward_corners"
    };

    private Texture texture;
    private String name;
    private int blockSize;
    private int format;

    public BlockSheet(Texture texture, String name) {
        this.name = name;
        this.texture = texture;

        this.blockSize = 16;
        this.format = 0;

        int numBlocks = 0;

        // Read the JSON file

        JsonObject json = null;

        try {
            json = (JsonObject) JsonParser.parseReader(new FileReader("assets/textures/blocks/" + name + ".json"));
        } catch (FileNotFoundException e0) {
            try {
                json = (JsonObject) JsonParser.parseReader(new FileReader("assets/templates/default_block.json"));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        assert json != null : name + ".json is improperly formatted";

        boolean validFormat = false;
        for (int i=0; i < FORMATS.length; i++) {
            if (FORMATS[i].equalsIgnoreCase(json.get("format").getAsString())) {
                this.format = i;
                validFormat = true;
                break;
            }
        }
        assert validFormat : name + ".json: Invalid 'format' tag";
        assert this.format < FORMATS.length : name + ".json: Invalid format ID";

        List<Integer> texShapes = new ArrayList<>();

        // More information needed unless format is 'single'
        if (this.format == 0) {
            numBlocks = 1;
            this.blockSize = this.texture.getWidth(); // May have a different height if animated
        } else {
            this.blockSize = json.get("texture_size").getAsInt();

            JsonArray textures = json.get("textures").getAsJsonArray();
            for (int j=0; j < textures.size(); j++) {
                numBlocks++;
                String tex =  textures.get(j).getAsString();

                if (this.format == 1) {
                    int shape = -1;
                    for (int i=0; i < FORMAT_1_SHAPES.length; i++) {
                        if (FORMAT_1_SHAPES[i].equalsIgnoreCase(tex)) {
                            shape = i;
                            break;
                        }
                    }
                    texShapes.add(shape);
                } else if (this.format == 2) {
                    int shape = -1;
                    for (int i=0; i < FORMAT_2_SHAPES.length; i++) {
                        if (FORMAT_2_SHAPES[i].equalsIgnoreCase(tex)) {
                            shape = i;
                            break;
                        }
                    }
                    texShapes.add(shape);
                }
            }
        }

        // Parse the texture

        int currentX = 0;
        int currentY = texture.getHeight() - blockSize;
        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < 4; j++) {
                float topY = 0;
                float rightX = 0;
                float leftX = 0;
                float bottomY = 0;
                switch (j) {
                    case 0:
                        topY = (currentY + blockSize) / (float) texture.getHeight();
                        rightX = (currentX + blockSize / 2f) / (float) texture.getWidth();
                        leftX = currentX / (float) texture.getWidth();
                        bottomY = (currentY + blockSize / 2f) / (float) texture.getHeight();
                        break;
                    case 1:
                        topY = (currentY + blockSize) / (float) texture.getHeight();
                        rightX = (currentX + blockSize) / (float) texture.getWidth();
                        leftX = (currentX + blockSize / 2f) / (float) texture.getWidth();
                        bottomY = (currentY + blockSize / 2f) / (float) texture.getHeight();
                        break;
                    case 2:
                        topY = (currentY + blockSize / 2f) / (float) texture.getHeight();
                        rightX = (currentX + blockSize / 2f) / (float) texture.getWidth();
                        leftX = currentX / (float) texture.getWidth();
                        bottomY = currentY / (float) texture.getHeight();
                        break;
                    case 3:
                        topY = (currentY + blockSize / 2f) / (float) texture.getHeight();
                        rightX = (currentX + blockSize) / (float) texture.getWidth();
                        leftX = (currentX + blockSize / 2f) / (float) texture.getWidth();
                        bottomY = currentY / (float) texture.getHeight();
                        break;
                }
                Vector2f[] texCoords = {
                        new Vector2f(rightX, topY),
                        new Vector2f(rightX, bottomY),
                        new Vector2f(leftX, bottomY),
                        new Vector2f(leftX, topY)
                };

                if (this.format == 0) {
                    Block.addQuad(new BlockQuad(this.texture, this.name, texCoords, blockSize, j, this.format));
                } else if (this.format == 1 || this.format == 2) {
                    Block.addQuad(new BlockQuad(this.texture, this.name, texCoords, blockSize, j, this.format, texShapes.get(i)));
                }
            }

            currentX += blockSize;

            assert currentX < texture.getWidth() : name + ".json: More textures defined than exist in block sheet";
        }
    }
}

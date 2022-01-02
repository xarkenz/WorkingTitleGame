package core;

import block.BlockQuad;
import block.BlockType;
import org.joml.Vector2i;
import util.AssetPool;
import util.Image;
import util.Logger;

import com.google.gson.*;
import org.joml.Vector2f;
import org.joml.Vector4i;

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

    private BlockType type;
    private int blockSize;
    private int format;

    public BlockSheet(Image image, BlockType type) {
        this.type = type;

        this.blockSize = 16;
        this.format = 0;

        int numBlocks = 0;

        // Read the JSON file

        JsonObject json = null;

        try {
            json = (JsonObject) JsonParser.parseReader(new FileReader("assets/textures/block/" + type.name() + ".json"));
        } catch (FileNotFoundException e) {
            try {
                json = (JsonObject) JsonParser.parseReader(new FileReader("assets/templates/default_block.json"));
            } catch (FileNotFoundException e2) {
                Logger.critical("default_block.json: Not found.");
            } catch (JsonParseException e2) {
                e2.printStackTrace(Logger.getErr());
                Logger.critical("default_block.json: Syntax error while parsing.");
            }
        } catch (JsonParseException e) {
            Logger.critical(type.name() + ".json: Syntax error while parsing.");
        }

        if (json == null) Logger.critical(type.name() + ".json: Invalid format.");

        boolean validFormat = false;
        for (int i = 0; i < FORMATS.length; i++) {
            if (FORMATS[i].equalsIgnoreCase(json.get("format").getAsString())) {
                format = i;
                validFormat = true;
                break;
            }
        }
        if (!validFormat) Logger.critical(type.name() + ".json: Invalid 'format' tag.");

        List<Integer> texShapes = new ArrayList<>();

        // More information needed unless format is 'single'
        if (format == 0) {
            numBlocks = 1;
            blockSize = image.getSize().x; // May have a different height if animated
        } else {
            blockSize = json.get("texture_size").getAsInt();

            JsonArray textures = json.get("textures").getAsJsonArray();
            for (int j = 0; j < textures.size(); j++) {
                numBlocks++;
                String tex =  textures.get(j).getAsString();

                if (this.format == 1) {
                    int shape = -1;
                    for (int i = 0; i < FORMAT_1_SHAPES.length; i++) {
                        if (FORMAT_1_SHAPES[i].equalsIgnoreCase(tex)) {
                            shape = i;
                            break;
                        }
                    }
                    texShapes.add(shape);
                } else if (this.format == 2) {
                    int shape = -1;
                    for (int i = 0; i < FORMAT_2_SHAPES.length; i++) {
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

        float texW = AssetPool.getBlockTexture().getWidth();
        float texH = AssetPool.getBlockTexture().getHeight();
        int currentX = image.getPos()[0].x;
        int currentY = image.getPos()[0].y + image.getSize().y - blockSize;
        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < 4; j++) {
                float topY = 0;
                float rightX = 0;
                float leftX = 0;
                float bottomY = 0;
                
                switch (j) {
                    case 0 -> {
                        topY = currentY + blockSize;
                        rightX = currentX + blockSize / 2f;
                        leftX = currentX;
                        bottomY = currentY + blockSize / 2f;
                    }
                    case 1 -> {
                        topY = currentY + blockSize;
                        rightX = currentX + blockSize;
                        leftX = currentX + blockSize / 2f;
                        bottomY = currentY + blockSize / 2f;
                    }
                    case 2 -> {
                        topY = currentY + blockSize / 2f;
                        rightX = currentX + blockSize / 2f;
                        leftX = currentX;
                        bottomY = currentY;
                    }
                    case 3 -> {
                        topY = currentY + blockSize / 2f;
                        rightX = currentX + blockSize;
                        leftX = currentX + blockSize / 2f;
                        bottomY = currentY;
                    }
                }

                Image quadImage = new Image(new Vector2i(blockSize / 2, blockSize / 2), new Vector2i[]{new Vector2i((int) leftX, (int) bottomY)});

                if (this.format == 0) {
                    BlockQuad.add(new BlockQuad(type, quadImage, blockSize, j, format));
                } else if (this.format == 1 || this.format == 2) {
                    BlockQuad.add(new BlockQuad(type, quadImage, blockSize, j, format, texShapes.get(i)));
                }
            }

            currentX += blockSize;
            if (currentX > image.getPos()[0].x + image.getSize().x)
                Logger.critical(type.name() + ".json: More textures are defined than exist in block sheet.");
        }
    }
}

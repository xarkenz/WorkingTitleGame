package components;

import core.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;
import renderer.Texture;
import scenes.Scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Block extends Component {

    private String name;
    private Vector2i position;
    private HashMap<String, String> attributes;
    private int tlShape, trShape, blShape, brShape;
    private transient boolean isDirty = true;
    private static HashMap<String, List<BlockQuad>> quads = new HashMap<>();
    private boolean[] surroundings = {false, false, false, false, false, false, false, false};

    public Block(String name, Vector2i position) {
        this.name = name;
        this.position = position;
    }

    public Block(String name, Vector2i position, HashMap<String, String> attributes) {
        this.name = name;
        this.position = position;
        this.attributes = attributes;
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt) {
        Scene scene = Window.getScene();
        int x = this.position.x;
        int y = this.position.y;
        boolean[] newSurroundings = {
                scene.getBlock(x - 1, y + 1) != null,
                scene.getBlock(x, y + 1) != null,
                scene.getBlock(x + 1, y + 1) != null,
                scene.getBlock(x - 1, y) != null,
                scene.getBlock(x + 1, y) != null,
                scene.getBlock(x - 1, y - 1) != null,
                scene.getBlock(x, y - 1) != null,
                scene.getBlock(x + 1, y - 1) != null,
        };

        if (!Arrays.equals(newSurroundings, this.surroundings)) {
            this.isDirty = true;
            this.surroundings = newSurroundings;

            // Determine shape of pos 0
            if (!surroundings[1] && !surroundings[3]) {
                tlShape = 0;
            } else if (!surroundings[1] && surroundings[3]) {
                tlShape = 1;
            } else if (surroundings[1] && !surroundings[3]) {
                tlShape = 2;
            } else if (!surroundings[0] && surroundings[1] && surroundings[3]) {
                tlShape = 3;
            } else {
                tlShape = 4;
            }

            // Determine shape of pos 1
            if (!surroundings[1] && !surroundings[4]) {
                trShape = 0;
            } else if (!surroundings[1] && surroundings[4]) {
                trShape = 1;
            } else if (surroundings[1] && !surroundings[4]) {
                trShape = 2;
            } else if (!surroundings[2] && surroundings[1] && surroundings[4]) {
                trShape = 3;
            } else {
                trShape = 4;
            }

            // Determine shape of pos 2
            if (!surroundings[6] && !surroundings[3]) {
                blShape = 0;
            } else if (!surroundings[6] && surroundings[3]) {
                blShape = 1;
            } else if (surroundings[6] && !surroundings[3]) {
                blShape = 2;
            } else if (!surroundings[5] && surroundings[6] && surroundings[3]) {
                blShape = 3;
            } else {
                blShape = 4;
            }

            // Determine shape of pos 3
            if (!surroundings[6] && !surroundings[4]) {
                brShape = 0;
            } else if (!surroundings[6] && surroundings[4]) {
                brShape = 1;
            } else if (surroundings[6] && !surroundings[4]) {
                brShape = 2;
            } else if (!surroundings[7] && surroundings[6] && surroundings[4]) {
                brShape = 3;
            } else {
                brShape = 4;
            }
        }
    }

    @Override
    public void imGui() {

    }

    public static BlockQuad getQuad(String name, int pos, int shape) {
        for (BlockQuad quad: quads.get(name)) {
            if (quad.getPos() == pos && quad.getShape() == shape) {
                return quad;
            }
        }
        return null;
    }

    public static void addQuad(BlockQuad quad) {
        if (!quads.containsKey(quad.getName())) {
            quads.put(quad.getName(), new ArrayList<>());
        }
        quads.get(quad.getName()).add(quad);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(Vector2i position) {
        this.position = position;
    }

    public boolean getDirty() {
        return this.isDirty;
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    public Texture getTexture() {
        return quads.get(this.name).get(0).getTexture();
    }

    public int getShape(int pos) {
        int shape = -1;
        switch (pos) {
            case 0:
                shape = tlShape;
                break;
            case 1:
                shape = trShape;
                break;
            case 2:
                shape = blShape;
                break;
            case 3:
                shape = brShape;
                break;
        }
        return shape;
    }

}

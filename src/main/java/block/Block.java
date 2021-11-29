package block;

import core.Window;
import renderer.Texture;
import world.World;

import org.joml.Vector2i;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class Block {

    public static final String[] BLOCK_NAMES = {
            "aluminum_block",
            "dirt",
//            "oak_log",
            "sand",
            "sandstone",
            "stone",
    };

    private final String name;
    private final String[] noConnect;

    private Vector2i position;
    private HashMap<String, String> attributes;

    private Vector4d collisionBox; // x: width, y: height, z: x-offset, w: y-offset
    private ArrayList<String> tags = new ArrayList<>();

    private int tlShape, trShape, blShape, brShape;
    private transient boolean isDirty = true;
    private boolean[] surroundings = new boolean[8];

    public Block(String name, String[] noConnect, int x, int y) {
        this.name = name;
        this.noConnect = noConnect;
        this.position = new Vector2i(x, y);
        this.attributes = new HashMap<>();
        this.collisionBox = new Vector4d(32, 32, 0, 0);
    }

    /*public void start() {
        this.updateAppearance();
    }

    public void update(float dt) {
        this.updateAppearance();
    }

    private void updateAppearance() {
        if (this.getFormat() == 1 || this.getFormat() == 2) {
            World scene = Window.getScene();
            int x = this.position.x;
            int y = this.position.y;

            boolean[] newSurroundings = {
                    scene.getBlock(x - 1, y + 1) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x - 1, y + 1).getName())),
                    scene.getBlock(x, y + 1) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x, y + 1).getName())),
                    scene.getBlock(x + 1, y + 1) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x + 1, y + 1).getName())),
                    scene.getBlock(x - 1, y) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x - 1, y).getName())),
                    scene.getBlock(x + 1, y) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x + 1, y).getName())),
                    scene.getBlock(x - 1, y - 1) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x - 1, y - 1).getName())),
                    scene.getBlock(x, y - 1) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x, y - 1).getName())),
                    scene.getBlock(x + 1, y - 1) != null && Arrays.stream(noConnect).noneMatch(t -> t.equals(scene.getBlock(x + 1, y - 1).getName())),
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
    }

    public void blockUpdate() {

    }

    public void randomUpdate() {

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

    public Vector2i getPosition() {
        return new Vector2i(position);
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
        return quads.get(name).get(0).getTexture();
    }

    public int getFormat() {
        return quads.get(name).get(0).getFormat();
    }

    public int getShape(int pos) {
        return switch (pos) {
            case 0 -> tlShape;
            case 1 -> trShape;
            case 2 -> blShape;
            case 3 -> brShape;
            default -> -1;
        };
    }

    public String setAttribute(String attribute, String value) {
        return attributes.put(attribute, value);
    }

    public String getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    public static boolean isValidBlockName(String name) {
        for (String n : BLOCK_NAMES) {
            if (n.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static Block createBlock(String name, int x, int y) {
        return switch (name) {
            case "aluminum_block" -> new AluminumBlock(x, y);
            case "dirt" -> new Dirt(x, y);
//            case "oak_log": return new OakLog(x, y);
            case "sand" -> new Sand(x, y);
            case "sandstone" -> new Sandstone(x, y);
            case "stone" -> new Stone(x, y);
            default -> null;
        };
    }*/

}

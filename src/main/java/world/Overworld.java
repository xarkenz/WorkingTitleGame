package world;

import block.*;
import component.*;
import core.*;
import entity.*;
import renderer.DebugDraw;
import util.AssetPool;
import util.Logger;
import util.Settings;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Overworld extends World {

    private final GameObject viewContainer = createGameObject("view");
    private BlockType holdingBlock = null;

    private boolean showGrid = false;
    private float delay = -1;

    public Overworld() {

    }

    @Override
    public void init() {
        loadResources();

        holdingBlock = BlockType.phylumus_block;
        camera = new Camera(new Vector2f());

        generate();

        Player player = new Player(new Vector2d(), new Vector2d(), 0, null);
        player.respawn();
        addEntity(player);

        viewContainer.addComponent(new MouseControls());
        if (showGrid) viewContainer.addComponent(new GridLines());
        viewContainer.addComponent(new EditorCamera(camera));

        viewContainer.getComponent(EditorCamera.class).snapTo(new Vector2f().set(player.getCenter()));

        viewContainer.start();
    }

    private void loadResources() {
        Logger.info("Loading resources...");
        AssetPool.getShader("assets/shaders/default.glsl");

        for (BlockType block : BlockType.values()) {
            BlockSheet sheet = new BlockSheet(AssetPool.getBlockImage(block.name()), block);
            AssetPool.addBlockSheet(block.name(), sheet);
        }
        AssetPool.getBlockTexture().upload();

        AssetPool.getEntityImage("player_test/player_test_idle");
        AssetPool.getEntityImage("player_test/player_test_run");
        AssetPool.getEntityTexture().upload();

        AssetPool.getSound("assets/sounds/block/wood_big_0.ogg");
        AssetPool.getSound("assets/sounds/block/wood_big_1.ogg");
        AssetPool.getSound("assets/sounds/block/wood_big_2.ogg");
        AssetPool.getSound("assets/sounds/block/fart.ogg");
        AssetPool.getSound("assets/sounds/block/blart.ogg");
        AssetPool.getSound("assets/sounds/block/dong.ogg");
    }

    @Override
    public void update(float dt) {
        Vector2i worldPos = new Vector2i((int) Math.floor(MouseListener.getWorldX() / Settings.BLOCK_SIZE), (int) Math.floor(MouseListener.getWorldY() / Settings.BLOCK_SIZE));

        DebugDraw.addRect(new Vector2f(
                Settings.BLOCK_SIZE * (int) Math.floor(MouseListener.getWorldX() / Settings.BLOCK_SIZE) + Settings.BLOCK_SIZE / 2f,
                Settings.BLOCK_SIZE * (int) Math.floor(MouseListener.getWorldY() / Settings.BLOCK_SIZE) + Settings.BLOCK_SIZE / 2f
                ), new Vector2f(14, 14), new Vector3f(0.717647f, 0.384314f, 0.941176f));

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (getBlockType(worldPos.x, worldPos.y) != null) {
                setBlock(worldPos.x, worldPos.y, null);
                AssetPool.getSound("assets/sounds/block/wood_big_0.ogg").play();
            }
        } else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (getBlockType(worldPos.x, worldPos.y) == null) {
                setBlock(worldPos.x, worldPos.y, holdingBlock);
                AssetPool.getSound("assets/sounds/block/wood_big_1.ogg").play();
            }
        } else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
            if (delay <= 0) {
                boolean getNext = false;
                for (BlockType block : BlockType.values()) {
                    if (getNext) {
                        getNext = false;
                        holdingBlock = block;
                        break;
                    }
                    getNext = block.equals(holdingBlock);
                }
                if (getNext) holdingBlock = BlockType.values()[0];
                delay = 1;
            }
        } else {
            delay = 0;
        }

        if (delay > 0) delay -= dt;

        viewContainer.update(dt);
        camera.adjustProjection();

        for (Entity entity : entities) {
            entity.update(dt);
            if (entity instanceof Player)
                viewContainer.getComponent(EditorCamera.class).setTargetPos(new Vector2f().set(entity.getCenter()));
        }
        for (Chunk chunk : chunks.values()) {
            chunk.update(dt);
        }
    }

    @Override
    public void render() {
        renderer.render();
    }

    @Override
    public void imGui() {

    }

    @Override
    public void generate() {
        Logger.info("Generating world...");
        generator.generateBaseTerrain(40, 100, 14, BlockType.stone);
        generator.worms(-40, 100, 1, 6, 8, 3, 10, 40, 30, null, x -> x != null && x.equals(BlockType.stone));
        generator.applyTopLayer(50, 6, 9, 3, BlockType.dirt, x -> x != null && x.equals(BlockType.stone));
        generator.applyTopLayer(50, 1, BlockType.grassy_dirt, x -> x != null && x.equals(BlockType.dirt));
        generator.worms(0, 70, 2, 1, 3, 2, 1, 3, 70, BlockType.packed_dirt, x -> x != null && x.equals(BlockType.stone));
    }

}

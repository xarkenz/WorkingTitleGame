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

        holdingBlock = BlockType.stone;
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

        for (BlockType block: BlockType.values()) {
            AssetPool.getBlockTexCoords(block.name());
        }
        for (BlockType block : BlockType.values()) {
            BlockSheet sheet = new BlockSheet(AssetPool.getBlockTexCoords(block.name()), block);
            AssetPool.addBlockSheet(block.name(), sheet);
        }
        AssetPool.getBlockTexture().upload();

        AssetPool.getEntityTexCoords("player_test/player_test_idle");
//        AssetPool.getEntityTexCoords("player_test_run");
        AssetPool.getEntityTexture().upload();
    }

    @Override
    public void update(float dt) {
        viewContainer.update(dt);
        camera.adjustProjection();

        for (GameObject object : gameObjects) {
            object.update(dt);
        }
        for (Entity entity : entities) {
            entity.update(dt);
            if (entity instanceof Player)
                viewContainer.getComponent(EditorCamera.class).setTargetPos(new Vector2f().set(entity.getCenter()));
        }
        for (Chunk chunk : chunks.values()) {
            chunk.update(dt);
        }

        Vector2i worldPos = new Vector2i((int) Math.floor(MouseListener.getWorldX() / Settings.BLOCK_SIZE), (int) Math.floor(MouseListener.getWorldY() / Settings.BLOCK_SIZE));

        DebugDraw.addRect(new Vector2f(
                Settings.BLOCK_SIZE * (int) Math.floor(MouseListener.getWorldX() / Settings.BLOCK_SIZE) + Settings.BLOCK_SIZE / 2f,
                Settings.BLOCK_SIZE * (int) Math.floor(MouseListener.getWorldY() / Settings.BLOCK_SIZE) + Settings.BLOCK_SIZE / 2f
                ), new Vector2f(14, 14), new Vector3f(0.223529f, 0.627451f, 0.980392f));

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (getBlockType(worldPos.x, worldPos.y) != null)
                setBlock(worldPos.x, worldPos.y, null);
        } else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (getBlockType(worldPos.x, worldPos.y) == null)
                setBlock(worldPos.x, worldPos.y, holdingBlock);
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
        for (int y = -32; y < 0; y++) {
            for (int x = -16; x < 16; x++) {
                setBlock(x, y, BlockType.dirt);
            }
        }
    }

}

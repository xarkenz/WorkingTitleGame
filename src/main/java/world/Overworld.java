package world;

import block.*;
import component.*;
import core.*;
import entity.*;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.AssetPool;
import util.Settings;

import java.io.FileNotFoundException;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class Overworld extends World {

    private GameObject viewContainer = createGameObject("level_editor");
    private BlockType holdingBlock = null;

    private boolean showGrid = true;

    public Overworld() {

    }

    @Override
    public void init() {
        loadResources();

        holdingBlock = BlockType.stone;
        camera = new Camera(new Vector2f());

        generate();

        Player player = new Player(new Vector2d(), new Vector2d());
        player.respawn();
        addEntity(player);

        viewContainer.addComponent(new MouseControls());
        if (showGrid) viewContainer.addComponent(new GridLines());
        viewContainer.addComponent(new EditorCamera(camera));

        viewContainer.start();
    }

    private void loadResources() {
        try {
            AssetPool.getShader("assets/shaders/Default.glsl");
            AssetPool.addSpriteSheet("assets/textures/test/test_tiles.png",
                    new SpriteSheet(AssetPool.getTexture("assets/textures/test/test_tiles.png"), 16, 16, 81));
            for (BlockType block : BlockType.values()) {
                AssetPool.addBlockSheet("assets/textures/block/" + block.name() + ".png",
                        new BlockSheet(AssetPool.getTexture("assets/textures/block/" + block.name() + ".png"), block));
            }
            for (GameObject go : gameObjects) {
                if (go.getComponent(SpriteRenderer.class) != null) {
                    SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
                    if (spr.getTexture() != null) {
                        spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilePath()));
                    }
                }
            }
        } catch (FileNotFoundException err) {
            err.printStackTrace();
        }
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
                viewContainer.getComponent(EditorCamera.class).setTargetPos((float) entity.getCenter().x, (float) entity.getCenter().y);
        }
        for (Chunk chunk : chunks.values()) {
            chunk.update(dt);
        }

        Vector2i worldPos = new Vector2i((int) Math.floor(MouseListener.getWorldX() / Settings.GRID_SIZE), (int) Math.floor(MouseListener.getWorldY() / Settings.GRID_SIZE));

        DebugDraw.addRect(new Vector2f(
                Settings.GRID_SIZE * (int) Math.floor(MouseListener.getWorldX() / Settings.GRID_SIZE) + Settings.GRID_SIZE / 2f,
                Settings.GRID_SIZE * (int) Math.floor(MouseListener.getWorldY() / Settings.GRID_SIZE) + Settings.GRID_SIZE / 2f
                ), new Vector2f(32, 32), new Vector3f(0, 0.5f, 0.5f));

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (getBlockType(worldPos.x, worldPos.y) != null)
                setBlock(worldPos.x, worldPos.y, null);
        } else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (getBlockType(worldPos.x, worldPos.y) == null)
                setBlock(worldPos.x, worldPos.y, holdingBlock);
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
        for (int y = -32; y < 0; y++) {
            for (int x = -16; x < 16; x++) {
                setBlock(x, y, BlockType.dirt);
            }
        }
    }

}

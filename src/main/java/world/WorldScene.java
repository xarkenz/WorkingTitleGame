package world;

import block.*;
import component.*;
import core.*;
import entity.*;
import gui.Button;
import gui.GuiElement;
import renderer.DebugDraw;
import util.AssetPool;
import util.Logger;
import util.Settings;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class WorldScene extends Scene {

    private final GameObject viewContainer = createGameObject("view");
    private BlockType holdingBlock = null;

    private float delay = -1;
    private boolean mouseLeftDown = false;

    public WorldScene() {

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

        Button button = new Button("test_button", null, -150, 100, 128, 96, x -> AssetPool.getSound("block/dong").play());
        addGuiElement(button);
        button.setVisible(true);

        viewContainer.addComponent(new MouseControls());
        viewContainer.addComponent(new GridLines());
        viewContainer.addComponent(new EditorCamera(camera));

        viewContainer.getComponent(EditorCamera.class).snapTo(new Vector2f().set(player.getCenter()));

        viewContainer.start();
    }

    private void loadResources() {
        Logger.info("Loading resources...");

        AssetPool.getGuiImage("buttons");
        AssetPool.getGuiImage("world_flags");
        AssetPool.getGuiTexture().upload();

        AssetPool.getEntityImage("player_test/player_test_idle");
        AssetPool.getEntityImage("player_test/player_test_run");
        AssetPool.getEntityTexture().upload();

        for (BlockType block : BlockType.values()) {
            AssetPool.addBlockSheet(block.name(), new BlockSheet(AssetPool.getBlockImage(block.name()), block));
        }
        AssetPool.getBlockTexture().upload();

        AssetPool.getSound("block/wood_big_0");
        AssetPool.getSound("block/wood_big_1");
        AssetPool.getSound("block/wood_big_2");
        AssetPool.getSound("block/fart");
        AssetPool.getSound("block/blart");
        AssetPool.getSound("block/dong");
    }

    @Override
    public void update(float dt) {
        Vector2i worldPos = new Vector2i((int) Math.floor(MouseListener.getWorldX() / Settings.BLOCK_SIZE), (int) Math.floor(MouseListener.getWorldY() / Settings.BLOCK_SIZE));
        Vector2i screenPos = new Vector2i(Math.round((MouseListener.getScreenX() - Settings.DISPLAY_WIDTH * 0.5f) / Settings.GUI_SCALE), Math.round((MouseListener.getScreenY() - Settings.DISPLAY_HEIGHT * 0.5f) / Settings.GUI_SCALE));

        DebugDraw.addRect(new Vector2f(
                Settings.BLOCK_SIZE * (int) Math.floor(MouseListener.getWorldX() / Settings.BLOCK_SIZE) + Settings.BLOCK_SIZE * 0.5f,
                Settings.BLOCK_SIZE * (int) Math.floor(MouseListener.getWorldY() / Settings.BLOCK_SIZE) + Settings.BLOCK_SIZE * 0.5f
                ), new Vector2f(14, 14), new Vector3f(0.717647f, 0.384314f, 0.941176f));

        GuiElement selected = null;
        for (GuiElement element : guiElements) {
            boolean wants = element.isVisible() && element.wantsMouse(screenPos.x, screenPos.y);
            element.setHovering(wants);
            if (wants) selected = element;
            element.setFocused(Window.getFocus() == element);
        }

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (selected == null && getBlockType(worldPos.x, worldPos.y) != null) {
                setBlock(worldPos.x, worldPos.y, null);
                AssetPool.getSound("block/wood_big_0").play();
            }

            if (!mouseLeftDown) {
                if (selected != null) {
                    selected.setFocused(true);
                    selected.mousePress(screenPos.x, screenPos.y);
                }
                Window.setFocus(selected);
            } else if (Window.getFocus() != null) {
                Window.getFocus().mouseDrag(screenPos.x, screenPos.y);
            }
            mouseLeftDown = true;
        } else {
            if (mouseLeftDown && Window.getFocus() != null) {
                Window.getFocus().mouseRelease(screenPos.x, screenPos.y);
            }
            mouseLeftDown = false;
        }
        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (getBlockType(worldPos.x, worldPos.y) == null) {
                setBlock(worldPos.x, worldPos.y, holdingBlock);
                AssetPool.getSound("block/wood_big_1").play();
            }
        }
        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
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
            } else {
                delay -= dt;
            }
        } else {
            delay = 0;
        }

        viewContainer.update(dt);
        camera.adjustProjection();

        for (GuiElement element : guiElements) {
            if (element.isVisible()) element.update(dt);
        }
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
        generator.worms(0, 70, 2, 1, 3, 2, 1, 3, 70, BlockType.phylumus_block, x -> x != null && x.equals(BlockType.stone));
    }

}

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

public class Overworld extends World {

    private SpriteSheet sprites;
    private GameObject viewContainer = createGameObject("level_editor");
    private BlockType holdingBlock = null;
    private float interactionDelay = 0;

    private boolean showGrid = true;

    public Overworld() {

    }

    @Override
    public void init() {
        loadResources();
        sprites = AssetPool.getSpritesheet("assets/textures/test/test_tiles.png");
        holdingBlock = BlockType.aluminum_block;
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

        DebugDraw.addRect(new Vector2f(
                Settings.GRID_SIZE * (int) Math.floor(MouseListener.getWorldX() / Settings.GRID_SIZE) + Settings.GRID_SIZE / 2f,
                Settings.GRID_SIZE * (int) Math.floor(MouseListener.getWorldY() / Settings.GRID_SIZE) + Settings.GRID_SIZE / 2f
                ), new Vector2f(32, 32), new Vector3f(0, 0.5f, 0.5f));

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (interactionDelay <= 0) {
                Vector2i worldPos = new Vector2i((int) Math.floor(MouseListener.getWorldX() / Settings.GRID_SIZE), (int) Math.floor(MouseListener.getWorldY() / Settings.GRID_SIZE));
                if (getBlockType(worldPos.x, worldPos.y) == null) {
                    if (holdingBlock != null) {
                        setBlock(worldPos.x, worldPos.y, holdingBlock);
                    }
                } else {
                    setBlock(worldPos.x, worldPos.y, null);
                }
                interactionDelay = 0.3f;
            } else {
                interactionDelay -= dt;
            }
        } else {
            interactionDelay = 0;
        }
    }

    @Override
    public void render() {
        renderer.render();
    }

    @Override
    public void imGui() {
        /*ImGui.begin("Debug Menu");
        viewContainer.imGui();
        ImGui.end();

        ImGui.begin("Tile Palette");

        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        float windowX2 = windowPos.x + windowSize.x;
        for (int i = 0; i < sprites.size(); i++) {
            Sprite sprite = sprites.getSprite(i);
            float spriteWidth = sprite.getWidth() * 2;
            float spriteHeight = sprite.getHeight() * 2;
            int id = sprite.getTexID();
            Vector2f[] texCoords = sprite.getTexCoords();

            ImGui.pushID(i);
            if (ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
                GameObject object = Prefabs.generateSpriteObject(sprite, 32, 32);
                levelEditorContainer.getComponent(MouseControls.class).bindObject(object);
            }
            ImGui.popID();

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float lastButtonX2 = lastButtonPos.x;
            float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
            if (i + 1 < sprites.size() && nextButtonX2 < windowX2) {
                ImGui.sameLine();
            }
        }

        ImGui.end();*/
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

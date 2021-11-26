package scenes;

import blocks.*;
import components.*;
import entities.Entity;
import entities.Player;
import imgui.ImGui;
import imgui.ImVec2;
import core.*;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import util.AssetPool;
import util.Settings;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class LevelEditorScene extends Scene {

    private SpriteSheet sprites;
    private GameObject levelEditorContainer = this.createGameObject("level_editor");
    private String holdingBlock = null;
    private float interactionDelay = 0;

    private boolean showGrid = true;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        loadResources();
        this.sprites = AssetPool.getSpritesheet("assets/textures/test/test_tiles.png");
        this.holdingBlock = "dirt";
        this.camera = new Camera(new Vector2f(0, 0));

        Vector2i[] positions0 = {
                new Vector2i(10, 7),
                new Vector2i(11, 7),
                new Vector2i(12, 7),
                new Vector2i(10, 8),
                new Vector2i(11, 8),
                new Vector2i(12, 8),
        };
        for (Vector2i pos : positions0) {
            this.addBlock(new Stone(pos, new HashMap<>()));
        }
        Vector2i[] positions1 = {
                new Vector2i(10, 10),
                new Vector2i(11, 10),
                new Vector2i(10, 9),
                new Vector2i(11, 9),
                new Vector2i(10, 11),
                new Vector2i(11, 12),
                new Vector2i(12, 9),
                new Vector2i(12, 10),
        };
        for (Vector2i pos : positions1) {
            this.addBlock(new Sandstone(pos, new HashMap<>()));
        }
        Vector2i[] positions2 = {
                new Vector2i(11, 13),
                new Vector2i(10, 13),
                new Vector2i(10, 14),
                new Vector2i(11, 14),
                new Vector2i(11, 11),
                new Vector2i(10, 12),
                new Vector2i(12, 11),
                new Vector2i(12, 12),
                new Vector2i(12, 13),
                new Vector2i(12, 14),
        };
        for (Vector2i pos : positions2) {
            this.addBlock(new Sand(pos, new HashMap<>()));
        }

        this.addEntity(new Player(new Vector2d(250, 400), new Vector2d(150, 512)));

        levelEditorContainer.addComponent(new MouseControls());
        if (this.showGrid) levelEditorContainer.addComponent(new GridLines());
        levelEditorContainer.addComponent(new EditorCamera(this.camera));

        levelEditorContainer.start();
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/Default.glsl");
        AssetPool.addSpriteSheet("assets/textures/test/test_tiles.png",
                new SpriteSheet(AssetPool.getTexture("assets/textures/test/test_tiles.png"), 16, 16, 81));
        for (String name: Block.BLOCK_NAMES) {
            AssetPool.addBlockSheet("assets/textures/blocks/" + name + ".png",
                    new BlockSheet(AssetPool.getTexture("assets/textures/blocks/" + name + ".png"), name));
        }
        for (GameObject go : gameObjects) {
            if (go.getComponent(SpriteRenderer.class) != null) {
                SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
                if (spr.getTexture() != null) {
                    spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilePath()));
                }
            }
        }
    }

    @Override
    public void update(float dt) {
        levelEditorContainer.update(dt);
        this.camera.adjustProjection();

        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }
        for (Entity entity : this.entities) {
            entity.update(dt);
        }
        for (Block block : this.blocks.values()) {
            block.update(dt);
        }

        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (this.interactionDelay <= 0f) {
                Vector2i worldPos = new Vector2i((int) (MouseListener.getWorldX() / Settings.GRID_SIZE), (int) (MouseListener.getWorldY() / Settings.GRID_SIZE));
                if (this.blocks.get(worldPos) == null) {
                    if (this.holdingBlock != null) {
                        Block newBlock = Block.createBlock(this.holdingBlock, worldPos, new HashMap<>());
                        this.addBlock(newBlock);
                    }
                } else {
                    this.removeBlock(this.blocks.get(worldPos));
                }
                this.interactionDelay = 0.3f;
            } else {
                this.interactionDelay -= dt;
            }
        } else {
            this.interactionDelay = 0f;
        }
    }

    @Override
    public void render() {
        this.renderer.render();
    }

    @Override
    public void imGui() {
        ImGui.begin("Level Editor Debug");
        levelEditorContainer.imGui();
        ImGui.end();

        ImGui.begin("Tile Palette");

        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        float windowX2 = windowPos.x + windowSize.x;
        for (int i=0; i < sprites.size(); i++) {
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

        ImGui.end();
    }
}

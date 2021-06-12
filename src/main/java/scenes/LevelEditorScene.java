package scenes;

import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import core.*;
import org.joml.Vector2f;
import org.joml.Vector2i;
import util.AssetPool;

public class LevelEditorScene extends Scene {

    private SpriteSheet sprites;

    GameObject levelEditorContainer = new GameObject("LevelEditor", new Transform(new Vector2f()), 0);

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        loadResources();
        sprites = AssetPool.getSpritesheet("assets/textures/test/test_tiles.png");


        this.camera = new Camera(new Vector2f(0, 0));

        levelEditorContainer.addComponent(new MouseControls());
        levelEditorContainer.addComponent(new GridLines());
        levelEditorContainer.addComponent(new EditorCamera(this.camera));

        levelEditorContainer.start();
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/Default.glsl");
        AssetPool.addSpriteSheet("assets/textures/test/test_tiles.png",
                new SpriteSheet(AssetPool.getTexture("assets/textures/test/test_tiles.png"), 16, 16, 81));
        String name = "sandstone";
        AssetPool.addBlockSheet("assets/textures/blocks/" + name + ".png",
                new BlockSheet(AssetPool.getTexture("assets/textures/blocks/" + name + ".png"), name, 16, 16, 5));
        Vector2i[] positions0 = {
                new Vector2i(10, 10),
                new Vector2i(11, 10),
                new Vector2i(10, 9),
                new Vector2i(11, 9),
                new Vector2i(10, 11),
                new Vector2i(11, 12),
                new Vector2i(12, 9),
                new Vector2i(12, 10),
        };
        for (Vector2i pos : positions0) {
            this.addBlock(new Block(name, pos));
        }
        name = "sand";
        AssetPool.addBlockSheet("assets/textures/blocks/" + name + ".png",
                new BlockSheet(AssetPool.getTexture("assets/textures/blocks/" + name + ".png"), name, 16, 16, 5));
        Vector2i[] positions1 = {
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
        for (Vector2i pos : positions1) {
            this.addBlock(new Block(name, pos));
        }
        name = "oak_log_y";
        AssetPool.addBlockSheet("assets/textures/blocks/" + name + ".png",
                new BlockSheet(AssetPool.getTexture("assets/textures/blocks/" + name + ".png"), name, 16, 16, 5));
        Vector2i[] positions2 = {
                new Vector2i(11, 15),
                new Vector2i(11, 16),
                new Vector2i(11, 17),
                new Vector2i(11, 18),
        };
        for (Vector2i pos : positions2) {
            this.addBlock(new Block(name, pos));
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
        for (Block block : this.blocks.values()) {
            block.update(dt);
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
                levelEditorContainer.getComponent(MouseControls.class).pickupObject(object);
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

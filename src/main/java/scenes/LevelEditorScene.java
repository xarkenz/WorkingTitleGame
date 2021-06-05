package scenes;

import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import jade.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import renderer.DebugDraw;
import scenes.Scene;
import util.AssetPool;

public class LevelEditorScene extends Scene {

    private GameObject obj1;
    private GameObject obj2;
    private Spritesheet sprites;

    GameObject levelEditorContainer = new GameObject("LevelEditor", new Transform(new Vector2f()), 0);

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        loadResources();
        sprites = AssetPool.getSpritesheet("assets/textures/test/test_tiles.png");
        Spritesheet gizmos = AssetPool.getSpritesheet("assets/textures/test/gizmos.png");

        this.camera = new Camera(new Vector2f(0, 0));

        levelEditorContainer.addComponent(new MouseControls());
        levelEditorContainer.addComponent(new GridLines());
        levelEditorContainer.addComponent(new EditorCamera(this.camera));

        levelEditorContainer.start();
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/Default.glsl");
        AssetPool.getTexture("assets/textures/test/blend_test_2.png");
        AssetPool.addSpritesheet("assets/textures/test/gizmos.png",
                new Spritesheet(AssetPool.getTexture("assets/textures/test/gizmos.png"), 24, 48, 3, 0));
        AssetPool.addSpritesheet("assets/textures/test/test_tiles.png",
                new Spritesheet(AssetPool.getTexture("assets/textures/test/test_tiles.png"), 16, 16, 81, 0));

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

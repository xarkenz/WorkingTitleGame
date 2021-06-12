package renderer;

import components.Block;
import components.SpriteRenderer;
import core.GameObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Renderer {
    private final int MAX_BATCH_SIZE = 1000;
    private List<RenderBatch> batches;
    private static Shader currentShader;

    public Renderer() {
        this.batches = new ArrayList<>();
    }

    public void add(GameObject go) {
        SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
        if (spr != null) {
            add(spr);
        }
    }

    public void add(SpriteRenderer sprite) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.getHasRoom() && batch.getZIndex() == sprite.gameObject.zIndex() && batch.getIsSpriteBatch()) {
                Texture tex = sprite.getTexture();
                if (tex == null || (batch.getHasTexture(tex) || batch.getHasTextureRoom())) {
                    batch.addSprite(sprite);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.zIndex(), true);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(sprite);
            Collections.sort(batches);
        }
    }

    public void add(Block block) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.getHasRoom() && batch.getZIndex() == 0 && !batch.getIsSpriteBatch()) {
                Texture tex = block.getTexture();
                if (tex == null || (batch.getHasTexture(tex) || batch.getHasTextureRoom())) {
                    batch.addBlock(block);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, 0, false);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addBlock(block);
            Collections.sort(batches);
        }
    }

    public static void bindShader(Shader shader) {
        currentShader = shader;
    }

    public static Shader getBoundShader() {
        return currentShader;
    }

    public void render() {
        currentShader.use();
        for (RenderBatch batch : batches) {
            batch.render();
        }
    }
}

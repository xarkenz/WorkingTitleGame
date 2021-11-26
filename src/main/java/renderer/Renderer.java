package renderer;

import blocks.Block;
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
            if (batch.hasRoom() && batch.getZIndex() == sprite.gameObject.transform.zIndex && batch.isSpriteBatch()) {
                Texture tex = sprite.getTexture();
                if (tex == null || (batch.hasTexture(tex) || batch.hasTextureRoom())) {
                    batch.addSprite(sprite);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.transform.zIndex, true);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(sprite);
            Collections.sort(batches);
        }
    }

    public void add(Block block) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.hasRoom() && batch.getZIndex() == 0 && !batch.isSpriteBatch()) {
                Texture tex = block.getTexture();
                if (tex == null || (batch.hasTexture(tex) || batch.hasTextureRoom())) {
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

    public void remove(Block block) {
        for (RenderBatch batch : batches) {
            // Only removes the block if it finds it, so we don't need to check if the batch has the block
            batch.removeBlock(block);
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

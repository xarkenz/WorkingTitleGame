package renderer;

import block.Chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Renderer {

//    private final int MAX_BATCH_SIZE = 1000;
    private final List<ChunkRenderer> batches;
    private static Shader currentShader;

    public Renderer() {
        batches = new ArrayList<>();
    }

    /*public void add(GameObject go) {
        SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
        if (spr != null) {
            add(spr);
        }
    }

    public void add(SpriteRenderer sprite) {
        boolean added = false;
        for (SpriteRenderBatch batch : batches) {
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
            SpriteRenderBatch newBatch = new SpriteRenderBatch(MAX_BATCH_SIZE, sprite.gameObject.transform.zIndex, true);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(sprite);
            Collections.sort(batches);
        }
    }*/

    public void add(Chunk chunk) {
        boolean added = false;

        for (ChunkRenderer batch : batches) {
            if (batch.isEmpty()) {
                batch.setChunk(chunk);
                added = true;
                break;
            }
        }

        if (!added) {
            ChunkRenderer newBatch = new ChunkRenderer(0);
            newBatch.start();
            batches.add(newBatch);
            newBatch.setChunk(chunk);
            Collections.sort(batches);
        }
    }

    public void remove(Chunk chunk) {
        for (ChunkRenderer batch : batches) {
            if (batch.getChunk().equals(chunk))
                batch.setChunk(null);
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
        for (ChunkRenderer batch : batches) {
            batch.render();
        }
    }
}

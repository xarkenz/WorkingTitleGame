package renderer;

import block.Chunk;
import entity.Entity;
import gui.GuiElement;

import java.util.ArrayList;
import java.util.Collections;

public class Renderer {

    private final ArrayList<ChunkRenderer> chunks;
    private final ArrayList<EntityRenderBatch> entityBatches;
    private final ArrayList<GuiRenderBatch> guiBatches;
    private static Shader currentShader;

    public Renderer() {
        chunks = new ArrayList<>();
        entityBatches = new ArrayList<>();
        guiBatches = new ArrayList<>();
    }

    public void addGuiElement(GuiElement element) {
        boolean added = false;
        for (GuiRenderBatch batch : guiBatches) {
            if (batch.getZIndex() == 0) {
                added = batch.add(element);
                break;
            }
        }

        if (!added) {
            GuiRenderBatch newBatch = new GuiRenderBatch(0);
            newBatch.start();
            guiBatches.add(newBatch);
            newBatch.add(element);
            Collections.sort(guiBatches);
        }
    }

    public void addEntity(Entity entity) {
        if (entity.getAppearance().numElements() == 0) return;

        boolean added = false;
        for (EntityRenderBatch batch : entityBatches) {
            if (batch.getZIndex() == 0) {
                added = batch.add(entity);
                break;
            }
        }

        if (!added) {
            EntityRenderBatch newBatch = new EntityRenderBatch(0);
            newBatch.start();
            entityBatches.add(newBatch);
            newBatch.add(entity);
            Collections.sort(entityBatches);
        }
    }

    public void addChunk(Chunk toAdd) {
        boolean added = false;

        for (ChunkRenderer chunk : chunks) {
            if (chunk.isEmpty()) {
                chunk.setChunk(toAdd);
                added = true;
                break;
            }
        }

        if (!added) {
            ChunkRenderer chunk = new ChunkRenderer(0);
            chunk.start();
            chunks.add(chunk);
            chunk.setChunk(toAdd);
            Collections.sort(chunks);
        }
    }

    public void removeChunk(Chunk toRemove) {
        for (ChunkRenderer chunk : chunks) {
            if (chunk.getChunk().equals(toRemove))
                chunk.setChunk(null);
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
        for (ChunkRenderer chunk : chunks) {
            chunk.render();
        }
        for (EntityRenderBatch batch : entityBatches) {
            batch.render();
        }
        for (GuiRenderBatch batch : guiBatches) {
            batch.render();
        }
    }
}

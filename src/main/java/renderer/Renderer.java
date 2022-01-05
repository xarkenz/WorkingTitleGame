package renderer;

import block.Chunk;
import entity.Entity;
import gui.GuiElement;
import util.AssetPool;
import util.EntityAppearance;

import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;

public class Renderer {

    private final ArrayList<Entity> entities = new ArrayList<>();
    private final ArrayList<GuiElement> guiElements = new ArrayList<>();

    private final ArrayList<ChunkRenderer> chunks = new ArrayList<>();
    private final ArrayList<RenderBatch> batches = new ArrayList<>();

    private static Shader currentShader;

    public Renderer() {

    }

    public int addImage(Texture texture, int zIndex, Vector2f[] position, Vector4f color, Vector2f[] texCoords, boolean staticPos, boolean visible) {
        int index = -1;
        for (RenderBatch batch : batches) {
            if (batch.getTexID() == texture.getID() && batch.getZIndex() == zIndex) {
                index = batch.add(position, color, texCoords, staticPos, visible);
                break;
            }
        }

        if (index == -1) {
            RenderBatch newBatch = new RenderBatch(texture, zIndex);
            newBatch.start();
            batches.add(newBatch);
            index = newBatch.add(position, color, texCoords, staticPos, visible);
            Collections.sort(batches);
        }

        return index;
    }

    public void removeImage(int texID, int zIndex, int index) {
        getBatch(texID, zIndex).remove(index);
    }

    public RenderBatch getBatch(int texID, int zIndex) {
        for (RenderBatch batch : batches) {
            if (batch.getTexID() == texID && batch.getZIndex() == zIndex) {
                return batch;
            }
        }

        return null;
    }

    public void addGuiElement(GuiElement element) {
        guiElements.add(element);

        element.updateGraphics(this);
    }

    public int[] addEntity(Entity entity) {
        entities.add(entity);

        EntityAppearance appearance = entity.getAppearance();
        int num = appearance.numElements();
        int[] indices = new int[num];
        Vector2f entityPos = new Vector2f().set(entity.getPosition());

        for (int i = 0; i < num; i++) {
            Vector4f place = new Vector4f(appearance.getPlace(i));
            place.add(entityPos.x, entityPos.y, 0, 0);
            Vector2f[] position = {new Vector2f(place.x + place.z, place.y + place.w), new Vector2f(place.x + place.z, place.y), new Vector2f(place.x, place.y), new Vector2f(place.x, place.y + place.w)};
            indices[i] = addImage(AssetPool.getEntityTexture(), 0, position, appearance.getColor(), appearance.getTexCoords(i), false, appearance.isVisible(i));
        }

        return indices;
    }

    public void removeEntity(Entity entity) {
        RenderBatch containing = getBatch(AssetPool.getEntityTexture().getID(), 0);
        if (containing != null) {
            for (int index : entity.getElementIndices()) {
                containing.remove(index);
            }
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
        for (Entity entity : entities) {
            if (entity.isDirty()) {
                entity.setDirty(false);
                RenderBatch containing = getBatch(AssetPool.getEntityTexture().getID(), 0);
                if (containing != null) {
                    EntityAppearance appearance = entity.getAppearance();
                    Vector2f entityPos = new Vector2f().set(entity.getPosition());
                    int[] indices = entity.getElementIndices();
                    for (int i = 0; i < appearance.numElements(); i++) {
                        Vector4f place = new Vector4f(appearance.getPlace(i));
                        place.add(entityPos.x, entityPos.y, 0, 0);
                        Vector2f[] position = {new Vector2f(place.x + place.z, place.y + place.w), new Vector2f(place.x + place.z, place.y), new Vector2f(place.x, place.y), new Vector2f(place.x, place.y + place.w)};
                        containing.setPosition(indices[i], position);
                        containing.setColor(indices[i], appearance.getColor());
                        containing.setTexCoords(indices[i], appearance.getTexCoords(i));
                        containing.setVisible(indices[i], appearance.isVisible(i));
                    }
                }
            }
        }

        for (GuiElement element : guiElements) {
            if (element.isDirty()) {
                element.setDirty(false);
                element.updateGraphics(this);
            }
        }

        currentShader.use();
        for (ChunkRenderer chunk : chunks) {
            chunk.render();
        }
        for (RenderBatch batch : batches) {
            batch.render();
        }
    }
}

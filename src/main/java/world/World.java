package world;

import block.BlockState;
import block.BlockType;
import block.Chunk;
import component.Component;
import component.ComponentSerializer;
import component.Transform;
import core.Camera;
import core.GameObject;
import core.GameObjectSerializer;
import entity.Entity;
import renderer.Renderer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joml.Vector2i;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class World {

    protected Renderer renderer = new Renderer();
    protected Camera camera;
    protected boolean isRunning = false;
    protected List<GameObject> gameObjects = new ArrayList<>();
    protected List<Entity> entities = new ArrayList<>();
    protected HashMap<Vector2i, Chunk> chunks = new HashMap<>();
    protected boolean worldLoaded = false;

    public World() {

    }

    public void init() {

    }

    public void start() {
        /*for (GameObject go : gameObjects) {
            go.start();
            renderer.add(go);
        }*/
        for (Entity entity : entities) {
            entity.start();
//            renderer.add(entity);
        }
        for (Chunk chunk : chunks.values()) {
            chunk.start();
            renderer.add(chunk);
        }
        isRunning = true;
    }

    public Chunk getChunk(Vector2i chunkPos) {
        Chunk chunk = chunks.get(chunkPos);
        if (chunk == null) {
            chunk = new Chunk(chunkPos, this);
            chunks.put(chunkPos, chunk);
            if (isRunning) {
                chunk.start();
                renderer.add(chunk);
            }
        }
        return chunk;
    }

    public Chunk getLoadedChunk(Vector2i chunkPos) {
        return chunks.get(chunkPos);
    }

    public void deleteChunk(Vector2i chunkPos) {
        Chunk chunk = chunks.remove(chunkPos);
        if (chunk != null) renderer.remove(chunk);
    }

    public BlockType getBlockType(int x, int y) {
        Vector2i chunkPos = new Vector2i(Math.floorDiv(x, Chunk.SIZE), Math.floorDiv(y, Chunk.SIZE));
        return getChunk(chunkPos).getBlockType(Math.floorMod(x, Chunk.SIZE), Math.floorMod(y, Chunk.SIZE));
    }

    public BlockState getBlockState(int x, int y) {
        Vector2i chunkPos = new Vector2i(Math.floorDiv(x, Chunk.SIZE), Math.floorDiv(y, Chunk.SIZE));
        return getChunk(chunkPos).getBlockState(Math.floorMod(x, Chunk.SIZE), Math.floorMod(y, Chunk.SIZE));
    }

    public GameObject createGameObject(String name) {
        GameObject go = new GameObject(name);
        go.addComponent(new Transform());
        go.transform = go.getComponent(Transform.class);
        return go;
    }

    /*public void addGameObject(GameObject go) {
        gameObjects.add(go);
        if (isRunning) {
            go.start();
            renderer.add(go);
        }
    }*/

    public void addEntity(Entity entity) {
        entities.add(entity);
        if (isRunning) {
            entity.start();
//            renderer.add(entity);
        }
    }

    public void setBlock(int x, int y, BlockType type, BlockState state) {
        Vector2i chunkPos = new Vector2i(Math.floorDiv(x, Chunk.SIZE), Math.floorDiv(y, Chunk.SIZE));
        getChunk(chunkPos).setBlock(Math.floorMod(x, Chunk.SIZE), Math.floorMod(y, Chunk.SIZE), type, state);
    }

    public void setBlock(int x, int y, BlockType type) {
        setBlock(x, y, type, type == null ? null : type.defaultState());
    }

    public GameObject getGameObject(int uid) {
        Optional<GameObject> result = gameObjects.stream()
                .filter(gameObject -> gameObject.getUID() == uid)
                .findFirst();
        return result.orElse(null);
    }

    public Entity getEntity(int uid) {
        Optional<Entity> result = entities.stream()
                .filter(entity -> entity.getUID() == uid)
                .findFirst();
        return result.orElse(null);
    }

    public abstract void update(float dt);

    public abstract void render();

    public Camera getCamera() {
        return camera;
    }

    public void imGui() {

    }

    public void save() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentSerializer())
                .registerTypeAdapter(GameObject.class, new GameObjectSerializer())
                .create();

        try {
            FileWriter writer = new FileWriter("overworld.json");
            List<GameObject> objsToSerialize = new ArrayList<>();
            for (GameObject obj : gameObjects) {
                if (obj.getDoSerialize()) {
                    objsToSerialize.add(obj);
                }
            }
            writer.write(gson.toJson(objsToSerialize));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentSerializer())
                .registerTypeAdapter(GameObject.class, new GameObjectSerializer())
                .create();
        String inFile = "";
        try {
            inFile = new String(Files.readAllBytes(Paths.get("overworld.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            int maxGObjID = -1;
            int maxCompID = -1;
            GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
            for (GameObject obj : objs) {
//                addGameObject(obj);

                for (Component c: obj.getAllComponents()) {
                    if (c.getUID() > maxCompID) {
                        maxCompID = c.getUID();
                    }
                }
                if (obj.getUID() > maxGObjID) {
                    maxGObjID = obj.getUID();
                }
            }

            maxGObjID++;
            maxCompID++;
            GameObject.init(maxGObjID);
            Component.init(maxCompID);
            worldLoaded = true;
        }
    }

    public abstract void generate();

}

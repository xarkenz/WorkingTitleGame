package scenes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import blocks.Block;
import components.Component;
import components.ComponentSerializer;
import components.Transform;
import core.Camera;
import core.GameObject;
import core.GameObjectSerializer;
import entities.Entity;
import org.joml.Vector2i;
import renderer.Renderer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class Scene {

    protected Renderer renderer = new Renderer();
    protected Camera camera;
    private boolean isRunning = false;
    protected List<GameObject> gameObjects = new ArrayList<>();
    protected List<Entity> entities = new ArrayList<>();
    protected HashMap<Vector2i, Block> blocks = new HashMap<>();
    protected boolean worldLoaded = false;

    public Scene() {

    }

    public void init() {

    }

    public void start() {
        for (GameObject go : gameObjects) {
            go.start();
            this.renderer.add(go);
        }
        for (Entity entity : entities) {
            entity.start();
//            this.renderer.add(entity);
        }
        for (Block block : blocks.values()) {
            block.start();
            this.renderer.add(block);
        }
        isRunning = true;
    }

    public GameObject createGameObject(String name) {
        GameObject go = new GameObject(name);
        go.addComponent(new Transform());
        go.transform = go.getComponent(Transform.class);
        return go;
    }

    public void addGameObject(GameObject go) {
        gameObjects.add(go);
        if (isRunning) {
            go.start();
            this.renderer.add(go);
        }
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        if (isRunning) {
            entity.start();
//            this.renderer.add(entity);
        }
    }

    public void addBlock(Block block) {
        blocks.put(block.getPosition(), block);
        if (isRunning) {
            block.start();
            this.renderer.add(block);
        }
    }

    public void removeBlock(Block block) {
        blocks.remove(block.getPosition());
        if (isRunning) {
            this.renderer.remove(block);
        }
    }

    public GameObject getGameObject(int uid) {
        Optional<GameObject> result = this.gameObjects.stream()
                .filter(gameObject -> gameObject.getUID() == uid)
                .findFirst();
        return result.orElse(null);
    }

    public Entity getEntity(int uid) {
        Optional<Entity> result = this.entities.stream()
                .filter(entity -> entity.getUID() == uid)
                .findFirst();
        return result.orElse(null);
    }

    public Block getBlock(Vector2i pos) {
        return this.blocks.get(pos);
    }

    public Block getBlock(int x, int y) {
        return this.getBlock(new Vector2i(x, y));
    }

    public abstract void update(float dt);
    public abstract void render();

    public Camera getCamera() {
        return this.camera;
    }

    public void imGui() {

    }

    public void saveExit() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Component.class, new ComponentSerializer())
                .registerTypeAdapter(GameObject.class, new GameObjectSerializer())
                .create();

        try {
            FileWriter writer = new FileWriter("world.json");
            List<GameObject> objsToSerialize = new ArrayList<>();
            for (GameObject obj : this.gameObjects) {
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
            inFile = new String(Files.readAllBytes(Paths.get("world.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            int maxGObjID = -1;
            int maxCompID = -1;
            GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
            for (GameObject obj : objs) {
                addGameObject(obj);

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
            this.worldLoaded = true;
        }
    }
}

package core;

import components.Component;

import java.util.ArrayList;
import java.util.List;

public class GameObject {

    private static int ID_COUNTER = 0;

    private int uid = -1;
    private String name;
    private List<Component> components;
    public Transform transform;
    private int zIndex;
    private boolean doSerialize = true;

    public GameObject(String name, Transform transform, int zIndex) {
        this.name = name;
        this.zIndex = zIndex;
        this.components = new ArrayList<>();
        this.transform = transform;

        this.uid = ID_COUNTER++;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "Error: Casting component";
                }
            }
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i=0; i < components.size(); i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component c) {
        c.generateID();
        this.components.add(c);
        c.gameObject = this;
    }

    public void update(float dt) {
        for (int i=0; i < components.size(); i++) {
            components.get(i).update(dt);
        }
    }

    public void start() {
        for (int i=0; i < components.size(); i++) {
            components.get(i).start();
        }
    }

    public void imGui() {
        for (Component c : components) {
            c.imGui();
        }
    }

    public int zIndex() {
        return this.zIndex;
    }

    public static void init(int maxID) {
        ID_COUNTER = maxID;
    }

    public int getUID() {
        return this.uid;
    }

    public List<Component> getAllComponents() {
        return this.components;
    }

    public void setDoSerialize(boolean serialize) {
        this.doSerialize = serialize;
    }

    public boolean getDoSerialize() {
        return this.doSerialize;
    }
}
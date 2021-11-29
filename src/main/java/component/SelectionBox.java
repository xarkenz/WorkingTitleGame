package component;

import editor.PropertiesPanel;
import core.GameObject;
import core.Prefabs;
import core.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class SelectionBox extends Component {
    private Vector4f xAxisColor = new Vector4f(1, 0, 0, 1);
    private Vector4f xAxisColorHover = new Vector4f();
    private Vector4f yAxisColor = new Vector4f(0, 1, 0, 1);
    private Vector4f yAxisColorHover = new Vector4f();

    private GameObject xAxisObject;
    private GameObject yAxisObject;
    private SpriteRenderer xAxisSprite;
    private SpriteRenderer yAxisSprite;
    private GameObject activeGameObject = null;

    private Vector2f xAxisOffset = new Vector2f(64, -5);
    private Vector2f yAxisOffset = new Vector2f(16, 61);

    private PropertiesPanel propertiesPanel;

    public SelectionBox(Sprite arrowSprite, PropertiesPanel propertiesPanel) {
        this.xAxisObject = Prefabs.generateSpriteObject(arrowSprite, 16, 48);
        this.yAxisObject = Prefabs.generateSpriteObject(arrowSprite, 16, 48);
        this.xAxisSprite = this.xAxisObject.getComponent(SpriteRenderer.class);
        this.yAxisSprite = this.yAxisObject.getComponent(SpriteRenderer.class);
        this.propertiesPanel = propertiesPanel;

//        Window.getWorld().addGameObject(this.xAxisObject);
//        Window.getWorld().addGameObject(this.yAxisObject);
    }

    @Override
    public void start() {
        this.xAxisObject.transform.rotation = 90;
        this.yAxisObject.transform.rotation = 180;
        this.xAxisObject.setDoSerialize(false);
        this.yAxisObject.setDoSerialize(false);
    }

    @Override
    public void update(float dt) {
        this.activeGameObject = this.propertiesPanel.getActiveGameObject();
        if (this.activeGameObject != null) {
            this.setActive();
        } else {
            this.setInactive();
        }
    }

    private void setActive() {
        this.xAxisObject.transform.position.set(this.activeGameObject.transform.position);
        this.yAxisObject.transform.position.set(this.activeGameObject.transform.position);
        this.xAxisObject.transform.position.add(this.xAxisOffset);
        this.yAxisObject.transform.position.add(this.yAxisOffset);
        this.xAxisSprite.setColor(xAxisColor);
        this.yAxisSprite.setColor(yAxisColor);
    }

    private void setInactive() {
        this.activeGameObject = null;
        this.xAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
        this.yAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
    }
}

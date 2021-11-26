package core;

import components.Sprite;
import components.SpriteRenderer;
import components.Transform;
import org.joml.Vector2f;

public class Prefabs {

    public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {
        GameObject obj = Window.getScene().createGameObject("Sprite_Object_Gen");
        obj.transform.scale.x = sizeX;
        obj.transform.scale.y = sizeY;
        SpriteRenderer renderer = new SpriteRenderer();
        renderer.setSprite(sprite);
        obj.addComponent(renderer);

        return obj;
    }
}

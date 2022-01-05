package core;

import component.Sprite;
import component.SpriteRenderer;

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

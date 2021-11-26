package entities;

import core.KeyListener;
import org.joml.Vector2d;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Entity {

    private float movementSpeed = 1000;
    private float jumpDelay = 0;

    public Player(Vector2d position, Vector2d velocity) {
        super("player", position, new Vector2d(28, 60), velocity, 100);
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt) {
        if (!KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
            this.jumpDelay = 0;
        }
        if (this.jumpDelay <= 0) {
            if (KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
                this.collisionBox.vy = 512;
                this.jumpDelay = 0.5f;
            }
        } else {
            this.jumpDelay -= dt;
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_A) && this.collisionBox.vx > -150) {
            this.collisionBox.vx -= this.movementSpeed * dt;
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_D) && this.collisionBox.vx < 150) {
            this.collisionBox.vx += this.movementSpeed * dt;
        }

        if (this.collisionBox.y < 0) {
            this.collisionBox.stop();
            this.collisionBox.setPosition(368, 480);
        }

        super.update(dt);
    }

}

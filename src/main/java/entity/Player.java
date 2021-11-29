package entity;

import core.KeyListener;
import org.joml.Vector2d;
import org.joml.Vector2i;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Entity {

    private float movementSpeed = 1000;
    private float jumpDelay = 0;
    private boolean crouching = false;
    private Vector2i spawnPoint;

    public Player(Vector2d position, Vector2d velocity) {
        super("player", position, new Vector2d(28, 60), velocity, 100);
        spawnPoint = null; // grab from file in the future
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt) {
        if (!KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
            jumpDelay = 0;
        }
        if (jumpDelay <= 0) {
            if (KeyListener.isKeyPressed(GLFW_KEY_SPACE) && isGrounded) {
                collisionBox.vy = 512;
                jumpDelay = 0.5f;
            }
        } else {
            jumpDelay -= dt;
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            crouching = true;
            collisionBox.h = 46;
        } else {
            crouching = false;
            collisionBox.h = 60;
        }

        double speedMultiplier = crouching ? 0.5 : 1;

        if (KeyListener.isKeyPressed(GLFW_KEY_A) && collisionBox.vx > -150 * speedMultiplier) {
            collisionBox.vx -= movementSpeed * dt;
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_D) && collisionBox.vx < 150 * speedMultiplier) {
            collisionBox.vx += movementSpeed * dt;
        }

        if (collisionBox.y < -128 * 32) {
            collisionBox.stop();
            respawn();
        }

        super.update(dt);
    }

    public void respawn() {
        if (spawnPoint != null) {
            collisionBox.setPosition(spawnPoint.x - collisionBox.w / 2, spawnPoint.y);
            if (!isColliding()) return;
        }

        collisionBox.setPosition(-collisionBox.w / 2, 0);
        while (isColliding()) {
            collisionBox.y++;
        }
    }

}

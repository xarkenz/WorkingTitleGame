package entity;

import core.KeyListener;
import core.MouseListener;
import org.joml.Vector4i;
import util.EntityAppearance;

import org.joml.Vector2d;
import org.joml.Vector2i;
import util.AssetPool;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Entity {

    private double movementSpeed = 500;
    private double jumpSpeed = 200;
    private float jumpDelay = 0;
    private boolean crouching = false;
    private Vector2i spawnPoint;

    public Player(Vector2d position, Vector2d velocity, double facing, Vector2i spawnPoint) {
        super("player", position, new Vector2d(12, 27), velocity, facing, 100);
        this.eyeOffset = 20;
        this.spawnPoint = spawnPoint;
        this.appearance = new EntityAppearance();
    }

    @Override
    public void start() {
        super.start();

        appearance.addImage(AssetPool.getEntityImage("player_test/player_test_idle"), new Vector4i(-6, 0, 24, 32));
        appearance.addImage(AssetPool.getEntityImage("player_test/player_test_run"), new Vector4i(-6, 0, 24, 32));
    }

    @Override
    public void update(float dt) {
        if (!KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
            jumpDelay = 0;
        }
        if (jumpDelay <= 0) {
            if (KeyListener.isKeyPressed(GLFW_KEY_SPACE) && isGrounded) {
                collisionBox.vy = jumpSpeed;
                jumpDelay = 0.5f;
            }
        } else {
            jumpDelay -= dt;
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_Z)) {
            crouching = true;
            collisionBox.h = 23;
        } else {
            crouching = false;
            collisionBox.h = 27;
        }

        double speedMultiplier = crouching && isGrounded ? 0.5 : 1;

        if (KeyListener.isKeyPressed(GLFW_KEY_A) && collisionBox.vx > -75 * speedMultiplier) {
            collisionBox.vx -= movementSpeed * dt;
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_D) && collisionBox.vx < 75 * speedMultiplier) {
            collisionBox.vx += movementSpeed * dt;
        }

        if (collisionBox.y < -128 * 16) {
            collisionBox.stop();
            respawn();
        }

        facing = Math.atan2(MouseListener.getWorldY() - (collisionBox.y + eyeOffset), MouseListener.getWorldX() - collisionBox.getCenter().x) / (2 * Math.PI);
        appearance.setFlipH(facing > 0.25 || facing < -0.25);

        super.update(dt);

        if (Math.abs(collisionBox.vx) >= 0.2f) {
            appearance.showImage(0, false);
            appearance.showImage(1, true);
        } else {
            appearance.showImage(0, true);
            appearance.showImage(1, false);
        }
    }

    public void respawn() {
        if (spawnPoint != null) {
            collisionBox.setPosition(spawnPoint.x - collisionBox.w / 2, spawnPoint.y);
            if (!isColliding()) return;
        }

        collisionBox.setPosition(-collisionBox.w / 2, 0);
        while (isColliding()) {
            collisionBox.y += 16;
        }
    }

    public void setSpawnPoint(Vector2i pos) {
        spawnPoint = pos;
    }

}

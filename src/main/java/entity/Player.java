package entity;

import core.KeyListener;
import core.MouseListener;
import org.joml.Vector2f;
import org.joml.Vector4i;
import renderer.EntityAppearance;

import org.joml.Vector2d;
import org.joml.Vector2i;
import util.AssetPool;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Entity {

    private float movementSpeed = 500;
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

        Vector4i texCoords = AssetPool.getEntityTexCoords("player_test/player_test_idle");
        Vector2f texSize = new Vector2f(AssetPool.getEntityTexture().getWidth(), AssetPool.getEntityTexture().getHeight());
        Vector2f[] vertexCoords = {
                new Vector2f((texCoords.x + texCoords.z) / texSize.x, (texCoords.y + texCoords.w) / texSize.y),
                new Vector2f((texCoords.x + texCoords.z) / texSize.x, texCoords.y / texSize.y),
                new Vector2f(texCoords.x / texSize.x, texCoords.y / texSize.y),
                new Vector2f(texCoords.x / texSize.x, (texCoords.y + texCoords.w) / texSize.y)
        };
        appearance.addTexElement(vertexCoords, new Vector4i(-6, 0, 24, 32));
    }

    @Override
    public void update(float dt) {
        if (!KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
            jumpDelay = 0;
        }
        if (jumpDelay <= 0) {
            if (KeyListener.isKeyPressed(GLFW_KEY_SPACE) && isGrounded) {
                collisionBox.vy = 256;
                jumpDelay = 0.5f;
            }
        } else {
            jumpDelay -= dt;
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
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

    public void setSpawnPoint(Vector2i pos) {
        spawnPoint = pos;
    }

}

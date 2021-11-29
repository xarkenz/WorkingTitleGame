package entity;

import block.BlockType;
import core.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.CollisionBox;

import java.util.LinkedList;

public abstract class Entity {

    private static int UID_COUNTER = 0;

    private final String name;
    private final int uid;
    private transient boolean isDirty = true;

    protected float health;
    protected boolean isGrounded = false;

    protected CollisionBox collisionBox;

    protected float accelGravity = 1024f;
    protected float accelResistance = 32f;
    protected float accelFriction = 512f;
    protected float terminalVelocity = 4096f;

    protected boolean usesGravity = true;
    protected boolean usesFriction = true;
    protected boolean usesCollision = true;

    public Entity(String name, Vector2d position, Vector2d size, Vector2d velocity, float health) {
        this.name = name;
        this.collisionBox = new CollisionBox(position.x, position.y, size.x, size.y, velocity.x, velocity.y);
        this.health = health;
        this.uid = UID_COUNTER++;
    }

    public void start() {

    }

    public void update(float dt) {

        if (usesGravity) {
            if (collisionBox.vy - accelGravity * dt < -terminalVelocity) {
                collisionBox.vy = -terminalVelocity;
            } else {
                collisionBox.vy -= accelGravity * dt;
            }
        }

        if (usesFriction) {
            if (collisionBox.vx != 0) {
                int xDirection = (int) (collisionBox.vx / Math.abs(collisionBox.vx));
                collisionBox.vx -= xDirection * accelFriction * dt;
                if (xDirection != (int) (collisionBox.vx / Math.abs(collisionBox.vx))) {
                    collisionBox.vx = 0;
                }
            }
        }

        if (usesCollision) {
            moveWithCollision(dt);
        } else {
            collisionBox.x += collisionBox.vx * dt;
            collisionBox.y += collisionBox.vy * dt;
        }

        DebugDraw.addRect(new Vector2f().set(collisionBox.getCenter()), new Vector2f().set(collisionBox.getSize()), new Vector3f(1, 0, 0), 0, 1);
//        DebugDraw.addRect(new Vector2f().set(collisionBox.getPosition()), new Vector2f(4, 4), new Vector3f(0, 0, 1), 45, 1);
    }

    public boolean isColliding() {
        for (int posY = (int) Math.floor(collisionBox.y / 32); posY <= (int) Math.floor((collisionBox.y + collisionBox.h) / 32); posY++) {
            for (int posX = (int) Math.floor(collisionBox.x / 32); posX <= (int) Math.floor((collisionBox.x + collisionBox.w) / 32); posX++) {
                BlockType block = Window.getWorld().getBlockType(posX, posY);
                if (block != null)
                    return true;
            }
        }
        return false;
    }

    public void moveWithCollision(float dt) {
        CollisionBox area = collisionBox.getBroadPhase(dt);
//        DebugDraw.addRect(new Vector2f().set(area.getCenter()), new Vector2f().set(area.getSize()), new Vector3f(0, 0.5f, 0), 0, 1);
        CollisionBox blockArea;
        LinkedList<CollisionBox> colliders = new LinkedList<>();

        for (int posY = (int) Math.floor(area.y / 32); posY <= (int) Math.floor((area.y + area.h) / 32); posY++) {
            for (int posX = (int) Math.floor(area.x / 32); posX <= (int) Math.floor((area.x + area.w) / 32); posX++) {
                BlockType block = Window.getWorld().getBlockType(posX, posY);
                if (block != null) {
                    blockArea = new CollisionBox(posX * 32, posY * 32, 32, 32);
                    colliders.add(blockArea);
//                    DebugDraw.addRect(new Vector2f().set(blockArea.getCenter()), new Vector2f().set(blockArea.getSize()), new Vector3f(0, 0.5f, 0), 0, 1);
                }
            }
        }

        if (colliders.isEmpty()) {
            collisionBox.x += collisionBox.vx * dt;
            collisionBox.y += collisionBox.vy * dt;
            isGrounded = false;
        } else {
            isGrounded = collisionBox.collide(colliders, dt);
        }
    }

    public String getName() {
        return name;
    }

    public Vector2d getPosition() {
        return collisionBox.getPosition();
    }

    public void setPosition(Vector2d position) {
        collisionBox.setPosition(position);
    }

    public void setPosition(double positionX, double positionY) {
        collisionBox.setPosition(positionX, positionY);
    }

    public Vector2d getVelocity() {
        return collisionBox.getVelocity();
    }

    public void setVelocity(Vector2d velocity) {
        collisionBox.setVelocity(velocity);
    }

    public void setVelocity(double velocityX, double velocityY) {
        collisionBox.setVelocity(velocityX, velocityY);
    }

    public boolean getDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public int getUID() {
        return uid;
    }

    public Vector2d getCenter() {
        return collisionBox.getCenter();
    }
}
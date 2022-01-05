package entity;

import block.BlockType;
import core.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.EntityAppearance;
import util.CollisionBox;
import util.Settings;

import java.util.LinkedList;

public abstract class Entity {

    private static int NEXT_UID = 1;

    protected String name;
    protected final int uid;
    protected boolean isDirty = true;

    protected float health;
    protected boolean isGrounded = false;
    protected double facing;

    protected CollisionBox collisionBox;
    protected double eyeOffset = 0;
    protected EntityAppearance appearance;
    protected int[] elementIndices;

    protected float accelGravity = 512;
    protected float accelResistance = 16;
    protected float accelFriction = 256;
    protected float terminalVelocity = 2048;

    protected boolean usesGravity = true;
    protected boolean usesFriction = true;
    protected boolean usesCollision = true;

    public Entity(String name, Vector2d position, Vector2d size, Vector2d velocity, double facing, float health) {
        this.name = name;
        this.collisionBox = new CollisionBox(position.x, position.y, size.x, size.y, velocity.x, velocity.y);
        this.facing = facing;
        this.health = health;
        this.uid = NEXT_UID++;
        this.appearance = null;
        this.elementIndices = null;
    }

    public void start() {

    }

    public void update(float dt) {
        if (usesGravity) {
            if (collisionBox.vy - accelGravity * dt < -terminalVelocity)
                collisionBox.vy = -terminalVelocity;
            else
                collisionBox.vy -= accelGravity * dt;
        }

        if (usesFriction) {
            if (collisionBox.vx != 0) {
                int xDirection = (int) Math.signum(collisionBox.vx);
                collisionBox.vx -= xDirection * accelFriction * dt;
                if (xDirection != (int) Math.signum(collisionBox.vx))
                    collisionBox.vx = 0;
            }
        }

        isDirty = collisionBox.vx != 0 || collisionBox.vy != 0;

        if (usesCollision) {
            moveWithCollision(dt);
        } else {
            collisionBox.x += collisionBox.vx * dt;
            collisionBox.y += collisionBox.vy * dt;
        }

        DebugDraw.addRect(new Vector2f().set(collisionBox.getCenter()), new Vector2f().set(collisionBox.getSize()), new Vector3f(0.909804f, 0.294118f, 0.200000f), 0, 1);
        Vector2f eyePos = new Vector2f().set(collisionBox.getCenter().x, getEyeLevel());
        DebugDraw.addLine(eyePos, new Vector2f(eyePos.x + (float) Math.cos(facing * 2 * Math.PI) * Settings.BLOCK_SIZE,
                eyePos.y + (float) Math.sin(facing * 2 * Math.PI) * Settings.BLOCK_SIZE), new Vector3f(0.223529f, 0.627451f, 0.980392f));
    }

    public boolean isColliding() {
        for (int posY = (int) Math.floor(collisionBox.y / Settings.BLOCK_SIZE); posY <= (int) Math.floor((collisionBox.y + collisionBox.h) / Settings.BLOCK_SIZE); posY++) {
            for (int posX = (int) Math.floor(collisionBox.x / Settings.BLOCK_SIZE); posX <= (int) Math.floor((collisionBox.x + collisionBox.w) / Settings.BLOCK_SIZE); posX++) {
                BlockType block = Window.getScene().getBlockType(posX, posY);
                if (block != null) return true;
            }
        }
        return false;
    }

    public void moveWithCollision(float dt) {
        CollisionBox area = collisionBox.getBroadPhase(dt);
//        DebugDraw.addRect(new Vector2f().set(area.getCenter()), new Vector2f().set(area.getSize()), new Vector3f(0, 0.5f, 0), 0, 1);
        LinkedList<CollisionBox> colliders = new LinkedList<>();

        for (int blockY = (int) Math.floor(area.y / Settings.BLOCK_SIZE); blockY <= (int) Math.floor((area.y + area.h) / Settings.BLOCK_SIZE) - ((area.y + area.h) % Settings.BLOCK_SIZE == 0 ? 1 : 0); blockY++) {
            for (int blockX = (int) Math.floor(area.x / Settings.BLOCK_SIZE); blockX <= (int) Math.floor((area.x + area.w) / Settings.BLOCK_SIZE) - ((area.x + area.w) % Settings.BLOCK_SIZE == 0 ? 1 : 0); blockX++) {
                BlockType block = Window.getScene().getBlockType(blockX, blockY);
                if (block != null) {
                    CollisionBox blockArea = new CollisionBox(blockX * Settings.BLOCK_SIZE, blockY * Settings.BLOCK_SIZE, Settings.BLOCK_SIZE, Settings.BLOCK_SIZE);
                    if (collisionBox.isTouching(blockArea)) continue;
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

    public boolean isDirty() {
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

    public double getEyeLevel() {
        return collisionBox.y + eyeOffset;
    }

    public EntityAppearance getAppearance() {
        return appearance;
    }

    public int[] getElementIndices() {
        return elementIndices;
    }

    public void setElementIndices(int[] indices) {
        elementIndices = indices;
    }
}

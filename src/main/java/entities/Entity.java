package entities;

import blocks.Block;
import core.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.CollisionBox;

import java.util.LinkedList;

public abstract class Entity {

    private static int UID_COUNTER = 0;

    private String name;
    private int uid;
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

        if (this.usesGravity) {
            if (this.collisionBox.vy - this.accelGravity * dt < -this.terminalVelocity) {
                this.collisionBox.vy = -this.terminalVelocity;
            } else {
                this.collisionBox.vy -= this.accelGravity * dt;
            }
        }

        if (this.usesFriction) {
            if (this.collisionBox.vx != 0) {
                int xDirection = (int) (this.collisionBox.vx / Math.abs(this.collisionBox.vx));
                this.collisionBox.vx -= xDirection * this.accelFriction * dt;
                if (xDirection != (int) (this.collisionBox.vx / Math.abs(this.collisionBox.vx))) {
                    this.collisionBox.vx = 0;
                }
            }
        }

        if (this.usesCollision) {
            this.checkCollision(dt);
        } else {
            this.collisionBox.x += this.collisionBox.vx * dt;
            this.collisionBox.y += this.collisionBox.vy * dt;
        }

        DebugDraw.addRect(new Vector2f().set(this.collisionBox.getCenter()), new Vector2f().set(this.collisionBox.getSize()), new Vector3f(1, 0, 0), 0, 1);
        DebugDraw.addRect(new Vector2f().set(this.collisionBox.getPosition()), new Vector2f(4, 4), new Vector3f(0, 0, 1), 45, 1);
    }

    public void checkCollision(float dt) {
        CollisionBox area = this.collisionBox.getBroadPhase(dt);
//        DebugDraw.addRect(new Vector2f().set(area.getCenter()), new Vector2f().set(area.getSize()), new Vector3f(0, 0.5f, 0), 0, 1);
        CollisionBox blockArea;
        LinkedList<CollisionBox> colliders = new LinkedList<>();

        for (int posY = (int) (area.y / 32); posY <= (int) ((area.y + area.h) / 32); posY++) {
            for (int posX = (int) (area.x / 32); posX <= (int) ((area.x + area.w) / 32); posX++) {
                Block block = Window.getScene().getBlock(posX, posY);
                if (block != null) {
                    blockArea = new CollisionBox(posX * 32, posY * 32, 32, 32);
                    colliders.add(blockArea);
//                    DebugDraw.addRect(new Vector2f().set(blockArea.getCenter()), new Vector2f().set(blockArea.getSize()), new Vector3f(0, 0.5f, 0), 0, 1);
                }
            }
        }

        if (colliders.isEmpty()) {
            this.collisionBox.x += this.collisionBox.vx * dt;
            this.collisionBox.y += this.collisionBox.vy * dt;
        } else
            this.collisionBox.collide(colliders, dt);
    }

    /*public void checkCollision() {
        // =======================================
        // Find all colliding blocks for each side
        // =======================================

        ArrayList<Block> topColliders = new ArrayList<>();
        for (int i = (int) (this.getLeft() / 32); i <= -(int) (-this.getRight() / 32); i++) {
            Vector2i pos = new Vector2i(i, -(int) (-this.getTop() / 32));
            Block block = Window.getScene().getBlock(pos);
            if (block != null) {
                DebugDraw.addLine(new Vector2f(pos).mul(32).add(4, 28), new Vector2f(pos).mul(32).add(28, 28), new Vector3f(1, 0, 0), 1);
                // TODO: give blocks collision boxes and test those
                topColliders.add(block);
            }
        }

        ArrayList<Block> bottomColliders = new ArrayList<>();
        for (int i = (int) (this.getLeft() / 32); i <= (int) (this.getRight() / 32); i++) {
            Vector2i pos = new Vector2i(i, (int) (this.getBottom() / 32));
            Block block = Window.getScene().getBlock(pos);
            if (block != null) {
                DebugDraw.addLine(new Vector2f(pos).mul(32).add(4, 4), new Vector2f(pos).mul(32).add(28, 4), new Vector3f(0, 0, 1), 1);
                // TODO: give blocks collision boxes and test those
                bottomColliders.add(block);
            }
        }

        ArrayList<Block> leftColliders = new ArrayList<>();
        for (int i = (int) (this.getBottom() / 32); i <= (int) (this.getTop() / 32); i++) {
            Vector2i pos = new Vector2i((int) (this.getLeft() / 32), i);
            Block block = Window.getScene().getBlock(pos);
            if (block != null) {
                DebugDraw.addLine(new Vector2f(pos).mul(32).add(4, 4), new Vector2f(pos).mul(32).add(4, 28), new Vector3f(1, 1, 0), 1);
                // TODO: give blocks collision boxes and test those
                leftColliders.add(block);
            }
        }

        ArrayList<Block> rightColliders = new ArrayList<>();
        for (int i = (int) (this.getBottom() / 32); i <= -(int) (-this.getTop() / 32); i++) {
            Vector2i pos = new Vector2i(-(int) (-this.getRight() / 32), i);
            Block block = Window.getScene().getBlock(pos);
            if (block != null) {
                DebugDraw.addLine(new Vector2f(pos).mul(32).add(28, 4), new Vector2f(pos).mul(32).add(28, 28), new Vector3f(0, 1, 0), 1);
                // TODO: give blocks collision boxes and test those
                rightColliders.add(block);
            }
        }

        this.isGrounded = bottomColliders.size() > 0;

        // =======================================
        // Find the largest collision on each side
        // =======================================

        Float topY = null;
        Optional<Block> lowest = null;
        if (topColliders.size() > 0) {
            Comparator<Block> lowestComparator = (o1, o2) -> (int) (o1.getBottom() - o2.getBottom());
            lowest = topColliders.stream().min(lowestComparator);
            // using lowest.isPresent() should be redundant but that's risky to assume
            if (lowest.isPresent()) topY = lowest.get().getBottom();
        }

        Float bottomY = null;
        Optional<Block> highest = null;
        if (bottomColliders.size() > 0) {
            Comparator<Block> highestComparator = (o1, o2) -> (int) (o1.getTop() - o2.getTop());
            highest = bottomColliders.stream().max(highestComparator);
            if (highest.isPresent()) bottomY = highest.get().getTop();
        }

        Float leftX = null;
        Optional<Block> rightmost = null;
        if (leftColliders.size() > 0) {
            Comparator<Block> rightmostComparator = (o1, o2) -> (int) (o1.getRight() - o2.getRight());
            rightmost = leftColliders.stream().max(rightmostComparator);
            if (rightmost.isPresent()) leftX = rightmost.get().getRight();
        }

        Float rightX = null;
        Optional<Block> leftmost = null;
        if (rightColliders.size() > 0) {
            Comparator<Block> leftmostComparator = (o1, o2) -> (int) (o1.getLeft() - o2.getLeft());
            leftmost = rightColliders.stream().min(leftmostComparator);
            if (leftmost.isPresent()) rightX = leftmost.get().getLeft();
        }

        if (lowest.isPresent()) {
            this.collisionBox.collideWith(new CollisionBox(lowest.get().getPosition().x, lowest.get().getPosition().y, 32, 32));
        }
        if (highest.isPresent()) {
            this.collisionBox.collideWith(new CollisionBox(highest.get().getPosition().x, highest.get().getPosition().y, 32, 32));
        }
        if (rightmost.isPresent()) {
            this.collisionBox.collideWith(new CollisionBox(rightmost.get().getPosition().x, rightmost.get().getPosition().y, 32, 32));
        }
        if (leftmost.isPresent()) {
            this.collisionBox.collideWith(new CollisionBox(leftmost.get().getPosition().x, leftmost.get().getPosition().y, 32, 32));
        }

        this.position.x = this.collisionBox.x;
        this.position.y = this.collisionBox.y;
        this.velocity.x = this.collisionBox.vx;
        this.velocity.y = this.collisionBox.vy;

        // ====================================
        // Resolve collisions based on velocity
        // ====================================

        boolean top = topY != null;
        boolean bottom = bottomY != null;
        boolean left = leftX != null;
        boolean right = rightX != null;

        if (left && right) {
            // If horizontally confined, put in exact middle
            this.setLeft((leftX + rightX) / 2 - (this.collisionBox.x / 2));
            this.velocity.x = 0;
        } else if (left) {
            this.setLeft(leftX);
            this.velocity.x = 0;
        } else if (right) {
            this.setRight(rightX);
            this.velocity.x = 0;
        }

        // Bottom takes priority over top so player is always standing on solid ground
        if (bottom) {
            if (Math.abs(bottomY - this.getBottom()) <= 16 && Window.getScene().getBlock(highest.get().getPosition().add(0, 1)) != null) {
                this.setBottom(bottomY);
                this.velocity.y = 0;
            }
        } else if (top) {
            this.setTop(topY);
            this.velocity.y = 0;
        }
    }*/

    public String getName() {
        return this.name;
    }

    public Vector2d getPosition() {
        return this.collisionBox.getPosition();
    }

    public void setPosition(Vector2d position) {
        this.collisionBox.setPosition(position);
    }

    public void setPosition(double positionX, double positionY) {
        this.collisionBox.setPosition(positionX, positionY);
    }

    public Vector2d getVelocity() {
        return this.collisionBox.getVelocity();
    }

    public void setVelocity(Vector2d velocity) {
        this.collisionBox.setVelocity(velocity);
    }

    public void setVelocity(double velocityX, double velocityY) {
        this.collisionBox.setVelocity(velocityX, velocityY);
    }

    public boolean getDirty() {
        return this.isDirty;
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    public int getUID() {
        return this.uid;
    }
}
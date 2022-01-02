package util;

import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;

public class CollisionBox {

    public double x, y, w, h;
    public double vx, vy;

    public CollisionBox(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.vx = 0;
        this.vy = 0;
    }

    public CollisionBox(double x, double y, double w, double h, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.vx = vx;
        this.vy = vy;
    }

    public Vector2d getPosition() {
        return new Vector2d(x, y);
    }

    public void setPosition(Vector2d position) {
        x = position.x;
        y = position.y;
    }

    public void setPosition(double positionX, double positionY) {
        x = positionX;
        y = positionY;
    }

    public Vector2d getSize() {
        return new Vector2d(w, h);
    }

    public void setSize(Vector2d size) {
        w = size.x;
        h = size.y;
    }

    public void setSize(double width, double height) {
        w = width;
        h = height;
    }

    public Vector2d getVelocity() {
        return new Vector2d(vx, vy);
    }

    public void setVelocity(Vector2d velocity) {
        vx = velocity.x;
        vy = velocity.y;
    }

    public void setVelocity(double velocityX, double velocityY) {
        vx = velocityX;
        vy = velocityY;
    }

    public Vector2d getCenter() {
        return new Vector2d(x + w / 2, y + h / 2);
    }

    public void stop() {
        setVelocity(0, 0);
    }

    public boolean isTouching(CollisionBox other) {
        return !(x + w <= other.x || x >= other.x + other.w || y + h <= other.y || y >= other.y + other.h);
    }

    public CollisionBox getBroadPhase(double dt) {
        // Enclosed area where movement will occur in the current frame
        return new CollisionBox(
                vx > 0 ? x : x + vx * dt,
                vy > 0 ? y : y + vy * dt,
                vx > 0 ? w + vx * dt : w - vx * dt,
                vy > 0 ? h + vy * dt : h - vy * dt
        );
    }

    public boolean passesBy(CollisionBox rect, double dt) {
        return !rect.isTouching(getBroadPhase(dt));
    }

    public boolean collide(List<CollisionBox> rects, double dt) {
        return collide(rects, dt, false);
    }

    public boolean collide(List<CollisionBox> rects, double dt, boolean collideOnce) {
        Vector3d firstHit = new Vector3d(0, 0, 1);

        for (CollisionBox rect : rects) {
            if (passesBy(rect, dt)) continue;

            Vector3d collision = getCollision(rect, dt);

            if (collision.z < 0 || collision.z > 1) continue;
            if (collision.z < firstHit.z) firstHit = collision;
        }

        if (firstHit.z >= 1) return false;

        x += vx * dt * firstHit.z;
        y += vy * dt * firstHit.z;

        if (firstHit.y == 0) vx = 0;
        else vy = 0;

        if (collideOnce) return firstHit.y == -1;

        Vector3d secondHit = new Vector3d(0, 0, 1);
        for (CollisionBox rect: rects) {
            if (passesBy(rect, dt)) continue;
            Vector3d collision = getCollision(rect, dt, firstHit.z);
            if (collision.z < secondHit.z && collision.z >= 0)
                secondHit = collision;
        }

        if (firstHit.z + secondHit.z >= 1) {
            x += vx * dt * (1 - firstHit.z);
            y += vy * dt * (1 - firstHit.z);
        } else {
            x += vx * dt * secondHit.z;
            y += vy * dt * secondHit.z;
            vx = 0;
            vy = 0;
        }

        return firstHit.y == -1 || secondHit.y == -1;
    }

    public Vector3d getCollision(CollisionBox rect, double dt) {
        return getCollision(rect, dt, 0);
    }

    public Vector3d getCollision(CollisionBox rect, double dt, double timeUsed) {
        // Find exit/entry positions
        double xEntryPos, xExitPos, yEntryPos, yExitPos;
        if (vx > 0) {
            xEntryPos = rect.x - (x + w);
            xExitPos = (rect.x + rect.w) - x;
        } else {
            xEntryPos = (rect.x + rect.w) - x;
            xExitPos = rect.x - (x + w);
        }
        if (vy > 0) {
            yEntryPos = rect.y - (y + h);
            yExitPos = (rect.y + rect.h) - y;
        } else {
            yEntryPos = (rect.y + rect.h) - y;
            yExitPos = rect.y - (y + h);
        }

        // Find time of exit/entry on a timescale from 0 to 1
        double xEntryTime, xExitTime, yEntryTime, yExitTime;
        if (vx == 0) {
            xEntryTime = Double.NEGATIVE_INFINITY;
            xExitTime = Double.POSITIVE_INFINITY;
        } else {
            xEntryTime = xEntryPos / (vx * dt) - timeUsed;
            xExitTime = xExitPos / (vx * dt) - timeUsed;
        }
        if (vy == 0) {
            yEntryTime = Double.NEGATIVE_INFINITY;
            yExitTime = Double.POSITIVE_INFINITY;
        } else {
            yEntryTime = yEntryPos / (vy * dt) - timeUsed;
            yExitTime = yExitPos / (vy * dt) - timeUsed;
        }

        // Find the earliest entry and latest exit
        double entryTime = Math.max(xEntryTime, yEntryTime);
        double exitTime = Math.min(xExitTime, yExitTime);

        // Determine collided surface normal
        double normalX, normalY;
        if (entryTime > exitTime || xEntryTime < 0 && yEntryTime < 0 || xEntryTime > 1 && yEntryTime > 1) {
            // Collides with an invalid rect or does not collide
            normalX = 0;
            normalY = 0;
            entryTime = 1 - timeUsed;
        } else if (xEntryTime > yEntryTime) {
            normalX = xEntryPos < 0 ? 1 : -1;
            normalY = 0;
        } else {
            normalY = yEntryPos < 0 ? 1 : -1;
            normalX = 0;
        }

        return new Vector3d(normalX, normalY, entryTime);
    }

    public String toString() {
        return String.format("CollisionBox(x=%f, y=%f, w=%f, h=%f, vx=%f, vy=%f)", x, y, w, h, vx, vy);
    }

}

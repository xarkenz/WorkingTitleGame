package util;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;

public class CollisionBox {

    public double x, y, w, h;
    public double vx, vy;

    private final Comparator<CollisionBox> COMPARE_COLLIDE_TIME = (b1, b2) -> (int) (getCollision(b1, 1).z - getCollision(b2, 1).z);

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

    /*public void collideSteps(List<CollisionBox> rects, double dt) {
        double startY = y;
        boolean collided = false;

        if (vy < 0) {
            for (; y > startY + vy; y--) {
                for (CollisionBox rect : rects) {
                    if (isTouching(rect)) {
                        collided = true;

                    }
                }

                if (collided)
                    break;
            }
        }
    }*/

    public CollisionBox getBroadPhase(double dt) {
        // Enclosed area where movement will occur in the current frame
        return new CollisionBox(
                vx > 0 ? x : x + vx * dt,
                vy > 0 ? y : y + vy * dt,
                vx > 0 ? w + vx * dt : w - vx * dt,
                vy > 0 ? h + vy * dt : h - vy * dt
        );
    }

    public boolean mightCollide(CollisionBox rect, double dt) {
        // Utility to speed up collision detection by checking intersection with broad phase rect
        return rect.isTouching(getBroadPhase(dt));
    }

    public void collide(List<CollisionBox> rects, double dt) {
        collide(rects, dt, false);
    }

    public void collide(List<CollisionBox> rects, double dt, boolean collideOnce) {
        double firstHit = 1;
        boolean hitsXFirst = false;

        for (CollisionBox rect : rects) {
            if (!mightCollide(rect, dt))
                continue;

            Vector3d collision = getCollision(rect, dt);

            if (collision.z < 0 || collision.z > 1)
                continue;

            /*// Advance to time of collision
            x += vx * collision.z * dt;
            y += vy * collision.z * dt;

            // Calculate new velocity after collision
            double dot = (vx * collision.y + vy * collision.x) * (1 - timeUsed - collision.z);
            setVelocity(dot * collision.y, dot * collision.x);

            // Update and check timeUsed
            timeUsed += collision.z;
            if (timeUsed >= 1) {
                break;
            }*/

            if (collision.z < firstHit) {
                firstHit = collision.z;
                hitsXFirst = collision.y == 0;
            }
        }

        if (firstHit >= 1)
            return;

        x += vx * dt * firstHit;
        y += vy * dt * firstHit;

        if (hitsXFirst)
            vx = 0;
        else
            vy = 0;

        if (collideOnce)
            return;

        double secondHit = 1;
        for (CollisionBox rect: rects) {
            if (!mightCollide(rect, dt))
                continue;
            Vector3d collision = getCollision(rect, dt, firstHit);
            if (collision.z < secondHit && collision.z >= 0)
                secondHit = collision.z;
        }

        if (secondHit >= 1) {
            x += vx * dt * (1 - firstHit);
            y += vy * dt * (1 - firstHit);
        } else {
            x += vx * dt * secondHit;
            y += vy * dt * secondHit;
            vx = 0;
            vy = 0;
        }
    }

    public void collide(CollisionBox rect, double dt) {
        collide(rect, dt, 0);
    }

    public void collide(CollisionBox rect, double dt, double timeUsed) {
        Vector3d collision = getCollision(rect, dt, timeUsed);

        // Advance to time of collision
        x += vx * collision.z * dt;
        y += vy * collision.z * dt;

        // Calculate new velocity after collision
        double dot = (vx * collision.y + vy * collision.x) * (1 - timeUsed - collision.z);
        setVelocity(dot * collision.y, dot * collision.x);

        // Advance with new velocity for remaining time
        x += vx * (1 - timeUsed - collision.z) * dt;
        y += vy * (1 - timeUsed - collision.z) * dt;
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
        } else {
            if (xEntryTime > yEntryTime) {
                if (xEntryPos < 0) {
                    normalX = 1;
                } else {
                    normalX = -1;
                }
                normalY = 0;
            } else {
                if (yEntryPos < 0) {
                    normalY = 1;
                } else {
                    normalY = -1;
                }
                normalX = 0;
            }
        }

        return new Vector3d(normalX, normalY, entryTime);
    }

    public String toString() {
        return String.format("CollisionBox(x=%f, y=%f, w=%f, h=%f, vx=%f, vy=%f)", x, y, w, h, vx, vy);
    }

}

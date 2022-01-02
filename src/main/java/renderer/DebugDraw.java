package renderer;

import core.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import util.AssetPool;
import util.MathUtil;
import util.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class DebugDraw {
    private static final int MAX_LINES = 5000;

    private static final List<Line> lines = new ArrayList<>();
    // 6 floats per vertex, 2 vertices per line
    private static final float[] vertexArray = new float[MAX_LINES * 6 * 2];
    private static final Shader shader = AssetPool.getShader("assets/shaders/debug.glsl");

    private static int vaoID;
    private static int vboID;

    private static boolean started = false;

    public static void start() {
        // Generate VAO
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Create VBO
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Vertex position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Vertex color
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glLineWidth(Settings.DEBUG_LINE_SIZE);
    }

    public static void beginFrame() {
        // Start renderer if not already started
        if (!started) {
            start();
            started = true;
        }

        // Remove expired lines
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).beginFrame() <= 0) lines.remove(i--);
        }
    }

    public static void draw(boolean isBackground) {
        if (lines.isEmpty()) return;

        // Clear vertex array (removes stray lines)
        Arrays.fill(vertexArray, 0);

        int index = 0;
        for (Line line: lines) {
            if (line.isBackground() == isBackground) {
                for (int i = 0; i < 2; i++) {
                    Vector2f position = i == 0 ? line.getFrom() : line.getTo();
                    Vector3f color = line.getColor();

                    // Load position
                    vertexArray[index] = position.x;
                    vertexArray[index + 1] = position.y;
                    vertexArray[index + 2] = -10;

                    // Load color
                    vertexArray[index + 3] = color.x;
                    vertexArray[index + 4] = color.y;
                    vertexArray[index + 5] = color.z;
                    index += 6;
                }
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);

        // Use shader and upload camera projection and view
        shader.use();
        shader.uploadMat4f("uProjection", Window.getWorld().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getWorld().getCamera().getViewMatrix());

        // Bind VAO
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Draw batch
        glDrawArrays(GL_LINES, 0, lines.size() * 6 * 2);

        // Disable location
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        // Detach shader
        shader.detach();
    }

    public static void addLine(Vector2f from, Vector2f to, Vector3f color, int lifetime, boolean isBackground) {
        if (lines.size() >= MAX_LINES) return;
        DebugDraw.lines.add(new Line(from, to, color, lifetime, isBackground));
    }

    public static void addLine(Vector2f from, Vector2f to, Vector3f color, int lifetime) {
        addLine(from, to, color, lifetime, false);
    }

    public static void addLine(Vector2f from, Vector2f to, Vector3f color) {
        addLine(from, to, color, 1, false);
    }

    public static void addLine(Vector2f from, Vector2f to) {
        addLine(from, to, new Vector3f(0, 0, 1), 1, false);
    }

    public static void addRect(Vector2f center, Vector2f dimensions, Vector3f color, float rotation, int lifetime) {
        Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).div(2));
        Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).div(2));

        Vector2f[] vertices = {
                new Vector2f(min.x, min.y), new Vector2f(min.x, max.y),
                new Vector2f(max.x, max.y), new Vector2f(max.x, min.y)
        };

        if (rotation != 0) {
            for (Vector2f vertex : vertices) {
                MathUtil.rotate(vertex, rotation, center);
            }
        }

        addLine(vertices[0], vertices[1], color, lifetime);
        addLine(vertices[1], vertices[2], color, lifetime);
        addLine(vertices[2], vertices[3], color, lifetime);
        addLine(vertices[3], vertices[0], color, lifetime);
    }

    public static void addRect(Vector2f center, Vector2f dimensions, Vector3f color, float rotation) {
        addRect(center, dimensions, color, rotation, 1);
    }

    public static void addRect(Vector2f center, Vector2f dimensions, Vector3f color) {
        addRect(center, dimensions, color, 0, 1);
    }

    public static void addRect(Vector2f center, Vector2f dimensions) {
        addRect(center, dimensions, new Vector3f(0, 0, 1), 0, 1);
    }

    public static void addCircle(Vector2f center, float radius, Vector3f color, int lifetime, int sides) {
        Vector2f[] points = new Vector2f[sides];
        float increment = 360f / points.length;
        float currentAngle = 0;

        for (int i=0; i < points.length; i++) {
            Vector2f tmp = new Vector2f(radius, 0);
            MathUtil.rotate(tmp, currentAngle, new Vector2f());
            points[i] = new Vector2f(tmp).add(center);

            if (i > 0) {
                addLine(points[i - 1], points[i], color, lifetime);
            }

            currentAngle += increment;
        }

        addLine(points[points.length - 1], points[0], color, lifetime);
    }

    public static void addCircle(Vector2f center, float radius, Vector3f color, int lifetime) {
        addCircle(center, radius, color, lifetime, 50);
    }

    public static void addCircle(Vector2f center, float radius, Vector3f color) {
        addCircle(center, radius, color, 1, 50);
    }

    public static void addCircle(Vector2f center, float radius) {
        addCircle(center, radius, new Vector3f(0, 0, 1), 1, 50);
    }
}

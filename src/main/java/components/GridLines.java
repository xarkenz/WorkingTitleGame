package components;

import core.Camera;
import core.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.Settings;

public class GridLines extends Component {

    @Override
    public void update(float dt) {
        Camera camera = Window.getScene().camera();
        Vector2f cameraPos = camera.position;
        Vector2f projectionSize = camera.getProjectionSize();

        if (camera.getZoom() <= 4) {
            int firstX = ((int) (cameraPos.x / Settings.GRID_WIDTH) - 1) * Settings.GRID_WIDTH;
            int firstY = ((int) (cameraPos.y / Settings.GRID_HEIGHT) - 1) * Settings.GRID_HEIGHT;

            int numXLines = (int) (projectionSize.x * camera.getZoom() / Settings.GRID_WIDTH) + 2;
            int numYLines = (int) (projectionSize.y * camera.getZoom() / Settings.GRID_HEIGHT) + 2;

            int width = (int) (projectionSize.x * camera.getZoom()) + Settings.GRID_WIDTH * 2;
            int height = (int) (projectionSize.y * camera.getZoom()) + Settings.GRID_HEIGHT * 2;

            int maxLines = Math.max(numXLines, numYLines);
            Vector3f color = new Vector3f(0.9f, 0.9f, 0.9f);
            Vector3f axisColor = new Vector3f(0.2f, 0.2f, 0.2f);
            for (int i = 0; i < maxLines; i++) {
                int x = firstX + (Settings.GRID_WIDTH * i);
                int y = firstY + (Settings.GRID_HEIGHT * i);

                if (i < numXLines) {
                    if (x == 0) {
                        DebugDraw.addLine(new Vector2f(x, firstY), new Vector2f(x, firstY + height), axisColor);
                    } else {
                        DebugDraw.addLine(new Vector2f(x, firstY), new Vector2f(x, firstY + height), color);
                    }
                }
                if (i < numYLines) {
                    if (y == 0) {
                        DebugDraw.addLine(new Vector2f(firstX, y), new Vector2f(firstX + width, y), axisColor);
                    } else {
                        DebugDraw.addLine(new Vector2f(firstX, y), new Vector2f(firstX + width, y), color);
                    }
                }
            }
        }
    }
}

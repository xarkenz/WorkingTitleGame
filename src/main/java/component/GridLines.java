package component;

import block.Chunk;
import core.Camera;
import core.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;
import util.Settings;

public class GridLines extends Component {

    @Override
    public void update(float dt) {
        Camera camera = Window.getWorld().getCamera();
        Vector2f cameraPos = camera.position;
        Vector2f projectionSize = camera.getProjectionSize();

        if (camera.getZoom() <= 4) {
            int firstX = ((int) ((cameraPos.x - projectionSize.x / 2) / Settings.GRID_SIZE) - 5) * Settings.GRID_SIZE;
            int firstY = ((int) ((cameraPos.y - projectionSize.y / 2) / Settings.GRID_SIZE) - 5) * Settings.GRID_SIZE;

            int numXLines = (int) (projectionSize.x * camera.getZoom() / Settings.GRID_SIZE) + 10;
            int numYLines = (int) (projectionSize.y * camera.getZoom() / Settings.GRID_SIZE) + 10;

            int width = (int) (projectionSize.x * camera.getZoom()) + Settings.GRID_SIZE * 10;
            int height = (int) (projectionSize.y * camera.getZoom()) + Settings.GRID_SIZE * 10;

            int maxLines = Math.max(numXLines, numYLines);
            Vector3f color = new Vector3f(0.9f, 0.9f, 0.9f);
            Vector3f chunkColor = new Vector3f(0.2f, 0.2f, 0.2f);

            for (int i = 0; i < maxLines; i++) {
                int x = firstX + (Settings.GRID_SIZE * i);
                int y = firstY + (Settings.GRID_SIZE * i);

                if (i < numXLines) {
                    if ((x / Settings.GRID_SIZE) % Chunk.SIZE == 0)
                        DebugDraw.addLine(new Vector2f(x, firstY), new Vector2f(x, firstY + height), chunkColor, 1, false);
                    else
                        DebugDraw.addLine(new Vector2f(x, firstY), new Vector2f(x, firstY + height), color, 1, true);
                }
                if (i < numYLines) {
                    if ((y / Settings.GRID_SIZE) % Chunk.SIZE == 0)
                        DebugDraw.addLine(new Vector2f(firstX, y), new Vector2f(firstX + width, y), chunkColor, 1, false);
                    else
                        DebugDraw.addLine(new Vector2f(firstX, y), new Vector2f(firstX + width, y), color, 1, true);
                }
            }
        }
    }
}

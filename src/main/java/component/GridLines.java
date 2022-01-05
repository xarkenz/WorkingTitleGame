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
        if (!Settings.ENABLE_BLOCK_GRID && !Settings.ENABLE_CHUNK_GRID) return;

        Camera camera = Window.getScene().getCamera();
        Vector2f cameraPos = camera.position;
        Vector2f projectionSize = camera.getProjectionSize();

        if (camera.getZoom() <= 4) {
            int firstX = ((int) ((cameraPos.x - projectionSize.x * camera.getZoom() / 2) / Settings.BLOCK_SIZE) - 5) * Settings.BLOCK_SIZE;
            int firstY = ((int) ((cameraPos.y - projectionSize.y * camera.getZoom() / 2) / Settings.BLOCK_SIZE) - 5) * Settings.BLOCK_SIZE;

            int numXLines = (int) (projectionSize.x * camera.getZoom() / Settings.BLOCK_SIZE) + 10;
            int numYLines = (int) (projectionSize.y * camera.getZoom() / Settings.BLOCK_SIZE) + 10;

            int width = (int) (projectionSize.x * camera.getZoom()) + Settings.BLOCK_SIZE * 10;
            int height = (int) (projectionSize.y * camera.getZoom()) + Settings.BLOCK_SIZE * 10;

            int maxLines = Math.max(numXLines, numYLines);
            Vector3f color = new Vector3f(1, 1, 1);
            Vector3f chunkColor = new Vector3f(1, 1, 0);

            for (int i = 0; i < maxLines; i++) {
                int x = firstX + (Settings.BLOCK_SIZE * i);
                int y = firstY + (Settings.BLOCK_SIZE * i);

                if (i < numXLines) {
                    if ((x / Settings.BLOCK_SIZE) % Chunk.SIZE == 0 && Settings.ENABLE_CHUNK_GRID)
                        DebugDraw.addLine(new Vector2f(x, firstY), new Vector2f(x, firstY + height), chunkColor, 1, false);
                    else if (Settings.ENABLE_BLOCK_GRID)
                        DebugDraw.addLine(new Vector2f(x, firstY), new Vector2f(x, firstY + height), color, 1, true);
                }
                if (i < numYLines) {
                    if ((y / Settings.BLOCK_SIZE) % Chunk.SIZE == 0 && Settings.ENABLE_CHUNK_GRID)
                        DebugDraw.addLine(new Vector2f(firstX, y), new Vector2f(firstX + width, y), chunkColor, 1, false);
                    else if (Settings.ENABLE_BLOCK_GRID)
                        DebugDraw.addLine(new Vector2f(firstX, y), new Vector2f(firstX + width, y), color, 1, true);
                }
            }
        }
    }
}

package block;

import org.joml.Vector2i;
import world.Scene;

import java.util.Arrays;

public class Chunk {

    public static final int SIZE = 16;

    private final Vector2i position;
    private final Scene world;
    private boolean isDirty;

    private final BlockType[][] blockTypes;
    private final BlockState[][] blockStates;
    private final BlockQuad[][] blockQuads;
    private final boolean[][] blocksDirty;
    private final int[][] blockLights;
    private final int[][] skyLights;

    public Chunk(Vector2i position, Scene world) {
        this.position = position;
        this.world = world;
        this.isDirty = true;

        blockTypes = new BlockType[SIZE][SIZE];
        blockStates = new BlockState[SIZE][SIZE];
        blockQuads = new BlockQuad[SIZE * 2][SIZE * 2];
        blocksDirty = new boolean[SIZE][SIZE];
        blockLights = new int[SIZE][SIZE];
        skyLights = new int[SIZE][SIZE];

        for (int[] inner : skyLights) Arrays.fill(inner, 15);
        for (boolean[] inner : blocksDirty) Arrays.fill(inner, true);
    }

    public void start() {
        update(0);
    }

    private void adjustBlockLight(int x, int y) {
        Vector2i[] d = {new Vector2i(0, 1), new Vector2i(1, 0), new Vector2i(0, -1), new Vector2i(-1, 0)};

        int[] lights = new int[4];
        for (int i = 0; i < 4; i++) {
            Vector2i chunkOffset = new Vector2i();
            if (x + d[i].x < 0) chunkOffset.x = -1;
            else if (x + d[i].x >= SIZE) chunkOffset.x = 1;
            if (y + d[i].y < 0) chunkOffset.y = -1;
            else if (y + d[i].y >= SIZE) chunkOffset.y = 1;

            if (chunkOffset.equals(0, 0)) {
                lights[i] = getBlockLight(x + d[i].x, y + d[i].y);
            } else {
                Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
                if (containingChunk == null) continue;
                lights[i] = containingChunk.getBlockLight(Math.floorMod(x + d[i].x, SIZE), Math.floorMod(y + d[i].y, SIZE));
            }
        }

        int light = Math.max(Arrays.stream(lights).max().getAsInt() - 1, getBlockType(x, y) == null ? 0 : getBlockType(x, y).light());

        if (getBlockLight(x, y) == light) return;
        blockLights[x][y] = light;

        for (int i = 0; i < 4; i++) {
            Vector2i chunkOffset = new Vector2i();
            if (x + d[i].x < 0) chunkOffset.x = -1;
            else if (x + d[i].x >= SIZE) chunkOffset.x = 1;
            if (y + d[i].y < 0) chunkOffset.y = -1;
            else if (y + d[i].y >= SIZE) chunkOffset.y = 1;

            if (chunkOffset.equals(0, 0)) {
                adjustBlockLight(x + d[i].x, y + d[i].y);
                isDirty = true;
            } else {
                Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
                if (containingChunk == null) continue;
                containingChunk.adjustBlockLight(Math.floorMod(x + d[i].x, SIZE), Math.floorMod(y + d[i].y, SIZE));
                containingChunk.setChunkDirty(true);
            }
        }
    }

    private void adjustSkyLight(int x, int y) {
        Vector2i[] d = {new Vector2i(0, 1), new Vector2i(1, 0), new Vector2i(0, -1), new Vector2i(-1, 0)};

        int[] lights = new int[4];
        for (int i = 0; i < 4; i++) {
            Vector2i chunkOffset = new Vector2i();
            if (x + d[i].x < 0) chunkOffset.x = -1;
            else if (x + d[i].x >= SIZE) chunkOffset.x = 1;
            if (y + d[i].y < 0) chunkOffset.y = -1;
            else if (y + d[i].y >= SIZE) chunkOffset.y = 1;

            if (chunkOffset.equals(0, 0)) {
                lights[i] = getSkyLight(x + d[i].x, y + d[i].y);
            } else {
                Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
                if (containingChunk == null) continue;
                lights[i] = containingChunk.getSkyLight(Math.floorMod(x + d[i].x, SIZE), Math.floorMod(y + d[i].y, SIZE));
            }
        }

        int light = Math.max(Arrays.stream(lights).max().getAsInt() - 1, getBlockType(x, y) == null && lights[0] == 15 ? 15 : 0);

        if (getSkyLight(x, y) == light) return;
        skyLights[x][y] = light;

        for (int i = 1; i < 4; i++) {
            Vector2i chunkOffset = new Vector2i();
            if (x + d[i].x < 0) chunkOffset.x = -1;
            else if (x + d[i].x >= SIZE) chunkOffset.x = 1;
            if (y + d[i].y < 0) chunkOffset.y = -1;
            else if (y + d[i].y >= SIZE) chunkOffset.y = 1;

            if (chunkOffset.equals(0, 0)) {
                adjustSkyLight(x + d[i].x, y + d[i].y);
                isDirty = true;
            } else {
                Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
                if (containingChunk == null) continue;
                containingChunk.adjustSkyLight(Math.floorMod(x + d[i].x, SIZE), Math.floorMod(y + d[i].y, SIZE));
                containingChunk.setChunkDirty(true);
            }
        }
    }

    private void spreadDirty(int x, int y) {
        int[] xOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] yOffsets = {1, 1, 1, 0, 0, -1, -1, -1};

        for (int i = 0; i < 8; i++) {
            Vector2i chunkOffset = new Vector2i();
            if (x + xOffsets[i] < 0) chunkOffset.x = -1;
            else if (x + xOffsets[i] >= SIZE) chunkOffset.x = 1;
            if (y + yOffsets[i] < 0) chunkOffset.y = -1;
            else if (y + yOffsets[i] >= SIZE) chunkOffset.y = 1;

            if (chunkOffset.equals(0, 0)) {
                setBlockDirty(x + xOffsets[i], y + yOffsets[i], true);
            } else {
                Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
                if (containingChunk == null) continue;
                containingChunk.setBlockDirty(Math.floorMod(x + xOffsets[i], SIZE), Math.floorMod(y + yOffsets[i], SIZE), true);
            }
        }
    }

    public void update(float dt) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (blocksDirty[x][y]) {
                    BlockType block = getBlockType(x, y);

                    if (block == null) {
                        setBlockQuad(x, y, 0, null);
                        setBlockQuad(x, y, 1, null);
                        setBlockQuad(x, y, 2, null);
                        setBlockQuad(x, y, 3, null);
                    } else {
                        BlockType upLeft = getBlockType(x - 1, y + 1);
                        BlockType up = getBlockType(x, y + 1);
                        BlockType upRight = getBlockType(x + 1, y + 1);
                        BlockType left = getBlockType(x - 1, y);
                        BlockType right = getBlockType(x + 1, y);
                        BlockType downLeft = getBlockType(x - 1, y - 1);
                        BlockType down = getBlockType(x, y - 1);
                        BlockType downRight = getBlockType(x + 1, y - 1);

                        int topLeftShape = !block.connectsTo(up) ? (!block.connectsTo(left) ? 0 : 1) : (!block.connectsTo(left) ? 2 : (!block.connectsTo(upLeft) ? 3 : 4));
                        int topRightShape = !block.connectsTo(up) ? (!block.connectsTo(right) ? 0 : 1) : (!block.connectsTo(right) ? 2 : (!block.connectsTo(upRight) ? 3 : 4));
                        int bottomLeftShape = !block.connectsTo(down) ? (!block.connectsTo(left) ? 0 : 1) : (!block.connectsTo(left) ? 2 : (!block.connectsTo(downLeft) ? 3 : 4));
                        int bottomRightShape = !block.connectsTo(down) ? (!block.connectsTo(right) ? 0 : 1) : (!block.connectsTo(right) ? 2 : (!block.connectsTo(downRight) ? 3 : 4));

                        setBlockQuad(x, y, 0, BlockQuad.get(getBlockType(x, y), 0, topLeftShape));
                        setBlockQuad(x, y, 1, BlockQuad.get(getBlockType(x, y), 1, topRightShape));
                        setBlockQuad(x, y, 2, BlockQuad.get(getBlockType(x, y), 2, bottomLeftShape));
                        setBlockQuad(x, y, 3, BlockQuad.get(getBlockType(x, y), 3, bottomRightShape));
                    }

                    blocksDirty[x][y] = false;
                    isDirty = true;
                }
            }
        }
    }

    public Vector2i getPosition() {
        return position;
    }

    public Scene getWorld() {
        return world;
    }

    public boolean isChunkDirty() {
        return isDirty;
    }

    public BlockType getBlockType(int x, int y) {
        Vector2i chunkOffset = new Vector2i();
        if (x < 0) chunkOffset.x = -1;
        else if (x >= SIZE) chunkOffset.x = 1;
        if (y < 0) chunkOffset.y = -1;
        else if (y >= SIZE) chunkOffset.y = 1;

        if (chunkOffset.equals(0, 0)) {
            return blockTypes[x][y];
        } else {
            Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
            if (containingChunk == null) return null;
            return containingChunk.getBlockType(Math.floorMod(x, SIZE), Math.floorMod(y, SIZE));
        }
    }

    public BlockState getBlockState(int x, int y) {
        BlockState state = blockStates[x][y];
        if (state == null) return new BlockState();
        return state;
    }

    public BlockQuad getBlockQuad(int x, int y, int pos) {
        return switch (pos) {
            case 0 -> blockQuads[x * 2][y * 2 + 1];
            case 1 -> blockQuads[x * 2 + 1][y * 2 + 1];
            case 2 -> blockQuads[x * 2][y * 2];
            case 3 -> blockQuads[x * 2 + 1][y * 2];
            default -> null;
        };
    }

    public boolean isBlockDirty(int x, int y) {
        return blocksDirty[x][y];
    }

    public int getBlockLight(int x, int y) {
        Vector2i chunkOffset = new Vector2i();
        if (x < 0) chunkOffset.x = -1;
        else if (x >= SIZE) chunkOffset.x = 1;
        if (y < 0) chunkOffset.y = -1;
        else if (y >= SIZE) chunkOffset.y = 1;

        if (chunkOffset.equals(0, 0)) {
            return blockLights[x][y];
        } else {
            Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
            if (containingChunk == null) return 0;
            return containingChunk.getBlockLight(Math.floorMod(x, SIZE), Math.floorMod(y, SIZE));
        }
    }

    public int getSkyLight(int x, int y) {
        Vector2i chunkOffset = new Vector2i();
        if (x < 0) chunkOffset.x = -1;
        else if (x >= SIZE) chunkOffset.x = 1;
        if (y < 0) chunkOffset.y = -1;
        else if (y >= SIZE) chunkOffset.y = 1;

        if (chunkOffset.equals(0, 0)) {
            return skyLights[x][y];
        } else {
            Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
            if (containingChunk == null) return 15;
            return containingChunk.getSkyLight(Math.floorMod(x, SIZE), Math.floorMod(y, SIZE));
        }
    }

    public int getVisualLight(int x, int y) {
        return Math.max(getBlockLight(x, y), getSkyLight(x, y));
    }

    public void setChunkDirty(boolean dirty) {
        isDirty = dirty;
    }

    public void setBlock(int x, int y, BlockType type, BlockState state) {
        blockTypes[x][y] = type;
        blockStates[x][y] = state;
        blocksDirty[x][y] = true;
        spreadDirty(x, y);
        adjustBlockLight(x, y);
        adjustSkyLight(x, y);
    }

    public void setBlock(int x, int y, BlockType type) {
        setBlock(x, y, type, type.defaultState());
    }

    public void setBlockQuad(int x, int y, int pos, BlockQuad quad) {
        switch (pos) {
            case 0 -> blockQuads[x * 2][y * 2 + 1] = quad;
            case 1 -> blockQuads[x * 2 + 1][y * 2 + 1] = quad;
            case 2 -> blockQuads[x * 2][y * 2] = quad;
            case 3 -> blockQuads[x * 2 + 1][y * 2] = quad;
        }
    }

    public void setBlockDirty(int x, int y, boolean dirty) {
        blocksDirty[x][y] = dirty;
    }

}

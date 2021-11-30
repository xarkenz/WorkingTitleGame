package block;

import org.joml.Vector2i;
import world.World;

import java.util.Arrays;

public class Chunk {

    public static final int SIZE = 16;

    private final Vector2i position;
    private final World world;
    private boolean isDirty;
    private final BlockType[][] blockTypes;
    private final BlockState[][] blockStates;
    private final BlockQuad[][] blockQuads;
    private final boolean[][] blocksDirty;

    public Chunk(Vector2i position, World world) {
        this.position = position;
        this.world = world;
        this.isDirty = true;
        this.blockTypes = new BlockType[SIZE][SIZE];
        this.blockStates = new BlockState[SIZE][SIZE];
        this.blockQuads = new BlockQuad[SIZE * 2][SIZE * 2];
        this.blocksDirty = new boolean[SIZE][SIZE];
    }

    public void start() {
        for (boolean[] inner : blocksDirty) {
            Arrays.fill(inner, true);
        }
        update(0);
    }

    private BlockType getAdjacentBlockType(int x, int y, int dx, int dy) {
        Vector2i chunkOffset = new Vector2i();
        if (x + dx < 0) chunkOffset.x = -1;
        else if (x + dx >= SIZE) chunkOffset.x = 1;
        if (y + dy < 0) chunkOffset.y = -1;
        else if (y + dy >= SIZE) chunkOffset.y = 1;

        if (chunkOffset.equals(0, 0)) {
            return getBlockType(x + dx, y + dy);
        } else {
            Chunk containingChunk = world.getLoadedChunk(chunkOffset.add(position));
            if (containingChunk == null) return null;
            return containingChunk.getBlockType(Math.floorMod(x + dx, SIZE), Math.floorMod(y + dy, SIZE));
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
                        BlockType upLeft = getAdjacentBlockType(x, y, -1, 1);
                        BlockType up = getAdjacentBlockType(x, y, 0, 1);
                        BlockType upRight = getAdjacentBlockType(x, y, 1, 1);
                        BlockType left = getAdjacentBlockType(x, y, -1, 0);
                        BlockType right = getAdjacentBlockType(x, y, 1, 0);
                        BlockType downLeft = getAdjacentBlockType(x, y, -1, -1);
                        BlockType down = getAdjacentBlockType(x, y, 0, -1);
                        BlockType downRight = getAdjacentBlockType(x, y, 1, -1);

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

    public World getWorld() {
        return world;
    }

    public boolean isChunkDirty() {
        return isDirty;
    }

    public BlockType getBlockType(int x, int y) {
        return blockTypes[x][y];
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

    public void setChunkDirty(boolean dirty) {
        isDirty = dirty;
    }

    public void setBlock(int x, int y, BlockType type, BlockState state) {
        blockTypes[x][y] = type;
        blockStates[x][y] = state;
        blocksDirty[x][y] = true;
        spreadDirty(x, y);
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

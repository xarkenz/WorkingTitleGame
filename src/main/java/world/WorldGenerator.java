package world;

import block.BlockType;

import java.util.function.Predicate;
import java.util.Random;

public class WorldGenerator {

    private final World world;
    private final Random random;

    private double interpolate(double a, double b, double t) {
        if (t < 0)
            return a;
        if (t > 1)
            return b;

        //return (b - a) * t + a; // linear
        return (b - a) * (3 - t * 2) * t * t + a; // cubic
        //return (b - a) * (t * (t * 6 - 15) + 10) * t * t * t + a; // whatever the hell this is
    }

    public WorldGenerator(World world) {
        this.random = new Random();
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void setRect(int x, int y, int w, int h, BlockType block) {
        for (int yPos = y; yPos < y + h; yPos++) {
            for (int xPos = x; xPos < x + w; xPos++) {
                world.setBlock(xPos, yPos, block);
            }
        }
    }

    public void generateBaseTerrain(int low, int high, int space, BlockType block) {
        int[] heights = new int[100 / space + 2];
        for (int i = 0; i < heights.length; i++) {
            heights[i] = (int) (random.nextDouble() * (high - low + 1)) + low;
        }

        setRect(-50, -50, 100, low, block);

        for (int x = 0; x < 100; x++) {
            int i1 = Math.floorDiv(x, space);
            int i2 = Math.floorDiv(x, space * 4);
            double t1 = (double) Math.floorMod(x, space) / space;
            double t2 = (double) Math.floorMod(x, space * 4) / (space * 4);

            double max = 0.25 * interpolate(heights[i1], heights[i1 + 1], t1) + 0.75 * interpolate(heights[i2], heights[i2 + 1], t2);

            for (int y = low; y <= max; y++) {
                world.setBlock(x - 50, y - 50, block);
            }
        }
    }

    public void applyTopLayer(int low, int depth, BlockType block, Predicate<BlockType> replace) {
        for (int x = 0; x < 100; x++) {
            int stamina = depth;
            for (int y = 100 - 1; y >= 0; y--) {
                if (world.getBlockType(x - 50, y - 50) == null) {
                    if (y >= low) stamina = depth;
                    else break;
                } else if (stamina >= 0 && replace.test(world.getBlockType(x - 50, y - 50)))
                    world.setBlock(x - 50, y - 50, block);
                stamina -= y < low ? 3 : 1;
            }
        }
    }

    public void applyTopLayer(int low, int minDepth, int maxDepth, int space, BlockType block, Predicate<BlockType> replace) {
        int[] depths = new int[100 / space + 2];
        for (int i = 0; i < depths.length; i++) {
            depths[i] = (int) (random.nextDouble() * (maxDepth - minDepth + 1)) + minDepth;
        }

        for (int x = 0; x < 100; x++) {
            int i = Math.floorDiv(x, space);
            double t = (double) Math.floorMod(x, space) / space;

            int depth = (int) interpolate(depths[i], depths[i + 1], t);

            int stamina = depth;
            boolean hit = false;
            for (int y = 100 - 1; y >= 0; y--) {
                if (world.getBlockType(x - 50, y - 50) == null) {
                    if (y >= low) stamina = hit ? depth / 2 : depth;
                    else break;
                } else if (stamina >= 0 && replace.test(world.getBlockType(x - 50, y - 50))) {
                    world.setBlock(x - 50, y - 50, block);
                    hit = true;
                }
                stamina -= y < low ? 2 : 1;
            }
        }
    }

    public void worms(int minY, int maxY, int maxPerChunk, int minW, int maxW, int space, int minL, int maxL, int maxDirShift, BlockType block, Predicate<BlockType> replace) {
        int lastChunk = Math.floorDiv(100, 16);
        for (int chunk = 0; chunk <= lastChunk; chunk++) {
            int numWorms = (int) (random.nextDouble() * (maxPerChunk + 1));
            for (int worm = 0; worm < numWorms; worm++) {
                int[] nodeR = new int[(int) (random.nextDouble() * (maxL - minL + 1)) + minL];
                nodeR[0] = (int) (random.nextDouble() * 360);
                for (int i = 1; i < nodeR.length; i++) {
                    int r = nodeR[i - 1];
                    int lowR = Math.floorMod(r - maxDirShift, 360);
                    int highR = Math.floorMod(r + maxDirShift, 360);
                    if (r > 85 && lowR < 95 || r > 265 && lowR < 275) lowR = r;
                    if (r < 95 && highR > 85 || r < 275 && highR > 265) highR = r;

                    nodeR[i] = Math.floorMod((int) (random.nextDouble() * (highR - lowR + 1)) + lowR, 360);
                }

                float[] nodeX = new float[nodeR.length + 1];
                float[] nodeY = new float[nodeR.length + 1];
                nodeX[0] = (int) (random.nextDouble() * 16) + chunk * 16;
                nodeY[0] = (int) (random.nextDouble() * (maxY - minY + 1) / 2d) + (int) (random.nextDouble() * (maxY - minY + 1) / 2d) + minY;
                for (int i = 0; i < nodeR.length; i++) {
                    nodeX[i + 1] = nodeX[i] + (float) (Math.cos(Math.toRadians(nodeR[i])) * space);
                    nodeY[i + 1] = nodeY[i] + (float) (Math.sin(Math.toRadians(nodeR[i])) * space);
                }

                for (int i = 0; i < nodeX.length; i++) {
                    float nx = nodeX[i];
                    float ny = nodeY[i];
                    float r = (float) (random.nextDouble() * (maxW - minW) + minW) / 2;
                    for (float x = -r + 0.5f; x <= r + 0.5f; x++) {
                        for (float y = -r + 0.5f; y <= r + 0.5f; y++) {
                            if (Math.sqrt(x * x + y * y) <= r && replace.test(world.getBlockType(Math.round(nx + x - 50), Math.round(ny + y - 50)))) {
                                world.setBlock(Math.round(nx + x - 50), Math.round(ny + y - 50), block);
                            }
                        }
                    }
                }
            }
        }
    }

}

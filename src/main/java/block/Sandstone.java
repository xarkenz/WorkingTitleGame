package block;

import org.joml.Vector2i;

import java.util.HashMap;

public class Sandstone extends Block {

    private static final String NAME = "sandstone";
    private static final String[] NO_CONNECT = {};

    public Sandstone(int x, int y) {
        super(NAME, NO_CONNECT, x, y);
    }

}

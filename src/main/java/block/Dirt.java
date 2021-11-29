package block;

import org.joml.Vector2i;

import java.util.HashMap;

public class Dirt extends Block {

    private static final String NAME = "dirt";
    private static final String[] NO_CONNECT = {};

    public Dirt(int x, int y) {
        super(NAME, NO_CONNECT, x, y);
    }

}

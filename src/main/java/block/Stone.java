package block;

import org.joml.Vector2i;

import java.util.HashMap;

public class Stone extends Block {

    private static final String NAME = "stone";
    private static final String[] NO_CONNECT = {};

    public Stone(int x, int y) {
        super(NAME, NO_CONNECT, x, y);
    }

}

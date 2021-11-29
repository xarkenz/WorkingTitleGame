package block;

import org.joml.Vector2i;

import java.util.HashMap;

public class Sand extends Block {

    private static final String NAME = "sand";
    private static final String[] NO_CONNECT = {};

    public Sand(int x, int y) {
        super(NAME, NO_CONNECT, x, y);
    }

}

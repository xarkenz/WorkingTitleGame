package block;

import org.joml.Vector2i;
import org.joml.Vector4d;

import java.util.HashMap;

public class AluminumBlock extends Block {

    private static final String NAME = "aluminum_block";
    private static final String[] NO_CONNECT = {"dirt", "sand", "sandstone", "stone"};

    public AluminumBlock(int x, int y) {
        super(NAME, NO_CONNECT, x, y);
    }

}
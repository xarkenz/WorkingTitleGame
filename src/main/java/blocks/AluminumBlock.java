package blocks;

import org.joml.Vector2i;

import java.util.HashMap;

public class AluminumBlock extends Block {

    public AluminumBlock(Vector2i position, HashMap<String, String> attributes) {
        super("aluminum_block", position, attributes);
    }

}
package block;

import java.util.function.Predicate;

public enum BlockType {

//    air            (x -> true),
    aluminum_block (x -> x.name().equals("aluminum_block")),
    aluminum_ore   (x -> true),
    amethyst_block (x -> x.name().equals("amethyst_block")),
    amethyst_ore   (x -> true),
    coal_block     (x -> x.name().equals("coal_block")),
    coal_ore       (x -> true),
    cobalt_block   (x -> x.name().equals("cobalt_block")),
    cobalt_ore     (x -> true),
    cobbles        (x -> true),
    copper_block   (x -> x.name().equals("copper_block")),
    copper_ore     (x -> true),
    copper_wire    (x -> x.name().equals("copper_wire") || x.name().equals("copper_block")),
    dirt           (x -> true),
    glass          (x -> x.name().equals("glass")),
    red_sand       (x -> true),
    red_sandstone  (x -> true),
    sand           (x -> true),
    sandstone      (x -> true),
    stone          (x -> true);

    private final Predicate<BlockType> connector;
    private final BlockState defaultState;

    BlockType(Predicate<BlockType> connector, BlockState defaultState) {
        this.connector = connector;
        this.defaultState = defaultState;
    }

    BlockType(Predicate<BlockType> connector) {
        this.connector = connector;
        this.defaultState = new BlockState();
    }

    public boolean connectsTo(BlockType other) {
        return other != null && connector.test(other);
    }

    public BlockState defaultState() {
        return defaultState;
    }

}

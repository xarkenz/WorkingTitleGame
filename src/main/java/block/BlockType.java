package block;

import java.util.function.Predicate;

public enum BlockType {

    aluminum_block      (x -> x.name().equals("aluminum_block")),
    aluminum_ore        (),
    amethyst_block      (x -> x.name().equals("amethyst_block")),
    amethyst_ore        (),
    amplifite_block     (x -> x.name().equals("amplifite_block")),
    coal_block          (x -> x.name().equals("coal_block")),
    coal_ore            (),
    cobalt_block        (x -> x.name().equals("cobalt_block")),
    cobalt_ore          (),
    cobbles             (),
    copper_block        (x -> x.name().equals("copper_block")),
    copper_ore          (),
    copper_wire         (x -> x.name().equals("copper_wire") || x.name().equals("copper_block") || x.name().equals("gold_wire") || x.name().equals("gold_block")),
    corruptite_block    (x -> x.name().equals("corruptite_block")),
    diamond_block       (x -> x.name().equals("diamond_block")),
    diamond_ore         (),
    dirt                (),
    emerald_block       (x -> x.name().equals("emerald_block")),
    emerald_ore         (),
    flamarite_block     (x -> x.name().equals("flamarite_block"), new BlockState(), 7),
    frigidite_block     (x -> x.name().equals("frigidite_block")),
    glass               (x -> x.name().equals("glass")),
    gold_block          (x -> x.name().equals("gold_block")),
    gold_ore            (),
    gold_wire           (x -> x.name().equals("gold_wire") || x.name().equals("gold_block") || x.name().equals("copper_wire") || x.name().equals("copper_block")),
    grassy_dirt         (),
    honey_crystal_block (x -> x.name().equals("honey_crystal_block")),
    iron_block          (x -> x.name().equals("iron_block")),
    iron_ore            (),
    luminite_block      (x -> x.name().equals("luminite_block")),
    magmium_block       (x -> x.name().equals("magmium_block")),
    obsidian_block      (x -> x.name().equals("obsidian_block")),
    packed_dirt         (),
    phylumus_block      (x -> x.name().equals("phylumus_block"), new BlockState(), 15),
    pipe                (x -> x.name().equals("pipe")),
    platinum_block      (x -> x.name().equals("platinum_block")),
    platinum_ore        (),
    quartz_block        (x -> x.name().equals("quartz_block")),
    quartz_ore          (),
    red_sand            (),
    red_sandstone       (),
    sand                (),
    sandstone           (),
    slate               (),
    steel_block         (x -> x.name().equals("steel_block")),
    stone               (),
    titanium_block      (x -> x.name().equals("titanium_block")),
    titanium_ore        (),
    turquoise_block     (x -> x.name().equals("turquoise_block")),
    turquoise_ore       (),
    versatilium_block   (x -> x.name().equals("versatilium_block")),
    voltagite_block     (x -> x.name().equals("voltagite_block"), new BlockState(), 4);

    private final Predicate<BlockType> connector;
    private final BlockState defaultState;
    private final int light;

    BlockType(Predicate<BlockType> connector, BlockState defaultState, int light) {
        this.connector = connector;
        this.defaultState = defaultState;
        this.light = light;
    }

    BlockType(Predicate<BlockType> connector, BlockState defaultState) {
        this.connector = connector;
        this.defaultState = defaultState;
        this.light = 0;
    }

    BlockType(Predicate<BlockType> connector) {
        this.connector = connector;
        this.defaultState = new BlockState();
        this.light = 0;
    }

    BlockType() {
        this.connector = x -> true;
        this.defaultState = new BlockState();
        this.light = 0;
    }

    public boolean connectsTo(BlockType other) {
        return other != null && connector.test(other);
    }

    public BlockState defaultState() {
        return defaultState;
    }

    public int light() {
        return light;
    }

}

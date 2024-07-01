package net.fxnt.fxntstorage.registry;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.fxnt.fxntstorage.FXNTStorage;

import static com.simibubi.create.foundation.block.connected.AllCTTypes.OMNIDIRECTIONAL;

public class SpriteShifts {
    public static final CTSpriteShiftEntry
            OAK_CASING = ct(OMNIDIRECTIONAL, "oak_casing"),
            SPRUCE_CASING = ct(OMNIDIRECTIONAL, "spruce_casing"),
            BIRCH_CASING = ct(OMNIDIRECTIONAL, "birch_casing"),
            JUNGLE_CASING = ct(OMNIDIRECTIONAL, "jungle_casing"),
            ACACIA_CASING = ct(OMNIDIRECTIONAL, "acacia_casing"),
            DARK_OAK_CASING = ct(OMNIDIRECTIONAL, "dark_oak_casing"),
            MANGROVE_CASING = ct(OMNIDIRECTIONAL, "mangrove_casing"),
            CHERRY_CASING = ct(OMNIDIRECTIONAL, "cherry_casing"),
            BAMBOO_CASING = ct(OMNIDIRECTIONAL, "bamboo_casing"),
            CRIMSON_CASING = ct(OMNIDIRECTIONAL, "crimson_casing"),
            WARPED_CASING = ct(OMNIDIRECTIONAL, "warped_casing")
    ;

    private static CTSpriteShiftEntry ct(AllCTTypes type, String name) {
        return CTSpriteShifter.getCT(type,
                FXNTStorage.asResource("block/casings/" + name),
                FXNTStorage.asResource("block/casings/" + name + "_connected"));
    }
}

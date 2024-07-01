package net.fxnt.fxntstorage.containers.util;

import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.util.StringRepresentable;

public class EnumProperties {
    public enum StorageUsed implements StringRepresentable {
        EMPTY, HAS_ITEMS, SLOTS_FILLED, FULL;
        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }

    }
    public enum Variant implements StringRepresentable {
        ACACIA, BAMBOO, BIRCH, CHERRY, CRIMSON, DARK_OAK, JUNGLE, MANGROVE, OAK, SPRUCE, WARPED;
        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }
}

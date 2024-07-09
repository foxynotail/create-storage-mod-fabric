package net.fxnt.fxntstorage.init;

import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class ModTags {

    public static final TagKey<Block> STORAGE_BOX = blockTag("storage_box");
    public static final TagKey<Block> BACK_PACK = blockTag("back_pack");
    public static final TagKey<Block> WRENCH_PICKUP = createBlockTag("wrench_pickup");
    public static final TagKey<Block> STORAGE_TRIM = createBlockTag("storage_trim");
    public static final TagKey<Block> SIMPLE_STORAGE_BOX = createBlockTag("simple_storage_box");
    public static final TagKey<Block> STORAGE_NETWORK_BLOCK = createBlockTag("storage_network_block");

    public static final TagKey<Item> STORAGE_BOX_ITEM = itemTag("storage_box");
    public static final TagKey<Item> BACK_PACK_ITEM = itemTag("back_pack");
    public static final TagKey<Item> BACK_PACK_UPGRADE = itemTag("back_pack_upgrade");
    public static final TagKey<Item> STORAGE_BOX_UPGRADE = itemTag("storage_box_upgrade");


    public static <T> TagKey<T> optionalTag(Registry<T> registry, ResourceLocation id) {
        return TagKey.create(registry.key(), id);
    }
    public static <T> TagKey<T> createTag(Registry<T> registry, String path) {
        return optionalTag(registry, new ResourceLocation(FXNTStorage.MOD_ID, path));
    }

    public static <T> TagKey<T> forgeTag(Registry<T> registry, String path) {
        return optionalTag(registry, new ResourceLocation(FXNTStorage.MOD_ID, path));
    }

    public static TagKey<Block> blockTag(String path) {
            return createTag(BuiltInRegistries.BLOCK, path);
        }

        public static TagKey<Item> itemTag(String path) {
            return createTag(BuiltInRegistries.ITEM, path);
        }

        public static TagKey<Fluid> fluidTag(String path) {
            return createTag(BuiltInRegistries.FLUID, path);
        }

    public static TagKey<Block> forgeBlockTag(String path) {
        return forgeTag(BuiltInRegistries.BLOCK, path);
    }

    public static TagKey<Block> createBlockTag(String path) {
        return createTag(BuiltInRegistries.BLOCK, path);
    }


}

package net.fxnt.fxntstorage.init;

import com.mojang.datafixers.types.Type;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackBlock;
import net.fxnt.fxntstorage.backpacks.main.BackPackEntity;
import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.fxnt.fxntstorage.containers.StorageBox;
import net.fxnt.fxntstorage.containers.StorageBoxEntity;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlocks {

    public static final Block STORAGE_BOX = registerBlock("storage_box",
        new StorageBox(FabricBlockSettings.create(), Util.IRON_STORAGE_BOX_SIZE, "industrial_iron_storage_box"), true);
    public static final Block ANDESITE_STORAGE_BOX = registerBlock("andesite_storage_box",
            new StorageBox(FabricBlockSettings.create(), Util.ANDESITE_STORAGE_BOX_SIZE, "andesite_storage_box"), true);
    public static final Block COPPER_STORAGE_BOX = registerBlock("copper_storage_box",
            new StorageBox(FabricBlockSettings.create(), Util.COPPER_STORAGE_BOX_SIZE, "copper_storage_box"), true);
    public static final Block BRASS_STORAGE_BOX = registerBlock("brass_storage_box",
            new StorageBox(FabricBlockSettings.create(), Util.BRASS_STORAGE_BOX_SIZE, "brass_storage_box"), true);

    public static final Block HARDENED_STORAGE_BOX = registerBlock("hardened_storage_box",
            new StorageBox(FabricBlockSettings.create(), Util.HARDENED_STORAGE_BOX_SIZE, "hardened_storage_box"), true);
    public static final Block BACK_PACK = registerBlock("back_pack",
                new BackPackBlock(FabricBlockSettings.create(), "back_pack", Util.IRON_BACKPACK_STACK_SIZE), false);
    public static final Block ANDESITE_BACK_PACK = registerBlock("andesite_back_pack",
                new BackPackBlock(FabricBlockSettings.create(), "andesite_back_pack", Util.ANDESITE_BACKPACK_STACK_SIZE), false);
    public static final Block COPPER_BACK_PACK = registerBlock("copper_back_pack",
                new BackPackBlock(FabricBlockSettings.create(), "copper_back_pack", Util.COPPER_BACKPACK_STACK_SIZE), false);
    public static final Block BRASS_BACK_PACK = registerBlock("brass_back_pack",
                new BackPackBlock(FabricBlockSettings.create(), "brass_back_pack", Util.BRASS_BACKPACK_STACK_SIZE), false);
    public static final Block HARDENED_BACK_PACK = registerBlock("hardened_back_pack",
                new BackPackBlock(FabricBlockSettings.create(), "hardened_back_pack", Util.HARDENED_BACKPACK_STACK_SIZE), false);

    // ITEMS
    public static final Item BACK_PACK_ITEM = registerBlockItem("back_pack",
            new BackPackItem(BACK_PACK, new FabricItemSettings()));

    public static final Item ANDESITE_BACK_PACK_ITEM = registerBlockItem("andesite_back_pack",
                new BackPackItem(ANDESITE_BACK_PACK, new FabricItemSettings()));

    public static final Item COPPER_BACK_PACK_ITEM = registerBlockItem("copper_back_pack",
                new BackPackItem(COPPER_BACK_PACK, new FabricItemSettings()));

    public static final Item BRASS_BACK_PACK_ITEM = registerBlockItem("brass_back_pack",
                new BackPackItem(BRASS_BACK_PACK, new FabricItemSettings()));

    public static final Item HARDENED_BACK_PACK_ITEM = registerBlockItem("hardened_back_pack",
                new BackPackItem(HARDENED_BACK_PACK, new FabricItemSettings().fireproof()));

    // ENTITIES
    public static final BlockEntityType<StorageBoxEntity> STORAGE_BOX_ENTITY =
            registerBlockEntity("storage_box_entity", BlockEntityType.Builder.of(StorageBoxEntity::new, STORAGE_BOX, ANDESITE_STORAGE_BOX, COPPER_STORAGE_BOX, BRASS_STORAGE_BOX, HARDENED_STORAGE_BOX));
    public static final BlockEntityType<BackPackEntity> BACK_PACK_ENTITY =
            registerBlockEntity("back_pack_entity", BlockEntityType.Builder.of(BackPackEntity::new, BACK_PACK, ANDESITE_BACK_PACK, COPPER_BACK_PACK, BRASS_BACK_PACK, HARDENED_BACK_PACK));

    private static Block registerBlock(String name, Block block, boolean autoRegisterItem) {
        if (autoRegisterItem) autoRegisterBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(FXNTStorage.MOD_ID, name), block);
    }
    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String key, BlockEntityType.Builder<T> builder) {
        Type<?> type = net.minecraft.Util.fetchChoiceType(References.BLOCK_ENTITY, key);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, builder.build(type));
    }
    private static Item autoRegisterBlockItem(String name, Block block) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(FXNTStorage.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }
    private static Item registerBlockItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(FXNTStorage.MOD_ID, name), item);
    }

    public static void register() {
        FXNTStorage.LOGGER.info("Registering Blocks for " + FXNTStorage.NAME);
    }
}
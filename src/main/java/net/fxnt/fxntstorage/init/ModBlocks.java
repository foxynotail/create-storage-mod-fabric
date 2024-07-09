package net.fxnt.fxntstorage.init;

import com.mojang.datafixers.types.Type;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackBlock;
import net.fxnt.fxntstorage.backpacks.main.BackPackEntity;
import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.fxnt.fxntstorage.containers.StorageBox;
import net.fxnt.fxntstorage.containers.StorageBoxEntity;
import net.fxnt.fxntstorage.controller.StorageControllerBlock;
import net.fxnt.fxntstorage.controller.StorageControllerEntity;
import net.fxnt.fxntstorage.controller.StorageInterfaceBlock;
import net.fxnt.fxntstorage.controller.StorageInterfaceEntity;
import net.fxnt.fxntstorage.passer.PasserBlock;
import net.fxnt.fxntstorage.passer.PasserEntity;
import net.fxnt.fxntstorage.passer.PasserSmartEntity;
import net.fxnt.fxntstorage.registry.SpriteShifts;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBox;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity;
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
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    private static final CreateRegistrate REGISTRATE = FXNTStorage.registrate();
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

    public static final Block PASSER_BLOCK = registerBlock("passer_block",
            new PasserBlock(FabricBlockSettings.create(), false), true);
    public static final Block SMART_PASSER_BLOCK = registerBlock("smart_passer_block",
            new PasserBlock(FabricBlockSettings.create(), true), true);


    public static final Block SIMPLE_STORAGE_BOX = registerBlock("simple_storage_box",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_SPRUCE = registerBlock("simple_storage_box_spruce",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_BIRCH = registerBlock("simple_storage_box_birch",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_JUNGLE = registerBlock("simple_storage_box_jungle",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_ACACIA = registerBlock("simple_storage_box_acacia",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_DARK_OAK = registerBlock("simple_storage_box_dark_oak",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_MANGROVE = registerBlock("simple_storage_box_mangrove",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_CHERRY = registerBlock("simple_storage_box_cherry",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_BAMBOO = registerBlock("simple_storage_box_bamboo",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_CRIMSON = registerBlock("simple_storage_box_crimson",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final Block SIMPLE_STORAGE_BOX_WARPED = registerBlock("simple_storage_box_warped",
            new SimpleStorageBox(FabricBlockSettings.create()), true);
    public static final BlockEntityType<SimpleStorageBoxEntity> SIMPLE_STORAGE_BOX_ENTITY =
            registerBlockEntity("simple_storage_box_entity", BlockEntityType.Builder.of(SimpleStorageBoxEntity::new,
                    SIMPLE_STORAGE_BOX,
                    SIMPLE_STORAGE_BOX_SPRUCE,
                    SIMPLE_STORAGE_BOX_BIRCH,
                    SIMPLE_STORAGE_BOX_JUNGLE,
                    SIMPLE_STORAGE_BOX_ACACIA,
                    SIMPLE_STORAGE_BOX_DARK_OAK,
                    SIMPLE_STORAGE_BOX_MANGROVE,
                    SIMPLE_STORAGE_BOX_CHERRY,
                    SIMPLE_STORAGE_BOX_BAMBOO,
                    SIMPLE_STORAGE_BOX_CRIMSON,
                    SIMPLE_STORAGE_BOX_WARPED
            ));
    
    // STORAGE TRIM
    public static final BlockEntry<CasingBlock> STORAGE_TRIM = REGISTRATE
            .block("storage_trim", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.OAK_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_SPRUCE = REGISTRATE
            .block("storage_trim_spruce", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.SPRUCE_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_BIRCH = REGISTRATE
            .block("storage_trim_birch", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.BIRCH_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_JUNGLE = REGISTRATE
            .block("storage_trim_jungle", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.JUNGLE_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_ACACIA = REGISTRATE
            .block("storage_trim_acacia", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.ACACIA_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_DARK_OAK = REGISTRATE
            .block("storage_trim_dark_oak", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.DARK_OAK_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_MANGROVE = REGISTRATE
            .block("storage_trim_mangrove", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.MANGROVE_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_CHERRY = REGISTRATE
            .block("storage_trim_cherry", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.CHERRY_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_BAMBOO = REGISTRATE
            .block("storage_trim_bamboo", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.BAMBOO_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_CRIMSON = REGISTRATE
            .block("storage_trim_crimson", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.CRIMSON_CASING))
            .register();
    public static final BlockEntry<CasingBlock> STORAGE_TRIM_WARPED = REGISTRATE
            .block("storage_trim_warped", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.casing(() -> SpriteShifts.WARPED_CASING))
            .register();

    // CONTROLLER
    public static final Block STORAGE_CONTROLLER = registerBlock("storage_controller",
            new StorageControllerBlock(FabricBlockSettings.create()), true);
    public static final Block STORAGE_INTERFACE = registerBlock("storage_interface",
            new StorageInterfaceBlock(FabricBlockSettings.create()), true);

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
    public static final BlockEntityType<PasserEntity> PASSER_ENTITY =
            registerBlockEntity("passer_entity", BlockEntityType.Builder.of(PasserEntity::new, PASSER_BLOCK));
    public static final BlockEntityType<PasserSmartEntity> SMART_PASSER_ENTITY =
            registerBlockEntity("smart_passer_entity", BlockEntityType.Builder.of(PasserSmartEntity::new, SMART_PASSER_BLOCK));
    public static final BlockEntityType<StorageControllerEntity> STORAGE_CONTROLLER_ENTITY =
            registerBlockEntity("storage_controller_entity", BlockEntityType.Builder.of(StorageControllerEntity::new, STORAGE_CONTROLLER));
    public static final BlockEntityType<StorageInterfaceEntity> STORAGE_INTERFACE_ENTITY =
            registerBlockEntity("storage_interface_entity", BlockEntityType.Builder.of(StorageInterfaceEntity::new, STORAGE_INTERFACE));


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
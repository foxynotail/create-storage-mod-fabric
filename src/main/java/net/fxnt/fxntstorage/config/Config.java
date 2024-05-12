package net.fxnt.fxntstorage.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class Config {

    private Config() {}
    public static final String GENERAL_CATEGORY = "general_settings";
    public static final String STORAGE_BOX_CATEGORY = "strorage_box_settings";
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.ConfigValue<Integer> STORAGE_BOX_UPDATE_TIME;
    public static ForgeConfigSpec.ConfigValue<Integer> BACKPACK_MAGNET_RANGE;
    public static ForgeConfigSpec.ConfigValue<Double> JETPACK_FUEL_DEPLETION_AMOUNT;

    static {

        COMMON_BUILDER.comment("General Settings").push(GENERAL_CATEGORY);


        COMMON_BUILDER.comment("Storage Box Settings").push(STORAGE_BOX_CATEGORY);

        STORAGE_BOX_UPDATE_TIME = COMMON_BUILDER
                .comment("Sets how many ticks pass before Storage Boxes Update their block count and Block States. More = better performance")
                .define("storage_box_update_time", 10);

        BACKPACK_MAGNET_RANGE = COMMON_BUILDER
                .comment("BackPack Magnet Range (In Blocks)")
                .define("backpack_magnet_range", 5);

        JETPACK_FUEL_DEPLETION_AMOUNT = COMMON_BUILDER
                .comment("How much fuel is used per tick while flying")
                .define("jetpack_fuel_depletion_amount", 0.011);


        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .preserveInsertionOrder()
                .build();
        configData.load();
        spec.setConfig(configData);
    }
}

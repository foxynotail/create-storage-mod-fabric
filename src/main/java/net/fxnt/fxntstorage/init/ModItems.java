package net.fxnt.fxntstorage.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.upgrades.UpgradeItem;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item BACK_PACK_BLANK_UPGRADE = registerItem(Util.BLANK_UPGRADE, new Item(new FabricItemSettings()));
    public static final Item BACK_PACK_MAGNET_UPGRADE = registerItem(Util.MAGNET_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.MAGNET_UPGRADE));
    public static final Item BACK_PACK_MAGNET_UPGRADE_DEACTIVATED = registerItem(Util.MAGNET_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.MAGNET_UPGRADE_DEACTIVATED));
    public static final Item BACK_PACK_PICKBLOCK_UPGRADE = registerItem(Util.PICKBLOCK_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.PICKBLOCK_UPGRADE));
    public static final Item BACK_PACK_PICKBLOCK_UPGRADE_DEACTIVATED = registerItem(Util.PICKBLOCK_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.PICKBLOCK_UPGRADE_DEACTIVATED));
    public static final Item BACK_PACK_ITEMPICKUP_UPGRADE = registerItem(Util.ITEMPICKUP_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.ITEMPICKUP_UPGRADE));
    public static final Item BACK_PACK_ITEMPICKUP_UPGRADE_DEACTIVATED = registerItem(Util.ITEMPICKUP_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.ITEMPICKUP_UPGRADE_DEACTIVATED));
    public static final Item BACK_PACK_FLIGHT_UPGRADE = registerItem(Util.FLIGHT_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.FLIGHT_UPGRADE));
    public static final Item BACK_PACK_FLIGHT_UPGRADE_DEACTIVATED = registerItem(Util.FLIGHT_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.FLIGHT_UPGRADE_DEACTIVATED));
    public static final Item BACK_PACK_REFILL_UPGRADE = registerItem(Util.REFILL_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.REFILL_UPGRADE));
    public static final Item BACK_PACK_REFILL_UPGRADE_DEACTIVATED = registerItem(Util.REFILL_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.REFILL_UPGRADE_DEACTIVATED));
    public static final Item BACK_PACK_FEEDER_UPGRADE = registerItem(Util.FEEDER_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.FEEDER_UPGRADE));
    public static final Item BACK_PACK_FEEDER_UPGRADE_DEACTIVATED = registerItem(Util.FEEDER_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.FEEDER_UPGRADE_DEACTIVATED));
    public static final Item BACK_PACK_TOOLSWAP_UPGRADE = registerItem(Util.TOOLSWAP_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.TOOLSWAP_UPGRADE));
    public static final Item BACK_PACK_TOOLSWAP_UPGRADE_DEACTIVATED = registerItem(Util.TOOLSWAP_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.TOOLSWAP_UPGRADE_DEACTIVATED));
    public static final Item BACK_PACK_FALLDAMAGE_UPGRADE = registerItem(Util.FALLDAMAGE_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.FALLDAMAGE_UPGRADE));
    public static final Item BACK_PACK_FALLDAMAGE_UPGRADE_DEACTIVATED = registerItem(Util.FALLDAMAGE_UPGRADE_DEACTIVATED, new UpgradeItem(new FabricItemSettings(), Util.FALLDAMAGE_UPGRADE_DEACTIVATED));
    public static final Item STORAGE_BOX_VOID_UPGRADE = registerItem(Util.STORAGE_BOX_VOID_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.STORAGE_BOX_VOID_UPGRADE));
    public static final Item STORAGE_BOX_CAPACITY_UPGRADE = registerItem(Util.STORAGE_BOX_CAPACITY_UPGRADE, new UpgradeItem(new FabricItemSettings(), Util.STORAGE_BOX_CAPACITY_UPGRADE));
    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(FXNTStorage.MOD_ID, name), item);
    }
    public static void register() {
        FXNTStorage.LOGGER.info("Registering Mod Items for " + FXNTStorage.MOD_ID);
    }
}

package net.fxnt.fxntstorage.backpacks.upgrades;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class UpgradeItem extends Item {

    private final String name;

    private static final int maxLength = 40;
    public UpgradeItem(FabricItemSettings properties, String name) {
        super(properties.stacksTo(64));
        this.name = name;
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public String getUpgradeName() {
        String name = this.getDescriptionId();
        String replaceTarget = "item." + FXNTStorage.MOD_ID + ".";
        return name.replace(replaceTarget, "");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        switch (name) {
            case Util.MAGNET_UPGRADE_DEACTIVATED:
            case Util.PICKBLOCK_UPGRADE_DEACTIVATED:
            case Util.ITEMPICKUP_UPGRADE_DEACTIVATED:
            case Util.FLIGHT_UPGRADE_DEACTIVATED:
            case Util.REFILL_UPGRADE_DEACTIVATED:
            case Util.FEEDER_UPGRADE_DEACTIVATED:
            case Util.TOOLSWAP_UPGRADE_DEACTIVATED:
            case Util.FALLDAMAGE_UPGRADE_DEACTIVATED:
                tooltipComponents.add(Component.literal("DEACTIVATED").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            case Util.MAGNET_UPGRADE:
            case Util.PICKBLOCK_UPGRADE:
            case Util.ITEMPICKUP_UPGRADE:
            case Util.REFILL_UPGRADE:
            case Util.FEEDER_UPGRADE:
            case Util.TOOLSWAP_UPGRADE:
            case Util.FALLDAMAGE_UPGRADE:
                addUpgradeDetails(tooltipComponents, name);
                break;
            case Util.STORAGE_BOX_VOID_UPGRADE:
            case Util.STORAGE_BOX_CAPACITY_UPGRADE:
                addStorageBoxUpgradeDetails(tooltipComponents, name);
                break;
            case Util.FLIGHT_UPGRADE:
                addFlightUpgradeDetails(tooltipComponents, name);
                break;
        }
    }

    private void addUpgradeDetails(List<Component> tooltipComponents, String upgradeName) {
        List<String> text = getUpgradeText(upgradeName, true);
        for (String line : text) {
            tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltipComponents.add(Component.translatable("fxntstorage.tooltip.holdForDescription").withStyle(ChatFormatting.GRAY));

        if (Screen.hasShiftDown()) {
            text = getUpgradeText(upgradeName, false);
            for (String line : text) {
                tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.GOLD));
            }
            text = disableText();
            for (String line : text) {
                tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            }
        }
    }

    private void addStorageBoxUpgradeDetails(List<Component> tooltipComponents, String upgradeName) {
        List<String> text = getUpgradeText(upgradeName, true);
        for (String line : text) {
            tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
        }
        text = getUpgradeText(upgradeName, false);
        for (String line : text) {
            tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.GOLD));
        }
    }
    private void addFlightUpgradeDetails(List<Component> tooltipComponents, String upgradeName) {
        List<String> text = getUpgradeText(upgradeName, true);
        for (String line : text) {
            tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltipComponents.add(Component.translatable("fxntstorage.tooltip.holdForDescription").withStyle(ChatFormatting.GRAY));

        if (Screen.hasShiftDown()) {
            text = getUpgradeText(upgradeName, false);
            for (String line : text) {
                tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.GOLD));
            }
            text = disableText();
            for (String line : text) {
                tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            }
        }
        tooltipComponents.add(Component.translatable("fxntstorage.tooltip.holdForControls").withStyle(ChatFormatting.GRAY));

        if (Screen.hasControlDown()) {
            text = getUpgradeText("flight_controls", false);
            for (String line : text) {
                tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }

    private List<String> getUpgradeText(String upgradeName, boolean isActive) {
        switch (upgradeName) {
            case Util.MAGNET_UPGRADE:
                return magnetText(isActive);
            case Util.PICKBLOCK_UPGRADE:
                return pickblockText(isActive);
            case Util.ITEMPICKUP_UPGRADE:
                return pickupText(isActive);
            case Util.FLIGHT_UPGRADE:
                return flightText(isActive);
            case "flight_controls":
                return flightControlsText(isActive);
            case Util.REFILL_UPGRADE:
                return refillText(isActive);
            case Util.FEEDER_UPGRADE:
                return feederText(isActive);
            case Util.TOOLSWAP_UPGRADE:
                return toolswapText(isActive);
            case Util.FALLDAMAGE_UPGRADE:
                return fallDamageText(isActive);
            case Util.STORAGE_BOX_VOID_UPGRADE:
                return storageBoxVoidText(isActive);
            case Util.STORAGE_BOX_CAPACITY_UPGRADE:
                return storageBoxCapacityText(isActive);
        }
        return new ArrayList<>();
    }


    private List<String> disableText() {
        String text = "\nToggle upgrade ability by holding CTRL and clicking the upgrade while in the Backpack's upgrade slot.";
        return Util.wrapText(text, maxLength);
    }

    private List<String> magnetText(boolean isActive) {
        String text = "Picks up items in a 5 block range around the backpack.\nRange can be configured in the [Config Menu].";
        String shiftText = "Upgrade activates while worn on the Player's back or while placed on the floor.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> pickblockText(boolean isActive) {
        String text = "Retrieves an item out of the Item Slots in the backpack when using pick block for an item that's not in the Player's inventory.";
        String shiftText = "Upgrade activates while worn on the Player's back.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> pickupText(boolean isActive) {
        String text = "Items will go into the backpack instead of the Player's inventory when picked up.\nDoes NOT work with the Magnet Upgrade";
        String shiftText = "Upgrade activates while worn on the Player's back.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> flightText(boolean isActive) {
        String text = "Turns the backpack into a functional jetpack!";
        String shiftText = "Upgrade activates while worn on the Player's back.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> flightControlsText(boolean isActive) {
        String text = "\n[Fuel]\nBackTank air from any BackTank inside of the Backpack\n\n" +
                "[Fly]\nHold Jump to fly upwards to a maximum height of 32 blocks above the ground\n\n" +
                "[Hover]\nPress H (or Jump and Crouch) to enable hovering\n\n" +
                "Deactivate hovering by pressing H or Jump";
        String shiftText = "";
        return Util.wrapText(text, maxLength);
    }

    private List<String> refillText(boolean isActive) {
        String text = "Refills the selected hotbar stack from the Backpack's Item Storage";
        String shiftText = "Upgrade activates while worn on the Player's back.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> feederText(boolean isActive) {
        String text = "Feeds the player when hungry from the Backpack's Item Storage";
        String shiftText = "Upgrade activates while worn on the Player's back.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> toolswapText(boolean isActive) {
        String text = "Swaps the main hand tool for the most appropriate from the Backpack's Tool Storage";
        String shiftText = "Detects the best tool for the block or entity being broken/attacked.\nTries to prioritize silk touch when beneficial.\nWill only swap tools from the Tool Section of the Backpack.\nUpgrade activates while worn on the Player's back.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> fallDamageText(boolean isActive) {
        String text = "Prevents the player from taking fall damage";
        String shiftText = "Upgrade activates while worn on the Player's back.";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> storageBoxVoidText(boolean isActive) {
        String text = "Voids Excess Items";
        String shiftText = "Only works on Simple Storage Boxes";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

    private List<String> storageBoxCapacityText(boolean isActive) {
        String text = "Doubles Simple Storage Box Capacity";
        String shiftText = "Only works on Simple Storage Boxes";
        if (isActive) return Util.wrapText(text, maxLength);
        else return Util.wrapText(shiftText, maxLength);
    }

}

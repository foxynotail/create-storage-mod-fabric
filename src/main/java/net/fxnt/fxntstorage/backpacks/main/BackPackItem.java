package net.fxnt.fxntstorage.backpacks.main;

import io.github.fabricators_of_create.porting_lib.item.EquipmentItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fxnt.fxntstorage.backpacks.util.BackPackHandler;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BackPackItem extends BlockItem implements EquipmentItem {

    /** @deprecated */
    @Deprecated
    private final Block block;

     public BackPackItem(Block block, FabricItemSettings properties) {
        super(block, properties.stacksTo(1).fireResistant());
         this.block = block;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(this.block instanceof BackPackBlock);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BackPackHandler.openBackpackFromInventory(serverPlayer, Util.BACKPACK_IN_HAND);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    // TRINKETS
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Run from ServerPlayer.class Mixin so Upgrade Handler works with BackPack as a Trinket
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        String text = "A handy backpack with two storage compartments.";
        String shiftText = "[Items Storage]\nHolds large stacks of items\nItems can be taken and added to by hoppers\n\n" +
                "[Tool Storage]\nHolds standard stack sizes\nCan be used with the Tool Swapper upgrade\nIt's a safe compartment that isn't interacted with by hoppers\n\n" +
                "[Upgrade Slots]\n6 upgrade slots that give the backpack abilities";


        List<String> textLines = Util.wrapText(text, 50);
        List<String> shiftTextLines = Util.wrapText(shiftText, 50);

        for (String line : textLines) {
            tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltipComponents.add(Component.translatable("fxntstorage.tooltip.holdForDescription").withStyle(ChatFormatting.GRAY));

        if (Screen.hasShiftDown()) {
            for (String line : shiftTextLines) {
                tooltipComponents.add(Component.literal(line).withStyle(ChatFormatting.GOLD));
            }
        }

        CompoundTag compoundTag = BlockItem.getBlockEntityData(stack);
        if (compoundTag != null) {
            if (compoundTag.contains("Items", 9)) {
                NonNullList<ItemStack> itemsList = NonNullList.withSize(27, ItemStack.EMPTY);
                ListTag listTag = compoundTag.getList("Items", Tag.TAG_COMPOUND);
                for (int i = 0; i < listTag.size(); ++i) {
                    CompoundTag tag = listTag.getCompound(i);
                    int slot = tag.getByte("Slot") & 255;
                    ItemStack itemStack = ItemStack.of(tag);
                    if (tag.contains("ActualCount", Tag.TAG_INT)) {
                        itemStack.setCount(tag.getInt("ActualCount"));
                    }
                    if (slot < itemsList.size()) {
                        itemsList.set(slot, itemStack);
                    }
                }
                int i = 0;
                int j = 0;

                for(ItemStack itemStack : itemsList) {
                    if (!itemStack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            MutableComponent mutableComponent = itemStack.getHoverName().copy();
                            mutableComponent.append(" x").append(String.valueOf(itemStack.getCount()));
                            tooltipComponents.add(mutableComponent);
                        }
                    }
                }

                if (j - i > 0) {
                    tooltipComponents.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
                }
            }
            if (compoundTag.contains("Upgrades", 9)) {
                NonNullList<String> upgradesList = NonNullList.create();

                ListTag upgrades = compoundTag.getList("Upgrades", Tag.TAG_STRING);
                for (int i = 0; i < upgrades.size(); i++) {
                    upgradesList.add(upgrades.getString(i));
                }

                if (!upgradesList.isEmpty()) {
                    tooltipComponents.add(Component.literal("Upgrades:"));
                }
                for(String upgradeName : upgradesList) {
                    upgradeName = upgradeName.replace("back_pack_", "").replace("_", " ");
                    upgradeName = "+ " + upgradeName.substring(0, 1).toUpperCase() + upgradeName.substring(1);
                    tooltipComponents.add(Component.literal(upgradeName));
                }
            }
        }
    }
}
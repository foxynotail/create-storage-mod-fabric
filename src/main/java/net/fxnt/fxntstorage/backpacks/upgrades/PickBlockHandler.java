package net.fxnt.fxntstorage.backpacks.upgrades;

import net.fxnt.fxntstorage.backpacks.main.BackPackContainer;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class PickBlockHandler {

    public static void pickBlockHandler(Player player, BackPackContainer container, BlockPos blockPos) {

        //FXNTStorage.LOGGER.info("{} Do Pick Block", player.level());
        BlockState state = player.level().getBlockState(blockPos);
        ItemStack itemStack = state.getBlock().getCloneItemStack(player.level(), blockPos, state);

        Inventory inventory = player.getInventory();
        int matchingBackPackSlot = new BackPackHelper().getItemSlotFromContainer(container, itemStack.getItem());

        if (matchingBackPackSlot != -1) {

            ItemStack backPackStack = container.getItem(matchingBackPackSlot);

            //int hotbarSlot = inventory.getSuitableHotbarSlot();
            int hotbarSlot = player.getInventory().selected;

            if (!inventory.contains(itemStack)) {

                // Logic when the item is not in the inventory
                // Find free hotbar slot
                if (!inventory.getItem(hotbarSlot).isEmpty()) { // Hotbar slot full
                    // Get inventory slot stack
                    ItemStack hotbarStack = inventory.getItem(hotbarSlot);
                    int hotbarStackSize = hotbarStack.getCount();

                    // Is there room to move selected hotbar slot into inventory?
                    int freeSlot = inventory.getFreeSlot();
                    if (freeSlot != -1) { // Room in inventory
                        // Move selected hotbar slot to free slot in inventory
                        ItemStack hotbarStackCopy = hotbarStack.copyWithCount(hotbarStack.getCount());
                        player.getInventory().setItem(freeSlot, hotbarStackCopy);

                        // Move backpack slot to hotbar slot
                        int amountToMove = Math.min(backPackStack.getCount(), backPackStack.getItem().getMaxStackSize());

                        ItemStack backPackStackCopy = backPackStack.copyWithCount(amountToMove);
                        player.getInventory().setItem(hotbarSlot, backPackStackCopy);

                        backPackStack.shrink(amountToMove);
                        container.setChanged();
                        inventory.selected = hotbarSlot;

                    } else { // No Room in inventory
                        for (int i = 0; i < inventory.getContainerSize(); i++) {
                            if (i != hotbarSlot) {
                                ItemStack thisStack = inventory.getItem(i);
                                int thisStackSize = thisStack.getCount();
                                int maxStackSize = thisStack.getMaxStackSize();
                                int freeSpace = maxStackSize - thisStackSize;
                                if (ItemStack.isSameItemSameTags(hotbarStack, thisStack) && freeSpace >= hotbarStackSize) { // Can merge

                                    // Merge Hotbar Slot with Inventory Slot
                                    // Move backpack slot to hotbar slot

                                    // Get inventory slot amount
                                    // Set to inventory slot amount + hotbar slot amount
                                    ItemStack newInventoryStack = thisStack.copyWithCount(thisStackSize + hotbarStackSize);
                                    player.getInventory().setItem(i, newInventoryStack);

                                    int amountToMove = Math.min(backPackStack.getCount(), backPackStack.getItem().getMaxStackSize());

                                    ItemStack backPackStackCopy = backPackStack.copyWithCount(amountToMove);
                                    player.getInventory().setItem(hotbarSlot, backPackStackCopy);

                                    backPackStack.shrink(amountToMove);
                                    container.setChanged();
                                    inventory.selected = hotbarSlot;
                                    break;
                                }
                            }
                        }
                    }
                } else { // Hotbar Slot is empty
                    // Move backpack slot to hotbar slot

                    int amountToMove = Math.min(backPackStack.getCount(), backPackStack.getItem().getMaxStackSize());

                    ItemStack backPackStackCopy = backPackStack.copyWithCount(amountToMove);
                    player.getInventory().setItem(hotbarSlot, backPackStackCopy);

                    // Update backpack
                    backPackStack.shrink(amountToMove);
                    container.setChanged();
                    inventory.selected = hotbarSlot;
                }


            } else {

                // If selected hotbar slot matches picked item then top up from backpack
                // Check if hotbar stack matches item stack

                ItemStack hotbarStack = inventory.getItem(hotbarSlot);
                int hotbarStackSize = hotbarStack.getCount();
                int maxHotBarStackSize = hotbarStack.getMaxStackSize();

                if (ItemStack.isSameItem(hotbarStack, itemStack)) {

                    // If hotbar slot only partially full, top up from backpack
                    if (maxHotBarStackSize > hotbarStackSize) {
                        int freeHotBarStackSpace = maxHotBarStackSize - hotbarStackSize;

                        // Move partial stack from backpack to hotbar

                        // Has backpack got enough items?
                        int backPackStackSize = backPackStack.getCount();

                        if (backPackStackSize > freeHotBarStackSpace) {
                            // Take partial amount from backpack stack

                            int amountToMove = hotbarStack.getMaxStackSize() - hotbarStackSize;

                            // Update hotbar stack with new amount
                            ItemStack newInventoryStack = itemStack.copyWithCount(hotbarStack.getMaxStackSize());
                            player.getInventory().setItem(hotbarSlot, newInventoryStack);

                            // Update backpack stack with reduced amount
                            backPackStack.shrink(amountToMove);
                            container.setChanged();
                            inventory.selected = hotbarSlot;

                        } else {

                            // Take entire amount from backpack stack

                            int amountToMove = Math.min(backPackStack.getCount(), backPackStack.getItem().getMaxStackSize());

                            // Send backpack stack to hotbar
                            ItemStack newInventoryStack = backPackStack.copyWithCount(amountToMove - hotbarStackSize);
                            player.getInventory().setItem(hotbarSlot, newInventoryStack);

                            // Update backpack
                            backPackStack.shrink(amountToMove);
                            container.setChanged();
                            inventory.selected = hotbarSlot;

                        }
                    }
                }
            }
        }
    }
}

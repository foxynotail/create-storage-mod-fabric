package net.fxnt.fxntstorage.backpacks.upgrades;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackContainer;
import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.fxnt.fxntstorage.backpacks.main.BackPackMenu;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class BackPackOnBackUpgradeHandler {
    public Player player;
    private final BackPackHelper helper;
    private final int magnetUpgradeRange = Config.BACKPACK_MAGNET_RANGE.get();
    private final ItemStack itemStack;
    public BackPackOnBackUpgradeHandler(Player player) {
        this.player = player;
        this.helper = new BackPackHelper();
        this.itemStack = this.helper.getWornBackPackStack(player);
    }

    public boolean hasUpgrade(String upgradeName) {
        if (this.itemStack.isEmpty()) return false;
        CompoundTag tag = this.itemStack.getTagElement("BlockEntityTag");
        if (tag != null) {
            if (tag.contains("Upgrades")) {
                ListTag upgradesList = tag.getList("Upgrades", Tag.TAG_STRING);
                for (Tag upgrade : upgradesList) {
                    if (upgrade.getAsString().equals(upgradeName)) return true;
                }
            }
        }
        return false;
    }

    private BackPackContainer getContainer() {
        if (player.containerMenu instanceof BackPackMenu backPackMenu && backPackMenu.backPackType == Util.BACKPACK_ON_BACK) {
            return (BackPackContainer) backPackMenu.container;
        } else {
            return new BackPackContainer(this.itemStack);
        }
    }

    // SERVER SIDE
    public void applyMagnetUpgrade() {
        if (this.itemStack.isEmpty() || this.player.level().isClientSide || !hasUpgrade(Util.MAGNET_UPGRADE)) return;
        // Define the bounding box around the center position
        AABB boundingBox = new AABB(this.player.blockPosition()).inflate(magnetUpgradeRange);
        // Retrieve all item entities within the range
        List<ItemEntity> nearbyItems = this.player.level().getEntitiesOfClass(ItemEntity.class, boundingBox);
        if (!nearbyItems.isEmpty()) {
            for (ItemEntity itemEntity : nearbyItems) {
                if (itemEntity.getItem().getItem() instanceof BackPackItem) continue;
                this.helper.itemEntityToBackPack(getContainer(), this.player.level(), itemEntity, Util.ITEM_SLOT_START_RANGE, Util.ITEM_SLOT_END_RANGE);
            }
        }
    }

    // SERVER SIDE
    public boolean applyItemPickupUpgrade(ItemEntity itemEntity, UUID target, int pickupDelay) {

        if (this.itemStack.isEmpty() || this.player.level().isClientSide || !hasUpgrade(Util.ITEMPICKUP_UPGRADE)  || hasUpgrade(Util.MAGNET_UPGRADE)) return false;
        ItemStack itemStack = itemEntity.getItem();
        Item item = itemStack.getItem();
        int i = itemStack.getCount();
        if (pickupDelay == 0 && (target == null || target.equals(player.getUUID())) &&
                this.helper.itemEntityToBackPack(getContainer(), this.player.level(), itemEntity, Util.ITEM_SLOT_START_RANGE, Util.ITEM_SLOT_END_RANGE)) {

            player.take(itemEntity, i);
            if (itemStack.isEmpty()) {
                itemEntity.discard();
                itemStack.setCount(i);
            }
            player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            player.onItemPickup(itemEntity);
            return true;
        }
        return false;
    }

    // SERVER SIDE
    public void applyPickBlockUpgrade(BlockPos blockPos) {
        if (this.itemStack.isEmpty() || this.player.level().isClientSide || !hasUpgrade(Util.PICKBLOCK_UPGRADE)) return;
        PickBlockHandler.pickBlockHandler(player, getContainer(), blockPos);
    }

    // SERVER SIDE
    public void applyFeederUpgrade() {
        if (this.itemStack.isEmpty() || this.player.level().isClientSide || !hasUpgrade(Util.FEEDER_UPGRADE)) return;
        boolean doFeed = false;
        int hunger = this.player.getFoodData().getFoodLevel(); // Max 20
        float saturation = this.player.getFoodData().getSaturationLevel();
        float health = this.player.getHealth();
        // If player is hurt and saturation gone then feed
        if (health < this.player.getMaxHealth() && hunger < 18 && saturation < 2) {
            doFeed = true;
        }
        // Feed if less than 3 hunger honches
        if (hunger <= 6) {
            doFeed = true;
        }
        // Find food in backpack
        if (doFeed) {
            for (int i = 0; i < getContainer().getContainerSize(); i++) {
                ItemStack food = getContainer().getItem(i);
                if (food.isEdible()) {
                    this.player.eat(this.player.level(), food);
                    break;
                }
            }
        }

    }

    // SERVER SIDE
    public void applyRefillUpgrade() {
        if (this.itemStack.isEmpty() || this.player.level().isClientSide || !hasUpgrade(Util.REFILL_UPGRADE)) return;
        // Item in main hand or offhand is less than max stack size, check for matching items in backpack and fill inventory stack
        refillHand(this.player.getMainHandItem(), false);
        refillHand(this.player.getOffhandItem(), true);
    }
    public void refillHand(ItemStack handItem, boolean isOffHand) {
        if (handItem.isEmpty()) return;
        boolean success;
        int requiredItems = handItem.getItem().getMaxStackSize() - handItem.getCount();
        if (requiredItems > 0) {
            int offHandSlotIndex = 40;
            int ignorePlayerSlot = isOffHand ? offHandSlotIndex : this.player.getInventory().selected;
            // Check Player inventory first
            success = refillMatchingItem(handItem, requiredItems, this.player.getInventory(), 0, this.player.getInventory().getContainerSize(), ignorePlayerSlot);
            if (!success) {
                refillMatchingItem(handItem, requiredItems, getContainer(), Util.ITEM_SLOT_START_RANGE, Util.ITEM_SLOT_END_RANGE, -1);
            }
        }
    }
    public boolean refillMatchingItem(ItemStack itemStack, int requiredItems, Container container, int startIndex, int endIndex, int ignoreSlot) {
        int amountToPlace = requiredItems;
        for (int i = startIndex; i < endIndex; i++) {
            if (i == ignoreSlot) continue;
            ItemStack containerItem = container.getItem(i);
            if (ItemStack.isSameItemSameTags(itemStack, containerItem)) {
                if (containerItem.getCount() < requiredItems) {
                    amountToPlace = containerItem.getCount();
                }
                itemStack.grow(amountToPlace);
                containerItem.shrink(amountToPlace);
                container.setChanged();
                return true;
            }
        }
        return false;
    }

    public boolean applyFallDamageUpgrade() {
        return !this.itemStack.isEmpty() && !this.player.level().isClientSide && hasUpgrade(Util.FALLDAMAGE_UPGRADE);
    }

    // SERVER SIDE
    public void fromAttackBlockEvent(Player player, Level level, InteractionHand hand, BlockPos pos) {
        if (this.itemStack.isEmpty() || hand != InteractionHand.OFF_HAND && player.isSpectator() || level.isClientSide || !player.isAlive()
                || player.isSleeping() || player.isDeadOrDying() || !hasUpgrade(Util.TOOLSWAP_UPGRADE)) return;

        ToolSwapHandler toolSwapHandler = new ToolSwapHandler(player, getContainer(), Util.TOOL_SLOT_START_RANGE, Util.TOOL_SLOT_END_RANGE);
        toolSwapHandler.doToolSwap(level, pos, null, "block");
    }
    public void fromAttackEntityEvent(Player player, Level level, InteractionHand hand, LivingEntity entity) {
        if (this.itemStack.isEmpty() || hand != InteractionHand.OFF_HAND && player.isSpectator() || level.isClientSide || !player.isAlive()
                || player.isSleeping() || player.isDeadOrDying() || !hasUpgrade(Util.TOOLSWAP_UPGRADE)) return;

        ToolSwapHandler toolSwapHandler = new ToolSwapHandler(player, getContainer(), Util.TOOL_SLOT_START_RANGE, Util.TOOL_SLOT_END_RANGE);
        toolSwapHandler.doToolSwap(level, null, entity, "entity");
    }

}

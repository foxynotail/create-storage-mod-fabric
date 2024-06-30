package net.fxnt.fxntstorage.backpacks.upgrades;

import io.github.fabricators_of_create.porting_lib.tool.ToolActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Comparator;

public class ToolSwapHandler {

    private final Player player;
    private final Container container;
    private final int startIndex;
    private final int endIndex;
    private static BlockState lastBlockState;
    private static ItemStack lastTool;
    private static LivingEntity lastEntity;
    private static ItemStack lastWeapon;

    public ToolSwapHandler(Player player, Container container, int startIndex, int endIndex) {
        this.player = player;
        this.container = container;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public class ToolInfo {
        public final ItemStack itemStack;
        public final int slot;
        public final double speed;
        public final boolean silkTouch;
        public ToolInfo(ItemStack itemStack, int slot, double speed, boolean silkTouch) {
            this.itemStack = itemStack;
            this.slot = slot;
            this.speed = speed;
            this.silkTouch = silkTouch;
        }
    }

    public class WeaponInfo {
        public final ItemStack itemStack;
        public final int slot;
        public final double damage;
        public WeaponInfo(ItemStack itemStack, int slot, double damage) {
            this.itemStack = itemStack;
            this.slot = slot;
            this.damage = damage;
        }
    }

    public void doToolSwap(Level level, BlockPos blockPos, LivingEntity entity, String type) {

        if (type == "block" && blockPos != null) {
            ItemStack currentItem = player.getMainHandItem();
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState != lastBlockState || currentItem != lastTool) {
                swapBlockTool(blockState);
            }
            lastBlockState = blockState;
            lastTool = currentItem;
        }
        if (type == "entity" && entity != null) {
            ItemStack currentItem = player.getMainHandItem();
            if (entity != lastEntity || currentItem != lastWeapon) {
                swapEntityTool(entity);
            }
            lastEntity = entity;
            lastWeapon = currentItem;
        }
    }

    public void swapBlockTool(BlockState blockState) {

        ToolInfo bestTool = findBestToolForBreakingBlock(blockState);
        if (bestTool == null) return;

        if (!bestTool.itemStack.isEmpty()) {
            int selectedSlot = player.getInventory().selected;
            if (selectedSlot != bestTool.slot) {
                ItemStack selectedStack = player.getInventory().getSelected().copy();
                if (!selectedStack.isEmpty()) {
                    //InventoryPackets.sendToServer(selectedSlot, bestTool.itemStack.copy());
                    player.getInventory().setItem(selectedSlot, bestTool.itemStack.copy());
                    container.setItem(bestTool.slot, selectedStack.copy());
                }
            }
        }
    }

    private ToolInfo findBestToolForBreakingBlock(BlockState blockState) {

        NonNullList<ToolInfo> suitableItems = NonNullList.create();
        boolean requiresSilkTouch = requiresSilkTouch(blockState);
        boolean prefersSilkTouch = prefersSilkTouch(blockState);
        boolean needsOrPrefersSilkTouch = requiresSilkTouch || prefersSilkTouch;

        // Is player's current tool good enough?
        ItemStack currentTool = player.getInventory().getSelected();
        boolean currentToolIsCorrect = currentTool.isCorrectToolForDrops(blockState);
        boolean currentToolHasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, currentTool) > 0;
        boolean currentToolMeetsSilkTouchRequirement = false;
        if (needsOrPrefersSilkTouch && currentToolHasSilkTouch) {
            currentToolMeetsSilkTouchRequirement = true;
        } else if (!needsOrPrefersSilkTouch && currentToolHasSilkTouch) {
            currentToolMeetsSilkTouchRequirement = true;
        } else if (!needsOrPrefersSilkTouch) {
            currentToolMeetsSilkTouchRequirement = true;
        }

        if (currentToolIsCorrect && currentToolMeetsSilkTouchRequirement) {
            return null;
        }

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isCorrectToolForDrops(blockState)) {
                double speed = itemStack.getDestroySpeed(blockState);
                boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) > 0;
                if (requiresSilkTouch && hasSilkTouch) {
                    suitableItems.add(new ToolInfo(itemStack, i, speed, hasSilkTouch));
                } else if (!requiresSilkTouch) {
                    suitableItems.add(new ToolInfo(itemStack, i, speed, hasSilkTouch));
                }
            }
        }
        if (suitableItems.isEmpty()) return new ToolInfo(ItemStack.EMPTY, -1, 0, false);

        sortTools(suitableItems, prefersSilkTouch);
        return suitableItems.get(0);
    }

    public void swapEntityTool(LivingEntity entity) {

        WeaponInfo bestWeapon = findBestWeaponForAttackingEntity(entity);
        if (bestWeapon == null) return;

        if (!bestWeapon.itemStack.isEmpty()) {
            int selectedSlot = player.getInventory().selected;
            if (selectedSlot != bestWeapon.slot) {
                ItemStack selectedStack = player.getInventory().getSelected().copy();
                if (!selectedStack.isEmpty()) {
                    //InventoryPackets.sendToServer(selectedSlot, bestWeapon.itemStack.copy());
                    player.getInventory().setItem(selectedSlot, bestWeapon.itemStack.copy());
                    container.setItem(bestWeapon.slot, selectedStack.copy());
                }
            }
        }
    }

    public WeaponInfo findBestWeaponForAttackingEntity(LivingEntity entity) {

        NonNullList<WeaponInfo> suitableItems = NonNullList.create();

        // Is player's current tool good enough?
        ItemStack currentItem = player.getInventory().getSelected();
        double currentItemDamage = getAttackDamage(currentItem, entity);

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.canPerformAction(ToolActions.SWORD_SWEEP) || itemStack.is(ItemTags.SWORDS) || itemStack.is(ItemTags.AXES)) {
                double itemDamage = getAttackDamage(itemStack, entity);
                if (itemDamage > currentItemDamage) {
                    suitableItems.add(new WeaponInfo(itemStack, i, itemDamage));
                }
            }
        }
        if (suitableItems.isEmpty()) return new WeaponInfo(ItemStack.EMPTY, -1, 0);

        sortWeapons(suitableItems);
        return suitableItems.get(0);

    }

    public static double getAttackDamage(ItemStack weapon, LivingEntity target) {
        double baseDamage = getBaseAttackDamage(weapon);
        double enchantmentDamage = calculateEnchantmentDamage(weapon, target);
        return baseDamage + enchantmentDamage;
    }

    public static double getBaseAttackDamage(ItemStack itemStack) {
        return itemStack.getItem().getAttributeModifiers(itemStack, EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream()
                .mapToDouble(AttributeModifier::getAmount).sum();
    }

    public static double calculateEnchantmentDamage(ItemStack weapon, LivingEntity target) {
        int sharpnessLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, weapon);
        int smiteLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, weapon);
        int baneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, weapon);

        double additionalDamage = 0.0;

        // Sharpness adds 1 extra damage for the first level, and 0.5 for each additional level.
        if (sharpnessLevel > 0) {
            additionalDamage += 1.0 + (sharpnessLevel - 1) * 0.5;
        }

        // Check if the target is undead for Smite.
        if (smiteLevel > 0 && target.getMobType() == MobType.UNDEAD) {
            additionalDamage += smiteLevel * 2.5;  // Each level adds 2.5 extra damage.
        }

        // Check if the target is an arthropod for Bane of Arthropods.
        if (baneLevel > 0 && target.getType() == EntityType.SPIDER || target.getType() == EntityType.CAVE_SPIDER || target.getType() == EntityType.SILVERFISH || target.getType() == EntityType.ENDERMITE) {
            additionalDamage += baneLevel * 2.5;  // Each level adds 2.5 extra damage.
        }

        return additionalDamage;
    }

    public static void sortTools(NonNullList<ToolInfo> tools, boolean sortSilkTouchFirst) {
        Collections.sort(tools, new Comparator<ToolInfo>() {
            @Override
            public int compare(ToolInfo o1, ToolInfo o2) {
                // Conditionally compare based on silkTouch if sortSilkTouchFirst is true
                if (sortSilkTouchFirst) {
                    int silkTouchComparison = Boolean.compare(o2.silkTouch, o1.silkTouch);
                    if (silkTouchComparison != 0) {
                        return silkTouchComparison;
                    }
                }
                // Compare by speed - higher speed first
                return Double.compare(o2.speed, o1.speed);
            }
        });
    }


    public static void sortWeapons(NonNullList<WeaponInfo> tools) {
        Collections.sort(tools, new Comparator<WeaponInfo>() {
            @Override
            public int compare(WeaponInfo o1, WeaponInfo o2) {
                // Compare by speed - higher speed first
                return Double.compare(o2.damage, o1.damage);
            }
        });
    }
    public boolean requiresSilkTouch(BlockState blockState) {
        Block block = blockState.getBlock();
        return block == Blocks.GLASS || block == Blocks.DEAD_BUSH || block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE || block == Blocks.BEE_NEST;
    }
    public boolean prefersSilkTouch(BlockState blockState) {
        Block block = blockState.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.PODZOL || block == Blocks.CLAY || block == Blocks.GRAVEL
                || block == Blocks.SNOW_BLOCK || block == Blocks.SNOW || block == Blocks.GLOWSTONE || block == Blocks.STONE
                || block == Blocks.SEA_LANTERN || block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE || block == Blocks.NETHER_GOLD_ORE
                || block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.GILDED_BLACKSTONE || block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE || block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE || block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE;
    }
}

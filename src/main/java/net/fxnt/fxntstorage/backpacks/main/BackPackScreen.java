package net.fxnt.fxntstorage.backpacks.main;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Rectangle;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.upgrades.UpgradeItem;
import net.fxnt.fxntstorage.backpacks.util.BackPackNetworkHelper;
import net.fxnt.fxntstorage.init.ModItems;
import net.fxnt.fxntstorage.init.ModTags;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BackPackScreen extends AbstractContainerScreen<BackPackMenu> {
    private static Container container;
    private final int containerSlots = BackPackBlock.getContainerSlotCount();
    private final int toolSlots = BackPackBlock.getToolSlotCount();
    private final int upgradeSlots = BackPackBlock.getUpgradeSlotCount();
    private final int totalSlots = BackPackBlock.getSlotCount();
    private final int containerColumns = 12;
    private int containerRows = 5;
    private final int totalRows = (int) Math.ceil((double) containerSlots/containerColumns);
    private final int toolSlotColumns = 12;
    private final int toolSlotRows = 2;
    private final int upgradeSlotColumns = 1;
    private final int upgradeSlotRows = 6;
    private final int containerSlotsMinX = 29;
    private final int containerSlotsMaxX = containerSlotsMinX + (Util.SLOT_SIZE * containerColumns);
    private final int containerSlotsMinZ = 17;
    private int containerSlotsMaxZ = containerSlotsMinX + (Util.SLOT_SIZE * containerRows);
    private int containerSlotsHeight = containerSlotsMaxZ - containerSlotsMinZ;
    private final int scrollBarMinX = containerSlotsMaxX + 4;
    private final int scrollBarMaxX = scrollBarMinX + 14;
    private final int scrollBarMinZ = containerSlotsMinZ;
    private int scrollBarMaxZ = containerSlotsMaxZ;
    private final int upgradeSlotsMinX = 7;
    private final int upgradeSlotsMaxX = upgradeSlotsMinX + Util.SLOT_SIZE;
    private final int upgradeSlotsMinZ = containerSlotsMinZ;
    private final int upgradeSlotsMaxZ = upgradeSlotsMinZ + (Util.SLOT_SIZE * upgradeSlotRows);
    private final int toolSlotsMinX = containerSlotsMinX;
    private final int toolSlotsMaxX = containerSlotsMaxX;
    private int toolSlotsMinZ = containerSlotsMaxZ + 4;
    private int toolSlotsMaxZ = toolSlotsMinZ + (Util.SLOT_SIZE * toolSlotRows);

    private final int inventorySlotsMinX = 60;
    private final int inventorySlotsMaxX = inventorySlotsMinX + (Util.SLOT_SIZE * 9);
    private int inventorySlotsMinZ = toolSlotsMaxZ + 15;
    private int inventorySlotsMaxZ = inventorySlotsMinZ + (Util.SLOT_SIZE * 3);
    private final int hotbarSlotsMinX = inventorySlotsMinX;
    private final int hotbarSlotsMaxX = inventorySlotsMaxX;
    private int hotbarSlotsMinZ = inventorySlotsMaxZ + 4;
    private int hotbarSlotsMaxZ = hotbarSlotsMinZ + Util.SLOT_SIZE;
    private final int scrollThumbMinX = 270;
    private final int scrollThumbMaxX = scrollThumbMinX + 12;
    private final int scrollThumbMinZ = 0;
    private final int scrollThumbMaxZ = 15;
    private int scrollThumbY = 0;
    private int topVisibleRow, scrollYOffset;
    private final int scrollThumbWidth = scrollThumbMaxX - scrollThumbMinX;
    private final int scrollThumbHeight = scrollThumbMaxZ - scrollThumbMinZ;
    private boolean isDragging;
    private int inventoryTextOffset = 11;

    private int containerExclusionZoneMinX, containerExclusionZoneMaxX, containerExclusionZoneMinZ, containerExclusionZoneMaxZ, containerExclusionZoneWidth, containerExclusionZoneHeight;
    private int inventoryExclusionZoneMinX, inventoryExclusionZoneMaxX, inventoryExclusionZoneMinZ, inventoryExclusionZoneMaxZ, inventoryExclusionZoneWidth, inventoryExclusionZoneHeight;
    private final ResourceLocation guiTexture5 = new ResourceLocation(FXNTStorage.MOD_ID,  "textures/gui/container/back_pack_screen_5.png");
    private final ResourceLocation guiTexture7 = new ResourceLocation(FXNTStorage.MOD_ID,  "textures/gui/container/back_pack_screen_7.png");
    private final ResourceLocation guiTexture9 = new ResourceLocation(FXNTStorage.MOD_ID,  "textures/gui/container/back_pack_screen_9.png");
    private final int guiTexture5Height = 240;
    private final int guiTexture5Rows = 5;
    private final int guiTexture7Height = 281;
    private final int guiTexture7Rows = 7;
    private final int guiTexture9Height = 317;
    private final int guiTexture9Rows = 9;
    private ResourceLocation guiTexture = guiTexture5;
    private int textureHeight = guiTexture5Height;
    private final int textureWidth = 282;
    //private final ResourceLocation helperTexture = new ResourceLocation(FXNTStorage.MOD_ID,  "textures/gui/container/helper_texture.png");
    //private final ResourceLocation helperTexture2 = new ResourceLocation(FXNTStorage.MOD_ID,  "textures/gui/container/helper_texture_2.png");
    private static boolean ctrlKeyDown = false;
    @Nullable
    public static BackPackScreen createScreen(BackPackMenu menu, Inventory playerInventory, Component title) {
        return new BackPackScreen(menu, playerInventory, title);
    }
    public BackPackScreen(BackPackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        container = menu.getInventory();
        width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        updateGuiTextureSize(width, height);
    }

    @Override
    public void resize(Minecraft minecraft, int winWidth, int winHeight) {
        updateGuiTextureSize(winWidth, winHeight);
        super.resize(minecraft, winWidth, winHeight);
    }

    private void updateGuiTextureSize(int winWidth, int winHeight) {
        // If screen height too small, then provide smaller screen layout (less rows)
        width = winWidth;
        height = winHeight;
        imageWidth = textureWidth - scrollThumbWidth;
        if (winHeight >= guiTexture9Height) {
            guiTexture = guiTexture9;
            textureHeight = guiTexture9Height;
            containerRows = guiTexture9Rows;
        } else if (winHeight >= guiTexture7Height) {
            guiTexture = guiTexture7;
            textureHeight = guiTexture7Height;
            containerRows = guiTexture7Rows;
        } else {
            guiTexture = guiTexture5;
            textureHeight = guiTexture5Height;
            containerRows = guiTexture5Rows;
        }
        imageHeight = textureHeight;
        containerSlotsMaxZ = containerSlotsMinZ + (Util.SLOT_SIZE * containerRows);
        scrollBarMaxZ = containerSlotsMaxZ;
        containerSlotsHeight = containerSlotsMaxZ - containerSlotsMinX;

        if (guiTexture == guiTexture5) {
            toolSlotsMinZ = containerSlotsMaxZ + 2;
            toolSlotsMaxZ = toolSlotsMinZ + (Util.SLOT_SIZE * toolSlotRows);
            inventorySlotsMinZ = toolSlotsMaxZ + 12;
            inventorySlotsMaxZ = inventorySlotsMinZ + (Util.SLOT_SIZE * 3);
            hotbarSlotsMinZ = inventorySlotsMaxZ + 4;
            hotbarSlotsMaxZ = hotbarSlotsMinZ + Util.SLOT_SIZE;
            inventoryTextOffset = 9;
        } else {
            toolSlotsMinZ = containerSlotsMaxZ + 4;
            toolSlotsMaxZ = toolSlotsMinZ + (Util.SLOT_SIZE * toolSlotRows);
            inventorySlotsMinZ = toolSlotsMaxZ + 15;
            inventorySlotsMaxZ = inventorySlotsMinZ + (Util.SLOT_SIZE * 3);
            hotbarSlotsMinZ = inventorySlotsMaxZ + 4;
            hotbarSlotsMaxZ = hotbarSlotsMinZ + Util.SLOT_SIZE;
            inventoryTextOffset = 11;
        }
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        containerExclusionZoneMinX = leftPos;
        containerExclusionZoneMinZ = topPos;
        containerExclusionZoneWidth = textureWidth - scrollThumbWidth;
        containerExclusionZoneHeight = toolSlotsMaxZ + 12;

        containerExclusionZoneMaxX = containerExclusionZoneMinX + containerExclusionZoneWidth;
        containerExclusionZoneMaxZ = containerExclusionZoneMinZ + containerExclusionZoneHeight;

        inventoryExclusionZoneMinX = leftPos + inventorySlotsMinX - 6;
        inventoryExclusionZoneMinZ = topPos + inventorySlotsMinZ - 6;
        inventoryExclusionZoneWidth = inventorySlotsMaxX - inventorySlotsMinX + 12;
        inventoryExclusionZoneHeight = hotbarSlotsMaxZ - inventorySlotsMinZ + 12;

        inventoryExclusionZoneMaxX = inventoryExclusionZoneMinX + inventoryExclusionZoneWidth;
        inventoryExclusionZoneMaxZ = inventoryExclusionZoneMinZ + inventoryExclusionZoneHeight;
        initializeSlots();
    }

    @Override
    protected void init() {
        super.init();
        isDragging = false;
    }

    private void initializeSlots() {

        int index = 0;
        for (int y = 0; y < totalRows; y++) {
            int yOffset = containerSlotsMinZ + (y * Util.SLOT_SIZE) + 1;
            int scrollSlotYOffset = y >= containerRows ? -2000 : yOffset;

            for (int x = 0; x < containerColumns; x++) {
                int xOffset = containerSlotsMinX + (x  * Util.SLOT_SIZE) + 1;
                Slot slot = menu.getSlot(index);
                slot.x = xOffset;
                slot.y = scrollSlotYOffset;
                index++;
                if (index == containerSlots) break;
            }
        }


        for (int y = 0; y < toolSlotRows; y++) {
            int yOffset = toolSlotsMinZ + (y * Util.SLOT_SIZE) + 1;
            for (int x = 0; x < toolSlotColumns; x++) {
                int xOffset = toolSlotsMinX + (x  * Util.SLOT_SIZE) + 1;
                Slot slot = menu.getSlot(index);
                slot.x = xOffset;
                slot.y = yOffset;
                index++;
                if (index == containerSlots + toolSlots) break;
            }
        }


        for (int y = 0; y < upgradeSlotRows; y++) {
            int yOffset = upgradeSlotsMinZ + (y * Util.SLOT_SIZE) + 1;
            for (int x = 0; x < upgradeSlotColumns; x++) {
                int xOffset = upgradeSlotsMinX + (x  * Util.SLOT_SIZE) + 1;
                Slot slot = menu.getSlot(index);
                slot.x = xOffset;
                slot.y = yOffset;
                index++;
                if (index == totalSlots) break;
            }
        }

        index = 0;
        for (int y = 0; y < 3; y++) {
            int yOffset = inventorySlotsMinZ + (y * Util.SLOT_SIZE) + 1;
            for (int x = 0; x < 9; x++) {
                int xOffset = inventorySlotsMinX + (x  * Util.SLOT_SIZE) + 1;
                Slot slot = menu.getPlayerSlot(index);
                slot.x = xOffset;
                slot.y = yOffset;
                index++;
            }
        }

        index = 0;
        for (int x = 0; x < 9; x++) {
            int xOffset = hotbarSlotsMinX + (x  * Util.SLOT_SIZE) + 1;
            Slot slot = menu.getHotbarSlot(index);
            slot.x = xOffset;
            slot.y = hotbarSlotsMinZ + 1;
            index++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //graphics.blit(helperTexture, containerExclusionZoneMinX, containerExclusionZoneMinZ, 0, 0, containerExclusionZoneWidth, containerExclusionZoneHeight, 16, 16);
        //graphics.blit(helperTexture2, inventoryExclusionZoneMinX, inventoryExclusionZoneMinZ, 0, 0, inventoryExclusionZoneWidth, inventoryExclusionZoneHeight, 16, 16);
        graphics.blit(guiTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight, textureWidth, textureHeight);
        graphics.blit(guiTexture, leftPos + scrollBarMinX + 1, getScrollThumbY(), scrollThumbMinX, scrollThumbMinZ, scrollThumbWidth, scrollThumbHeight, textureWidth, textureHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventorySlotsMinX, inventorySlotsMinZ-inventoryTextOffset, 0x404040, false);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {

        boolean clickedInside = false;
        double containerLeft = containerExclusionZoneMinX;
        double containerRight = containerExclusionZoneMaxX;
        double containerTop = containerExclusionZoneMinZ;
        double containerBottom = containerExclusionZoneMaxZ;

        if (mouseX > containerLeft && mouseX < containerRight && mouseY > containerTop && mouseY < containerBottom
        ) {
            clickedInside = true;
        }

        double inventoryLeft = inventoryExclusionZoneMinX;
        double inventoryRight = inventoryExclusionZoneMaxX;
        double inventoryTop = inventoryExclusionZoneMinZ;
        double inventoryBottom = inventoryExclusionZoneMaxZ;

        if (mouseX > inventoryLeft && mouseX < inventoryRight && mouseY > inventoryTop && mouseY < inventoryBottom
        ) {
            clickedInside = true;
        }

        if (!clickedInside) {
            return true;
        }

        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    private int getScrollThumbY() {
        return topPos + scrollBarMinZ + 1 + scrollThumbY;
    }

    private void updateThumbPosition(double adjustedMouseY) {
        scrollThumbY = (int) Math.min(Math.max(adjustedMouseY, 0), containerSlotsHeight - scrollThumbHeight - 2);
        int row = (int) Math.round(((double) scrollThumbY) / (containerSlotsHeight - scrollThumbHeight - 2) * (totalRows - containerRows));
        this.setTopRow(topVisibleRow, row);
    }

    private void snapThumbToGradation() {
        scrollThumbY = (int) (((double) topVisibleRow / (totalRows - containerRows)) * (containerRows * Util.SLOT_SIZE - 2 - scrollThumbHeight));
    }

    private void setTopRow(int oldTopRow, int newTopRow) {
        if (oldTopRow == newTopRow) return;  // No change in row

        topVisibleRow = newTopRow;  // Update the current top visible row
        boolean atBottom = newTopRow + containerRows > totalRows; // Check if the new position is at or extends beyond the bottom
        int rowsToMove = newTopRow - oldTopRow;

        // Base offset for y coordinate
        int yOffsetBase = containerSlotsMinZ + 1;

        // Determine the number of slots to update
        int numSlotsToUpdate = containerColumns * containerRows;

        // Calculate starting indexes for slots
        int oldStartIndex = oldTopRow * containerColumns;
        int newStartIndex = newTopRow * containerColumns;

        // Clear old visible range
        for (int index = oldStartIndex; index < oldStartIndex + numSlotsToUpdate; index++) {
            menu.slots.get(index).y = -2000;
        }

        // New range to set slots
        int newRangeEnd = newStartIndex + numSlotsToUpdate;
        if (atBottom) {
            // Adjust end index if at the bottom to not exceed total slots
            newRangeEnd = Math.min(newRangeEnd, containerSlots);
        }

        // Apply new y-offset to the new range of slots
        for (int index = newStartIndex; index < newRangeEnd; index++) {
            int row = (index / containerColumns) - newTopRow;  // Calculate row relative to the new top row
            int yOffset = yOffsetBase + row * Util.SLOT_SIZE;  // Calculate the y-offset for each slot
            menu.slots.get(index).y = yOffset;
        }
    }
    private void setTopRowAndMoveThumb(int oldTopRow, int newTopRow) {
        this.setTopRow(oldTopRow, newTopRow);
        this.snapThumbToGradation();
    }
    private boolean isMouseOverScrollArea(double mouseX, double mouseY) {
        return mouseX >= leftPos + containerSlotsMinX && mouseX <= leftPos + scrollBarMaxX && mouseY >= topPos + containerSlotsMinZ && mouseY <= topPos + scrollBarMaxZ;
    }
    private boolean isMouseOverScrollBar(double mouseX, double mouseY) {
        return mouseX >= leftPos + scrollBarMinX && mouseX <= leftPos + scrollBarMaxX && mouseY >= topPos + scrollBarMinZ && mouseY <= topPos + scrollBarMaxZ;
    }
    private boolean isMouseOverScrollThumb(double mouseX, double mouseY) {
        return mouseX >= leftPos + scrollBarMinX && mouseX <= leftPos + scrollBarMaxX && mouseY >= getScrollThumbY() && mouseY <= getScrollThumbY() + scrollThumbHeight;
    }

    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) {
            ctrlKeyDown = true;
        }
        if(this.handleKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) {
            ctrlKeyDown = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    protected boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            if (topVisibleRow != totalRows - containerRows) {
                if (hasShiftDown()) {
                    this.setTopRowAndMoveThumb(topVisibleRow, Math.min(topVisibleRow + containerRows, totalRows - containerRows));
                } else {
                    this.setTopRowAndMoveThumb(topVisibleRow, topVisibleRow + 1);
                }
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            if (topVisibleRow != 0) {
                if (hasShiftDown()) {
                    this.setTopRowAndMoveThumb(topVisibleRow, Math.max(topVisibleRow - containerRows, 0));
                } else {
                    this.setTopRowAndMoveThumb(topVisibleRow, topVisibleRow - 1);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOverScrollThumb(mouseX, mouseY) && button == 0) {
            scrollYOffset = (int) mouseY - scrollThumbY;
            isDragging = true;
        } else if (this.isMouseOverScrollBar(mouseX, mouseY) && button == 0) {
            this.updateThumbPosition(mouseY - Util.CONTAINER_HEADER_HEIGHT - 1 - topPos);
            this.snapThumbToGradation();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            this.updateThumbPosition(mouseY - scrollYOffset);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging) {
            isDragging = false;
            this.snapThumbToGradation();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (super.mouseScrolled(mouseX, mouseY, deltaY)) {
            return true;
        }
        if (this.isMouseOverScrollArea(mouseX, mouseY)) {
            int newTop;
            if (deltaY < 0) {
                newTop = Math.min(topVisibleRow + (hasShiftDown() ? containerRows : 1), totalRows - containerRows);
            } else {
                newTop = Math.max(topVisibleRow - (hasShiftDown() ? containerRows : 1), 0);
            }
            this.setTopRowAndMoveThumb(topVisibleRow, newTop);
            return true;
        }
        return false;
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slotId >= Util.UPGRADE_SLOT_START_RANGE && slotId < Util.UPGRADE_SLOT_END_RANGE) {
            if(slot.getItem().is(ModTags.BACK_PACK_UPGRADE) && slot.getItem().getItem() instanceof UpgradeItem upgradeItem) {
                if (ctrlKeyDown) {
                    String itemName = upgradeItem.getUpgradeName();
                    String baseItemName = itemName
                            .replace("back_pack_", "")
                            .replace("_upgrade", "")
                            .replace("_deactivated", "");
                    ItemStack itemStack = menu.slots.get(slotId).getItem();
                    if (!itemStack.isEmpty()) {
                        if (itemName.contains("_deactivated")) {
                            itemStack = switch (baseItemName) {
                                case "magnet" -> new ItemStack(ModItems.BACK_PACK_MAGNET_UPGRADE);
                                case "pickblock" -> new ItemStack(ModItems.BACK_PACK_PICKBLOCK_UPGRADE);
                                case "itempickup" -> new ItemStack(ModItems.BACK_PACK_ITEMPICKUP_UPGRADE);
                                case "flight" -> new ItemStack(ModItems.BACK_PACK_FLIGHT_UPGRADE);
                                case "refill" -> new ItemStack(ModItems.BACK_PACK_REFILL_UPGRADE);
                                case "feeder" -> new ItemStack(ModItems.BACK_PACK_FEEDER_UPGRADE);
                                case "toolswap" -> new ItemStack(ModItems.BACK_PACK_TOOLSWAP_UPGRADE);
                                case "falldamage" -> new ItemStack(ModItems.BACK_PACK_FALLDAMAGE_UPGRADE);
                                default -> ItemStack.EMPTY;
                            };
                        } else {
                            itemStack = switch (baseItemName) {
                                case "magnet" -> new ItemStack(ModItems.BACK_PACK_MAGNET_UPGRADE_DEACTIVATED);
                                case "pickblock" -> new ItemStack(ModItems.BACK_PACK_PICKBLOCK_UPGRADE_DEACTIVATED);
                                case "itempickup" -> new ItemStack(ModItems.BACK_PACK_ITEMPICKUP_UPGRADE_DEACTIVATED);
                                case "flight" -> new ItemStack(ModItems.BACK_PACK_FLIGHT_UPGRADE_DEACTIVATED);
                                case "refill" -> new ItemStack(ModItems.BACK_PACK_REFILL_UPGRADE_DEACTIVATED);
                                case "feeder" -> new ItemStack(ModItems.BACK_PACK_FEEDER_UPGRADE_DEACTIVATED);
                                case "toolswap" -> new ItemStack(ModItems.BACK_PACK_TOOLSWAP_UPGRADE_DEACTIVATED);
                                case "falldamage" -> new ItemStack(ModItems.BACK_PACK_FALLDAMAGE_UPGRADE_DEACTIVATED);
                                default -> ItemStack.EMPTY;
                            };
                        }
                        if (!itemStack.isEmpty()) {
                            BackPackNetworkHelper.sendToServer(slotId, itemStack.copyWithCount(slot.getItem().getCount()), menu.backPackType, menu.blockPos);
                        }
                        return;
                    }
                } else {
                    // If try to pickup deactivated item, then toggle back to activated version
                    String itemName = upgradeItem.getUpgradeName();
                    if (itemName.contains("_deactivated")) {
                        String baseItemName = itemName
                                .replace("back_pack_", "")
                                .replace("_upgrade", "")
                                .replace("_deactivated", "");
                        ItemStack itemStack = switch (baseItemName) {
                            case "magnet" -> new ItemStack(ModItems.BACK_PACK_MAGNET_UPGRADE);
                            case "pickblock" -> new ItemStack(ModItems.BACK_PACK_PICKBLOCK_UPGRADE);
                            case "itempickup" -> new ItemStack(ModItems.BACK_PACK_ITEMPICKUP_UPGRADE);
                            case "flight" -> new ItemStack(ModItems.BACK_PACK_FLIGHT_UPGRADE);
                            case "refill" -> new ItemStack(ModItems.BACK_PACK_REFILL_UPGRADE);
                            case "feeder" -> new ItemStack(ModItems.BACK_PACK_FEEDER_UPGRADE);
                            case "toolswap" -> new ItemStack(ModItems.BACK_PACK_TOOLSWAP_UPGRADE);
                            case "falldamage" -> new ItemStack(ModItems.BACK_PACK_FALLDAMAGE_UPGRADE);
                            default -> ItemStack.EMPTY;
                        };
                        BackPackNetworkHelper.sendToServer(slotId, itemStack.copyWithCount(slot.getItem().getCount()), menu.backPackType, menu.blockPos);
                    }
                }
            }
        }
        super.slotClicked(slot, slotId, mouseButton, type);
    }

    @NotNull
    @ApiStatus.OverrideOnly
    public List<Rect2i> getExclusionZones() {
        return Arrays.asList(
                new Rect2i(containerExclusionZoneMinX, containerExclusionZoneMinZ, containerExclusionZoneWidth, containerExclusionZoneHeight),
                new Rect2i(inventoryExclusionZoneMinX, inventoryExclusionZoneMinZ, inventoryExclusionZoneWidth, inventoryExclusionZoneHeight)
        );
    }

    public Collection<Rectangle> getREIExclusionZones() {
        return Arrays.asList(
                new Rectangle(containerExclusionZoneMinX, containerExclusionZoneMinZ, containerExclusionZoneWidth, containerExclusionZoneHeight),
                new Rectangle(inventoryExclusionZoneMinX, inventoryExclusionZoneMinZ, inventoryExclusionZoneWidth, inventoryExclusionZoneHeight)
        );
    }

}

package net.fxnt.fxntstorage.containers.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class TexturedRect {
    private final ResourceLocation texture;
    private final int x, y, width, height, textureX, textureY, textureWidth, textureHeight;

    public TexturedRect(ResourceLocation texture, int x, int y, int width, int height, int textureX, int textureY, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public void render(GuiGraphics graphics) {
        graphics.blit(texture, x, y, textureX, textureY, width, height, textureWidth, textureHeight);
    }
}

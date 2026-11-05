package com.v1mod.client;

import com.v1mod.network.InputPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Eingabefenster des V1 Blocks (wie beim Command Block). */
public class V1InputScreen extends Screen {
    private final BlockPos pos;
    private EditBox box;

    public V1InputScreen(BlockPos pos) {
        super(Component.translatable("gui.v1mod.terminal"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        box = new EditBox(this.font, this.width / 2 - 150, this.height / 2 - 10, 300, 20, Component.translatable("gui.v1mod.terminal"));
        box.setMaxLength(64);
        box.setHint(Component.translatable("gui.v1mod.hint"));
        this.addRenderableWidget(box);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.v1mod.send"), b -> submit())
                .bounds(this.width / 2 - 100, this.height / 2 + 20, 200, 20).build());
        this.setInitialFocus(box);
    }

    private void submit() {
        String text = box.getValue();
        if (!text.isBlank()) {
            PacketDistributor.sendToServer(new InputPayload(pos, text));
        }
        this.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter / Numpad-Enter
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFB070FF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

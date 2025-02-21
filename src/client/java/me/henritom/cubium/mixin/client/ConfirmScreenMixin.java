package me.henritom.cubium.mixin.client;

import me.henritom.cubium.ui.impl.BrowserScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreen.class)
public abstract class ConfirmScreenMixin extends Screen {

    @Shadow protected abstract int getMessageY();

    @Shadow protected abstract int getMessagesHeight();

    @Shadow @Final private Text message;

    protected ConfirmScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addButton(CallbackInfo ci) {
        int y = MathHelper.clamp(this.getMessageY() + this.getMessagesHeight() + 20, this.height / 6 + 96, this.height - 24);
        String message = this.message.getString();

        if (BrowserScreen.specialSchemes.stream().anyMatch(message::contains)) {
            ButtonWidget button = ButtonWidget.builder(Text.translatable("cubium.ui.confirm.button"), (buttonWidget) -> {
                        if (this.client != null)
                            this.client.setScreen(new BrowserScreen(this, message));
                    })
                    .dimensions(this.width / 2 - 155, y + 30, 310, 20)
                    .build();

            this.addDrawableChild(button);
        }
    }
}

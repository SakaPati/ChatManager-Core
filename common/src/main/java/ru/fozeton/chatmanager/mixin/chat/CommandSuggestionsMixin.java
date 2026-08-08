package ru.fozeton.chatmanager.mixin.chat;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.fozeton.chatmanager.utils.HiddenCommand;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
    @Shadow
    @Final
    EditBox input;

    @ModifyVariable(method = "formatChat", at = @At("HEAD"), argsOnly = true)
    private String onHiddenPassword(String text) {
        if (HiddenCommand.isHidden(this.input.getValue())) {
            String[] parts = text.split(" ", -1);

            if (parts.length > 1) {
                StringBuilder masked = new StringBuilder(parts[0]);

                for (int i = 1; i < parts.length; i++) {
                    masked.append(" ");
                    masked.append("*".repeat(parts[i].length()));
                }
                return masked.toString();
            }
        }

        return text;
    }
}

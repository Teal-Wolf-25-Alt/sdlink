package com.hypherionmc.sdlink.mixin;

import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.accounts.DiscordAuthor;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessage;
import com.hypherionmc.sdlink.core.messaging.discord.DiscordMessageBuilder;
import com.hypherionmc.sdlink.util.ModUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/**
 * @author HypherionSA
 * Relay messages from other mods
 */
@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Z)V", at = @At("HEAD"))
    private void injectBroadcast(Component component, Function<ServerPlayer, Component> function, boolean bl, CallbackInfo ci) {
        String thread = Thread.currentThread().getStackTrace()[3].getClassName();

        if (thread.startsWith("net.minecraft") || thread.startsWith("com.hypherionmc"))
            return;

        if (SDLinkConfig.INSTANCE.ignoreConfig.enabled) {
            if (SDLinkConfig.INSTANCE.ignoreConfig.ignoredThread.stream().anyMatch(thread::startsWith))
                return;
        }

        if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
            SDLinkConstants.LOGGER.info("Relaying message from {}", thread);
        }

        try {
            DiscordMessage message = new DiscordMessageBuilder(MessageType.CHAT).author(DiscordAuthor.SERVER).message(ModUtils.resolve(component)).build();
            message.sendMessage();
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to broadcast message", e);
            }
        }

    }

}
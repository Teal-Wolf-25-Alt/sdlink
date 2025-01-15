package com.hypherionmc.sdlink.server.commands;

import com.hypherionmc.craterlib.api.commands.CraterCommand;
import com.hypherionmc.craterlib.api.events.server.CraterRegisterCommandEvent;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.util.SDLinkUtils;
import shadow.kyori.adventure.text.Component;

public class DiscordVerifyCommand {

    public static void register(CraterRegisterCommandEvent event) {
        CraterCommand cmd = CraterCommand.literal("discordverify")
                .requiresPermission(1)
                .withNode("sdlink.discord_verify")
                .execute(ctx -> {
                    if (!ctx.isPlayer() || ctx.getPlayer() == null) {
                        ctx.sendFailure(Component.text("This command can only be used by players"));
                        return 1;
                    }

                    if (!SDLinkConfig.INSTANCE.accessControl.enabled && !SDLinkConfig.INSTANCE.accessControl.optionalVerification) {
                        ctx.sendFailure(Component.text("Verification is not enabled for this server"));
                        return 1;
                    }

                    MinecraftAccount account = MinecraftAccount.of(ctx.getPlayer().getGameProfile());
                    SDLinkAccount sdLinkAccount = account.getStoredAccount();

                    if (sdLinkAccount == null) {
                        ctx.sendFailure(Component.text("Failed to load your account"));
                        return 1;
                    }

                    if (SDLinkUtils.isNullOrEmpty(sdLinkAccount.getVerifyCode())) {
                        int code = SDLinkUtils.intInRange(1000, 9999);
                        sdLinkAccount.setVerifyCode(String.valueOf(code));
                        DatabaseManager.INSTANCE.updateEntry(sdLinkAccount);
                        ctx.sendSuccess(() -> Component.text(SDLinkConfig.INSTANCE.accessControl.verificationMessages.optionalVerificationMessage.replace("{code}", String.valueOf(code))), false);
                    } else {
                        ctx.sendSuccess(() -> Component.text(SDLinkConfig.INSTANCE.accessControl.verificationMessages.optionalVerificationMessage.replace("{code}", String.valueOf(sdLinkAccount.getVerifyCode()))), false);
                    }
                    return 1;
                });

        event.registerCommand(cmd);
    }

}

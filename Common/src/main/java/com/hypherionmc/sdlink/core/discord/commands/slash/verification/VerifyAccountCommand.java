/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.verification;

import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public final class VerifyAccountCommand extends SDLinkSlashCommand {

    public VerifyAccountCommand() {
        super(false);
        this.name = "verify";
        this.help = "Verify your Minecraft account to access the server";

        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "code", "The verification code from the Minecraft Kick Message").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();


        int mcCode = event.getOption("code") != null ? event.getOption("code").getAsInt() : 0;

        if (mcCode == 0) {
            event.getHook().sendMessage("You need to provide a verification code").setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);

        if (accounts.isEmpty()) {
            event.getHook().sendMessage("Sorry, but this server does not contain any stored players in its database").setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        boolean didVerify = false;

        for (SDLinkAccount account : accounts) {
            if (account.getVerifyCode() == null)
                continue;

            if (accounts.stream().anyMatch(a -> a.getDiscordID() != null && a.getDiscordID().equals(event.getMember().getId())) && !SDLinkConfig.INSTANCE.accessControl.allowMultipleAccounts) {
                event.getHook().sendMessage("Sorry, you already have a verified account and this server does not allow multiple accounts").queue();
                return;
            }

            if (account.getVerifyCode().equalsIgnoreCase(String.valueOf(mcCode))) {
                MinecraftAccount minecraftAccount = MinecraftAccount.of(account);
                Result result = minecraftAccount.verifyAccount(event.getMember(), event.getGuild());
                event.getHook().sendMessage(result.getMessage()).setEphemeral(true).queue();
                didVerify = true;
                break;
            }
        }

        if (!didVerify)
            event.getHook().sendMessage("Sorry, we could not verify your Minecraft account. Please try again").setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
    }

}
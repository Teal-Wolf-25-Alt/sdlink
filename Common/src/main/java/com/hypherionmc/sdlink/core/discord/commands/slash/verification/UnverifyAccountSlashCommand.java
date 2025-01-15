/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.discord.commands.slash.verification;

import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public final class UnverifyAccountSlashCommand extends SDLinkSlashCommand {

    public UnverifyAccountSlashCommand() {
        super(false);
        this.name = "unverify";
        this.help = "Unverify your previously verified Minecraft account";
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();

        List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.findAll(SDLinkAccount.class);

        if (accounts.isEmpty()) {
            event.getHook().sendMessage("Sorry, but this server does not contain any stored players in its database").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.isFromGuild() ? event.getGuild() : (event.getJDA().getGuilds().isEmpty() ? null : event.getJDA().getGuilds().get(0));
        if (guild == null) {
            event.getHook().sendMessage("Sorry, I cannot find a discord server attached to this bot. Please report this to the server operator").setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        Member m = event.isFromGuild() ? event.getMember() : guild.getMemberById(event.getUser().getId());
        if (m == null) {
            event.getHook().sendMessage("Sorry, you do not seem to be a member of " + guild.getName() + ". Please try again").setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
            return;
        }

        boolean didUnverify = false;

        for (SDLinkAccount account : accounts) {
            if (account.getDiscordID() != null && account.getDiscordID().equalsIgnoreCase(m.getId())) {
                MinecraftAccount minecraftAccount = MinecraftAccount.of(account);
                Result result = minecraftAccount.unverifyAccount(m, guild);
                event.getHook().sendMessage(result.getMessage()).setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
                didUnverify = true;
                break;
            }
        }

        if (!didUnverify)
            event.getHook().sendMessage("Sorry, we could not un-verify your Minecraft account. Please try again").setEphemeral(SDLinkConfig.INSTANCE.botConfig.silentReplies).queue();
    }

}
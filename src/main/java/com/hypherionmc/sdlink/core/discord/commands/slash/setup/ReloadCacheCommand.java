package com.hypherionmc.sdlink.core.discord.commands.slash.setup;

import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class ReloadCacheCommand extends SDLinkSlashCommand {

    public ReloadCacheCommand() {
        super(true);
        this.name = "reloadcache";
        this.help = "Force reload the bot cache";
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {
        try {
            CacheManager.loadCache();
            slashCommandEvent.reply("Cache has been reloaded").setEphemeral(true).queue();
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to reload cache", e);
            slashCommandEvent.reply("Failed to reload cache. Please check your server log").setEphemeral(true).queue();
        }
    }

}
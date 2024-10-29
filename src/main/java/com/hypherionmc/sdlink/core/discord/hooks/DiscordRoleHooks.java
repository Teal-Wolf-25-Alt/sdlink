package com.hypherionmc.sdlink.core.discord.hooks;

import com.hypherionmc.sdlink.core.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.TriggerCommandsConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.core.messaging.Result;
import com.hypherionmc.sdlink.platform.SDLinkMCPlatform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordRoleHooks {

    public static final DiscordRoleHooks INSTANCE = new DiscordRoleHooks();

    public void onRoleAdded(@NotNull GuildMemberRoleAddEvent event) {
        if (!SDLinkConfig.INSTANCE.accessControl.enabled)
            return;

        try {
            List<SDLinkAccount> accounts = DatabaseManager.sdlinkDatabase.getCollection(SDLinkAccount.class);

            if (accounts.isEmpty())
                return;

            Optional<SDLinkAccount> account = accounts.stream().filter(d -> d.getDiscordID() != null && d.getDiscordID().equalsIgnoreCase(event.getMember().getId())).findFirst();

            account.ifPresent(acc -> {
                MinecraftAccount mcAccount = MinecraftAccount.of(acc);

                for (Role role : event.getRoles()) {
                    Optional<TriggerCommandsConfig.TriggerHolder> triggerHolder = SDLinkConfig.INSTANCE.triggerCommands.roleAdded.stream().filter(r -> r.discordRole.equalsIgnoreCase(role.getName()) || r.discordRole.equalsIgnoreCase(role.getId())).findFirst();
                    if (triggerHolder.isEmpty())
                        continue;

                    triggerHolder.get().minecraftCommand.forEach(cmd -> {
                        executeCommand(cmd.replace("%player%", mcAccount.getUsername()).replace("%role%", role.getName()));
                    });
                }
            });
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to run roleAdded trigger", e);
        }
    }

    public void onRoleRemoved(@NotNull GuildMemberRoleRemoveEvent event) {
        if (!SDLinkConfig.INSTANCE.accessControl.enabled)
            return;

        try {
            List<SDLinkAccount> accounts = DatabaseManager.sdlinkDatabase.getCollection(SDLinkAccount.class);

            if (accounts.isEmpty())
                return;

            Optional<SDLinkAccount> account = accounts.stream().filter(d -> d.getDiscordID() != null && d.getDiscordID().equalsIgnoreCase(event.getMember().getId())).findFirst();

            account.ifPresent(acc -> {
                MinecraftAccount mcAccount = MinecraftAccount.of(acc);

                for (Role role : event.getRoles()) {
                    Optional<TriggerCommandsConfig.TriggerHolder> triggerHolder = SDLinkConfig.INSTANCE.triggerCommands.roleRemoved.stream().filter(r -> r.discordRole.equalsIgnoreCase(role.getName()) || r.discordRole.equalsIgnoreCase(role.getId())).findFirst();
                    if (triggerHolder.isEmpty())
                        continue;

                    triggerHolder.get().minecraftCommand.forEach(cmd -> {
                        executeCommand(cmd.replace("%player%", mcAccount.getUsername()).replace("%role%", role.getName()));
                    });
                }
            });
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to run roleRemoved trigger", e);
        }
    }

    private static void executeCommand(String command) {
        CompletableFuture<Result> result = new CompletableFuture<>();
        SDLinkMCPlatform.INSTANCE.executeCommand(command, 4, SDLinkConfig.INSTANCE.channelsAndWebhooks.serverName, result);

        result.thenAccept(res -> {
           if (res.isError()) {
               BotController.INSTANCE.getLogger().error("Failed to trigger command {}: {}", command, res.getMessage());
           }
        });
    }
}

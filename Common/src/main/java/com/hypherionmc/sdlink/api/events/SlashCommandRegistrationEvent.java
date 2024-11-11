package com.hypherionmc.sdlink.api.events;

import com.hypherionmc.craterlib.core.event.CraterEvent;
import com.hypherionmc.sdlink.core.discord.commands.slash.SDLinkSlashCommand;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HypherionSA
 *
 * Event that is fired before Simple Discord Link registers its slash commands
 */
@Getter
public class SlashCommandRegistrationEvent extends CraterEvent {

    private final List<SDLinkSlashCommand> commands = new ArrayList<>();

    /**
     * Add your own slash command
     *
     * @param command A copy of your command class extending {@link SDLinkSlashCommand}
     */
    public void addCommand(SDLinkSlashCommand command) {
        commands.add(command);
    }

}

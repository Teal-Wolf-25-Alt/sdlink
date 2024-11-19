package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.database.HiddenPlayers;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Getter
public final class HiddenPlayersManager {

    public static final HiddenPlayersManager INSTANCE = new HiddenPlayersManager();
    private final HashMap<String, HiddenPlayers> hiddenPlayers = new LinkedHashMap<>();

    private HiddenPlayersManager() {}

    public void loadHiddenPlayers() {
        hiddenPlayers.clear();
        DatabaseManager.INSTANCE.getCollection(HiddenPlayers.class).forEach(p -> hiddenPlayers.put(p.getIdentifier(), p));
    }

    public Result hidePlayer(String identifier, String displayName, String type) {
        try {
            HiddenPlayers player = HiddenPlayers.of(identifier, displayName, type);
            DatabaseManager.INSTANCE.updateEntry(player);
            hiddenPlayers.put(identifier, player);
            return Result.success(displayName + " is now hidden");
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to hide player {}", displayName, e);
            return Result.error("Failed to hide player. Error: " + e.getMessage());
        }
    }

    public Result unhidePlayer(String identifier) {
        try {
            HiddenPlayers player = DatabaseManager.INSTANCE.findById(identifier, HiddenPlayers.class);

            if (player == null) {
                return Result.error("Player is not hidden");
            }

            hiddenPlayers.remove(identifier);
            DatabaseManager.INSTANCE.deleteEntry(player);
            return Result.success("Player " + player.getDisplayName() + " is no longer hidden");
        } catch (Exception e) {
            SDLinkConstants.LOGGER.error("Failed to unhide player {}", identifier, e);
            return Result.error("Failed to unhide player. Error: " + e.getMessage());
        }
    }

    public boolean isPlayerHidden(String identifier) {
        return hiddenPlayers.containsKey(identifier);
    }

}

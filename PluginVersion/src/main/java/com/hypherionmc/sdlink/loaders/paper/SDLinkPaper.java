package com.hypherionmc.sdlink.loaders.paper;

import com.hypherionmc.craterlib.api.events.server.CraterServerLifecycleEvent;
import com.hypherionmc.craterlib.core.platform.CommonPlatform;
import com.hypherionmc.craterlib.nojang.server.BridgedMinecraftServer;
import com.hypherionmc.sdlink.server.ServerEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class SDLinkPaper extends JavaPlugin {

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        // SDLink unloads before CraterLib does, so we can't receive these events.
        // Not that they are needed, but they are "faked" to keep feature parity with the modded versions
        BridgedMinecraftServer server = CommonPlatform.INSTANCE.getMCServer();
        ServerEvents.getInstance().onServerStopping(new CraterServerLifecycleEvent.Stopping(server));
        ServerEvents.getInstance().onServerStoppedEvent(new CraterServerLifecycleEvent.Stopped(server));
    }
}

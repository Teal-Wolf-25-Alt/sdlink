package com.hypherionmc.sdlink.loaders.paper;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.sdlink.server.ServerEvents;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

public class SDLinkPaperBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext bootstrapContext) {
        ServerEvents events = ServerEvents.getInstance();
        CraterEventBus.INSTANCE.registerEventListener(events);
    }
}

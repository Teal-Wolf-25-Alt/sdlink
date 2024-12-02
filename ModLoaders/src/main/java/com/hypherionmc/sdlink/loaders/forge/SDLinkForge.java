package com.hypherionmc.sdlink.loaders.forge;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.core.platform.ModloaderEnvironment;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.client.ClientEvents;
import com.hypherionmc.sdlink.compat.MModeCompat;
import com.hypherionmc.sdlink.compat.rolesync.RoleSync;
import com.hypherionmc.sdlink.compat.rolesync.impl.FTBRankSync;
import com.hypherionmc.sdlink.compat.rolesync.impl.LuckPermsSync;
import com.hypherionmc.sdlink.networking.SDLinkNetworking;
import com.hypherionmc.sdlink.server.ServerEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(SDLinkConstants.MOD_ID)
public final class SDLinkForge {

    public SDLinkForge() {
        SDLinkNetworking.registerPackets();

        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            ServerEvents events = ServerEvents.getInstance();
            CraterEventBus.INSTANCE.registerEventListener(events);

            if (ModloaderEnvironment.INSTANCE.isModLoaded("mmode")) {
                MModeCompat.init();
            }

            if (ModloaderEnvironment.INSTANCE.isModLoaded("ftbranks")) {
                CraterEventBus.INSTANCE.registerEventListener(FTBRankSync.INSTANCE);
            }

            if (ModloaderEnvironment.INSTANCE.isModLoaded("luckperms")) {
                CraterEventBus.INSTANCE.registerEventListener(LuckPermsSync.INSTANCE);
            }
        });

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientEvents::init);
    }
}

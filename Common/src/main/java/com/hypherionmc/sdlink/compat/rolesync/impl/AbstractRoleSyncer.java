package com.hypherionmc.sdlink.compat.rolesync.impl;

import com.hypherionmc.craterlib.nojang.world.entity.player.BridgedPlayer;
import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractRoleSyncer {

    private final Supplier<Boolean> isSyncActive;
    boolean ignoreEvent = false;

    public AbstractRoleSyncer(Supplier<Boolean> isSyncActive) {
        this.isSyncActive = isSyncActive;
    }

    public abstract void sync(BridgedPlayer p, List<Role> roles, Guild guild, Member member);

    public void discordRoleAddedToMember(Member member, Role role, Guild guild) {
        if (ignoreEvent || !isSyncActive.get())
            return;

        discordRoleChanged(member, guild, role, true);
    }

    public void discordRoleRemovedFromMember(Member member, Role role, Guild guild, MinecraftAccount oldAccount) {
        if (ignoreEvent || !isSyncActive.get())
            return;

        discordRoleChanged(member, guild, role, false, oldAccount);
    }

    void discordRoleChanged(Member member, Guild guild, Role role, boolean added) {
        this.discordRoleChanged(member, guild, role, added, null);
    }

    abstract void discordRoleChanged(Member member, Guild guild, Role role, boolean added, @Nullable MinecraftAccount oldAccount);

}

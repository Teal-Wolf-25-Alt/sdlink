/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.accounts;

import com.hypherionmc.craterlib.core.event.CraterEventBus;
import com.hypherionmc.craterlib.nojang.authlib.BridgedGameProfile;
import com.hypherionmc.sdlink.api.events.VerificationEvent;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.compat.rolesync.RoleSync;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.managers.CacheManager;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.core.managers.RoleManager;
import com.hypherionmc.sdlink.util.SDLinkUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author HypherionSA
 * Represents a Minecraft Account. Used for communication between this library and minecraft
 */
@Getter
public final class MinecraftAccount {

    private final String username;
    private final UUID uuid;
    //private final boolean isOffline;

    /**
     * Internal.
     *
     * @param username  The Username of the Player
     * @param uuid      The UUID of the player
     */
    private MinecraftAccount(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    /**
     * Convert a database account into a Minecraft Account
     * @param account The database entry
     */
    public static MinecraftAccount of(SDLinkAccount account) {
        return new MinecraftAccount(account.getUsername(), UUID.fromString(account.getUuid()));
    }

    /**
     * Convert a GameProfile into a MinecraftAccount for usage inside the mod
     *
     * @param profile The player GameProfile
     */
    public static MinecraftAccount of(BridgedGameProfile profile) {
        return new MinecraftAccount(profile.getName(), profile.getId());
    }

    public BridgedGameProfile toGameProfile() {
        return BridgedGameProfile.mojang(uuid, username);
    }

    @Nullable
    public static MinecraftAccount fromDiscordId(String discordId) {
        SDLinkAccount account = DatabaseManager.INSTANCE.getCollection(SDLinkAccount.class).stream().filter(a -> a.getDiscordID() != null && a.getDiscordID().equals(discordId)).findFirst().orElse(null);

        if (account == null) {
            return null;
        }

        return MinecraftAccount.of(account);
    }

    public boolean isAccountVerified() {
        SDLinkAccount account = getStoredAccount();

        if (account == null || account.getDiscordID() == null)
            return false;

        return !SDLinkUtils.isNullOrEmpty(account.getDiscordID());
    }

    public SDLinkAccount getStoredAccount() {
        SDLinkAccount account = DatabaseManager.INSTANCE.findById(this.uuid.toString(), SDLinkAccount.class);
        return account == null ? newDBEntry() : account;
    }

    @NotNull
    private SDLinkAccount newDBEntry() {
        SDLinkAccount account = new SDLinkAccount();
        account.setUsername(this.username);
        account.setUuid(this.uuid.toString());
        account.setInGameName(this.username);
        account.setDiscordID(null);
        account.setVerifyCode(null);
        account.setOffline(false);

        DatabaseManager.INSTANCE.updateEntry(account);

        return account;
    }

    @NotNull
    public String getDiscordName() {
        SDLinkAccount account = getStoredAccount();
        if (account == null || SDLinkUtils.isNullOrEmpty(account.getDiscordID()))
            return "Unlinked";

        DiscordUser user = getDiscordUser();

        return user == null ? "Unlinked" : user.getEffectiveName();
    }

    @Nullable
    public DiscordUser getDiscordUser() {
        SDLinkAccount storedAccount = getStoredAccount();
        if (storedAccount == null || SDLinkUtils.isNullOrEmpty(storedAccount.getDiscordID()))
            return null;

        if (CacheManager.getDiscordMembers().isEmpty())
            return null;

        Optional<Member> member = CacheManager.getDiscordMembers().stream().filter(m -> m.getId().equalsIgnoreCase(storedAccount.getDiscordID())).findFirst();
        return member.map(value -> DiscordUser.of(value.getEffectiveName(), value.getEffectiveAvatarUrl(), value.getIdLong(), value.getAsMention())).orElse(null);
    }

    public Result verifyAccount(Member member, Guild guild) {
        SDLinkAccount account = getStoredAccount();

        if (account == null)
            return Result.error("We couldn't find your Minecraft account. Please ask the staff for assistance");

        account.setDiscordID(member.getId());
        account.setVerifyCode(null);

        try {
            DatabaseManager.INSTANCE.updateEntry(account);
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to store verified account", e);
        }

        if (RoleManager.getVerifiedRole() != null) {
            try {
                guild.addRoleToMember(UserSnowflake.fromId(member.getId()), RoleManager.getVerifiedRole()).queue();
            } catch (Exception e) {
                BotController.INSTANCE.getLogger().error("Failed to add verified role to user", e);
            }
        }

        if (SDLinkConfig.INSTANCE.accessControl.changeDiscordNickname) {
            try {
                member.modifyNickname(account.getInGameName()).queue();
            } catch (Exception e) {
                BotController.INSTANCE.getLogger().error("Failed to update Nickname for {}", member.getEffectiveName(), e);
            }
        }

        CraterEventBus.INSTANCE.postEvent(new VerificationEvent.PlayerVerified(this));

        return Result.success("Your account has been verified");
    }

    public Result unverifyAccount(Member member, Guild guild) {
        SDLinkAccount account = getStoredAccount();

        if (account == null)
            return Result.error("We couldn't find your Minecraft account. Please ask the staff for assistance");

        MinecraftAccount oldAccount = this;
        account.setDiscordID(null);
        account.setVerifyCode(null);

        try {
            DatabaseManager.INSTANCE.updateEntry(account);
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to remove verified account", e);
        }

        if (RoleManager.getVerifiedRole() != null) {
            try {
                guild.removeRoleFromMember(UserSnowflake.fromId(member.getId()), RoleManager.getVerifiedRole()).queue();
            } catch (Exception e) {
                BotController.INSTANCE.getLogger().error("Failed to remove verified role from user", e);
            }
        }

        if (SDLinkConfig.INSTANCE.accessControl.changeDiscordNickname) {
            try {
                if (member.getNickname() != null && member.getNickname().equalsIgnoreCase(account.getInGameName())) {
                    member.modifyNickname(null).queue();
                }
            } catch (Exception e) {
                BotController.INSTANCE.getLogger().error("Failed to update Nickname for {}", member.getEffectiveName(), e);
            }
        }

        try {
            List<Role> roles = member.getRoles();

            for (Role role : roles) {
                RoleSync.INSTANCE.roleRemovedFromMember(member, role, guild, oldAccount);
            }
        } catch (Exception ignored) {}

        CraterEventBus.INSTANCE.postEvent(new VerificationEvent.PlayerUnverified(this));

        return Result.success("Your account has been un-verified");
    }

    public Result canLogin() {
        if (!SDLinkConfig.INSTANCE.accessControl.enabled)
            return Result.success("");

        SDLinkAccount account = getStoredAccount();

        if (account == null)
            return Result.error("Failed to load your account");

        if (!isAccountVerified()) {
            if (SDLinkUtils.isNullOrEmpty(account.getVerifyCode())) {
                int code = SDLinkUtils.intInRange(1000, 9999);
                account.setVerifyCode(String.valueOf(code));
                DatabaseManager.INSTANCE.updateEntry(account);
                return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.accountVerify.replace("{code}", String.valueOf(code)));
            } else {
                return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.accountVerify.replace("{code}", account.getVerifyCode()));
            }
        }

        Result result = checkAccessControl();

        if (result.isError()) {
            switch (result.getMessage()) {
                case "notFound" -> {
                    return Result.error("Account not found in server database");
                }
                case "noGuildFound" -> {
                    return Result.error("No Discord Server Found");
                }
                case "memberNotFound" -> {
                    return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.nonMember);
                }
                case "userCacheEmpty" -> {
                    return Result.error("The discord member cache of this server is empty. Please ask the server owner to run the reloadcache discord command");
                }
                case "rolesNotLoaded" -> {
                    return Result.error("Server has required roles configured, but no discord roles were loaded. Please notify the server owner");
                }
                case "accessDeniedByRole" -> {
                    return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.roleDenied);
                }
                case "rolesNotFound" -> {
                    return Result.error(SDLinkConfig.INSTANCE
                            .accessControl
                            .verificationMessages
                            .requireRoles
                            .replace("{roles}", ArrayUtils.toString(RoleManager.getVerificationRoles().stream().map(Role::getName).toList())));
                }
            }
        }

        return Result.success("");
    }

    public Result checkAccessControl() {
        if (!SDLinkConfig.INSTANCE.accessControl.enabled) {
            return Result.success("pass");
        }

        SDLinkAccount account = getStoredAccount();
        if (account == null)
            return Result.error("notFound");

        if (SDLinkUtils.isNullOrEmpty(account.getDiscordID()))
            return Result.error("notVerified");

        if (SDLinkConfig.INSTANCE.accessControl.requireDiscordMembership) {
            DiscordUser user = getDiscordUser();
            if (user == null)
                return Result.error("memberNotFound");
        }


        if (!SDLinkConfig.INSTANCE.accessControl.requiredRoles.isEmpty() || !SDLinkConfig.INSTANCE.accessControl.deniedRoles.isEmpty()) {
            AtomicBoolean anyFound = new AtomicBoolean(false);
            AtomicBoolean deniedFound = new AtomicBoolean(false);

            Optional<Member> member = CacheManager.getDiscordMembers().stream().filter(m -> m.getId().equals(account.getDiscordID())).findFirst();
            member.ifPresent(m -> m.getRoles().forEach(r -> {
                if (RoleManager.getDeniedRoles().stream().anyMatch(role -> r.getIdLong() == role.getIdLong())) {
                    if (!deniedFound.get())
                        deniedFound.set(true);
                }

                if (RoleManager.getVerificationRoles().stream().anyMatch(role -> role.getIdLong() == r.getIdLong())) {
                    if (!anyFound.get()) {
                        anyFound.set(true);
                    }
                }
            }));

            if (deniedFound.get() && !RoleManager.getDeniedRoles().isEmpty())
                return Result.error("accessDeniedByRole");

            if (!anyFound.get() && !RoleManager.getVerificationRoles().isEmpty())
                return Result.error("rolesNotFound");

            if (member.isEmpty())
                return Result.error("memberCacheEmpty");
        }

        return Result.success("pass");
    }

    public void banDiscordMember() {
        if (!SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan)
            return;

        DiscordUser user = getDiscordUser();

        if (user == null)
            return;

        try {
            BotController.INSTANCE.getJDA().getGuilds().get(0).ban(UserSnowflake.fromId(user.getUserId()), 7, TimeUnit.DAYS).queue();
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to ban discord member", e);
        }
    }
}

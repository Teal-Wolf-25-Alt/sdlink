/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.services.helpers;

import com.hypherionmc.sdlink.api.accounts.MinecraftAccount;
import com.hypherionmc.sdlink.api.messaging.Result;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author HypherionSA
 * Service to bridge communication between the Library and Minecraft
 */
public interface IMinecraftHelper {

    void discordMessageReceived(Member member, String message, @Nullable String replyMessage);

    Result checkWhitelisting();

    Pair<Integer, Integer> getPlayerCounts();

    List<MinecraftAccount> getOnlinePlayers();

    long getServerUptime();

    String getServerVersion();

    void executeMinecraftCommand(String command, int permLevel, MessageReceivedEvent event, @Nullable SDLinkAccount account, CompletableFuture<Result> replier);

    boolean isOnlineMode();

    void banPlayer(MinecraftAccount acc);
}

/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import club.minnced.discord.webhook.WebhookClient;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageChannelConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.api.messaging.MessageDestination;
import com.hypherionmc.sdlink.api.messaging.MessageType;
import com.hypherionmc.sdlink.core.messaging.SDLinkWebhookClientBuilder;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static club.minnced.discord.webhook.WebhookClientBuilder.WEBHOOK_PATTERN;

/**
 * @author HypherionSA
 * Load and cache Webhook clients for later use
 */
public final class WebhookManager {

    private static final HashMap<MessageDestination, WebhookClient> clientMap = new HashMap<>();
    private static final HashMap<MessageType, WebhookClient> overrides = new HashMap<>();
    private static WebhookClient chatWebhookClient, eventWebhookClient, consoleWebhookClient;
    private static final Pattern THREAD_PATTERN = Pattern.compile("thread_id=(\\d+)");

    /**
     * Load configured webhook clients
     * Webhooks that are not configured, will use their Channel ID instead
     */
    public static void init() {
        clientMap.clear();
        overrides.clear();

        if (SDLinkConfig.INSTANCE == null || !SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.enabled)
            return;

        if (!SDLinkConfig.INSTANCE.generalConfig.enabled)
            return;

        if (!SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.chatWebhook.isEmpty()) {
            chatWebhookClient = createClient(
                    "Chat",
                    EncryptionUtil.INSTANCE.decrypt(SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.chatWebhook)
            );
            BotController.INSTANCE.getLogger().info("Using Webhook for Chat Messages");
        }

        if (!SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.eventsWebhook.isEmpty()) {
            eventWebhookClient = createClient(
                    "Events",
                    EncryptionUtil.INSTANCE.decrypt(SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.eventsWebhook)
            );
            BotController.INSTANCE.getLogger().info("Using Webhook for Event Messages");
        }

        if (!SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.consoleWebhook.isEmpty()) {
            consoleWebhookClient = createClient(
                    "Console",
                    EncryptionUtil.INSTANCE.decrypt(SDLinkConfig.INSTANCE.channelsAndWebhooks.webhooks.consoleWebhook)
            );
            BotController.INSTANCE.getLogger().info("Using Webhook for Console Messages");
        }

        if (chatWebhookClient != null) {
            clientMap.put(MessageDestination.CHAT, chatWebhookClient);
        }

        clientMap.put(MessageDestination.EVENT, eventWebhookClient);
        clientMap.put(MessageDestination.CONSOLE, consoleWebhookClient);

        for (Map.Entry<MessageType, MessageChannelConfig.DestinationObject> d : CacheManager.messageDestinations.entrySet()) {
            String url = EncryptionUtil.INSTANCE.decrypt(d.getValue().override);
            if (!d.getValue().channel.isOverride() || d.getValue().override == null || !url.startsWith("http"))
                continue;

            if (overrides.containsKey(d.getKey()))
                continue;

            WebhookClient client = createClient(d.getKey().name() + " override", url);
            BotController.INSTANCE.getLogger().info("Using Webhook override for {} Messages", d.getKey().name());

            overrides.put(d.getKey(), client);
        }
    }

    @Nullable
    public static WebhookClient getOverride(MessageType type) {
        if (overrides.get(type) == null)
            return null;

        return overrides.get(type);
    }

    public static WebhookClient getWebhookClient(MessageDestination destination) {
        return clientMap.get(destination);
    }

    public static void shutdown() {
        if (chatWebhookClient != null) {
            chatWebhookClient.close();
        }
        if (eventWebhookClient != null) {
            eventWebhookClient.close();
        }
        if (consoleWebhookClient != null) {
            consoleWebhookClient.close();
        }

        overrides.forEach((k, v) -> {
            if (v != null)
                v.close();
        });
    }

    /**
     * Workaround to support ThreadID's in Webhook URLS
     * @param name The name of the Webhook Client
     * @param url The Webhook URL
     * @return The client with thread id set, if found
     */
    private static WebhookClient createClient(String name, String url) {
        Matcher threadMatcher = THREAD_PATTERN.matcher(url);
        Matcher webhookMatcher = WEBHOOK_PATTERN.matcher(url);

        if (threadMatcher.find() && webhookMatcher.find()) {
            return new SDLinkWebhookClientBuilder(
                    name,
                    String.format("https://discord.com/api/webhooks/%s/%s", webhookMatcher.group(1), webhookMatcher.group(2))
            ).setThreadChannelID(threadMatcher.group(1)).build();
        } else if (webhookMatcher.matches()) {
            return new SDLinkWebhookClientBuilder(name, String.format("https://discord.com/api/webhooks/%s/%s", webhookMatcher.group(1), webhookMatcher.group(2))).build();
        }

        return new SDLinkWebhookClientBuilder(name, url).build();
    }

    public static boolean isAppWebhook(long id) {
        return clientMap.values().stream().filter(Objects::nonNull).anyMatch(c -> c.getId() == id)
                || overrides.values().stream().filter(Objects::nonNull).anyMatch(c -> c.getId() == id);
    }
}

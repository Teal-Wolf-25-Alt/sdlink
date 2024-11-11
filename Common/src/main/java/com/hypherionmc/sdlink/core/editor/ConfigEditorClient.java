package com.hypherionmc.sdlink.core.editor;

import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.util.EncryptionUtil;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigEditorClient {

    public static final ConfigEditorClient INSTANCE = new ConfigEditorClient();

    public void openConnection() {
        String identifier = EncryptionUtil.getSaltString();

        try {
            WebSocket webSocket = new WebSocketFactory().createSocket("wss://editor.firstdark.dev/ws/config?identifier=" + identifier);
            webSocket.setPingInterval(10000);
            webSocket.addListener(new ConfigEditorWSEvents(identifier));
            webSocket.connect();
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to open connection to Config Editor", e);
        }
    }

}

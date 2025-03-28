package com.example.questplugin;

import org.bukkit.entity.Player;
import org.geysermc.geyser.api.GeyserApi;

public class BedrockHelper {
    public static boolean isBedrock(Player player) {
        return GeyserApi.api().connectedPlayers().stream()
            .anyMatch(session -> session.javaUuid().equals(player.getUniqueId()));
    }
}

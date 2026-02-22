package xyz.bannach.bnnch_sort.services;

import java.util.ServiceLoader;

public class Services {
    public static final INetworkHandler NETWORK =
        ServiceLoader.load(INetworkHandler.class)
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "No INetworkHandler found — is the correct loader jar present?"));

    public static final IPlayerDataService PLAYER_DATA =
        ServiceLoader.load(IPlayerDataService.class)
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "No IPlayerDataService found — is the correct loader jar present?"));
}

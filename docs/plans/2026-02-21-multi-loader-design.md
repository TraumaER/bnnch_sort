# Multi-Loader Support Design

**Date:** 2026-02-21
**Branch:** bannach/superpowers-multi-loader
**Status:** Approved

## Goal

Add Fabric support to Bnnch: Sort while sharing as much code as possible between loaders. Keep the mod dependency-free — no Architectury, no Cloth Config.

## Approach

A manual multi-project Gradle setup with three subprojects: `common`, `neoforge`, and `fabric`. Platform implementations are wired to common code at runtime via Java's `ServiceLoader`. The `common` module is not shipped as a standalone artifact; it is compiled into each platform jar at build time.

## Project Structure

```
bnnch_sort/
├── settings.gradle          # declares subprojects: common, neoforge, fabric
├── build.gradle             # shared Java 21 conventions and repositories
├── gradle.properties        # shared versions (MC, NeoForge, Fabric Loader, mod)
├── common/                  # platform-agnostic code (~73% of codebase)
│   ├── build.gradle
│   └── src/main/java/...
├── neoforge/                # NeoForge platform (~10 files)
│   ├── build.gradle
│   └── src/main/java/...
└── fabric/                  # Fabric platform (~10 files)
    ├── build.gradle
    └── src/main/java/...
```

The `neoforge` module uses the NeoForge ModDev Gradle plugin. The `fabric` module uses Fabric Loom.

## Module Responsibilities

### common

Contains everything that does not touch loader APIs:

| Category | Files |
|---|---|
| Sorting logic | `ItemSorter`, comparators, `SortMethod`, `SortOrder`, `SortPreference`, `LockedSlots` |
| Payload data | Payload record definitions (data only, no networking wiring) |
| Server handlers | `SortHandler`, `PreferenceHandler`, `LockHandler` — use service interfaces for player data access |
| Commands | `ModCommands` — Brigadier is identical on both loaders |
| Client logic | `SortKeyHandler`, `ScreenButtonInjector`, `SortButton`, `SortFeedback`, `SlotLockRenderer`, `SlotLockInputHandler`, client caches |
| Config values | `Config.java` — static fields and defaults only; each platform populates them |
| Service interfaces | `INetworkHandler`, `IPlayerDataService` |
| Service loader | `Services.java` — discovers implementations at startup |
| Utilities | `ModifierKey`, `SlotUtils` |

### neoforge

Contains only NeoForge-specific wiring:

- `BnnchSort.java` — `@Mod` entrypoint
- Thin event subscribers (`ClientEvents`, `ClientModBusEvents`, `ServerEvents`) that delegate to common logic
- `ModAttachments.java` — NeoForge attachment registration
- `ModPayloads.java` — NeoForge networking registration
- `NeoForgeNetworkHandler.java`, `NeoForgePlayerDataService.java` — service implementations
- `NeoForgeConfigLoader.java` — `ModConfigSpec` setup, populates `Config` static fields

### fabric

Mirrors the NeoForge module's shape:

- `BnnchSortFabric.java` — `ModInitializer` + `ClientModInitializer` entrypoints
- Thin Fabric API event callbacks that delegate to common logic
- `FabricNetworkHandler.java`, `FabricPlayerDataService.java` — service implementations
- `FabricConfigLoader.java` — reads/writes a JSON config file, populates `Config` static fields
- `FabricPlayerDataManager.java` — `PersistentState` keyed by player UUID; uses `ServerPlayerEvents.COPY_FROM` for death persistence

## Service Interfaces

Two interfaces live in `common/.../services/`.

### INetworkHandler

Abstracts sending packets in both directions.

```java
public interface INetworkHandler {
    void sendToServer(SortRequestPayload payload);
    void sendToServer(CyclePreferencePayload payload);
    void sendToServer(ToggleLockPayload payload);
    void sendToPlayer(ServerPlayer player, SyncPreferencePayload payload);
    void sendToPlayer(ServerPlayer player, SyncLockedSlotsPayload payload);
}
```

NeoForge uses `PacketDistributor` and `CustomPacketPayload`. Fabric uses the Fabric Networking API. Payload data classes stay in `common`; each platform wraps them in its own packet format.

### IPlayerDataService

Abstracts reading and writing per-player sort preferences and locked slots.

```java
public interface IPlayerDataService {
    SortPreference getPreference(Player player);
    void setPreference(Player player, SortPreference preference);
    LockedSlots getLockedSlots(Player player);
    void setLockedSlots(Player player, LockedSlots slots);
}
```

NeoForge reads and writes `AttachmentType` data directly on the player. Fabric reads and writes a world-scoped `PersistentState` keyed by player UUID.

### Services

`Services.java` uses `ServiceLoader` to discover the active platform's implementations at startup.

```java
public class Services {
    public static final INetworkHandler NETWORK =
        ServiceLoader.load(INetworkHandler.class).findFirst().orElseThrow();
    public static final IPlayerDataService PLAYER_DATA =
        ServiceLoader.load(IPlayerDataService.class).findFirst().orElseThrow();
}
```

Each platform registers its implementations via `META-INF/services/` files inside its own jar.

## Migration Strategy

Each step leaves the NeoForge build in a working state.

**Step 1 — Restructure the Gradle build**
Create `common/`, `neoforge/`, and `fabric/` directories. Update `settings.gradle` to declare all three subprojects. Wire the `neoforge` subproject to compile against `common` sources. No Java files change; the NeoForge build produces an identical jar.

**Step 2 — Extract common code**
Move the ~35 platform-agnostic files into `common/`. Add `Services.java` and the two service interfaces. Replace direct NeoForge attachment calls in the server handlers with `Services.PLAYER_DATA.*`. Replace direct `PacketDistributor` calls with `Services.NETWORK.*`. The NeoForge build compiles and all game tests pass.

**Step 3 — Add NeoForge service implementations**
Create `NeoForgeNetworkHandler` and `NeoForgePlayerDataService` in the `neoforge` module. Register them in `META-INF/services/`. The full NeoForge mod works end-to-end through the new abstraction layer.

**Step 4 — Add the Fabric subproject**
Configure `fabric/build.gradle` with Fabric Loom. Write `BnnchSortFabric.java`, thin Fabric event subscribers, and both Fabric service implementations. Register Fabric service implementations. Both loaders build and the mod runs on Fabric.

**Step 5 — Config on Fabric**
Write `FabricConfigLoader.java` to read and write a JSON config file, populating the same static fields in `Config.java` that `ModConfigSpec` populates on NeoForge. No new dependency required.

## Dependencies

| Module | New dependencies |
|---|---|
| `common` | None (compiles against vanilla Minecraft via Loom/ModDev) |
| `neoforge` | No change from current |
| `fabric` | Fabric Loader, Fabric API (standard for all Fabric mods) |

Architectury is not used. No new player-facing dependencies are added on the NeoForge side.

# Multi-Loader Support Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task.

**Goal:** Add Fabric mod loader support alongside NeoForge by restructuring into `common`, `neoforge`, and `fabric` subprojects sharing ~73% of the codebase.

**Architecture:** Manual multi-project Gradle setup. Two service interfaces (`INetworkHandler`, `IPlayerDataService`) in `common` are discovered at runtime via Java `ServiceLoader`. Each platform module provides implementations registered via `META-INF/services`. No Architectury dependency.

**Tech Stack:** Java 21, Gradle 8.8, NeoForge ModDev 2.0.140, Fabric Loom 1.8-SNAPSHOT, Fabric API 0.103.0+1.21.1, Fabric Loader 0.16.9

**Reference:** `docs/plans/2026-02-21-multi-loader-design.md`

---

## Pre-flight check

Before starting, note the typo in `gradle.properties`:
```
parchment_minecraft_version=1.21.11   # should be 1.21.1
```
Fix this in Task 1 while editing `gradle.properties`.

---

## Task 1: Restructure Gradle as multi-project build

**What changes:** The root project loses `src/`. All existing source moves into a `neoforge/` subproject. Two empty subprojects (`common/`, `fabric/`) are declared. The NeoForge build must produce an identical jar at the end of this task.

**Files:**
- Modify: `settings.gradle`
- Replace: `build.gradle` (root — becomes shared conventions only)
- Create: `neoforge/build.gradle`
- Create: `common/build.gradle`
- Modify: `gradle.properties` (add Fabric versions, fix Parchment typo)
- Shell: move `src/` → `neoforge/src/`

---

**Step 1: Create subproject directories**

```bash
mkdir -p neoforge
mkdir -p common/src/main/java/xyz/bannach/bnnch_sort
mkdir -p fabric/src/main/java/xyz/bannach/bnnch_sort
```

**Step 2: Move all existing sources into neoforge/**

```bash
mv src neoforge/src
```

This moves `src/main/java/`, `src/main/resources/`, `src/main/templates/`, and `src/generated/resources/` in one shot.

**Step 3: Replace `settings.gradle`**

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { url = 'https://maven.neoforged.net/releases' }
        maven { url = 'https://maven.fabricmc.net/' }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

rootProject.name = 'bnnch_sort'
include 'common', 'neoforge', 'fabric'
```

**Step 4: Replace root `build.gradle` with shared conventions**

```groovy
subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = 'https://maven.neoforged.net/releases' }
        maven { url = 'https://maven.fabricmc.net/' }
        maven { url = 'https://maven.parchmentmc.org' }
    }
}
```

Do not apply `java-library` here — Loom and ModDev each apply their own Java plugin.

**Step 5: Create `neoforge/build.gradle`**

This is the current root `build.gradle` with minor adjustments. Key differences from the original:
- Add `implementation project(':common')` to `dependencies`
- Add `jar { from(project(':common').sourceSets.main.output) }` (needed once common has files)
- Add `sourceSet(project(':common').sourceSets.main)` to the `mods` block (needed for dev run classpath)

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'idea'
    id 'net.neoforged.moddev' version '2.0.140'
}

version = "${mod_version}+mc${minecraft_version}"
group = mod_group_id

base {
    archivesName = "${mod_id}-neoforge"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = project.neo_version

    parchment {
        mappingsVersion = project.parchment_mappings_version
        minecraftVersion = project.parchment_minecraft_version
    }

    runs {
        client {
            client()
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        server {
            server()
            programArgument '--nogui'
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        gameTestServer {
            type = "gameTestServer"
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        data {
            data()
            programArguments.addAll '--mod', project.mod_id, '--all',
                '--output', file('src/generated/resources/').getAbsolutePath(),
                '--existing', file('src/main/resources/').getAbsolutePath()
        }
        configureEach {
            systemProperty 'forge.logging.markers', 'REGISTRIES'
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        "${mod_id}" {
            sourceSet(sourceSets.main)
            sourceSet(project(':common').sourceSets.main)
        }
    }
}

sourceSets.main.resources { srcDir 'src/generated/resources' }

dependencies {
    implementation project(':common')
}

jar {
    from(project(':common').sourceSets.main.output)
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}

var generateModMetadata = tasks.register("generateModMetadata", ProcessResources) {
    var replaceProperties = [
        minecraft_version      : minecraft_version,
        minecraft_version_range: minecraft_version_range,
        neo_version            : neo_version,
        neo_version_range      : neo_version_range,
        loader_version_range   : loader_version_range,
        mod_id                 : mod_id,
        mod_name               : mod_name,
        mod_license            : mod_license,
        mod_version            : mod_version,
        mod_authors            : mod_authors,
        mod_description        : mod_description
    ]
    inputs.properties replaceProperties
    expand replaceProperties
    from "src/main/templates"
    into "build/generated/sources/modMetadata"
}

sourceSets.main.resources.srcDir generateModMetadata
neoForge.ideSyncTask generateModMetadata

publishing {
    publications {
        register('mavenJava', MavenPublication) {
            from components.java
        }
    }
    repositories {
        maven {
            url = "file://${project.projectDir}/repo"
        }
    }
}

idea {
    module {
        downloadSources = true
        downloadJavadoc = true
    }
}
```

**Step 6: Create `common/build.gradle`**

```groovy
plugins {
    id 'java-library'
    id 'fabric-loom' version "${loom_version}"
}

version = "${mod_version}+mc${minecraft_version}"
group = mod_group_id

base {
    archivesName = "${mod_id}-common"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}

dependencies {
    minecraft "com.mojang:minecraft:${minecraft_version}"
    mappings loom.officialMojangMappings()
}
```

**Step 7: Update `gradle.properties`**

Fix the Parchment typo and add Fabric versions:

```properties
parchment_minecraft_version=1.21.1

# Fabric
loom_version=1.8-SNAPSHOT
fabric_loader_version=0.16.9
fabric_api_version=0.103.0+1.21.1
```

**Step 8: Verify the NeoForge build**

```
./gradlew.bat :neoforge:build
```

Expected: `BUILD SUCCESSFUL`. Jar at `neoforge/build/libs/bnnch_sort-neoforge-0.2.2+mc1.21.1.jar`.

**Step 9: Commit**

```bash
git add .
git commit -m "chore: restructure to Gradle multi-project with common/neoforge/fabric subprojects"
```

---

## Task 2: Move pure-logic code to common

**What changes:** All platform-agnostic files move from `neoforge/src/` to `common/src/`. None of these files import NeoForge classes. `ModPayloads.java` stays in neoforge (it uses `RegisterPayloadHandlersEvent`). The NeoForge build must still pass after every move.

**Files to move:**
- `neoforge/.../sorting/` → `common/.../sorting/` (entire package: ItemSorter, SortMethod, SortOrder, SortPreference, LockedSlots, comparator/)
- `neoforge/.../util/SlotUtils.java` → `common/.../util/`
- `neoforge/.../ModifierKey.java` → `common/...`
- `neoforge/.../network/` payload records → `common/.../network/` (all except `ModPayloads.java`)
- `neoforge/.../commands/ModCommands.java` → `common/.../commands/`

The payload records (`CyclePreferencePayload`, `SortRequestPayload`, `SyncLockedSlotsPayload`, `SyncPreferencePayload`, `ToggleLockPayload`) only import vanilla Minecraft classes (`CustomPacketPayload`, `StreamCodec`, `ByteBuf`, `ResourceLocation`) and are safe to move.

---

**Step 1: Move sorting package**

```bash
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/sorting \
   common/src/main/java/xyz/bannach/bnnch_sort/sorting
```

**Step 2: Move util and ModifierKey**

```bash
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/util \
   common/src/main/java/xyz/bannach/bnnch_sort/util
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/ModifierKey.java \
   common/src/main/java/xyz/bannach/bnnch_sort/ModifierKey.java
```

**Step 3: Move payload records (not ModPayloads.java)**

```bash
mkdir -p common/src/main/java/xyz/bannach/bnnch_sort/network
for f in CyclePreferencePayload SortRequestPayload SyncLockedSlotsPayload SyncPreferencePayload ToggleLockPayload; do
    mv neoforge/src/main/java/xyz/bannach/bnnch_sort/network/${f}.java \
       common/src/main/java/xyz/bannach/bnnch_sort/network/${f}.java
done
```

**Step 4: Move commands**

```bash
mkdir -p common/src/main/java/xyz/bannach/bnnch_sort/commands
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/commands/ModCommands.java \
   common/src/main/java/xyz/bannach/bnnch_sort/commands/ModCommands.java
```

**Step 5: Check how ModCommands is registered on NeoForge**

Search for `ModCommands` in `neoforge/src/` to find where it's called. There may be a `RegisterCommandsEvent` subscriber somewhere. Ensure this event wiring stays in `neoforge/` — only the command logic moved to `common/`.

If `ModCommands.register()` takes a `CommandDispatcher<CommandSourceStack>`, it is already platform-agnostic and no changes to the method body are needed.

**Step 6: Move any package-info.java files**

Each moved package may have a `package-info.java`. Move it with the package:

```bash
# Example for sorting package — already moved in step 1.
# Check for package-info.java in each moved package:
find neoforge/src -name "package-info.java"
```

Move any remaining ones that belong to moved packages.

**Step 7: Verify**

```
./gradlew.bat :neoforge:build
```

Expected: `BUILD SUCCESSFUL`.

**Step 8: Commit**

```bash
git add .
git commit -m "refactor: move pure-logic code (sort, payloads, commands, utils) to common module"
```

---

## Task 3: Add service layer and migrate server handlers to common

**What changes:** Create the two service interfaces and `Services` class in `common`. Create NeoForge implementations in `neoforge`. Refactor the three server handlers to take `ServerPlayer` instead of `IPayloadContext` and use `Services.*` instead of `ModAttachments.*` and `PacketDistributor`. Move the refactored handlers to `common`. Update `ModPayloads.java` and `ServerEvents.java` to use the new signatures and services.

This entire task must be done before verifying, because the service interfaces, implementations, and service registrations must all exist together for `ServiceLoader` to succeed at startup.

**Files:**
- Create: `common/src/main/java/xyz/bannach/bnnch_sort/services/INetworkHandler.java`
- Create: `common/src/main/java/xyz/bannach/bnnch_sort/services/IPlayerDataService.java`
- Create: `common/src/main/java/xyz/bannach/bnnch_sort/services/Services.java`
- Create: `neoforge/src/main/java/xyz/bannach/bnnch_sort/platform/NeoForgeNetworkHandler.java`
- Create: `neoforge/src/main/java/xyz/bannach/bnnch_sort/platform/NeoForgePlayerDataService.java`
- Create: `neoforge/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.INetworkHandler`
- Create: `neoforge/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.IPlayerDataService`
- Modify then move: `neoforge/.../server/SortHandler.java` → `common/.../server/`
- Modify then move: `neoforge/.../server/PreferenceHandler.java` → `common/.../server/`
- Modify then move: `neoforge/.../server/LockHandler.java` → `common/.../server/`
- Modify: `neoforge/.../network/ModPayloads.java`
- Modify: `neoforge/.../server/ServerEvents.java`

---

**Step 1: Create `INetworkHandler.java` in common**

```java
package xyz.bannach.bnnch_sort.services;

import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;

public interface INetworkHandler {
    void sendToServer(SortRequestPayload payload);
    void sendToServer(CyclePreferencePayload payload);
    void sendToServer(ToggleLockPayload payload);
    void sendToPlayer(ServerPlayer player, SyncPreferencePayload payload);
    void sendToPlayer(ServerPlayer player, SyncLockedSlotsPayload payload);
}
```

**Step 2: Create `IPlayerDataService.java` in common**

```java
package xyz.bannach.bnnch_sort.services;

import net.minecraft.world.entity.player.Player;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public interface IPlayerDataService {
    SortPreference getPreference(Player player);
    void setPreference(Player player, SortPreference preference);
    LockedSlots getLockedSlots(Player player);
    void setLockedSlots(Player player, LockedSlots slots);
}
```

**Step 3: Create `Services.java` in common**

```java
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
```

**Step 4: Create `NeoForgeNetworkHandler.java` in neoforge**

```java
package xyz.bannach.bnnch_sort.platform;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.services.INetworkHandler;

public class NeoForgeNetworkHandler implements INetworkHandler {

    @Override
    public void sendToServer(SortRequestPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToServer(CyclePreferencePayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToServer(ToggleLockPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncPreferencePayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncLockedSlotsPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
```

**Step 5: Create `NeoForgePlayerDataService.java` in neoforge**

```java
package xyz.bannach.bnnch_sort.platform;

import net.minecraft.world.entity.player.Player;
import xyz.bannach.bnnch_sort.ModAttachments;
import xyz.bannach.bnnch_sort.services.IPlayerDataService;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public class NeoForgePlayerDataService implements IPlayerDataService {

    @Override
    public SortPreference getPreference(Player player) {
        return player.getData(ModAttachments.SORT_PREFERENCE);
    }

    @Override
    public void setPreference(Player player, SortPreference preference) {
        player.setData(ModAttachments.SORT_PREFERENCE, preference);
    }

    @Override
    public LockedSlots getLockedSlots(Player player) {
        return player.getData(ModAttachments.LOCKED_SLOTS);
    }

    @Override
    public void setLockedSlots(Player player, LockedSlots slots) {
        player.setData(ModAttachments.LOCKED_SLOTS, slots);
    }
}
```

**Step 6: Create META-INF/services files in neoforge**

Create `neoforge/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.INetworkHandler` with contents:
```
xyz.bannach.bnnch_sort.platform.NeoForgeNetworkHandler
```

Create `neoforge/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.IPlayerDataService` with contents:
```
xyz.bannach.bnnch_sort.platform.NeoForgePlayerDataService
```

**Step 7: Refactor `SortHandler.java`**

Change the `handle` method signature and replace all `ModAttachments` references. The method no longer takes `IPayloadContext` — that concern moves to the caller in `ModPayloads.java`.

Remove imports:
- `net.neoforged.neoforge.network.handling.IPayloadContext`
- `xyz.bannach.bnnch_sort.ModAttachments`

Add imports:
- `xyz.bannach.bnnch_sort.services.Services`

Change `handle`:
```java
// Before:
public static void handle(SortRequestPayload payload, IPayloadContext context) {
    context.enqueueWork(() -> {
        ServerPlayer player = (ServerPlayer) context.player();
        // ...
    });
}

// After:
public static void handle(SortRequestPayload payload, ServerPlayer player) {
    if (player.isSpectator()) return;
    // ... rest of body unchanged except:
}
```

In `sortContainerRegion`, replace:
```java
// Before:
ItemSorter.sort(stacks, player.getData(ModAttachments.SORT_PREFERENCE));

// After:
ItemSorter.sort(stacks, Services.PLAYER_DATA.getPreference(player));
```

In `sortRegion`, replace:
```java
// Before:
LockedSlots lockedSlots = player.getData(ModAttachments.LOCKED_SLOTS);
SortPreference preference = player.getData(ModAttachments.SORT_PREFERENCE);

// After:
LockedSlots lockedSlots = Services.PLAYER_DATA.getLockedSlots(player);
SortPreference preference = Services.PLAYER_DATA.getPreference(player);
```

**Step 8: Refactor `PreferenceHandler.java`**

```java
// Before:
public static void handle(CyclePreferencePayload payload, IPayloadContext context) {
    context.enqueueWork(() -> {
        ServerPlayer player = (ServerPlayer) context.player();
        SortPreference current = player.getData(ModAttachments.SORT_PREFERENCE);
        SortPreference updated = current.next();
        player.setData(ModAttachments.SORT_PREFERENCE, updated);
        PacketDistributor.sendToPlayer(player, new SyncPreferencePayload(...));
    });
}

// After:
public static void handle(CyclePreferencePayload payload, ServerPlayer player) {
    SortPreference current = Services.PLAYER_DATA.getPreference(player);
    SortPreference updated = current.next();
    Services.PLAYER_DATA.setPreference(player, updated);
    Services.NETWORK.sendToPlayer(player, new SyncPreferencePayload(updated.method(), updated.order()));
}
```

Remove NeoForge imports, add `Services` import.

**Step 9: Refactor `LockHandler.java`**

```java
// After:
public static void handle(ToggleLockPayload payload, ServerPlayer player) {
    int slotIndex = payload.slotIndex();
    if (slotIndex < 0 || slotIndex > 35) return;
    LockedSlots current = Services.PLAYER_DATA.getLockedSlots(player);
    LockedSlots updated = current.toggle(slotIndex);
    Services.PLAYER_DATA.setLockedSlots(player, updated);
    Services.NETWORK.sendToPlayer(player, new SyncLockedSlotsPayload(updated.slots()));
}
```

Remove NeoForge imports, add `Services` import.

**Step 10: Update `ModPayloads.java` to wrap handlers with enqueueWork**

The handlers no longer take `IPayloadContext`. Update the registrations:

```java
registrar.playToServer(
    SortRequestPayload.TYPE, SortRequestPayload.STREAM_CODEC,
    (payload, context) -> context.enqueueWork(
        () -> SortHandler.handle(payload, (ServerPlayer) context.player())));

registrar.playToServer(
    CyclePreferencePayload.TYPE, CyclePreferencePayload.STREAM_CODEC,
    (payload, context) -> context.enqueueWork(
        () -> PreferenceHandler.handle(payload, (ServerPlayer) context.player())));

registrar.playToServer(
    ToggleLockPayload.TYPE, ToggleLockPayload.STREAM_CODEC,
    (payload, context) -> context.enqueueWork(
        () -> LockHandler.handle(payload, (ServerPlayer) context.player())));
```

The `ClientPreferenceCache::handle` and `ClientLockedSlotsCache::handle` registrations also pass `IPayloadContext` as a second argument. Check those files — if the context is unused (only the payload data matters), change their signatures to take only the payload, then update the registrations here to match.

**Step 11: Update `ServerEvents.java` to use Services**

```java
// Replace:
SortPreference pref = player.getData(ModAttachments.SORT_PREFERENCE);
PacketDistributor.sendToPlayer(player, new SyncPreferencePayload(pref.method(), pref.order()));
LockedSlots locked = player.getData(ModAttachments.LOCKED_SLOTS);
PacketDistributor.sendToPlayer(player, new SyncLockedSlotsPayload(locked.slots()));

// With:
SortPreference pref = Services.PLAYER_DATA.getPreference(player);
Services.NETWORK.sendToPlayer(player, new SyncPreferencePayload(pref.method(), pref.order()));
LockedSlots locked = Services.PLAYER_DATA.getLockedSlots(player);
Services.NETWORK.sendToPlayer(player, new SyncLockedSlotsPayload(locked.slots()));
```

Remove `PacketDistributor` and `ModAttachments` imports. Add `Services` import.

**Step 12: Move refactored server handlers to common**

```bash
mkdir -p common/src/main/java/xyz/bannach/bnnch_sort/server
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/server/SortHandler.java \
   common/src/main/java/xyz/bannach/bnnch_sort/server/SortHandler.java
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/server/PreferenceHandler.java \
   common/src/main/java/xyz/bannach/bnnch_sort/server/PreferenceHandler.java
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/server/LockHandler.java \
   common/src/main/java/xyz/bannach/bnnch_sort/server/LockHandler.java
```

**Step 13: Verify build and game tests**

```
./gradlew.bat :neoforge:build
./gradlew.bat :neoforge:runGameTestServer
```

Expected: both succeed.

**Step 14: Commit**

```bash
git add .
git commit -m "refactor: add service layer (ServiceLoader) and move server handlers to common"
```

---

## Task 4: Move client code to common

**What changes:** The client logic files (`SortKeyHandler`, `ScreenButtonInjector`, `SortButton`, `SortFeedback`, `SlotLockInputHandler`, `SlotLockRenderer`, `ClientPreferenceCache`, `ClientLockedSlotsCache`) all use vanilla Minecraft APIs and move to `common`. Their handler methods currently accept NeoForge event objects — change them to accept vanilla types and return `boolean` (true = event handled). The NeoForge `ClientEvents.java` becomes a thin wrapper that passes vanilla values to the common methods.

**Files:**
- Modify then move: `neoforge/.../client/SortKeyHandler.java` → `common/.../client/`
- Inspect and move: `neoforge/.../client/SlotLockInputHandler.java` → `common/.../client/`
- Move (no changes expected): `ScreenButtonInjector`, `SortButton`, `SortFeedback`, `SlotLockRenderer`, `ClientPreferenceCache`, `ClientLockedSlotsCache`
- Modify: `neoforge/.../client/ClientEvents.java`
- Modify: `neoforge/.../client/ClientModBusEvents.java`

---

**Step 1: Refactor `SortKeyHandler.java`**

The `register(RegisterKeyMappingsEvent)` method uses a NeoForge type. Move that registration into `ClientModBusEvents.java` directly and remove the method from `SortKeyHandler`.

Change `onKeyPressed` and `onMouseClicked` signatures:

```java
// Before:
public static void onKeyPressed(ScreenEvent.KeyPressed.Post event)
public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event)

// After:
// Returns true if the event was handled (caller should cancel the platform event)
public static boolean onKeyPressed(Screen screen, int keyCode, int scanCode)
public static boolean onMouseClicked(Screen screen, int button)
```

In the bodies:
- `event.getScreen()` → `screen`
- `event.getKeyCode()` → `keyCode`, `event.getScanCode()` → `scanCode`, `event.getButton()` → `button`
- `event.isCanceled()` → remove (this check is NeoForge-specific; handled in the caller)
- `event.setCanceled(true)` → `return true`
- `PacketDistributor.sendToServer(...)` → `Services.NETWORK.sendToServer(...)`
- Add early `return false` at the end of any non-returning path

Remove NeoForge imports. Add `Services` import.

**Step 2: Inspect each remaining client file for NeoForge imports**

Run:
```bash
grep -r "neoforged" neoforge/src/main/java/xyz/bannach/bnnch_sort/client/
```

For any file that imports NeoForge types in its method signatures, apply the same pattern: extract the logic to accept vanilla types, let `ClientEvents.java` handle the event unwrapping.

`SlotLockInputHandler` likely has a handler method that takes a `ScreenEvent.MouseButtonPressed` — refactor it the same way as `SortKeyHandler.onMouseClicked`.

**Step 3: Update `ClientEvents.java` in neoforge**

`ClientEvents.java` becomes the thin NeoForge event adapter:

```java
@SubscribeEvent
public static void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
    if (event.isCanceled()) return;
    if (SortKeyHandler.onKeyPressed(event.getScreen(), event.getKeyCode(), event.getScanCode())) {
        event.setCanceled(true);
    }
}

@SubscribeEvent
public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
    if (SortKeyHandler.onMouseClicked(event.getScreen(), event.getButton())) {
        event.setCanceled(true);
    }
    // Also delegate to SlotLockInputHandler if it was refactored similarly
}
```

Update all other event methods in `ClientEvents.java` to delegate to the common handlers with vanilla parameters.

**Step 4: Update `ClientModBusEvents.java` in neoforge**

```java
@SubscribeEvent
public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(SortKeyHandler.SORT_KEY);
    event.register(SortKeyHandler.CYCLE_PREFERENCE_KEY);
}
```

(Remove the `SortKeyHandler.register(event)` delegation if it existed.)

**Step 5: Move client files to common**

```bash
mkdir -p common/src/main/java/xyz/bannach/bnnch_sort/client
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/SortKeyHandler.java common/.../client/
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/ScreenButtonInjector.java common/.../client/
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/SortButton.java common/.../client/
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/SortFeedback.java common/.../client/
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/SlotLockInputHandler.java common/.../client/
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/SlotLockRenderer.java common/.../client/
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/ClientPreferenceCache.java common/.../client/
mv neoforge/src/main/java/xyz/bannach/bnnch_sort/client/ClientLockedSlotsCache.java common/.../client/
```

**Step 6: Verify**

```
./gradlew.bat :neoforge:build
```

Expected: `BUILD SUCCESSFUL`.

**Step 7: Commit**

```bash
git add .
git commit -m "refactor: move client logic to common module"
```

---

## Task 5: Split Config into common values and NeoForge spec

**What changes:** `Config.java` currently mixes value fields (platform-agnostic) with `ModConfigSpec` definitions (NeoForge-specific). Split them: a plain `Config.java` in `common` holds only the static fields and defaults; a new `NeoForgeConfig.java` in `neoforge` holds the `ModConfigSpec` builder and `@SubscribeEvent` handlers that populate `Config`'s fields on load.

**Files:**
- Create: `common/src/main/java/xyz/bannach/bnnch_sort/Config.java`
- Create: `neoforge/src/main/java/xyz/bannach/bnnch_sort/NeoForgeConfig.java`
- Delete: `neoforge/src/main/java/xyz/bannach/bnnch_sort/Config.java`
- Modify: `neoforge/src/main/java/xyz/bannach/bnnch_sort/BnnchSort.java`

---

**Step 1: Create `common/Config.java`**

```java
package xyz.bannach.bnnch_sort;

import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;

public class Config {
    private Config() {}

    public static boolean showSortButton = true;
    public static ModifierKey lockModifierKey = ModifierKey.ALT;
    public static int lockTintColor = 0x80FFD700;
    public static boolean showLockTooltip = true;
    public static SortMethod defaultSortMethod = SortMethod.ALPHABETICAL;
    public static SortOrder defaultSortOrder = SortOrder.ASCENDING;
}
```

**Step 2: Create `neoforge/NeoForgeConfig.java`**

Move all `ModConfigSpec` builder fields, `CLIENT_SPEC`, `SERVER_SPEC`, `@EventBusSubscriber`, `onLoad`, `onReload`, `applyConfig`, and `parseColor` from the old `Config.java` into `NeoForgeConfig.java`. The `applyConfig` method sets `Config.*` static fields:

```java
package xyz.bannach.bnnch_sort;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
// ... other imports

@EventBusSubscriber(modid = BnnchSort.MODID)
public class NeoForgeConfig {
    private NeoForgeConfig() {}

    // All ModConfigSpec.Builder fields and build() calls from old Config.java
    // applyConfig populates Config.showSortButton, Config.lockModifierKey, etc.

    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
    static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) { ... }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) { ... }

    private static void applyConfig(Object spec) {
        if (spec == CLIENT_SPEC) {
            Config.showSortButton = SHOW_SORT_BUTTON.get();
            Config.lockModifierKey = LOCK_MODIFIER_KEY.get();
            Config.lockTintColor = parseColor(LOCK_TINT_COLOR.get(), 0x80FFD700);
            Config.showLockTooltip = SHOW_LOCK_TOOLTIP.get();
        } else if (spec == SERVER_SPEC) {
            Config.defaultSortMethod = DEFAULT_SORT_METHOD.get();
            Config.defaultSortOrder = DEFAULT_SORT_ORDER.get();
        }
    }

    // parseColor method unchanged from old Config.java
}
```

**Step 3: Update `BnnchSort.java`**

```java
// Before:
modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

// After:
modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.CLIENT_SPEC);
modContainer.registerConfig(ModConfig.Type.SERVER, NeoForgeConfig.SERVER_SPEC);
```

**Step 4: Delete old `neoforge/Config.java`**

```bash
rm neoforge/src/main/java/xyz/bannach/bnnch_sort/Config.java
```

**Step 5: Verify build and game tests**

```
./gradlew.bat :neoforge:build
./gradlew.bat :neoforge:runGameTestServer
```

Expected: both succeed. All game tests pass.

**Step 6: Commit**

```bash
git add .
git commit -m "refactor: split Config into common value fields and NeoForge ModConfigSpec"
```

---

## Task 6: Configure Fabric subproject

**What changes:** Add `fabric/build.gradle` and `fabric/src/main/resources/fabric.mod.json`. Verify the Fabric module compiles against Minecraft classes via Loom.

**Files:**
- Create: `fabric/build.gradle`
- Create: `fabric/src/main/resources/fabric.mod.json`

---

**Step 1: Create `fabric/build.gradle`**

```groovy
plugins {
    id 'fabric-loom' version "${loom_version}"
}

version = "${mod_version}+mc${minecraft_version}"
group = mod_group_id

base {
    archivesName = "${mod_id}-fabric"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}

dependencies {
    minecraft "com.mojang:minecraft:${minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${fabric_loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${fabric_api_version}"
    compileOnly project(':common')
}

// Compile common before fabric
compileJava.dependsOn ':common:compileJava'

// Merge common classes into the fabric jar
jar {
    from(project(':common').sourceSets.main.output)
}

processResources {
    inputs.property "version", project.version
    inputs.property "mod_id", mod_id
    inputs.property "mod_name", mod_name
    inputs.property "mod_description", mod_description
    inputs.property "mod_authors", mod_authors
    inputs.property "mod_license", mod_license
    inputs.property "minecraft_version", minecraft_version
    inputs.property "fabric_loader_version", fabric_loader_version
    filteringCharset "UTF-8"
    filesMatching("fabric.mod.json") {
        expand([
            version             : project.version,
            mod_id              : mod_id,
            mod_name            : mod_name,
            mod_description     : mod_description,
            mod_authors         : mod_authors,
            mod_license         : mod_license,
            minecraft_version   : minecraft_version,
            fabric_loader_version: fabric_loader_version
        ])
    }
}
```

**Step 2: Create `fabric/src/main/resources/fabric.mod.json`**

```json
{
    "schemaVersion": 1,
    "id": "${mod_id}",
    "version": "${version}",
    "name": "${mod_name}",
    "description": "${mod_description}",
    "authors": ["${mod_authors}"],
    "license": "${mod_license}",
    "icon": "assets/bnnch_sort/textures/logo_256.png",
    "environment": "*",
    "entrypoints": {
        "main": ["xyz.bannach.bnnch_sort.fabric.BnnchSortFabric"],
        "client": ["xyz.bannach.bnnch_sort.fabric.BnnchSortFabricClient"]
    },
    "depends": {
        "fabricloader": ">=${fabric_loader_version}",
        "fabric-api": "*",
        "minecraft": "~1.21.1"
    }
}
```

**Step 3: Verify common compiles**

```
./gradlew.bat :common:compileJava
```

Expected: `BUILD SUCCESSFUL`.

**Step 4: Commit**

```bash
git add .
git commit -m "chore: configure fabric subproject build and mod metadata"
```

---

## Task 7: Implement Fabric server side

**What changes:** Create the Fabric mod entrypoint, player data persistence via `SavedData`, the two Fabric service implementations, `META-INF/services` registrations, and server event handlers (player join sync, death data copy, command registration, packet handler registration).

**Files:**
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/BnnchSortFabric.java`
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/platform/FabricPlayerDataManager.java`
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/platform/FabricPlayerDataService.java`
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/platform/FabricNetworkHandler.java`
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/server/FabricServerEvents.java`
- Create: `fabric/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.INetworkHandler`
- Create: `fabric/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.IPlayerDataService`

---

**Step 1: Create `FabricPlayerDataManager.java`**

Uses vanilla Minecraft's `SavedData` to persist per-player data keyed by UUID. Stored in the overworld's data storage so it saves with the world.

```java
package xyz.bannach.bnnch_sort.fabric.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import xyz.bannach.bnnch_sort.Config;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FabricPlayerDataManager extends SavedData {

    private static final String ID = "bnnch_sort_player_data";

    private static final Factory<FabricPlayerDataManager> FACTORY = new Factory<>(
        FabricPlayerDataManager::new,
        FabricPlayerDataManager::load,
        null
    );

    private final Map<UUID, SortPreference> preferences = new HashMap<>();
    private final Map<UUID, LockedSlots> lockedSlots = new HashMap<>();

    public static FabricPlayerDataManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public SortPreference getPreference(UUID uuid) {
        return preferences.getOrDefault(uuid,
            new SortPreference(Config.defaultSortMethod, Config.defaultSortOrder));
    }

    public void setPreference(UUID uuid, SortPreference pref) {
        preferences.put(uuid, pref);
        setDirty();
    }

    public LockedSlots getLockedSlots(UUID uuid) {
        return lockedSlots.getOrDefault(uuid, LockedSlots.EMPTY);
    }

    public void setLockedSlots(UUID uuid, LockedSlots slots) {
        lockedSlots.put(uuid, slots);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag prefsTag = new CompoundTag();
        preferences.forEach((uuid, pref) ->
            SortPreference.CODEC.encodeStart(NbtOps.INSTANCE, pref)
                .ifSuccess(t -> prefsTag.put(uuid.toString(), t)));
        tag.put("preferences", prefsTag);

        CompoundTag locksTag = new CompoundTag();
        lockedSlots.forEach((uuid, slots) ->
            LockedSlots.CODEC.encodeStart(NbtOps.INSTANCE, slots)
                .ifSuccess(t -> locksTag.put(uuid.toString(), t)));
        tag.put("lockedSlots", locksTag);

        return tag;
    }

    public static FabricPlayerDataManager load(CompoundTag tag,
            net.minecraft.core.HolderLookup.Provider provider) {
        FabricPlayerDataManager manager = new FabricPlayerDataManager();

        CompoundTag prefsTag = tag.getCompound("preferences");
        for (String key : prefsTag.getAllKeys()) {
            SortPreference.CODEC.parse(NbtOps.INSTANCE, prefsTag.get(key))
                .ifSuccess(pref -> manager.preferences.put(UUID.fromString(key), pref));
        }

        CompoundTag locksTag = tag.getCompound("lockedSlots");
        for (String key : locksTag.getAllKeys()) {
            LockedSlots.CODEC.parse(NbtOps.INSTANCE, locksTag.get(key))
                .ifSuccess(slots -> manager.lockedSlots.put(UUID.fromString(key), slots));
        }

        return manager;
    }
}
```

**Step 2: Create `FabricPlayerDataService.java`**

```java
package xyz.bannach.bnnch_sort.fabric.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xyz.bannach.bnnch_sort.services.IPlayerDataService;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public class FabricPlayerDataService implements IPlayerDataService {

    @Override
    public SortPreference getPreference(Player player) {
        return FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .getPreference(player.getUUID());
    }

    @Override
    public void setPreference(Player player, SortPreference preference) {
        FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .setPreference(player.getUUID(), preference);
    }

    @Override
    public LockedSlots getLockedSlots(Player player) {
        return FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .getLockedSlots(player.getUUID());
    }

    @Override
    public void setLockedSlots(Player player, LockedSlots slots) {
        FabricPlayerDataManager.get(((ServerPlayer) player).serverLevel())
            .setLockedSlots(player.getUUID(), slots);
    }
}
```

**Step 3: Create `FabricNetworkHandler.java`**

`ClientPlayNetworking.send()` is client-only. It is only called from `SortKeyHandler` which runs client-side. The `sendToPlayer` methods are server-only and use `ServerPlayNetworking.send()`.

```java
package xyz.bannach.bnnch_sort.fabric.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.services.INetworkHandler;

public class FabricNetworkHandler implements INetworkHandler {

    @Override
    public void sendToServer(SortRequestPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToServer(CyclePreferencePayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToServer(ToggleLockPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncPreferencePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SyncLockedSlotsPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
```

**Step 4: Create META-INF/services files in fabric**

`fabric/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.INetworkHandler`:
```
xyz.bannach.bnnch_sort.fabric.platform.FabricNetworkHandler
```

`fabric/src/main/resources/META-INF/services/xyz.bannach.bnnch_sort.services.IPlayerDataService`:
```
xyz.bannach.bnnch_sort.fabric.platform.FabricPlayerDataService
```

**Step 5: Create `FabricServerEvents.java`**

```java
package xyz.bannach.bnnch_sort.fabric.server;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.commands.ModCommands;
import xyz.bannach.bnnch_sort.fabric.platform.FabricPlayerDataManager;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.services.Services;
import xyz.bannach.bnnch_sort.sorting.LockedSlots;
import xyz.bannach.bnnch_sort.sorting.SortPreference;

public class FabricServerEvents {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> ModCommands.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            SortPreference pref = Services.PLAYER_DATA.getPreference(player);
            Services.NETWORK.sendToPlayer(player,
                new SyncPreferencePayload(pref.method(), pref.order()));
            LockedSlots locked = Services.PLAYER_DATA.getLockedSlots(player);
            Services.NETWORK.sendToPlayer(player, new SyncLockedSlotsPayload(locked.slots()));
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            FabricPlayerDataManager manager =
                FabricPlayerDataManager.get(newPlayer.serverLevel());
            manager.setPreference(newPlayer.getUUID(),
                manager.getPreference(oldPlayer.getUUID()));
            manager.setLockedSlots(newPlayer.getUUID(),
                manager.getLockedSlots(oldPlayer.getUUID()));
        });
    }
}
```

**Step 6: Create `BnnchSortFabric.java`**

```java
package xyz.bannach.bnnch_sort.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import xyz.bannach.bnnch_sort.fabric.server.FabricServerEvents;
import xyz.bannach.bnnch_sort.network.CyclePreferencePayload;
import xyz.bannach.bnnch_sort.network.SortRequestPayload;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;
import xyz.bannach.bnnch_sort.network.ToggleLockPayload;
import xyz.bannach.bnnch_sort.server.LockHandler;
import xyz.bannach.bnnch_sort.server.PreferenceHandler;
import xyz.bannach.bnnch_sort.server.SortHandler;

public class BnnchSortFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register payload types
        PayloadTypeRegistry.playC2S().register(
            SortRequestPayload.TYPE, SortRequestPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(
            CyclePreferencePayload.TYPE, CyclePreferencePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(
            ToggleLockPayload.TYPE, ToggleLockPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(
            SyncPreferencePayload.TYPE, SyncPreferencePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(
            SyncLockedSlotsPayload.TYPE, SyncLockedSlotsPayload.STREAM_CODEC);

        // Register server-side packet handlers
        ServerPlayNetworking.registerGlobalReceiver(SortRequestPayload.TYPE,
            (payload, context) -> context.server().execute(
                () -> SortHandler.handle(payload, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CyclePreferencePayload.TYPE,
            (payload, context) -> context.server().execute(
                () -> PreferenceHandler.handle(payload, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ToggleLockPayload.TYPE,
            (payload, context) -> context.server().execute(
                () -> LockHandler.handle(payload, context.player())));

        FabricServerEvents.register();
    }
}
```

**Step 7: Verify fabric compiles**

```
./gradlew.bat :fabric:build
```

Expected: `BUILD SUCCESSFUL`. Also verify NeoForge still passes:

```
./gradlew.bat :neoforge:build
```

**Step 8: Commit**

```bash
git add .
git commit -m "feat: implement Fabric server side (entrypoint, player data, networking, events)"
```

---

## Task 8: Implement Fabric client side

**What changes:** Create the Fabric client entrypoint, register keybindings via Fabric's API, register client-side packet handlers, and wire screen events to the common handler methods.

**Files:**
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/BnnchSortFabricClient.java`
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/client/FabricClientEvents.java`

---

**Step 1: Create `FabricClientEvents.java`**

Fabric uses `ScreenEvents.AFTER_INIT` to hook per-screen events, and `ClientPlayNetworking` for client-side packet handlers.

```java
package xyz.bannach.bnnch_sort.fabric.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import xyz.bannach.bnnch_sort.client.ClientLockedSlotsCache;
import xyz.bannach.bnnch_sort.client.ClientPreferenceCache;
import xyz.bannach.bnnch_sort.client.ScreenButtonInjector;
import xyz.bannach.bnnch_sort.client.SlotLockInputHandler;
import xyz.bannach.bnnch_sort.client.SlotLockRenderer;
import xyz.bannach.bnnch_sort.client.SortFeedback;
import xyz.bannach.bnnch_sort.client.SortKeyHandler;
import xyz.bannach.bnnch_sort.network.SyncLockedSlotsPayload;
import xyz.bannach.bnnch_sort.network.SyncPreferencePayload;

public class FabricClientEvents {

    public static void register() {
        // S2C packet handlers
        ClientPlayNetworking.registerGlobalReceiver(SyncPreferencePayload.TYPE,
            (payload, context) -> ClientPreferenceCache.handle(payload));
        ClientPlayNetworking.registerGlobalReceiver(SyncLockedSlotsPayload.TYPE,
            (payload, context) -> ClientLockedSlotsCache.handle(payload));

        // Per-screen event hooks
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?>)) return;

            ScreenButtonInjector.onScreenInit(screen);

            ScreenKeyboardEvents.afterKeyPress(screen).register(
                (s, key, scancode, modifiers) ->
                    SortKeyHandler.onKeyPressed(s, key, scancode));

            ScreenMouseEvents.beforeMouseClick(screen).register(
                (s, mouseX, mouseY, button) ->
                    SortKeyHandler.onMouseClicked(s, button));

            // Delegate to SlotLockInputHandler for Alt+Click locking
            ScreenMouseEvents.beforeMouseClick(screen).register(
                (s, mouseX, mouseY, button) ->
                    SlotLockInputHandler.onMouseClicked(s, mouseX, mouseY, button));

            ScreenEvents.afterRender(screen).register(
                (s, drawContext, mouseX, mouseY, tickDelta) -> {
                    SlotLockRenderer.onScreenRender(s, drawContext);
                    SortFeedback.onScreenRender(s, drawContext);
                });
        });
    }
}
```

> **Note:** The exact method signatures for `SlotLockInputHandler.onMouseClicked`, `SlotLockRenderer.onScreenRender`, and `SortFeedback.onScreenRender` depend on how they were refactored in Task 4. Match signatures accordingly.

**Step 2: Create `BnnchSortFabricClient.java`**

```java
package xyz.bannach.bnnch_sort.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import xyz.bannach.bnnch_sort.client.SortKeyHandler;
import xyz.bannach.bnnch_sort.fabric.client.FabricClientEvents;

public class BnnchSortFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SortKeyHandler.SORT_KEY);
        KeyBindingHelper.registerKeyBinding(SortKeyHandler.CYCLE_PREFERENCE_KEY);
        FabricClientEvents.register();
    }
}
```

**Step 3: Verify both builds**

```
./gradlew.bat :fabric:build
./gradlew.bat :neoforge:build
./gradlew.bat :neoforge:runGameTestServer
```

Expected: all succeed.

**Step 4: Commit**

```bash
git add .
git commit -m "feat: implement Fabric client side (keybindings, screen events, sync handlers)"
```

---

## Task 9: Add Fabric JSON config loader

**What changes:** Create a simple JSON config reader/writer for Fabric. It populates the same `Config.*` static fields that `NeoForgeConfig` populates on NeoForge. No new dependency — Gson is already bundled with Minecraft.

**Files:**
- Create: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/platform/FabricConfigLoader.java`
- Modify: `fabric/src/main/java/xyz/bannach/bnnch_sort/fabric/BnnchSortFabric.java`

---

**Step 1: Create `FabricConfigLoader.java`**

```java
package xyz.bannach.bnnch_sort.fabric.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import xyz.bannach.bnnch_sort.Config;
import xyz.bannach.bnnch_sort.ModifierKey;
import xyz.bannach.bnnch_sort.sorting.SortMethod;
import xyz.bannach.bnnch_sort.sorting.SortOrder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfigLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("bnnch_sort.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data == null) return;
            if (data.showSortButton != null)    Config.showSortButton    = data.showSortButton;
            if (data.lockModifierKey != null)   Config.lockModifierKey   = ModifierKey.valueOf(data.lockModifierKey);
            if (data.lockTintColor != null)     Config.lockTintColor     = (int) Long.parseLong(data.lockTintColor, 16);
            if (data.showLockTooltip != null)   Config.showLockTooltip   = data.showLockTooltip;
            if (data.defaultSortMethod != null) Config.defaultSortMethod = SortMethod.valueOf(data.defaultSortMethod);
            if (data.defaultSortOrder != null)  Config.defaultSortOrder  = SortOrder.valueOf(data.defaultSortOrder);
        } catch (IOException | IllegalArgumentException ignored) {
            // Fall back to defaults
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            ConfigData data = new ConfigData();
            data.showSortButton    = Config.showSortButton;
            data.lockModifierKey   = Config.lockModifierKey.name();
            data.lockTintColor     = String.format("%08X", Config.lockTintColor);
            data.showLockTooltip   = Config.showLockTooltip;
            data.defaultSortMethod = Config.defaultSortMethod.name();
            data.defaultSortOrder  = Config.defaultSortOrder.name();
            GSON.toJson(data, writer);
        } catch (IOException ignored) {}
    }

    private static class ConfigData {
        Boolean showSortButton;
        String  lockModifierKey;
        String  lockTintColor;
        Boolean showLockTooltip;
        String  defaultSortMethod;
        String  defaultSortOrder;
    }
}
```

**Step 2: Call `FabricConfigLoader.load()` in `BnnchSortFabric.onInitialize()`**

Add as the first line of `onInitialize`:

```java
FabricConfigLoader.load();
```

**Step 3: Final verification**

```
./gradlew.bat :fabric:build
./gradlew.bat :neoforge:build
./gradlew.bat :neoforge:runGameTestServer
```

Expected: all succeed. Both platform jars are produced:
- `neoforge/build/libs/bnnch_sort-neoforge-*.jar`
- `fabric/build/libs/bnnch_sort-fabric-*.jar`

**Step 4: Commit**

```bash
git add .
git commit -m "feat: add Fabric JSON config loader"
```

---

## What to watch for during implementation

**`ModCommands` registration on NeoForge:** Before moving `ModCommands.java` to common in Task 2, check how it is currently registered. Search for its usage across the codebase. If it is registered via a `RegisterCommandsEvent` subscriber, that subscriber stays in `neoforge/server/`. The common `ModCommands.register(CommandDispatcher<CommandSourceStack>)` method is called from both loaders' event hooks.

**`ClientPreferenceCache.handle()` and `ClientLockedSlotsCache.handle()`:** These are registered in `ModPayloads.java` as `ClientPreferenceCache::handle`. Check their current signatures. If they take `(payload, IPayloadContext)` but never use the context, simplify to `(payload)` before moving to common. Update both the `ModPayloads.java` registration and the Fabric `ClientPlayNetworking` registration to match.

**`SlotLockInputHandler` method signatures:** Inspect this file for NeoForge event imports before moving. Apply the same refactor pattern as `SortKeyHandler`: change method signatures to vanilla types, return boolean, let `ClientEvents.java` handle the event cancellation.

**Fabric `ScreenMouseEvents` return type:** Fabric's `ScreenMouseEvents.beforeMouseClick` listener does not use a return value to cancel — registering multiple listeners just calls them all. There is no cancellation mechanism. If `SlotLockInputHandler` or `SortKeyHandler` need to prevent further processing, that must be handled by checking state in later listeners rather than cancellation.

**Loom and `java-library` conflict:** If `common/build.gradle` applies both `java-library` and `fabric-loom`, Loom should win. If you see plugin conflict errors, remove `java-library` from `common/build.gradle` and rely on Loom's implicit Java plugin.

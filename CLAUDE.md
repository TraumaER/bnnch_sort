# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NeoForge mod for Minecraft 1.21.1 ("Better Inventory Sorter"). Java 21, Gradle 8.8, NeoForge ModDev plugin v2.0.140. Parchment mappings for human-readable Minecraft names.

## Build and Run Commands

All commands use the Windows Gradle wrapper:

- **Build:** `./gradlew.bat build`
- **Run client:** `./gradlew.bat runClient`
- **Run server:** `./gradlew.bat runServer`
- **Run game tests:** `./gradlew.bat runGameTestServer`
- **Data generation:** `./gradlew.bat runData`

Game tests use the NeoForge GameTest framework (not JUnit). Tests are filtered by the mod namespace `betterinventorysorter`. The `runGameTestServer` task will crash if no game tests are registered.

## Architecture

- **Mod entrypoint:** `src/main/java/xyz/bannach/betterinventorysorter/Betterinventorysorter.java` — annotated with `@Mod("betterinventorysorter")`. Registers blocks, items, and creative tabs via NeoForge Deferred Registers.
- **Configuration:** `src/main/java/xyz/bannach/betterinventorysorter/Config.java` — uses `ModConfigSpec` and `@EventBusSubscriber` for config loading.
- **Mod metadata template:** `src/main/templates/META-INF/neoforge.mods.toml` — property placeholders are expanded from `gradle.properties` by the `generateModMetadata` task. Edit this template (not the generated output in `build/`).
- **Assets:** `src/main/resources/assets/betterinventorysorter/` (lang files, textures, models, etc.)
- **Generated resources:** `src/generated/resources/` — output from `runData`. Do not edit by hand.

## Key Conventions

- The mod ID `betterinventorysorter` must stay aligned across `gradle.properties` (`mod_id`), the `@Mod` annotation, and `neoforge.mods.toml`.
- Register new items/blocks via Deferred Registers in the main mod class or dedicated registry classes.
- Mod version and dependency ranges are defined in `gradle.properties` and templated into `neoforge.mods.toml` at build time — change them in `gradle.properties`, not the template.
- Generated resources (`src/generated/resources/`) should not be hand-edited; update data providers and re-run `.\gradlew.bat runData`.

## Git and PR Conventions

- **Commits:** Use [Conventional Commits](https://www.conventionalcommits.org/) format (e.g., `feat: add sort keybinding`, `fix: prevent crash on empty inventory`, `chore: update dependencies`).
- **PR titles:** Must also use conventional commit format (e.g., `feat: add keybind-triggered inventory sorting`).
- **PR descriptions:** Include a brief description of the changes, a summary of what was changed, and any testing steps that may need to be taken.
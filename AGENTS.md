# Project Overview

- Minecraft NeoForge mod (MC 1.21.1) using Java 21 and Gradle.
- Mod metadata is templated in `src/main/templates/META-INF/neoforge.mods.toml` and expanded via `generateModMetadata`.

# Key Paths

- Main mod entrypoint: `src/main/java/xyz/bannach/bnnch_sort/BnnchSort.java`
- Mod config: `src/main/java/xyz/bannach/bnnch_sort/Config.java`
- Assets: `src/main/resources/assets/bnnch_sort`
- Generated resources: `src/generated/resources` (do not edit by hand)

# Build And Run

- Build: `.\gradlew.bat build`
- Run client: `.\gradlew.bat runClient`
- Run server: `.\gradlew.bat runServer`
- Run game tests: `.\gradlew.bat runGameTestServer`
- Generate data: `.\gradlew.bat runData`

# Conventions

- Keep mod id `bnnch_sort` aligned with `mod_id` in `gradle.properties` and `@Mod` in the entrypoint.
- Prefer adding new items/blocks via NeoForge Deferred Registers in the main mod class or dedicated registry classes.
- Avoid editing generated files; update templates or sources and re-run generation instead.

# Feature: Fabric Multi-Loader Support

## Overview

Add Fabric loader support to the existing NeoForge-only mod, enabling the mod to run on both NeoForge and Fabric mod loaders. This multi-loader architecture will use a multi-project Gradle build with a shared common module containing platform-agnostic code and separate loader-specific modules for NeoForge and Fabric. The feature targets a 2.0.0 major release with 100% feature parity between loaders, distributed as separate JAR artifacts to CurseForge and Modrinth. This expansion will reach a wider player audience, enable compatibility with Fabric-only mods in modpacks, and future-proof the mod against single-loader platform risks.

## Functional Requirements

### FR-001: Multi-Project Gradle Build System
The build system shall consist of three Gradle subprojects: `common` (platform-agnostic code), `fabric` (Fabric loader implementation), and `neoforge` (NeoForge loader implementation), where the common module uses a shared sourceSet accessible to both loader-specific modules.

### FR-002: Common Module Scope
The common module shall contain core sorting logic, configuration system abstractions, inventory access interfaces, and network packet definitions.

### FR-003: Platform Abstraction Layer
When platform-specific functionality is required, the system shall use a service interface pattern where interfaces are defined in the common module and implemented in loader-specific modules (fabric/neoforge).

### FR-004: Fabric Build Artifact
When the Fabric project is built, the system shall produce a `bnnch-sort-2.0.0-fabric.jar` artifact that runs on Fabric loader with Minecraft 1.21.1.

### FR-005: NeoForge Build Artifact
When the NeoForge project is built, the system shall produce a `bnnch-sort-2.0.0-neoforge.jar` artifact that runs on NeoForge loader with Minecraft 1.21.1.

### FR-006: Synchronized Versioning
Both loader artifacts shall share the same major.minor.patch version number (e.g., 2.0.0), with loader-specific suffixes appended to the artifact name.

### FR-007: Feature Parity - Sorting Functionality
The Fabric version shall implement identical sorting functionality to the NeoForge version, including all sorting algorithms, keybindings, and inventory interaction behaviors.

### FR-008: Feature Parity - Configuration
The Fabric version shall support the same configuration options as the NeoForge version, though the underlying config implementation may use loader-specific APIs.

### FR-009: Feature Parity - Slot Locking
The Fabric version shall support inventory slot locking with the same behavior and keybindings as the NeoForge version.

### FR-010: Feature Parity - Keybindings
The Fabric version shall support the same keybinding configuration as the NeoForge version, preventing keybind triggers during text input.

### FR-011: Separate Configuration Storage
While players have a Fabric installation, the Fabric mod shall use Fabric-specific configuration file paths separate from NeoForge configuration files, with no automatic migration required.

### FR-012: Zero-Impact NeoForge Compatibility
When refactoring code for multi-loader support, the system shall maintain identical behavior in the NeoForge artifact compared to pre-2.0.0 versions, ensuring existing users experience no breaking changes in functionality.

### FR-013: Fabric API Dependency
The Fabric artifact shall depend on Fabric API for platform abstractions (specific version TBD based on research).

### FR-014: CI Build for Both Artifacts
When the CI/CD pipeline builds the project, the system shall produce both `fabric.jar` and `neoforge.jar` artifacts from a single repository.

### FR-015: Manual Publishing Process
When a 2.0.0 release is ready, maintainers shall manually publish both artifacts to CurseForge and Modrinth with appropriate loader tags.

### FR-016: Mod Metadata Templating
The build system shall generate loader-specific `fabric.mod.json` and `neoforge.mods.toml` from templates with property expansion from `gradle.properties`, maintaining the synchronized version number and mod metadata.

## Non-Functional Requirements

### Performance
- Abstraction overhead: Platform abstraction layer should add negligible runtime overhead (< 1% CPU impact)
- Build time: Full clean build of both artifacts should complete in < 5 minutes on standard development hardware
- Artifact size: Each loader-specific JAR should be < 500KB

### Maintainability
- Code duplication: Maximum 10% code duplication between loader-specific modules (aim for 90% shared code in common module)
- Build system complexity: Gradle build should be understandable by contributors familiar with standard multi-project Gradle setups
- Documentation: Architecture must be documented with diagrams showing module dependencies

### Compatibility
- Minecraft version: 1.21.1 (current project target)
- Java version: Java 21 (current project requirement)
- NeoForge backward compatibility: NeoForge artifact behavior identical to 1.x versions
- Fabric API compatibility: Compatible with latest stable Fabric API for MC 1.21.1

### Testing
- Common module: Unit tests using JUnit for platform-agnostic logic
- NeoForge module: GameTest framework integration tests (existing framework)
- Fabric module: Fabric test mod for loader-specific functionality testing
- Coverage target: > 70% coverage for common module

### Documentation
- Developer setup guide for building multi-loader project
- Architecture documentation explaining common/fabric/neoforge structure
- Updated CLAUDE.md with new build commands and conventions
- Contribution guide updates for multi-loader development patterns

## Acceptance Criteria

### AC-001: Gradle Project Structure Created
Given the repository is restructured for multi-loader support
When the project is opened in an IDE
Then three Gradle subprojects exist: `:common`, `:fabric`, `:neoforge`
And the common module has no loader-specific dependencies
And both fabric and neoforge modules depend on the common module
And gradle.properties defines shared version properties

### AC-002: Fabric Artifact Builds Successfully
Given the Gradle build is configured
When `./gradlew.bat :fabric:build` is executed
Then a `fabric/build/libs/bnnch-sort-2.0.0-fabric.jar` file is created
And the JAR contains a valid `fabric.mod.json` with mod metadata
And the JAR loads successfully in a Fabric development environment

### AC-003: NeoForge Artifact Builds Successfully
Given the Gradle build is configured
When `./gradlew.bat :neoforge:build` is executed
Then a `neoforge/build/libs/bnnch-sort-2.0.0-neoforge.jar` file is created
And the JAR contains a valid `META-INF/neoforge.mods.toml` with mod metadata
And the JAR loads successfully in a NeoForge development environment

### AC-004: Both Artifacts Build Together
Given the Gradle build is configured
When `./gradlew.bat build` is executed
Then both fabric and neoforge JARs are built successfully
And both artifacts share the same version number from gradle.properties

### AC-005: Inventory Sorting Works on Fabric
Given a Fabric client is running with the mod installed
When a player opens an inventory and triggers the sort keybinding
Then the inventory is sorted using the same algorithm as NeoForge
And the sorting completes in < 50ms for a full inventory

### AC-006: Inventory Sorting Works on NeoForge
Given a NeoForge client is running with the 2.0.0 mod installed
When a player opens an inventory and triggers the sort keybinding
Then the inventory is sorted identically to version 1.x behavior
And no regressions are observed in sorting behavior

### AC-007: Slot Locking Works on Fabric
Given a Fabric client with the mod installed
When a player locks an inventory slot using the configured keybinding
Then the slot is visually marked as locked
And the slot is excluded from sorting operations
And the behavior matches NeoForge slot locking exactly

### AC-008: Configuration Works on Fabric
Given a Fabric client with the mod installed
When a player modifies configuration options
Then the changes are persisted to a Fabric-specific config file
And the configuration changes take effect (keybindings, sort behavior)
And the config file location is separate from NeoForge config

### AC-009: Keybindings Don't Trigger During Text Input on Fabric
Given a Fabric client with the mod installed
When a player is typing in a text field or chat
Then sort and lock keybindings are suppressed
And keybindings only work when not in text input mode
And the behavior matches NeoForge implementation

### AC-010: Common Module Unit Tests Pass
Given unit tests are written for common module sorting logic
When `./gradlew.bat :common:test` is executed
Then all unit tests pass
And code coverage is > 70%
And tests run without any loader-specific dependencies

### AC-011: NeoForge GameTests Still Work
Given the existing NeoForge GameTest suite
When `./gradlew.bat :neoforge:runGameTestServer` is executed
Then all existing game tests pass
And no regressions are introduced in the NeoForge implementation

### AC-012: Fabric Test Mod Validates Core Features
Given a Fabric test mod is created
When the test mod is run
Then core sorting functionality is validated on Fabric
And slot locking functionality is validated on Fabric
And the test environment confirms Fabric API integration

### AC-013: CI Builds Both Artifacts
Given GitHub Actions CI configuration is updated
When a commit is pushed to main
Then the CI workflow builds both fabric.jar and neoforge.jar
And both artifacts are available as build artifacts
And build failures are caught for both loaders

### AC-014: Developer Documentation Exists
Given the multi-loader architecture is implemented
When a new contributor reads the documentation
Then they understand the common/fabric/neoforge structure
And they can successfully build both artifacts locally
And CLAUDE.md reflects the new build commands
And architecture diagrams show module dependencies

### AC-015: Manual Publishing to CurseForge Works
Given both artifacts are built for release 2.0.0
When the artifacts are manually uploaded to CurseForge
Then separate mod files exist with "Fabric" and "NeoForge" loader tags
And the version numbers match (2.0.0) for both
And players can download the correct version for their loader

### AC-016: Manual Publishing to Modrinth Works
Given both artifacts are built for release 2.0.0
When the artifacts are manually uploaded to Modrinth
Then separate mod versions exist with loader filters
And the version numbers match (2.0.0) for both
And the mod page explains which JAR to download

## Error Handling

| Error Condition | Build Phase | User Message / Resolution |
|-----------------|-------------|---------------------------|
| Loader-specific code in common module | Compile-time | Gradle build fails with "Cannot resolve NeoForge/Fabric API in common module" |
| Missing platform implementation | Runtime | Mod fails to load with clear error: "Platform service X not implemented for [loader]" |
| Version mismatch between artifacts | Build-time | CI validation fails if versions differ between fabric and neoforge |
| Wrong JAR on wrong loader | Runtime | Mod loader rejects JAR with clear message about loader incompatibility |
| Fabric API not installed (Fabric) | Runtime | Fabric loader shows missing dependency error listing required Fabric API version |
| Incompatible Minecraft version | Runtime | Mod loader prevents loading with "Requires Minecraft 1.21.1" message |
| Conflicting mod IDs | Runtime | Loader prevents both fabric.jar and neoforge.jar from loading together |
| Build fails for one loader | CI Build | GitHub Actions marks build as failed, maintainers investigate loader-specific issue |

## Implementation TODO

### Phase 1: Project Restructuring
- [ ] Create multi-project Gradle structure (common, fabric, neoforge subprojects)
- [ ] Move existing NeoForge code to `neoforge/` subproject
- [ ] Extract platform-agnostic code to `common/` subproject
- [ ] Define service interfaces for platform abstractions in common module
- [ ] Update `gradle.properties` with multi-project configuration
- [ ] Configure Gradle `settings.gradle.kts` to include all subprojects
- [ ] Set up shared sourceSet from common to fabric/neoforge

### Phase 2: NeoForge Adapter
- [ ] Implement platform service interfaces for NeoForge
- [ ] Create NeoForge-specific entry point (`@Mod` annotation)
- [ ] Implement NeoForge config adapter
- [ ] Implement NeoForge keybinding adapter
- [ ] Implement NeoForge inventory access adapter
- [ ] Verify NeoForge artifact builds successfully
- [ ] Run existing GameTests to ensure no regressions

### Phase 3: Fabric Implementation
- [ ] Research Fabric API dependency requirements for 1.21.1
- [ ] Create Fabric subproject with Fabric Loom plugin
- [ ] Implement platform service interfaces for Fabric
- [ ] Create Fabric-specific entry point (ModInitializer)
- [ ] Implement Fabric config adapter (using Fabric API or custom)
- [ ] Implement Fabric keybinding adapter (using Fabric Keybindings API)
- [ ] Implement Fabric inventory access adapter
- [ ] Create `fabric.mod.json` template with property expansion
- [ ] Verify Fabric artifact builds successfully

### Phase 4: Testing Infrastructure
- [ ] Write JUnit unit tests for common module sorting logic
- [ ] Ensure existing NeoForge GameTests pass in new structure
- [ ] Create Fabric test mod for integration testing
- [ ] Add test cases for slot locking on Fabric
- [ ] Add test cases for keybinding suppression during text input on Fabric
- [ ] Verify > 70% code coverage for common module

### Phase 5: CI/CD Integration
- [ ] Update GitHub Actions workflow to build both artifacts
- [ ] Add CI validation that both artifacts share same version
- [ ] Configure CI to run NeoForge GameTests
- [ ] Configure CI to run Fabric test mod (if feasible)
- [ ] Set up build artifacts upload for both JARs
- [ ] Document manual publishing process to CurseForge
- [ ] Document manual publishing process to Modrinth

### Phase 6: Documentation
- [ ] Write developer setup guide for multi-loader development
- [ ] Create architecture diagram showing common/fabric/neoforge dependencies
- [ ] Update CLAUDE.md with new build commands and conventions
- [ ] Add contribution guide section for multi-loader patterns
- [ ] Document platform abstraction service interface pattern
- [ ] Create release checklist for publishing both loaders

### Phase 7: Feature Parity Validation
- [ ] Manual test: inventory sorting on Fabric matches NeoForge behavior
- [ ] Manual test: slot locking on Fabric matches NeoForge behavior
- [ ] Manual test: keybindings on Fabric match NeoForge configuration
- [ ] Manual test: text input suppression works identically on both loaders
- [ ] Manual test: config changes persist correctly on both loaders
- [ ] Manual test: modded containers (SlotItemHandler) work on Fabric

### Phase 8: Release Preparation
- [ ] Bump version to 2.0.0 in gradle.properties
- [ ] Generate release notes highlighting multi-loader support
- [ ] Create CurseForge project pages for both loaders (or update existing)
- [ ] Create Modrinth project page with loader filters
- [ ] Build final release artifacts for both loaders
- [ ] Manually publish to CurseForge with loader tags
- [ ] Manually publish to Modrinth with loader filters
- [ ] Tag release in git: `v2.0.0`

## Out of Scope

- **Quilt Loader Support**: Only Fabric and NeoForge are in scope for 2.0.0. Quilt support can be considered in future releases if there is demand.
- **Automatic Config Migration**: Players switching from NeoForge to Fabric (or vice versa) will need to reconfigure the mod. No automatic migration tool will be provided.
- **Unified JAR Artifact**: A single JAR that runs on both loaders is not feasible due to loader-specific metadata requirements. Separate JARs are required.
- **Backwards Compatibility with Forge**: Only NeoForge is supported. Legacy Forge (pre-NeoForge split) is not in scope.
- **Automated Publishing**: CI/CD will build artifacts but not automatically publish to CurseForge/Modrinth. Manual review and publishing is required initially.
- **Minecraft Version Expansion**: This spec focuses on 1.21.1 only. Multi-version support (e.g., 1.20.1, 1.21.x) is a separate feature.
- **Server-Side-Only Mode**: Both loaders require client-side installation. Server-only operation is not planned for 2.0.0.

## Open Questions

- [x] **Fabric API Version**: Which version of Fabric API should be required for MC 1.21.1? (Marked for research in Phase 3)
- [ ] **Parchment Mappings on Fabric**: Should the Fabric subproject also use Parchment mappings for consistency, or use standard Yarn mappings?
- [ ] **License Implications**: Are there any license considerations for using both NeoForge and Fabric in the same repository?
- [ ] **Mod ID Consistency**: Should both loaders use the same mod ID `bnnch_sort`, or should they differ (e.g., `bnnch_sort_fabric`, `bnnch_sort_neoforge`) to prevent confusion?
- [ ] **Hotfix Versioning**: If a loader-specific bug is found, how should versioning work? (e.g., 2.0.1-fabric, 2.0.0-neoforge, breaking synchronization?)

## Success Metrics

- **Adoption Rate**: Track downloads of Fabric artifact vs NeoForge artifact on CurseForge/Modrinth in the first 90 days post-launch
- **Bug Reports**: Monitor for loader-specific bugs or feature parity issues reported by users
- **Build Success Rate**: CI builds should succeed for both loaders > 95% of the time
- **Community Feedback**: Gather feedback from Fabric users about feature completeness compared to NeoForge
- **Maintenance Overhead**: Track time spent on loader-specific bug fixes vs shared code improvements (target: < 20% of development time on loader-specific issues)
# Contributing to Bnnch: Sort

Thank you for your interest in contributing to Bnnch: Sort! This document provides guidelines and
instructions for contributing to the project.

## Prerequisites

Before contributing, ensure you have:

- **JDK 21** or later installed
- **Git** for version control
- A basic understanding of **NeoForge 1.21.1** mod development
- An IDE such as IntelliJ IDEA (recommended) or Eclipse

## Getting Started

### 1. Fork and Clone

```bash
git clone https://github.com/YOUR_USERNAME/bnnch_sort.git
cd bnnch_sort
```

### 2. Build the Project

```bash
./gradlew.bat build
```

### 3. Run the Development Client

```bash
./gradlew.bat runClient
```

### 4. Import into IDE

For IntelliJ IDEA:

1. Open the project folder
2. Import as a Gradle project
3. Let Gradle sync complete
4. Run `genIntellijRuns` if run configurations aren't generated automatically

## Project Structure

```
src/
├── main/
│   ├── java/xyz/bannach/bnnch_sort/
│   │   ├── BnnchSort.java    # Mod entrypoint (@Mod annotation)
│   │   ├── Config.java                   # Configuration (ModConfigSpec)
│   │   ├── ModAttachments.java           # Data attachments for persistence
│   │   ├── sorting/                      # Core sorting logic
│   │   ├── client/                       # Client-side UI and input
│   │   ├── server/                       # Server-side handlers
│   │   ├── network/                      # Network packets
│   │   ├── commands/                     # Brigadier slash commands
│   │   └── test/                         # NeoForge GameTests
│   ├── resources/assets/bnnch_sort/
│   │   ├── lang/                         # Translation files
│   │   └── textures/                     # Textures and icons
│   └── templates/META-INF/
│       └── neoforge.mods.toml            # Mod metadata template
├── generated/resources/                   # Auto-generated (do not edit)
└── test/                                  # Test resources
```

## Code Style and Conventions

### Mod ID Alignment

The mod ID `bnnch_sort` must remain consistent across:

- `gradle.properties` (`mod_id` property)
- `@Mod` annotation in `BnnchSort.java`
- `neoforge.mods.toml` template

### Registration Pattern

Register new items, blocks, or other game objects using NeoForge Deferred Registers:

```java
public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, MOD_ID);

public static final DeferredItem<Item> MY_ITEM =
        ITEMS.register("my_item", () -> new Item(new Item.Properties()));
```

### Javadoc Requirements

All public classes and methods should include Javadoc:

```java
/**
 * Brief description of the class.
 *
 * <h2>Details</h2>
 * Additional information about usage and behavior.
 *
 * @since 1.0.0
 * @see RelatedClass
 */
public class MyClass {
}
```

### Configuration Changes

- Edit configuration in `Config.java` using `ModConfigSpec` builders
- Version and dependency changes go in `gradle.properties`, not the TOML template
- Never hand-edit files in `src/generated/resources/`

## Git Workflow

### Branch Naming

Create feature branches with the format:

```
your-name/issue-number-brief-description
```

Example: `johndoe/42-add-creative-tab`

### Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/) format:

| Type        | Description              |
|-------------|--------------------------|
| `feat:`     | New feature              |
| `fix:`      | Bug fix                  |
| `docs:`     | Documentation changes    |
| `chore:`    | Maintenance tasks        |
| `refactor:` | Code refactoring         |
| `test:`     | Adding or updating tests |

Examples:

```
feat: add quantity-based sorting option
fix: prevent crash when sorting empty containers
docs: update README with new keybindings
chore: update NeoForge to 21.1.50
```

### Pull Requests

1. **Title:** Must use conventional commit format
2. **Description:** Include:
    - Brief summary of changes
    - Why the changes were made
    - Testing steps if applicable
3. **Target branch:** `main`

## Building and Testing

### Gradle Commands

| Command                           | Description               |
|-----------------------------------|---------------------------|
| `./gradlew.bat build`             | Build the mod JAR         |
| `./gradlew.bat runClient`         | Launch development client |
| `./gradlew.bat runServer`         | Launch development server |
| `./gradlew.bat runGameTestServer` | Run GameTest suite        |
| `./gradlew.bat runData`           | Generate data files       |

### Running Tests

Tests use the NeoForge GameTest framework (not JUnit):

```bash
./gradlew.bat runGameTestServer
```

Test files are located in `src/main/java/xyz/bannach/bnnch_sort/test/`.

### Writing Tests

```java

@GameTestHolder("bnnch_sort")
@PrefixGameTestTemplate(false)
public class MyGameTests {

    @GameTest(template = "empty")
    public static void testMyFeature(GameTestHelper helper) {
        // Test implementation
        helper.succeed();
    }
}
```

### Data Generation

If you modify data providers:

```bash
./gradlew.bat runData
```

This regenerates files in `src/generated/resources/`. Commit the regenerated files with your changes.

## Adding New Features

### New Sorting Method

1. Add the method to `SortMethod` enum in `sorting/`
2. Implement the comparator logic in `ItemSorter`
3. Add translations in `assets/bnnch_sort/lang/`
4. Update configuration if needed
5. Add GameTests for the new method

### New Network Packet

1. Create a payload class in `network/` implementing `CustomPacketPayload`
2. Register the packet in `ModPayloads`
3. Implement handlers for client/server sides

### New Keybinding

1. Add the keybind registration in `client/ModKeybinds`
2. Implement the handler in the appropriate client class
3. Add translations for the keybind name

## Submission Checklist

Before submitting a PR, verify:

- [ ] Code compiles without errors: `./gradlew.bat build`
- [ ] Development client runs: `./gradlew.bat runClient`
- [ ] All tests pass: `./gradlew.bat runGameTestServer`
- [ ] Data generation runs (if applicable): `./gradlew.bat runData`
- [ ] Mod ID is consistent across all files
- [ ] New public classes/methods have Javadoc
- [ ] Translations are added for new user-facing text
- [ ] Commits follow conventional commit format
- [ ] PR title uses conventional commit format
- [ ] PR description explains the changes

## Getting Help

- Open an issue for bugs or feature requests
- Check existing issues before creating new ones
- Reference issue numbers in commits and PRs when applicable

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

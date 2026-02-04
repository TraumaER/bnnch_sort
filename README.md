![Bnnch Sort Banner](docs/images/bnnch_sort_banner.png)

# Bnnch: Sort

A NeoForge mod for Minecraft 1.21.1 that provides intelligent inventory sorting with multiple sorting methods,
customizable preferences, and an intuitive user interface.

## Features

- **Four Sorting Methods**: Sort items alphabetically, by category, by quantity, or by mod ID
- **Ascending/Descending Order**: Reverse any sorting method with a single keypress
- **Slot Locking**: Lock specific inventory slots to exclude them from sorting
- **Smart Stack Merging**: Automatically combines partial stacks before sorting
- **Visual Feedback**: On-screen overlay shows current sort preferences
- **Keybindings**: Quick keyboard shortcuts for sorting and cycling preferences
- **Slash Commands**: Full command support for sorting and configuration
- **UI Button**: Optional sort button on container screens
- **Per-Player Preferences**: Settings persist across sessions and respawn

## Sorting Methods

| Method           | Description                                                         |
|------------------|---------------------------------------------------------------------|
| **Alphabetical** | Sorts items by display name (A-Z or Z-A)                            |
| **Category**     | Groups items by creative tab, then alphabetically within each group |
| **Quantity**     | Sorts by stack count (highest to lowest, or reversed)               |
| **Mod ID**       | Groups items by mod namespace, then alphabetically within each mod  |

## Installation

1. Install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1
2. Download the latest release of Bnnch: Sort
3. Place the `.jar` file in your `mods` folder
4. Launch Minecraft

## Usage

### Keybindings

| Key             | Action                                             |
|-----------------|----------------------------------------------------|
| `R`             | Sort the inventory region under your cursor        |
| `P`             | Cycle to the next sort preference (method + order) |
| `Alt` + `Click` | Toggle slot lock on a player inventory slot        |

The sort key automatically detects which inventory section to sort based on mouse position:

- Hovering over a container (chest, shulker box) sorts that container
- Hovering over your main inventory sorts the 27-slot grid
- Hovering over your hotbar sorts just the hotbar

### Slot Locking

Hold the modifier key (default: `Alt`) and click a player inventory slot to lock or unlock it. Locked slots are
highlighted with a colored tint and are excluded from sorting operations.

- **Locked slots keep their contents** in place when sorting occurs
- **Locked empty slots** remain empty during sorts
- **Non-full stacks** in locked slots can still receive matching items merged from unlocked slots during sorting
- Lock state **persists across death, relog, and dimension changes**
- Only player inventory slots (main grid and hotbar) can be locked — container slots cannot

Hovering over an empty locked slot shows a tooltip with unlock instructions. The modifier key, tint color, and tooltip
visibility are all configurable in the client config.

### Commands

All commands use the `/bnnchsort` prefix:

```
/bnnchsort sortinv [region]    - Sort your inventory
                           Regions: all, main, hotbar (default: main)

/bnnchsort change <method> <order>  - Set your sorting preference
                                Methods: alphabetical, category, quantity, mod_id
                                Orders: ascending, descending

/bnnchsort reset               - Reset preferences to server defaults

/bnnchsort unlock              - Unlock all locked inventory slots

/bnnchsort config [key]        - View configuration settings
                           Keys: method, order, button

/bnnchsort help                - Display help and current preferences
                           Also shows locked slot counts
```

### UI Button

A small sort button appears on compatible container screens (chests, shulker boxes, player inventory). Click it to sort
the container contents. The button tooltip shows your current sort method and order.

The button can be disabled in the client configuration.

## Configuration

### Client Configuration

Located at `config/bnnch_sort-client.toml`

| Option            | Default    | Description                                                |
|-------------------|------------|------------------------------------------------------------|
| `showSortButton`  | `true`     | Display the sort button on container screens               |
| `lockModifierKey` | `ALT`      | Modifier key for locking slots (`ALT`, `CONTROL`, `SHIFT`) |
| `lockTintColor`   | `800000FF` | ARGB hex color for the locked slot overlay tint            |
| `showLockTooltip` | `true`     | Show unlock hint tooltip on locked slots                   |

### Server Configuration

Located at `config/bnnch_sort-server.toml`

| Option              | Default        | Description                            |
|---------------------|----------------|----------------------------------------|
| `defaultSortMethod` | `ALPHABETICAL` | Default sorting method for new players |
| `defaultSortOrder`  | `ASCENDING`    | Default sort order for new players     |

## Compatibility

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.x or higher
- **Java**: 21 or higher

### Supported Containers

The mod sorts items in:

- Player inventory (main grid and hotbar)
- Chests and double chests
- Shulker boxes
- Ender chests
- Barrels

### Protected Slots

The following slot types are never sorted:

- Armor slots
- Offhand slot
- Crafting grid slots
- Furnace input/output/fuel slots
- Other special result slots
- Player-locked slots (see [Slot Locking](#slot-locking))

## Building from Source

### Prerequisites

- JDK 21 or higher
- Git

### Build Commands

```bash
# Clone the repository
git clone https://github.com/yourusername/bnnch_sort.git
cd bnnch_sort

# Build the mod
./gradlew.bat build

# Run the client for testing
./gradlew.bat runClient

# Run the dedicated server
./gradlew.bat runServer

# Run game tests
./gradlew.bat runGameTestServer

# Generate data files
./gradlew.bat runData
```

The compiled `.jar` file will be in `build/libs/`.

## Project Structure

```
src/main/java/xyz/bannach/bnnch_sort/
├── BnnchSort.java               # Mod entrypoint
├── Config.java                  # Configuration management
├── ModAttachments.java          # Player preference persistence
├── ModifierKey.java             # Modifier key enum (Alt/Ctrl/Shift)
├── sorting/                     # Core sorting logic
│   ├── SortMethod.java          # Sorting method enum
│   ├── SortOrder.java           # Sort order enum
│   ├── SortPreference.java      # Preference record
│   ├── ItemSorter.java          # Main sorting pipeline
│   ├── LockedSlots.java         # Locked slot state record
│   └── comparator/              # Sort comparators
├── client/                      # Client-side UI and input
│   ├── SortKeyHandler.java      # Keybinding handling
│   ├── SlotLockInputHandler.java # Slot lock click handling
│   ├── SlotLockRenderer.java    # Locked slot visual overlay
│   ├── ClientLockedSlotsCache.java # Client-side lock state
│   ├── SortButton.java          # UI button widget
│   ├── SortFeedback.java        # Visual feedback overlay
│   └── ScreenButtonInjector.java
├── server/                      # Server-side handlers
│   ├── SortHandler.java         # Sort request processing
│   ├── LockHandler.java         # Slot lock toggle processing
│   └── PreferenceHandler.java   # Preference management
├── commands/                    # Slash commands
│   └── ModCommands.java
└── network/                     # Network packets
    ├── SortRequestPayload.java
    ├── CyclePreferencePayload.java
    ├── SyncPreferencePayload.java
    ├── ToggleLockPayload.java
    └── SyncLockedSlotsPayload.java
```

## Technical Details

### Sorting Pipeline

1. **Condense**: Merge partial stacks of identical items into full stacks
2. **Filter**: Remove empty slots from consideration
3. **Sort**: Apply the selected comparator
4. **Reverse**: Reverse order if descending preference is set
5. **Pad**: Restore empty slots to maintain slot count

### Network Protocol

The mod uses five custom network packets:

- `SortRequestPayload`: Client requests a sort operation
- `CyclePreferencePayload`: Client requests preference cycling
- `SyncPreferencePayload`: Server syncs preferences to client
- `ToggleLockPayload`: Client requests toggling a slot lock
- `SyncLockedSlotsPayload`: Server syncs locked slot state to client

All packets use protocol version "1".

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

### Quick Start

1. Fork the repository
2. Create a branch from `main` (or `mc/*` for version-specific changes):
    - `feature/<name>` for new features
    - `fix/<name>` for bug fixes
3. Use [Conventional Commits](https://www.conventionalcommits.org/) for commit messages
4. Submit a pull request with a clear description

### Branching Strategy

See [docs/BRANCHING_AND_RELEASE.md](docs/BRANCHING_AND_RELEASE.md) for details on:

- Branch structure and naming conventions
- Multi-version support workflow
- Release process

## License

This project is licensed under the MIT License.

## Credits

Built with:

- [NeoForge](https://neoforged.net/) - Minecraft modding framework
- [Parchment](https://parchmentmc.org/) - Human-readable mappings

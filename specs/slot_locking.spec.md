# Feature: Slot Locking

## Overview

Allow players to lock specific inventory slots so that sorting does not move items in or out of them. Locked slots
containing a stackable, non-full stack will still accept merges from identical items found elsewhere in the inventory
during a sort. This gives players fine-grained control over inventory layout while preserving the convenience of
automatic sorting.

**Target users:** All players using Bnnch: Sort who want to keep specific items in fixed positions (e.g., tools on
hotbar, building blocks in a preferred slot).

**Supported regions:** Player main inventory (slots 9-35) and hotbar (slots 0-8). Container inventories are out of
scope.

---

## Functional Requirements

### FR-LOCK-001: Toggle Slot Lock

While a container screen is open, when the player holds the configured modifier key and clicks a player inventory slot,
the system shall toggle the lock state of that slot.

### FR-LOCK-002: Configurable Modifier Key

The system shall provide a client-side configuration option `lockModifierKey` (default: Right Alt) that determines which
modifier key must be held to toggle slot locks.

### FR-LOCK-003: Visual Lock Indicator

While a container screen is open, the system shall render a colored tint overlay on each locked slot in the player's
inventory to visually distinguish it from unlocked slots. The tint color shall default to semi-transparent blue and be
configurable via a client-side config option `lockTintColor`.

### FR-LOCK-003a: Lock Tooltip

While a container screen is open and `showLockTooltip` is enabled (default: true), when the player hovers over a locked
slot, the system shall display a tooltip indicating the slot is locked and how to unlock it (e.g.,
"Locked — hold [key] + click to unlock").

### FR-LOCK-004: Sorting Exclusion

When a sort is triggered, the system shall exclude all locked slots from the sorting pipeline — items in locked slots
shall not be moved, and unlocked items shall not be sorted into locked slots.

### FR-LOCK-005: Merge Into Locked Slot

While a locked slot contains a stackable item that is not at its maximum stack size, when a sort is triggered and the
sortable inventory contains additional items of the same type, the system shall merge items into the locked slot up to
the item's maximum stack size before sorting the remainder normally.

### FR-LOCK-006: Locked Empty Slot Preservation

While a locked slot is empty, when a sort is triggered, the system shall keep the slot empty — no items shall be sorted
into it.

### FR-LOCK-007: Lock Persistence

The system shall persist locked slot data per-player via NeoForge data attachments, surviving death, relog, and
dimension
changes.

### FR-LOCK-008: Lock Toggle Audio Feedback

When a slot lock is toggled, the system shall play a subtle UI click sound effect.

### FR-LOCK-009: Unlock All Command

When a player executes `/bnnchsort unlock`, the system shall unlock all locked slots in the player's main inventory and
hotbar.

### FR-LOCK-010: Help Command Lock Info

When a player executes `/bnnchsort help`, the system shall display the count of locked slots per region (main inventory
and hotbar) alongside existing preference information.

### FR-LOCK-011: Position-Based Locking

The system shall associate locks with slot indices, not with item identities. A locked slot remains locked regardless of
which item occupies it or whether it is empty.

---

## Non-Functional Requirements

### Performance

- Lock toggle must be instantaneous from the player's perspective (handled within the same tick).
- The sorting pipeline modification (excluding locked slots, performing merges) must not introduce perceptible delay
  compared to the current sort operation.
- Lock state checks during sorting shall be O(1) per slot (set/bitfield lookup).

### Security

- Lock state is authoritative on the server. The client sends toggle requests; the server validates and applies them.
- Lock toggle requests shall only be accepted for valid player inventory slot indices (0-35). Requests for out-of-range
  or non-player slots shall be silently ignored.

### Data

- Lock data shall be stored as a set of locked slot indices in a NeoForge data attachment on the player entity.
- Serialization shall use a codec compatible with NBT (e.g., list of integers).
- Lock data shall persist identically to existing `SORT_PREFERENCE` attachment (survives death via `copyOnDeath`).

---

## Acceptance Criteria

### AC-001: Lock a Slot

Given a player with an open inventory screen
When the player holds the configured modifier key and clicks a player inventory slot
Then the slot becomes locked
And a colored tint overlay appears on the slot
And a click sound plays

### AC-002: Unlock a Slot

Given a player with a locked slot and an open inventory screen
When the player holds the configured modifier key and clicks the locked slot
Then the slot becomes unlocked
And the colored tint overlay is removed
And a click sound plays

### AC-003: Sort Skips Locked Slots

Given a player with slots 0 and 1 locked (containing a Diamond Sword and a stack of Torches)
When the player triggers a sort on the hotbar
Then slots 0 and 1 remain unchanged
And all other hotbar slots are sorted according to current preferences

### AC-004: Merge Into Locked Non-Full Stack

Given a player with slot 5 locked containing 32 Stone and 48 Stone elsewhere in the inventory
When the player triggers a sort on the main inventory
Then slot 5 contains 64 Stone (filled to max)
And the remaining 16 Stone are sorted normally into unlocked slots

### AC-005: Locked Empty Slot Stays Empty

Given a player with slot 3 locked and empty
When the player triggers a sort
Then slot 3 remains empty
And all items are sorted into unlocked slots only

### AC-006: Persistence Across Death

Given a player with slots 0, 1, and 9 locked
When the player dies and respawns
Then slots 0, 1, and 9 are still locked

### AC-007: Persistence Across Relog

Given a player with locked slots who disconnects
When the player reconnects
Then all previously locked slots remain locked

### AC-008: Unlock All Command

Given a player with 5 locked slots across main and hotbar
When the player executes `/bnnchsort unlock`
Then all slots are unlocked
And a confirmation message is displayed

### AC-009: Help Command Shows Lock Count

Given a player with 3 locked main inventory slots and 2 locked hotbar slots
When the player executes `/bnnchsort help`
Then the output includes "Locked slots: 3 (main), 2 (hotbar)"

### AC-010: Modifier Key Config

Given a player who has changed `lockModifierKey` to Left Ctrl in the client config
When the player holds Left Ctrl and clicks a slot
Then the slot lock toggles as expected

### AC-011: Invalid Slot Index Rejected

Given a malformed network packet requesting a lock toggle on slot index 99
When the server processes the packet
Then the request is silently ignored and no state changes occur

---

## Error Handling

| Error Condition                                       | Behavior                                                      |
|-------------------------------------------------------|---------------------------------------------------------------|
| Lock toggle on non-player slot (e.g., container slot) | Request silently ignored                                      |
| Lock toggle on out-of-range slot index                | Request silently ignored, no crash                            |
| Lock toggle while no screen is open                   | Input not captured (keybind only active on container screens) |
| Merge source item disappears mid-sort (edge case)     | Skip merge for that slot, sort remaining items normally       |
| Lock data attachment missing (new player)             | Initialize with empty lock set (no slots locked)              |
| Config modifier key set to invalid value              | Fall back to default (Right Alt)                              |

---

## Implementation TODO

### Data & Persistence

- [ ] Create `LockedSlots` record/class holding a `Set<Integer>` of locked slot indices
- [ ] Add codec for `LockedSlots` (list of integers for NBT serialization)
- [ ] Register `LOCKED_SLOTS` data attachment in `ModAttachments.java` with `copyOnDeath`
- [ ] Default value: empty set

### Network

- [ ] Create `ToggleLockPayload` (client -> server) containing `int slotIndex`
- [ ] Create `SyncLockedSlotsPayload` (server -> client) containing `Set<Integer> lockedSlots`
- [ ] Register payloads in `ModPayloads.java`
- [ ] Add server handler for `ToggleLockPayload` — validate slot index, toggle in attachment, sync to client
- [ ] Sync locked slots to client on player login (in `ServerEvents.java`)

### Configuration

- [ ] Add `lockModifierKey` to client config in `Config.java` (default: Right Alt, enum of modifier keys)
- [ ] Add `lockTintColor` to client config in `Config.java` (default: blue, configurable color value)
- [ ] Add `showLockTooltip` to client config in `Config.java` (default: true, boolean toggle)

### Sorting Pipeline

- [ ] Modify `SortHandler.java` to read locked slots before sorting
- [ ] Pre-sort phase: identify locked slots with non-full stacks of stackable items, find matching items in unlocked
  slots, merge up to max stack size
- [ ] Filter locked slot indices out of the sortable slot list
- [ ] Ensure sorted items are only placed into unlocked slots

### Client — Input

- [ ] Add lock toggle input handling in `ClientEvents.java` (check modifier key + click on player slot)
- [ ] Send `ToggleLockPayload` to server when toggle is triggered
- [ ] Play UI click sound on toggle

### Client — Rendering

- [ ] Create `SlotLockRenderer` or extend `ClientEvents` rendering to draw colored tint on locked slots
- [ ] Render tint overlay during `ScreenEvent.Render.Post` for all locked slot positions, using configured tint color
- [ ] Render lock tooltip on hover when `showLockTooltip` is enabled

### Client — State

- [ ] Create `ClientLockedSlotsCache` (mirrors `ClientPreferenceCache` pattern) to store local lock state
- [ ] Handle `SyncLockedSlotsPayload` to update cache

### Commands

- [ ] Add `/bnnchsort unlock` subcommand to `ModCommands.java` — clears all locks, syncs to client
- [ ] Update `/bnnchsort help` to display locked slot counts per region

### Localization

- [ ] Add translation keys to `en_us.json`:
    - Slot locked/unlocked feedback
    - Unlock all confirmation
    - Help command lock count format
    - Lock tooltip text
    - Config option labels and tooltips for `lockModifierKey`, `lockTintColor`, and `showLockTooltip`

---

## Out of Scope

- Locking slots in container inventories (chests, shulker boxes, etc.)
- Item-based locking (lock follows the item, not the slot position)
- Per-item-type lock rules (e.g., "always lock swords")
- Lock presets or profiles

## Open Questions

None — all questions resolved.

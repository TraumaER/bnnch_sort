/**
 * NeoForge GameTest framework tests for the Bnnch: Sort mod.
 *
 * <p>This package contains automated game tests that verify the mod's functionality using
 * NeoForge's GameTest framework. Tests run in a real Minecraft environment and can interact with
 * blocks, entities, and inventories.
 *
 * <h2>Test Classes</h2>
 *
 * <ul>
 *   <li>{@link xyz.bannach.bnnch_sort.test.SortingGameTests} - Tests for core sorting
 *       functionality, stack merging, and comparators
 *   <li>{@link xyz.bannach.bnnch_sort.test.PreferenceGameTests} - Tests for player preference
 *       attachments and preference cycling
 *   <li>{@link xyz.bannach.bnnch_sort.test.EdgeCaseGameTests} - Tests for edge cases like empty
 *       inventories, special slots, and restricted containers
 *   <li>{@link xyz.bannach.bnnch_sort.test.CommandGameTests} - Tests for slash command
 *       functionality and argument parsing
 * </ul>
 *
 * <h2>Running Tests</h2>
 *
 * <p>Tests are executed using the Gradle task:
 *
 * <pre>{@code ./gradlew.bat runGameTestServer}</pre>
 *
 * <h2>Test Structure</h2>
 *
 * <p>All test classes use {@link net.neoforged.neoforge.gametest.GameTestHolder} with the mod
 * namespace "bnnch_sort" and {@link net.neoforged.neoforge.gametest.PrefixGameTestTemplate} set to
 * false. Tests use the "empty" template structure.
 *
 * @see net.minecraft.gametest.framework.GameTest
 * @since 1.0.0
 */
package xyz.bannach.bnnch_sort.test;

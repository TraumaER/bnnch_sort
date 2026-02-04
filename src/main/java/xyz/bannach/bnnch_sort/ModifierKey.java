package xyz.bannach.bnnch_sort;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Modifier keys that can be used to trigger slot locking.
 *
 * <p>Each value maps to a keyboard modifier key that the player must hold while clicking a slot to
 * toggle its lock state. The actual key-down check is performed client-side.
 *
 * <h2>Side: Common</h2>
 *
 * <p>This enum is used in {@link Config} (common) and checked client-side.
 *
 * @see Config#lockModifierKey
 * @since 1.1.0
 */
public enum ModifierKey implements StringRepresentable {
  ALT("alt"),
  CONTROL("control"),
  SHIFT("shift");

  /** The string representation used for serialization and translation keys. */
  private final String serializedName;

  /**
   * Constructs a ModifierKey with the given serialized name.
   *
   * @param serializedName the string identifier for this modifier key
   */
  ModifierKey(String serializedName) {
    this.serializedName = serializedName;
  }

  /**
   * Returns the serialized name of this modifier key.
   *
   * @return the string identifier for this modifier key
   */
  @Override
  public @NotNull String getSerializedName() {
    return serializedName;
  }

  /**
   * Returns the translation key for this modifier key's display name.
   *
   * @return the translation key
   */
  public String getTranslationKey() {
    return "modifier_key." + BnnchSort.MODID + "." + serializedName;
  }
}

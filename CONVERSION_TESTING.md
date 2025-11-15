# Item Conversion Testing Guide

This guide explains how to test the item conversion system using the built-in test commands.

## Prerequisites

1. Start your Minecraft server with the Alchemy mod loaded
2. Make sure you're an operator (have permissions to use commands)

## Step 1: Enable Debug Logging

Debug logging shows detailed information about what conversions are being checked and applied:

```
/alchemy debug
```

**What it does:**
- Toggles verbose conversion logging on/off
- Shows green "ENABLED" message when turned on
- Shows red "DISABLED" message when turned off
- Logs appear in both console and server logs

**Example debug output:**
```
[Conversion] Processing item: minecraft:diamond_sword
[Conversion] Checking rule: oraxen_items (priority: 100)
[Conversion] ✓ Rule oraxen_items matched! Applying 2 operations
[Conversion]   Operation: SET_ITEM_ID_FROM_LOOKUP
[Conversion]     Mapping: ruby_sword → custom:ruby_sword
[Conversion]   Operation: REMOVE_COMPONENT
[Conversion]     Removing component at: components.minecraft:custom_data.PublicBukkitValues
[Conversion] Item transformed: minecraft:diamond_sword → custom:ruby_sword
```

## Step 2: Copy Example Conversions to Config

The example conversion files need to be copied from the resources folder to your config:

```bash
# From your server directory:
cp src/main/resources/assets/alchemy/transmutations/*.json config/alchemy/transmutations/
```

Or manually copy these files:
- `example_oraxen.json`
- `example_armor.json`
- `example_complex.json`

From `src/main/resources/assets/alchemy/transmutations/` to `config/alchemy/transmutations/`

## Step 3: Reload Conversions

After copying conversion files:

```
/alchemy reload
```

**What it does:**
- Reloads the main config
- Rescans `config/alchemy/transmutations/` for all JSON files
- Logs how many conversion rules were loaded from each file

**Expected output in logs:**
```
Loaded 1 conversion rules from example_oraxen.json
Loaded 1 conversion rules from example_armor.json
Loaded 2 conversion rules from example_complex.json
Loaded 4 total conversion rules
```

## Step 4: Get Test Items

Use the test item command to get items with NBT data that will trigger conversions:

```
/alchemy testitem <type> [player]
```

### Available Test Items

#### 1. Oraxen Items (from example_oraxen.json)

```
/alchemy testitem oraxen_ruby_sword
/alchemy testitem oraxen_emerald_pickaxe
/alchemy testitem oraxen_sapphire_axe
```

**NBT Structure:**
```
components.minecraft:custom_data.PublicBukkitValues.oraxen:id = "ruby_sword"
```

**Expected conversion:**
- Item ID changes to `custom:ruby_sword` (etc.)
- `PublicBukkitValues` component is removed

#### 2. Armor Items (from example_armor.json)

```
/alchemy testitem armor_ruby_helmet
/alchemy testitem armor_emerald_chestplate
```

**NBT Structure:**
```
components.minecraft:custom_data.PublicBukkitValues.armament:armor = "ruby_set_helmet"
```

**Expected conversion:**
- Item ID changes to `custom:ruby_set_helmet` (etc.)
- `PublicBukkitValues` component is removed

#### 3. Complex Conversions (from example_complex.json)

```
/alchemy testitem magic_wand
```

**NBT Structure:**
```
components.minecraft:custom_data.legacy_item_id = "magic_wand"
components.minecraft:custom_data.power_level = 9001
```

**Expected conversion:**
- Item ID changes to `custom:magic_wand`
- `minecraft:custom_model_data` set to 100
- `power_level` copied to `custom:power_level` component
- Old custom_data keys removed

```
/alchemy testitem legacy_stick
```

**NBT Structure:**
```
id = "minecraft:stick"
```

**Expected conversion:**
- Item ID stays `minecraft:stick`
- `minecraft:item_name` component added with golden "Magic Stick" text

## Step 5: Test the Conversion

### Method 1: Save and Reload (Triggers Conversion)

1. Get a test item: `/alchemy testitem oraxen_ruby_sword`
2. Put the item in a chest
3. Log out and log back in (or reload chunks)
4. Open the chest
5. Watch the server logs for conversion debug output

### Method 2: World Save/Load

1. Get test items and place them in containers
2. Stop the server
3. Start the server
4. Items will be converted when the world loads

### Method 3: Drop and Pick Up

1. Get a test item
2. Drop it on the ground (Q key)
3. Wait for it to despawn or pick it up again
4. Conversion happens when the entity is serialized/deserialized

## Understanding Debug Output

With debug mode enabled, you'll see output like this:

```
[Conversion] Processing item: minecraft:diamond_sword
```
The item being checked for conversion.

```
[Conversion] Checking rule: oraxen_items (priority: 100)
```
Which conversion rule is being evaluated.

```
[Conversion] ✓ Rule oraxen_items matched! Applying 2 operations
```
The condition matched! Operations will be applied.

```
[Conversion]   Operation: SET_ITEM_ID_FROM_LOOKUP
[Conversion]     Mapping: ruby_sword → custom:ruby_sword
```
Detailed operation execution.

```
[Conversion] Item transformed: minecraft:diamond_sword → custom:ruby_sword
```
Final result of the conversion.

If a rule doesn't match:
```
[Conversion] ✗ Rule oraxen_items did not match
```

## Troubleshooting

### Conversions Not Happening

1. **Check debug logs** - Enable debug mode and look for conversion messages
2. **Verify files are loaded** - Look for "Loaded X conversion rules" messages
3. **Check JSON syntax** - Use a JSON validator if rules aren't loading
4. **Priority conflicts** - Higher priority rules are checked first
5. **Path typos** - NBT paths are case-sensitive and must match exactly

### Items Not Converting

1. **Trigger a save/load** - Conversions happen during deserialization
2. **Check the condition** - Use debug logs to see if conditions are matching
3. **Verify NBT structure** - Use `/data get entity @s SelectedItem` to inspect NBT
4. **Check mappings** - Ensure the mapping table has the right keys

### Debug Mode Not Working

1. **Reload after toggling** - Try `/alchemy reload` after enabling debug
2. **Check config.json** - Verify `debug: true` is saved
3. **Server logs** - Make sure you're looking at the right log file

## Quick Test Script

Here's a quick sequence to test everything:

```
# 1. Enable debug logging
/alchemy debug

# 2. Reload to ensure conversions are loaded
/alchemy reload

# 3. Get a test item
/alchemy testitem oraxen_ruby_sword

# 4. Put it in a chest, log out, log back in
# 5. Check the logs for conversion output
# 6. Open the chest - item should be converted!
```

## Writing Your Own Conversions

Once you've tested the examples, you can write your own conversions:

1. Create a new JSON file in `config/alchemy/transmutations/`
2. Follow the schema in `src/main/resources/assets/alchemy/transmutations/README.md`
3. Use `/alchemy reload` to load your new conversions
4. Test with debug mode enabled
5. Watch the logs to troubleshoot

## Example: Creating a Custom Conversion

Let's say you want to convert old "mythic:dragon_sword" items to "custom:dragon_blade":

**File: `config/alchemy/transmutations/my_custom_items.json`**
```json
{
  "conversions": [
    {
      "id": "my_dragon_sword",
      "priority": 100,
      "condition": {
        "type": "component_exists",
        "path": "components.minecraft:custom_data.mythic_item_id"
      },
      "operations": [
        {
          "type": "set_item_id_from_lookup",
          "source": "components.minecraft:custom_data.mythic_item_id",
          "mapping": {
            "dragon_sword": "custom:dragon_blade"
          }
        },
        {
          "type": "remove_custom_data_key",
          "path": "mythic_item_id"
        }
      ]
    }
  ]
}
```

Then:
1. `/alchemy reload`
2. Use NBT commands or test in-game
3. Watch debug logs to verify it works!

## Additional Notes

- Conversions only apply to the **first matching rule** per item
- Rules are checked in **priority order** (highest first)
- Debug logging can be verbose - disable it in production
- Test conversions on a backup/test server first
- Not all items will need conversion - that's normal!

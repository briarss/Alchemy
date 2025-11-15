# Quick Test Commands Reference

## Setup Commands

```bash
# 1. Enable verbose debug logging
/alchemy debug

# 2. Reload transmutations after changing JSON files
/alchemy reload
```

## Test Item Commands

### Oraxen Item Tests
```bash
/alchemy testitem oraxen_ruby_sword
/alchemy testitem oraxen_emerald_pickaxe
/alchemy testitem oraxen_sapphire_axe
```
**Tests:** `example_oraxen.json` - ID mapping from `PublicBukkitValues.oraxen:id`

### Armor Item Tests
```bash
/alchemy testitem armor_ruby_helmet
/alchemy testitem armor_emerald_chestplate
```
**Tests:** `example_armor.json` - Armor piece conversion from `PublicBukkitValues.armament:armor`

### Complex Conversion Tests
```bash
/alchemy testitem magic_wand
```
**Tests:** `example_complex.json` - Multi-operation conversion with component copying and custom_model_data

```bash
/alchemy testitem legacy_stick
```
**Tests:** `example_complex.json` - Simple item_id match with component addition

### Component Restructuring Tests
```bash
/alchemy testitem flourish_showdown_item
```
**Tests:** `example_restructure.json` - Complex nested component restructuring (Flourish → GTG format)

```bash
/alchemy testitem legacy_nested_data
```
**Tests:** `example_restructure.json` - Multi-level nested restructuring with mixed literal and source values

## Give to Another Player
```bash
/alchemy testitem <type> <player>

# Examples:
/alchemy testitem oraxen_ruby_sword Steve
/alchemy testitem magic_wand Alex
```

## Complete Test Workflow

```bash
# 1. Copy example transmutations to config (do this once)
# From server root:
cp src/main/resources/assets/alchemy/transmutations/*.json config/alchemy/transmutations/

# 2. Enable debug mode
/alchemy debug

# 3. Reload transmutations
/alchemy reload

# 4. Get a test item
/alchemy testitem oraxen_ruby_sword

# 5. Put item in chest, save, and reload world
# Check server logs for conversion output!
```

## What Each Test Item Has

| Command | Base Item | NBT Data | Expected Result |
|---------|-----------|----------|-----------------|
| `oraxen_ruby_sword` | Diamond Sword | `PublicBukkitValues.oraxen:id = "ruby_sword"` | → `custom:ruby_sword` |
| `oraxen_emerald_pickaxe` | Diamond Pickaxe | `PublicBukkitValues.oraxen:id = "emerald_pickaxe"` | → `custom:emerald_pickaxe` |
| `oraxen_sapphire_axe` | Diamond Axe | `PublicBukkitValues.oraxen:id = "sapphire_axe"` | → `custom:sapphire_axe` |
| `armor_ruby_helmet` | Diamond Helmet | `PublicBukkitValues.armament:armor = "ruby_set_helmet"` | → `custom:ruby_set_helmet` |
| `armor_emerald_chestplate` | Diamond Chestplate | `PublicBukkitValues.armament:armor = "emerald_set_chestplate"` | → `custom:emerald_set_chestplate` |
| `magic_wand` | Stick | `legacy_item_id = "magic_wand"`, `power_level = 9001` | → `custom:magic_wand` + custom_model_data |
| `legacy_stick` | Stick | (none) | Adds gold name "Magic Stick" |
| `flourish_showdown_item` | Nether Star | Nested `flourish:showdown_item` structure | Restructured to `gtg:showdownitem` format |
| `legacy_nested_data` | Enchanted Book | Multi-level `legacy_data` structure | Restructured to `custom:new_structure` |

## Debug Output Example

When debug mode is on, you'll see:
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

## Useful Vanilla Commands

```bash
# Inspect item NBT
/data get entity @s SelectedItem

# Give yourself a custom NBT item (advanced)
/give @s diamond_sword{custom_data:{test_key:"test_value"}}
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Conversions not happening | Enable debug, trigger save/load (put in chest, relog) |
| No debug output | Check `/alchemy debug` is enabled, check config.json |
| Rules not loading | Check JSON syntax, look for "Loaded X rules" message |
| Wrong conversion applied | Check priority order, higher numbers run first |

## File Locations

- **Config directory:** `config/alchemy/`
- **Conversion files:** `config/alchemy/transmutations/*.json`
- **Example conversions:** `src/main/resources/assets/alchemy/transmutations/`
- **Documentation:** `src/main/resources/assets/alchemy/transmutations/README.md`
- **Main config:** `config/alchemy/config.json`

---

**For detailed information, see:** `CONVERSION_TESTING.md` and `src/main/resources/assets/alchemy/transmutations/README.md`

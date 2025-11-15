# Alchemy

A Fabric mod for Minecraft 1.21.1 that provides a flexible, JSON-configurable item transmutation system. Convert legacy items with custom NBT data into new formats automatically during world load.

## Features

- **JSON-Based Transmutations**: Define item conversions in JSON files without writing code
- **Flexible Rule System**: Priority-based rules with complex conditions and operations
- **Component Manipulation**: Set, copy, remove, and restructure item components
- **Nested Data Restructuring**: Transform complex NBT structures between different formats
- **Debug Logging**: Verbose conversion logging for troubleshooting
- **Test Commands**: Built-in test items for validating conversions

## Quick Start

### Building the Mod

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

### Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. Install [Fabric Language Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin)
4. Place the Alchemy mod JAR in your `mods/` folder

## Usage

### Creating Transmutations

Create JSON files in `config/alchemy/transmutations/` to define conversion rules:

```json
{
  "conversions": [
    {
      "id": "example_conversion",
      "priority": 100,
      "condition": {
        "type": "component_exists",
        "path": "components.minecraft:custom_data.legacy_item_id"
      },
      "operations": [
        {
          "type": "set_item_id_from_lookup",
          "source": "components.minecraft:custom_data.legacy_item_id",
          "mapping": {
            "old_sword": "custom:new_sword",
            "old_pickaxe": "custom:new_pickaxe"
          }
        },
        {
          "type": "remove_component",
          "path": "components.minecraft:custom_data"
        }
      ]
    }
  ]
}
```

### In-Game Commands

All commands require operator permission (level 2):

```
/alchemy debug          - Toggle verbose conversion logging
/alchemy reload         - Reload config and transmutation rules
/alchemy testitem <type> [player] - Get a test item for conversion testing
```

### Testing Conversions

1. Enable debug mode: `/alchemy debug`
2. Reload transmutations: `/alchemy reload`
3. Get a test item: `/alchemy testitem oraxen_ruby_sword`
4. Place item in a container, log out, and log back in
5. Check server logs for conversion output

For detailed testing instructions, see `CONVERSION_TESTING.md` and `TEST_COMMANDS.md`

## Transmutation System

### Supported Conditions

- `component_exists` - Check if a component path exists
- `component_equals` - Check if a component value matches
- `item_id` - Match specific item IDs
- `any` / `all` - Combine multiple conditions
- `always` - Always match (useful for default rules)

### Supported Operations

- `set_item_id` - Change the item ID
- `set_item_id_from_lookup` - Map values to new item IDs
- `set_component` - Set a component with literal or sourced values
- `copy_component` - Copy data from one path to another
- `remove_component` - Remove a component
- `remove_custom_data_key` - Remove specific custom_data keys

### Examples

The mod includes several example transmutation files:

- `example_oraxen.json` - Convert Oraxen items
- `example_armor.json` - Convert custom armor pieces
- `example_complex.json` - Complex multi-operation conversions
- `example_restructure.json` - Nested data restructuring

## Documentation

- `CONVERSION_TESTING.md` - Complete guide to testing conversions
- `TEST_COMMANDS.md` - Quick reference for test commands
- `src/main/resources/assets/alchemy/transmutations/README.md` - Full transmutation schema documentation

## Technical Details

- **Minecraft Version**: 1.21.1
- **Mod Loader**: Fabric
- **Language**: Kotlin
- **Key Dependencies**: Fabric API, Fabric Language Kotlin

### How It Works

Item conversions are applied during ItemStack deserialization using a Mixin on the ItemStack codec. When items are loaded from world data, transmutation rules are evaluated in priority order, and the first matching rule is applied.

## License

See `LICENSE` file for details.

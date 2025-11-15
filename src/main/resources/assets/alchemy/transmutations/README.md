# Item Conversion System

This directory contains example conversion rules. Copy these files to `config/alchemy/transmutations/` to use them, or create your own!

## How It Works

When items are loaded (e.g., from player inventories, chests, etc.), the conversion system checks each item against the configured rules in priority order. When a rule's condition matches, its operations are applied to transform the item.

## JSON Structure

Each conversion file contains:

```json
{
  "conversions": [
    {
      "id": "unique_identifier",
      "priority": 100,
      "condition": { ... },
      "operations": [ ... ]
    }
  ]
}
```

### Priority

- Higher priority rules are checked first
- Only the first matching rule is applied
- Use priority to control which conversion happens when multiple rules could match

### Conditions

#### `always`
Always matches (useful for testing or catch-all rules)
```json
{
  "type": "always"
}
```

#### `component_exists`
Checks if a component/NBT path exists
```json
{
  "type": "component_exists",
  "path": "components.minecraft:custom_data.my_key"
}
```

#### `component_equals`
Checks if a component equals a specific value
```json
{
  "type": "component_equals",
  "path": "components.minecraft:custom_data.my_key",
  "value": "expected_value"
}
```

#### `item_id`
Checks if the item ID matches
```json
{
  "type": "item_id",
  "itemId": "minecraft:stick"
}
```

#### `any`
Matches if ANY sub-condition matches
```json
{
  "type": "any",
  "any": [
    { "type": "component_exists", "path": "..." },
    { "type": "item_id", "itemId": "..." }
  ]
}
```

#### `all`
Matches only if ALL sub-conditions match
```json
{
  "type": "all",
  "all": [
    { "type": "component_exists", "path": "..." },
    { "type": "component_equals", "path": "...", "value": "..." }
  ]
}
```

### Operations

Operations are applied in order when a rule matches.

#### `set_item_id`
Changes the item to a different item type
```json
{
  "type": "set_item_id",
  "itemId": "custom:new_item"
}
```

#### `set_item_id_from_lookup`
Looks up the new item ID from a mapping table
```json
{
  "type": "set_item_id_from_lookup",
  "source": "components.minecraft:custom_data.old_id",
  "mapping": {
    "old_item_1": "new:item_1",
    "old_item_2": "new:item_2"
  },
  "defaultValue": "minecraft:barrier"
}
```

#### `set_item_id_from_component`
Uses a component value as the new item ID
```json
{
  "type": "set_item_id_from_component",
  "source": "components.minecraft:custom_data.target_item"
}
```

#### `remove_component`
Removes a component or nested NBT key
```json
{
  "type": "remove_component",
  "path": "components.minecraft:custom_data.unwanted_key"
}
```

#### `remove_custom_data_key`
Shorthand for removing keys from custom_data
```json
{
  "type": "remove_custom_data_key",
  "path": "legacy_key"
}
```

#### `add_component` / `set_component`
Adds or sets a component value
```json
{
  "type": "add_component",
  "component": "minecraft:custom_model_data",
  "value": {
    "type": "number",
    "number": 123
  }
}
```

Component value types:
- `string`: `{"type": "string", "string": "text"}`
- `number`: `{"type": "number", "number": 123}`
- `boolean`: `{"type": "boolean", "boolean": true}`
- `list`: `{"type": "list", "list": [...]}`
- `map`: `{"type": "map", "map": {"key": {...}}}`
- `from_source`: Copy from original item `{"type": "from_source", "fromPath": "components.old_component"}`

#### `copy_component`
Copies a value from one path to another
```json
{
  "type": "copy_component",
  "from": "components.minecraft:custom_data.old_location",
  "to": "components.custom:new_location"
}
```

## Path Syntax

Paths use dot-notation to navigate nested structures:
- `id` - The item's ID
- `components` - The item's components
- `components.minecraft:custom_data` - The custom data component
- `components.minecraft:custom_data.my_key` - A key within custom data

Remember to include the namespace (e.g., `minecraft:custom_data`, not just `custom_data`)!

## Examples

See the example files in this directory:
- `example_oraxen.json` - Simple ID mapping for custom items
- `example_armor.json` - Converting armor pieces
- `example_complex.json` - Multiple conversions with component manipulation

## Testing Your Conversions

1. Copy your JSON file to `config/alchemy/transmutations/`
2. Start/restart the server
3. Check the server logs for:
   - "Loaded X conversion rules from your_file.json"
   - Any parsing errors
4. Test with items that should match your conditions

## Common Use Cases

### Converting Plugin Items to Fabric Items
Many Bukkit/Spigot plugins store custom item IDs in NBT. Use `component_exists` to detect these and `set_item_id_from_lookup` to convert them.

### Cleaning Up Legacy Data
Use `remove_component` or `remove_custom_data_key` to remove old NBT data that's no longer needed.

### Migrating Between Item Systems
Combine ID changes with component copying to preserve important data while updating the item structure.

### Conditional Transformations
Use `all` and `any` conditions to create complex matching logic for edge cases.

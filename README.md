# Dimensional Control Neo 
Previously called Dimensional Structure Restrict (DimStructRestrict)

Dimensional Control Neo  is a lightweight Minecraft Forge mod for **Minecraft 1.20.1** that allows you to **control where structures can spawn** based on dimension and structure rules. Designed and developed by **Troy Cook (@cookta2012)**, this mod provides fine-grained control over world generation behavior — especially useful for modpack developers and world designers.

Initial release will be 1.20.1 other versions will be coming later.

---

## 🌍 Features

- Prevent or allow specific structures from generating in specific dimensions.
- Define whitelist or blacklist rules.
- Optionally flag structures to not place the structure while it believes it placed it.
- JSON-based configuration — supports comments and regenerates missing keys automatically.
- Logs rule enforcement with structure/dimension context for debugging.

---

## 🔧 Configuration

The config file is located at:
```
<minecraft_root>/config/dimstructrestrict.json
```

If the file doesn't exist, it will be generated with example entries.

---

### 📐 Structure of `dimstructrestrict.json`

```jsonc
{
  "structures": [
    {                                       // Individual structure rules will override dimension rules FULL STOP
      "id": "minecraft:village_plains",     // Structure ID
      "whitelist": ["minecraft:overworld"], // Allowed dimensions
      "false_place": false,                 // If true, the structure will be "falsely placed" (may break mods)
      "active": true                        // Whether this rule is active
    }
  ],
  "dimensions": [
    {
      "id": "minecraft:overworld",        // Dimension ID
      "whitelist": [],                    // Structures allowed in this dimension
      "active": true
    }
  ]
}
```

---

## 🧠 Rule Types

### ✅ `whitelist`
Only listed dimensions or structures are allowed.

### ❌ `blacklist`
Listed dimensions or structures will be prevented.

### ⚙️ `false_place` (optional, default `false`)
If `true`, prevents the structure from placing **without skipping the generation step** meaning it is marked on the map as generated.

> ⚠️ May cause issues with some structure-dependent mods.

### 🔄 `active` (optional, default `false`)
Controls whether the rule is enforced. Useful for temporarily disabling rules without removing them.

---

## 🧪 Example Use Case

```json
{
  "structures": [
    {
      "id": "minecraft:ruined_portal",
      "blacklist": ["minecraft:the_end", "minecraft:the_nether"],
      "active": true
    }
  ],
  "dimensions": [
    {
      "id": "minecraft:overworld",
      "whitelist": ["minecraft:village_savanna", "minecraft:village_plains"],
      "false_place": true,
      "active": true
    }
  ]
}
```

---

## 🛠️ Advanced Setup

The mod uses a generic `Rule` system internally with two maps:

- `STRUCTURE_RULES` for structure-specific rules
- `DIMENSION_RULES` for dimension-based restrictions

Each `Rule` is backed by:
- `id`: ResourceLocation
- `mode`: WHITELIST or BLACKLIST
- `resource`: Set of targets (structure/dimension IDs)
- `false_place`: Boolean
- `active`: Boolean

---

## 🧑‍💻 Developer Notes

- Author: **Troy Cook**
- GitHub: [@cookta2012](https://github.com/cookta2012)
- Language: Java
- Environment: Minecraft Forge for Minecraft 1.20.1

---

## 📄 License

MIT License.

---

## 📬 Feedback & Contributions

Feel free to open an issue or fork the project if you'd like to expand the system (e.g., gamerule-based control, datapack integration, or GUI support).

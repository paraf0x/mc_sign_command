# Sign Command

A client-side Fabric mod that lets you execute commands by clicking on signs.

## Features

- **Shift + Right-click** on any sign to run the command written on it
- **Command preview** - See the command in your action bar when looking at a sign
- **Visual feedback** - Enchant particles around command signs
- **Sound feedback** - Pling sound when executing
- **Hanging signs supported** - Works with both normal and hanging signs
- **Color code stripping** - Handles `&` color codes in sign text
- **Cooldown protection** - 500ms cooldown prevents accidental double-clicks

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the mod and place it in your `mods` folder

## Usage

### Writing Command Signs

**Normal signs:** Write your command on lines 3-4
```
[Title]
[Description]
/gamemode creative
```

**Hanging signs:** Write your command on lines 2-3
```
[Title]
/tp @s 0 100 0
```

Commands must start with `/` to be recognized.

### Executing Commands

1. Look at the sign - you'll see the command preview and particles
2. Hold **Shift** and **Right-click** the sign
3. The command executes and you hear a confirmation sound

## Requirements

- Minecraft 1.21.1
- Fabric Loader ≥0.15.0
- Fabric API

## Use Cases

- Quick teleport signs in your base
- Gamemode switchers
- Time/weather control panels
- Server command shortcuts

## License

MIT License - see [LICENSE](LICENSE)

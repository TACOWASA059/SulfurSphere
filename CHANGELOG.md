# Changelog

## 1.1.0

- Added rolling: a Sulfur Cube that carries a block now rolls like a ball in the direction it travels
  - A Sulfur Cube with nothing inside does not roll
  - The turn matches the distance moved, so the sphere does not slide across the ground
- Added the `roll` config option (`config/sulfursphere.json`, default `true`)
- Added the `/sulfursphere roll [true|false]` client command, which also saves the setting

## 1.0.1

- Fixed rendering of the Sulfur Cube and the block it carries

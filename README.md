<!-- modrinth_exclude.start -->
[日本語](README.ja.md)
<!-- modrinth_exclude.end -->

# SulfurSphere

A Minecraft mod that makes the **Sulfur Cube** — and the block it carries — render as a **sphere** instead of a cube.

![A Sulfur Cube rendered as a sphere](https://cdn.modrinth.com/data/cached_images/17aa53f0d2b11790231641cf5edc17108999fbf3_0.webp)

## Features

- The Sulfur Cube's body is warped into a sphere, outer shell and inner layer alike
- The block a Sulfur Cube carries is warped into the same sphere, keeping its texture and biome tint
- **Rolling:** a Sulfur Cube that carries a block rolls like a ball, turning in the direction it travels
  - A Sulfur Cube with nothing inside never rolls
  - Rolling matches the distance moved, so the sphere does not slide across the ground
- Purely visual and client-side: nothing has to be installed on the server

## Config

`config/sulfursphere.json`

| Option | Type | Default | Description |
| --- | --- | --- | --- |
| `roll` | boolean | `true` | Whether the sphere rolls while the Sulfur Cube carries a block |

## Commands

- `/sulfursphere roll` — show the current setting
- `/sulfursphere roll <true\|false>` — turn rolling on or off, saved to the config file

Both are client commands, so they work on any server, single player or multiplayer.

## Requirements

- **Minecraft:** 26.2
- **Fabric:** requires [Fabric API](https://modrinth.com/mod/fabric-api)
- **NeoForge:** no additional mods required
- **Author:** TACOWASA059
- **License:** MIT

[English](README.md)

# SulfurSphere

**サルファーキューブ**（と、その中に入っているブロック）を、立方体ではなく**球体**として描画する Minecraft MOD。

![球体として描画されたサルファーキューブ](https://cdn.modrinth.com/data/cached_images/17aa53f0d2b11790231641cf5edc17108999fbf3_0.webp)

## 特徴

- サルファーキューブの本体を、外殻・内層ともに球体に変形して描画
- 中に入っているブロックも同じ球体に変形し、テクスチャとバイオーム色はそのまま維持
- **ロール（転がり）:** ブロックを持っているサルファーキューブは、ボールのように進行方向へ転がる
  - 中にブロックが入っていないサルファーキューブは転がらない
  - 移動距離に合わせて回転するため、地面を滑っているようには見えない
- 見た目だけを変えるクライアント専用 MOD。サーバー側への導入は不要

## コンフィグ

`config/sulfursphere.json`

| 項目 | 型 | 既定値 | 説明 |
| --- | --- | --- | --- |
| `roll` | boolean | `true` | ブロックを持っているときに球体を転がすかどうか |

## コマンド

- `/sulfursphere roll` — 現在の設定を表示
- `/sulfursphere roll <true\|false>` — ロールの ON / OFF を切り替え、コンフィグにも保存

どちらもクライアントコマンドなので、シングルでもマルチでも、どのサーバーでも使用可能。

## 動作環境

- **Minecraft:** 26.2
- **Fabric:** [Fabric API](https://modrinth.com/mod/fabric-api) が必要
- **NeoForge:** 前提 MOD なし
- **作者:** TACOWASA059
- **ライセンス:** MIT

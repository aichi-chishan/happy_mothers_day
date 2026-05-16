#!/usr/bin/env python3
"""
Android App Icon Generator — 一键生成所有密度、所有格式的图标文件

用法:
    python generate_icons.py <你的图片路径>
    python generate_icons.py icon.png -o ./output --bg-color "#FF5722"

输出:
    app_icons/
    ├── drawable-mdpi/       ic_launcher_background.webp  + ic_launcher_foreground.webp
    ├── drawable-hdpi/       ...
    ├── drawable-xhdpi/      ...
    ├── drawable-xxhdpi/     ...
    ├── drawable-xxxhdpi/    ...
    ├── mipmap-mdpi/         ic_launcher.webp  + ic_launcher_round.webp
    ├── mipmap-hdpi/         ...
    ├── mipmap-xhdpi/        ...
    ├── mipmap-xxhdpi/       ...
    └── mipmap-xxxhdpi/      ...
"""

import os
import sys
import argparse
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("❌ 缺少 Pillow 库，请先安装: pip install Pillow")
    sys.exit(1)

# ── 密度配置 ──────────────────────────────────────────────
# (目录名前缀, 缩放因子, 旧版启动图标尺寸, Adaptive 画布尺寸)
DENSITIES = [
    ("mdpi",    1.0,   48,  108),
    ("hdpi",    1.5,   72,  162),
    ("xhdpi",   2.0,   96,  216),
    ("xxhdpi",  3.0,  144,  324),
    ("xxxhdpi", 4.0,  192,  432),
]

# 安全区占画布的比例 (72dp / 108dp = 2/3)
SAFE_ZONE_RATIO = 2 / 3


def parse_hex_color(hex_str: str) -> tuple:
    """ '#3DDC84' / '#3DDC84FF' / '3DDC84' → (R, G, B) 或 (R, G, B, A)"""
    h = hex_str.lstrip("#")
    if len(h) == 6:
        return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))
    if len(h) == 8:
        return tuple(int(h[i:i+2], 16) for i in (0, 2, 4, 6))
    raise ValueError(f"无法解析颜色: {hex_str}")


def make_adaptive_bg(size: int, color: tuple) -> Image.Image:
    """生成 Adaptive Icon 背景层（纯色矩形）"""
    return Image.new("RGBA", (size, size), color)


def make_adaptive_fg(icon: Image.Image, size: int) -> Image.Image:
    """生成 Adaptive Icon 前景层（图标居中放置在安全区内）"""
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    safe_px = int(size * SAFE_ZONE_RATIO)
    offset = (size - safe_px) // 2

    # 如果图标不是正方形，取短边做内切缩放
    w, h = icon.size
    if w != h:
        edge = min(w, h)
        left = (w - edge) // 2
        top = (h - edge) // 2
        icon = icon.crop((left, top, left + edge, top + edge))

    fg_layer = icon.resize((safe_px, safe_px), Image.LANCZOS)
    canvas.paste(fg_layer, (offset, offset), fg_layer)
    return canvas


def make_legacy_icon(icon: Image.Image, size: int) -> Image.Image:
    """生成旧版启动图标（直接缩放至目标尺寸）"""
    return icon.resize((size, size), Image.LANCZOS)


def anydpi_xml() -> str:
    """返回 anydpi adaptive-icon XML（前景 + 背景 + monochrome）"""
    return """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>"""


def main():
    parser = argparse.ArgumentParser(
        description="Android App Icon Generator — 拖入一张图片，自动生成全部图标",
    )
    parser.add_argument(
        "image",
        help="源图片路径（支持拖拽）",
    )
    parser.add_argument(
        "-o", "--output",
        default="./app_icons",
        help="输出目录 (默认: ./app_icons)",
    )
    parser.add_argument(
        "--bg-color",
        default="#3DDC84",
        help="Adaptive Icon 背景颜色，支持 6位/8位 hex (默认: #3DDC84)",
    )
    parser.add_argument(
        "--no-adaptive",
        action="store_true",
        help="不生成 Adaptive Icon 层",
    )
    parser.add_argument(
        "--no-legacy",
        action="store_true",
        help="不生成 Legacy 旧版图标",
    )
    parser.add_argument(
        "--no-monochrome",
        action="store_true",
        help="不在 anydpi XML 中添加 monochrome 引用",
    )

    args = parser.parse_args()

    # ── 解析背景色 ──
    try:
        bg_color = parse_hex_color(args.bg_color)
    except ValueError as e:
        print(f"❌ {e}")
        sys.exit(1)
    if len(bg_color) == 3:
        bg_color = (*bg_color, 255)  # 补全 alpha

    # ── 加载源图 ──
    src_path = Path(args.image).resolve()
    if not src_path.exists():
        print(f"❌ 文件不存在: {src_path}")
        sys.exit(1)

    try:
        source = Image.open(src_path).convert("RGBA")
    except Exception as e:
        print(f"❌ 无法打开图片: {e}")
        sys.exit(1)

    print(f"📷 源图: {src_path.name}  ({source.size[0]}×{source.size[1]})")
    print(f"🎨 背景色: #{args.bg_color.lstrip('#')}")
    print()

    # ── 创建输出目录结构 ──
    out = Path(args.output)
    out.mkdir(parents=True, exist_ok=True)

    total = 0

    # ── Adaptive Icon 层 ──
    if not args.no_adaptive:
        print("── Adaptive Icon 前景 + 背景 ──")
        for name, scale, _, adaptive_size in DENSITIES:
            draw_dir = out / f"drawable-{name}"
            draw_dir.mkdir(parents=True, exist_ok=True)

            bg = make_adaptive_bg(adaptive_size, bg_color)
            bg_path = draw_dir / "ic_launcher_background.webp"
            bg.save(bg_path, "WEBP", quality=90)
            print(f"  ✓ drawable-{name}/ic_launcher_background.webp  ({adaptive_size}×{adaptive_size})")

            fg = make_adaptive_fg(source, adaptive_size)
            fg_path = draw_dir / "ic_launcher_foreground.webp"
            fg.save(fg_path, "WEBP", quality=90)
            print(f"  ✓ drawable-{name}/ic_launcher_foreground.webp  ({adaptive_size}×{adaptive_size})")

            total += 2

        # ── anydpi XML ──
        anydpi_dir = out / "mipmap-anydpi"
        anydpi_dir.mkdir(parents=True, exist_ok=True)
        xml_content = anydpi_xml()
        if args.no_monochrome:
            xml_content = xml_content.replace(
                '    <monochrome android:drawable="@drawable/ic_launcher_foreground" />\n',
                ""
            )
        (anydpi_dir / "ic_launcher.xml").write_text(xml_content, encoding="utf-8")
        (anydpi_dir / "ic_launcher_round.xml").write_text(xml_content, encoding="utf-8")
        print(f"  ✓ mipmap-anydpi/ic_launcher.xml")
        print(f"  ✓ mipmap-anydpi/ic_launcher_round.xml")
        print()

    # ── Legacy 旧版启动图标 ──
    if not args.no_legacy:
        print("── Legacy 启动图标 (ic_launcher + ic_launcher_round) ──")
        for name, scale, legacy_size, _ in DENSITIES:
            mip_dir = out / f"mipmap-{name}"
            mip_dir.mkdir(parents=True, exist_ok=True)

            icon = make_legacy_icon(source, legacy_size)
            icon_path = mip_dir / "ic_launcher.webp"
            icon.save(icon_path, "WEBP", quality=90)
            print(f"  ✓ mipmap-{name}/ic_launcher.webp  ({legacy_size}×{legacy_size})")

            round_path = mip_dir / "ic_launcher_round.webp"
            icon.save(round_path, "WEBP", quality=90)
            print(f"  ✓ mipmap-{name}/ic_launcher_round.webp  ({legacy_size}×{legacy_size})")

            total += 2

    print()
    print(f"✅ 完成！共生成 {total} 个图标文件")
    print(f"📁 输出目录: {out.resolve()}")
    print()
    print("💡 下一步:")
    print("   将 app_icons/ 下的 drawable-* 和 mipmap-* 目录覆盖到:")
    print("   app/src/main/res/")


if __name__ == "__main__":
    main()

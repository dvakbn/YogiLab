import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_yogilab_icon(size):
    # Base canvas
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Background: Smooth rounded square / circle gradient
    # Deep sky navy gradient background
    bg = Image.new('RGBA', (size, size), (11, 24, 38, 255)) # #0b1826

    # Draw radial glow in background
    glow = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    center = size // 2
    glow_radius = int(size * 0.45)

    for r in range(glow_radius, 0, -2):
        alpha = int(140 * (1.0 - (r / glow_radius) ** 1.5))
        # Cyan-gold ambient glow
        color = (2, 132, 199, alpha) if r > glow_radius * 0.4 else (56, 189, 248, alpha)
        glow_draw.ellipse([center - r, center - r - int(size * 0.05), center + r, center + r - int(size * 0.05)], fill=color)

    bg = Image.alpha_composite(bg, glow)

    # Composite onto icon with rounded corners for maskable & standard icon
    mask = Image.new('L', (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    corner_radius = int(size * 0.22) # 22% corner radius
    mask_draw.rounded_rectangle([0, 0, size, size], radius=corner_radius, fill=255)

    icon_bg = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    icon_bg.paste(bg, (0, 0), mask)

    draw_fg = ImageDraw.Draw(icon_bg)

    # Draw Geometric Sun / Arc Emblem
    # Outer thin ring
    ring_r = int(size * 0.32)
    stroke_w = max(2, int(size * 0.025))
    draw_fg.arc([center - ring_r, center - ring_r - int(size * 0.03), center + ring_r, center + ring_r - int(size * 0.03)],
                start=180, end=360, fill=(217, 119, 6, 230), width=stroke_w) # Amber Gold arc

    # Inner Sun Disc
    sun_r = int(size * 0.16)
    sun_cy = center - int(size * 0.08)
    draw_fg.ellipse([center - sun_r, sun_cy - sun_r, center + sun_r, sun_cy + sun_r],
                    fill=(245, 158, 11, 255)) # Gold sun

    # Stylized "Y" Triangle / Arch Emblem
    a_width = int(size * 0.38)
    a_height = int(size * 0.34)
    a_top = (center, center - int(size * 0.16))
    a_left = (center - a_width // 2, center + a_height // 2)
    a_right = (center + a_width // 2, center + a_height // 2)

    # Draw glowing Arch / emblem stroke
    arch_stroke = max(3, int(size * 0.05))
    draw_fg.line([a_left, a_top, a_right], fill=(248, 250, 252, 255), width=arch_stroke, joint='round')

    # Horizontal crossbar
    bar_y = center + int(size * 0.04)
    bar_left = (center - int(a_width * 0.28), bar_y)
    bar_right = (center + int(a_width * 0.28), bar_y)
    draw_fg.line([bar_left, bar_right], fill=(56, 189, 248, 255), width=int(arch_stroke * 0.8))

    # Bottom brand label "YogiLab" text
    try:
        font_size = int(size * 0.095)
        font = ImageFont.truetype("arial.ttf", font_size)
        text = "YogiLab"
        bbox = draw_fg.textbbox((0, 0), text, font=font)
        text_w = bbox[2] - bbox[0]
        text_x = center - text_w // 2
        text_y = center + int(size * 0.22)
        draw_fg.text((text_x, text_y), text, font=font, fill=(248, 250, 252, 240))
    except Exception as e:
        pass

    return icon_bg

if __name__ == '__main__':
    icon192 = create_yogilab_icon(192)
    icon192.save('E:/Aura/icon-192.png', 'PNG')
    print("Saved E:/Aura/icon-192.png")

    icon512 = create_yogilab_icon(512)
    icon512.save('E:/Aura/icon-512.png', 'PNG')
    print("Saved E:/Aura/icon-512.png")

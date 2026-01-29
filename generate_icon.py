#!/usr/bin/env python3
"""
Generate a monochromatic guitar tuning app icon.
Creates a 512x512 PNG image with white on black background.
"""

import math
from PIL import Image, ImageDraw


def create_guitar_tuner_icon(size=512):
    """Create a guitar tuner icon with white on black."""

    # Create black background
    image = Image.new("RGB", (size, size), color="black")
    draw = ImageDraw.Draw(image)

    # Calculate dimensions
    center_x = size // 2
    center_y = size // 2
    icon_size = size * 0.8  # Use 80% of the canvas

    # Draw guitar headstock shape (simplified silhouette)
    # Headstock is wider at top, narrower at bottom (where it meets neck)
    headstock_width = icon_size * 0.7
    headstock_height = icon_size * 0.6
    headstock_top = center_y - headstock_height // 2
    headstock_bottom = center_y + headstock_height // 2
    headstock_left = center_x - headstock_width // 2
    headstock_right = center_x + headstock_width // 2

    # Create headstock shape points (trapezoid-like shape)
    # Top is wider, bottom is narrower (where it would connect to neck)
    top_width = headstock_width
    bottom_width = headstock_width * 0.5

    points = [
        (center_x - top_width // 2, headstock_top),  # Top left
        (center_x + top_width // 2, headstock_top),  # Top right
        (center_x + bottom_width // 2, headstock_bottom),  # Bottom right
        (center_x - bottom_width // 2, headstock_bottom),  # Bottom left
    ]

    # Draw headstock
    draw.polygon(points, fill="white", outline="white")

    # Draw tuning pegs (6 total, 3 on each side)
    peg_radius = icon_size * 0.04
    peg_spacing = headstock_height * 0.25

    # Left side pegs
    for i in range(3):
        y_pos = headstock_top + (i + 1) * peg_spacing
        x_pos = headstock_left + headstock_width * 0.15
        draw.ellipse(
            (
                x_pos - peg_radius,
                y_pos - peg_radius,
                x_pos + peg_radius,
                y_pos + peg_radius,
            ),
            fill="black",
            outline="white",
            width=2,
        )
        # Draw small line from peg to headstock edge
        draw.line(
            (x_pos + peg_radius, y_pos, x_pos + peg_radius * 3, y_pos),
            fill="white",
            width=2,
        )

    # Right side pegs
    for i in range(3):
        y_pos = headstock_top + (i + 1) * peg_spacing
        x_pos = headstock_right - headstock_width * 0.15
        draw.ellipse(
            (
                x_pos - peg_radius,
                y_pos - peg_radius,
                x_pos + peg_radius,
                y_pos + peg_radius,
            ),
            fill="black",
            outline="white",
            width=2,
        )
        # Draw small line from peg to headstock edge
        draw.line(
            (x_pos - peg_radius, y_pos, x_pos - peg_radius * 3, y_pos),
            fill="white",
            width=2,
        )

    # Draw tuning indicator (a small arrow or note symbol in the center)
    # Let's draw a simple musical note (quarter note)
    note_size = icon_size * 0.15
    note_x = center_x
    note_y = center_y - note_size * 0.2

    # Note head (ellipse)
    note_head_width = note_size * 0.5
    note_head_height = note_size * 0.3
    draw.ellipse(
        (
            note_x - note_head_width // 2,
            note_y - note_head_height // 2,
            note_x + note_head_width // 2,
            note_y + note_head_height // 2,
        ),
        fill="white",
    )

    # Note stem (vertical line)
    stem_length = note_size * 0.6
    stem_width = note_size * 0.1
    stem_x = note_x + note_head_width // 2 - stem_width // 2
    draw.rectangle(
        (stem_x, note_y, stem_x + stem_width, note_y + stem_length), fill="white"
    )

    # Note flag (small curved line at top of stem)
    flag_size = note_size * 0.3
    for i in range(5):
        y_offset = note_y + i * flag_size / 10
        x_offset = stem_x + stem_width + i * flag_size / 15
        draw.ellipse(
            (
                x_offset - flag_size / 10,
                y_offset - flag_size / 20,
                x_offset + flag_size / 10,
                y_offset + flag_size / 20,
            ),
            fill="white",
        )

    return image


def main():
    """Generate and save the icon."""
    try:
        # Generate the icon
        icon = create_guitar_tuner_icon(512)

        # Save as PNG
        output_path = "guitar_tuner_icon.png"
        icon.save(output_path, "PNG")

        print(f"Icon generated successfully: {output_path}")
        print(f"Size: {icon.size[0]}x{icon.size[1]} pixels")
        print("Colors: White on black (monochromatic)")

        # Also create a smaller version for Android launcher (192x192)
        small_icon = create_guitar_tuner_icon(192)
        small_icon.save("guitar_tuner_icon_small.png", "PNG")
        print(f"Small icon generated: guitar_tuner_icon_small.png")

    except ImportError as e:
        print("Error: Pillow library is required.")
        print("Install it with: pip install Pillow")
        print(f"Details: {e}")
    except Exception as e:
        print(f"Error generating icon: {e}")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Generate a simple monochromatic guitar tuning app icon.
Clean design with just guitar headstock and tuning pegs.
"""

import os
from PIL import Image, ImageDraw


def create_simple_headstock_icon(size=512, include_note=False):
    """Create a simple guitar headstock icon."""

    # Create black background
    image = Image.new("RGB", (size, size), color="black")
    draw = ImageDraw.Draw(image)

    # Calculate dimensions
    center_x = size // 2
    center_y = size // 2
    icon_size = size * 0.8

    # Headstock dimensions
    headstock_width = icon_size * 0.7
    headstock_height = icon_size * 0.6

    # Create smooth headstock shape with rounded corners
    # Use a rounded rectangle for a modern look
    top_left = (center_x - headstock_width // 2, center_y - headstock_height // 2)
    bottom_right = (center_x + headstock_width // 2, center_y + headstock_height // 2)

    # Draw rounded rectangle for headstock
    corner_radius = headstock_width * 0.1
    draw.rounded_rectangle(
        [top_left, bottom_right],
        radius=corner_radius,
        fill="white",
        outline="white",
        width=2,
    )

    # Draw tuning pegs (6 total)
    peg_radius = icon_size * 0.035
    peg_spacing = headstock_height * 0.25

    # Left side pegs
    for i in range(3):
        y_pos = center_y - headstock_height // 2 + (i + 1) * peg_spacing
        x_pos = center_x - headstock_width // 2 + headstock_width * 0.2
        draw.ellipse(
            (
                x_pos - peg_radius,
                y_pos - peg_radius,
                x_pos + peg_radius,
                y_pos + peg_radius,
            ),
            fill="white",
        )

    # Right side pegs
    for i in range(3):
        y_pos = center_y - headstock_height // 2 + (i + 1) * peg_spacing
        x_pos = center_x + headstock_width // 2 - headstock_width * 0.2
        draw.ellipse(
            (
                x_pos - peg_radius,
                y_pos - peg_radius,
                x_pos + peg_radius,
                y_pos + peg_radius,
            ),
            fill="white",
        )

    # Draw tuning indicator lines (subtle)
    line_length = headstock_width * 0.3
    line_y = center_y

    # Left tuning indicator
    left_x_start = center_x - headstock_width // 2 + headstock_width * 0.35
    left_x_end = left_x_start + line_length
    draw.line(
        (left_x_start, line_y, left_x_end, line_y),
        fill="black",
        width=int(icon_size * 0.03),
    )

    # Right tuning indicator
    right_x_start = center_x + headstock_width // 2 - headstock_width * 0.35
    right_x_end = right_x_start - line_length
    draw.line(
        (right_x_start, line_y, right_x_end, line_y),
        fill="black",
        width=int(icon_size * 0.03),
    )

    if include_note:
        # Add a subtle musical note in the center
        note_size = icon_size * 0.12
        note_x = center_x
        note_y = center_y

        # Simple note symbol
        draw.ellipse(
            (
                note_x - note_size * 0.2,
                note_y - note_size * 0.1,
                note_x + note_size * 0.2,
                note_y + note_size * 0.1,
            ),
            fill="black",
        )

        # Note stem
        stem_length = note_size * 0.4
        draw.rectangle(
            (
                note_x + note_size * 0.15,
                note_y - stem_length // 2,
                note_x + note_size * 0.25,
                note_y + stem_length // 2,
            ),
            fill="black",
        )

    return image


def create_tuning_fork_icon(size=512):
    """Create a tuning fork icon (alternative design)."""

    image = Image.new("RGB", (size, size), color="black")
    draw = ImageDraw.Draw(image)

    center_x = size // 2
    center_y = size // 2
    icon_size = size * 0.7

    # Tuning fork dimensions
    fork_width = icon_size * 0.3
    fork_height = icon_size * 0.6
    fork_thickness = icon_size * 0.08

    # Draw the two prongs
    left_prong_x = center_x - fork_width // 2
    right_prong_x = center_x + fork_width // 2

    # Left prong
    draw.rectangle(
        (
            left_prong_x - fork_thickness // 2,
            center_y - fork_height // 2,
            left_prong_x + fork_thickness // 2,
            center_y + fork_height // 2,
        ),
        fill="white",
    )

    # Right prong
    draw.rectangle(
        (
            right_prong_x - fork_thickness // 2,
            center_y - fork_height // 2,
            right_prong_x + fork_thickness // 2,
            center_y + fork_height // 2,
        ),
        fill="white",
    )

    # Draw the handle (horizontal bar at top)
    draw.rectangle(
        (
            left_prong_x,
            center_y - fork_height // 2 - fork_thickness,
            right_prong_x,
            center_y - fork_height // 2,
        ),
        fill="white",
    )

    # Draw base (horizontal bar at bottom)
    base_width = fork_width * 1.5
    base_height = fork_thickness * 1.5
    draw.rectangle(
        (
            center_x - base_width // 2,
            center_y + fork_height // 2,
            center_x + base_width // 2,
            center_y + fork_height // 2 + base_height,
        ),
        fill="white",
    )

    # Add musical note above
    note_size = icon_size * 0.15
    note_x = center_x
    note_y = center_y - fork_height // 2 - fork_thickness - note_size

    # Note head
    draw.ellipse(
        (
            note_x - note_size * 0.25,
            note_y - note_size * 0.15,
            note_x + note_size * 0.25,
            note_y + note_size * 0.15,
        ),
        fill="white",
    )

    # Note stem
    stem_length = note_size * 0.5
    draw.rectangle(
        (
            note_x + note_size * 0.2,
            note_y,
            note_x + note_size * 0.3,
            note_y + stem_length,
        ),
        fill="white",
    )

    return image


def main():
    """Generate multiple icon variations."""

    print("Generating guitar tuner icons...")

    try:
        # Ensure assets directory exists
        os.makedirs("assets", exist_ok=True)

        # Generate simple headstock icon (clean design)
        print("1. Generating simple headstock icon...")
        simple_icon = create_simple_headstock_icon(512, include_note=False)
        simple_icon.save("assets/guitar_tuner_simple.png", "PNG")

        # Generate headstock with note
        print("2. Generating headstock with note icon...")
        note_icon = create_simple_headstock_icon(512, include_note=True)
        note_icon.save("assets/guitar_tuner_with_note.png", "PNG")

        # Generate tuning fork icon
        print("3. Generating tuning fork icon...")
        fork_icon = create_tuning_fork_icon(512)
        fork_icon.save("assets/tuning_fork_icon.png", "PNG")

        # Generate Android launcher sizes
        print("4. Generating Android launcher sizes (192x192)...")
        for name, func, args in [
            ("simple_192", create_simple_headstock_icon, (192, False)),
            ("note_192", create_simple_headstock_icon, (192, True)),
            ("fork_192", create_tuning_fork_icon, (192,)),
        ]:
            icon = func(*args)
            icon.save(f"assets/{name}.png", "PNG")

        print("\nIcons generated successfully:")
        print("- assets/guitar_tuner_simple.png (512x512) - Clean headstock design")
        print("- assets/guitar_tuner_with_note.png (512x512) - Headstock with note")
        print("- assets/tuning_fork_icon.png (512x512) - Tuning fork design")
        print(
            "- assets/simple_192.png, assets/note_192.png, assets/fork_192.png (192x192) - Android launcher sizes"
        )
        print("\nAll icons are monochromatic (white on black).")

    except Exception as e:
        print(f"Error: {e}")
        print("Make sure Pillow is installed: pip install Pillow")


if __name__ == "__main__":
    main()

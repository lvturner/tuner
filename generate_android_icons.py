#!/usr/bin/env python3
"""
Generate Android density-specific PNG icons from a source image.
Creates properly sized icons for all standard Android screen densities.
"""

import os
from PIL import Image

# Android density buckets and their scale factors
# Reference: https://developer.android.com/training/multiscreen/screendensities
DENSITY_BUCKETS = {
    "mdpi": 1.0,  # Baseline
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}

# Launcher icon sizes (in dp)
LAUNCHER_ICON_SIZE_DP = 48  # Standard launcher icon size in dp


def create_density_icons(source_image_path, output_dir="android_icons"):
    """
    Create density-specific icons from a source image.

    Args:
        source_image_path: Path to source PNG (should be high resolution)
        output_dir: Directory to save generated icons
    """

    try:
        # Load source image
        source_img = Image.open(source_image_path)
        source_width, source_height = source_img.size

        print(f"Source image: {source_image_path} ({source_width}x{source_height})")

        # Create output directory
        os.makedirs(output_dir, exist_ok=True)

        # Calculate pixel sizes for each density
        base_size_px = LAUNCHER_ICON_SIZE_DP  # mdpi uses dp as px

        print("\nGenerating icons for Android density buckets:")
        print("=" * 50)

        for density, scale_factor in DENSITY_BUCKETS.items():
            # Calculate target size in pixels
            target_size = int(base_size_px * scale_factor)

            # Resize source image to target size
            # Use high-quality resampling
            resized_img = source_img.resize(
                (target_size, target_size), Image.Resampling.LANCZOS
            )

            # Save to appropriate directory structure
            density_dir = os.path.join(output_dir, f"mipmap-{density}")
            os.makedirs(density_dir, exist_ok=True)

            output_path = os.path.join(density_dir, "ic_launcher.png")
            resized_img.save(output_path, "PNG")

            print(f"{density:6s} | {target_size:3d}x{target_size:<3d} | {output_path}")

        # Also create adaptive icon layers if needed
        # Adaptive icons need foreground and background layers
        print("\nAdditional icon assets:")

        # Create notification icon (24dp)
        for density, scale_factor in DENSITY_BUCKETS.items():
            notif_size = int(24 * scale_factor)
            resized_img = source_img.resize(
                (notif_size, notif_size), Image.Resampling.LANCZOS
            )

            # Save as notification icon
            output_path = os.path.join(output_dir, f"notification_{density}.png")
            resized_img.save(output_path, "PNG")
            print(f"Notification icon ({density}): {notif_size}x{notif_size}")

        # Create a preview of all sizes
        print("\n" + "=" * 50)
        print(f"Icons generated in: {output_dir}/")
        print("\nTo use these icons in your Android app:")
        print("1. Copy the mipmap-* directories to app/src/main/res/")
        print("2. Remove or backup the existing ic_launcher.xml vector drawable")
        print("3. Build and run your app")

        # Create a simple README for the generated icons
        readme_path = os.path.join(output_dir, "README.txt")
        with open(readme_path, "w") as f:
            f.write(f"""Android Launcher Icons
Generated from: {source_image_path}

Directory Structure:
{output_dir}/
├── mipmap-mdpi/ic_launcher.png (48x48)
├── mipmap-hdpi/ic_launcher.png (72x72)
├── mipmap-xhdpi/ic_launcher.png (96x96)
├── mipmap-xxhdpi/ic_launcher.png (144x144)
└── mipmap-xxxhdpi/ic_launcher.png (192x192)

Also includes notification icons (24dp scale).

To install:
1. Backup existing app/src/main/res/drawable/ic_launcher.xml
2. Copy all mipmap-* directories to app/src/main/res/
3. If keeping vector drawable, rename PNGs to avoid conflict

Note: Android Studio may warn about missing density versions.
These icons cover all standard densities.
""")

        print(f"\nSee {readme_path} for more details.")

    except FileNotFoundError:
        print(f"Error: Source image not found: {source_image_path}")
        print("Available PNG files:")
        for file in os.listdir("."):
            if file.endswith(".png"):
                print(f"  - {file}")
    except Exception as e:
        print(f"Error: {e}")


def main():
    """Generate Android icons from available designs."""

    print("Android Icon Generator")
    print("=" * 50)

    # List available source images
    source_images = [
        ("guitar_tuner_simple.png", "Clean headstock design"),
        ("guitar_tuner_with_note.png", "Headstock with musical note"),
        ("tuning_fork_icon.png", "Tuning fork design"),
        ("guitar_tuner_icon.png", "Original detailed design"),
    ]

    available_images = []
    for filename, description in source_images:
        if os.path.exists(filename):
            available_images.append((filename, description))

    if not available_images:
        print("No source images found. Please run generate_icon.py first.")
        return

    print("\nAvailable source images:")
    for i, (filename, description) in enumerate(available_images, 1):
        img = Image.open(filename)
        print(f"{i}. {filename:30s} ({img.size[0]}x{img.size[1]}) - {description}")

    print("\nSelect source image (1-4) or 'a' for all: ", end="")

    # For automation, use first available
    selection = "1"  # Default to first

    if selection.lower() == "a":
        # Generate for all available images
        for filename, description in available_images:
            print(f"\n{'=' * 60}")
            print(f"Generating icons from: {filename} ({description})")
            print("=" * 60)
            output_dir = f"android_icons_{os.path.splitext(filename)[0]}"
            create_density_icons(filename, output_dir)
    else:
        try:
            index = int(selection) - 1
            if 0 <= index < len(available_images):
                filename, description = available_images[index]
                output_dir = f"android_icons_{os.path.splitext(filename)[0]}"
                create_density_icons(filename, output_dir)
            else:
                print(
                    f"Invalid selection. Using first available: {available_images[0][0]}"
                )
                filename, description = available_images[0]
                output_dir = f"android_icons_{os.path.splitext(filename)[0]}"
                create_density_icons(filename, output_dir)
        except ValueError:
            print(f"Invalid input. Using first available: {available_images[0][0]}")
            filename, description = available_images[0]
            output_dir = f"android_icons_{os.path.splitext(filename)[0]}"
            create_density_icons(filename, output_dir)


if __name__ == "__main__":
    main()

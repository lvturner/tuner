# Updating the Guitar Tuner App Icon

You have several options for updating your app icon with the generated monochromatic designs.

## Quick Installation (Recommended)

Run the installation script to use the clean headstock design:

```bash
# Make the script executable
chmod +x install_icon.sh

# Install the icon
./install_icon.sh
```

This will:
1. Backup the current vector drawable icon
2. Copy the generated PNG icons to the correct directories
3. Update the AndroidManifest.xml to use mipmap icon

## Manual Installation

### Option A: Use PNG Icons (Simplest)

1. Copy the generated mipmap directories:
   ```bash
   cp -r android_icons_guitar_tuner_simple/mipmap-* app/src/main/res/
   ```

2. Optional: Backup or remove the vector drawable:
   ```bash
   mv app/src/main/res/drawable/ic_launcher.xml app/src/main/res/drawable/ic_launcher.xml.backup
   ```

3. Update AndroidManifest.xml (line 10):
   ```xml
   android:icon="@mipmap/ic_launcher"
   ```

### Option B: Keep Vector Drawable

Edit the existing vector drawable (`app/src/main/res/drawable/ic_launcher.xml`) to match the new design, or create a new vector drawable based on the generated icons.

## Available Icon Designs

### Generated PNG Icons
- **assets/guitar_tuner_simple.png** - Recommended clean headstock design
- **assets/guitar_tuner_with_note.png** - Headstock with subtle musical note
- **assets/tuning_fork_icon.png** - Tuning fork with note
- **assets/guitar_tuner_icon.png** - Original detailed design

### Android Density-Specific Icons
Already generated in `android_icons_guitar_tuner_simple/`:
- `mipmap-mdpi/ic_launcher.png` (48x48)
- `mipmap-hdpi/ic_launcher.png` (72x72)  
- `mipmap-xhdpi/ic_launcher.png` (96x96)
- `mipmap-xxhdpi/ic_launcher.png` (144x144)
- `mipmap-xxxhdpi/ic_launcher.png` (192x192)

## Design Preview

### Clean Headstock Design (Recommended)
```
    ┌─────────────────┐
    │                 │
    │   ○       ○     │
    │                 │
    │   ○       ○     │
    │   ▬▬▬   ▬▬▬     │
    │   ○       ○     │
    │                 │
    └─────────────────┘
```
- White guitar headstock silhouette
- 6 tuning pegs (3 per side)
- Tuning indicator lines
- Clean, modern, guitar-specific

## Verification

After installing, rebuild the app:
```bash
./gradlew clean assembleDebug
```

Install on device/emulator to verify the new icon appears correctly.

## Reverting Changes

To revert to the original vector icon:
```bash
# Remove PNG icons
rm -rf app/src/main/res/mipmap-*

# Restore vector drawable (if backed up)
mv app/src/main/res/drawable/ic_launcher.xml.backup app/src/main/res/drawable/ic_launcher.xml

# Restore AndroidManifest reference
# Change line 10 back to: android:icon="@drawable/ic_launcher"
```

## Customization

To modify the icon design:
1. Edit `tools/generate_simple_icon.py` or `tools/generate_icon.py`
2. Regenerate icons: `python3 tools/generate_simple_icon.py`
3. Regenerate Android densities: `python3 tools/generate_android_icons.py`
4. Reinstall: `./tools/install_icon.sh`
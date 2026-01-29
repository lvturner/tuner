#!/bin/bash
# Install the generated monochromatic guitar tuner icon

set -e  # Exit on error

echo "=== Guitar Tuner Icon Installation ==="
echo

# Check if we have generated icons
ICON_DIR="android_icons_guitar_tuner_simple"
if [ ! -d "$ICON_DIR" ]; then
    echo "Error: Generated icons not found."
    echo "Please run: python3 generate_android_icons.py"
    exit 1
fi

# Backup current icon
BACKUP_TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="project_backups/icon_backup_$BACKUP_TIMESTAMP"
if [ -f "app/src/main/res/drawable/ic_launcher.xml" ]; then
    echo "Backing up current vector icon..."
    mkdir -p "$BACKUP_DIR"
    cp app/src/main/res/drawable/ic_launcher.xml "$BACKUP_DIR/"
    echo "Backup saved to: $BACKUP_DIR/ic_launcher.xml"
    
    # Rename original to avoid potential conflicts
    mv app/src/main/res/drawable/ic_launcher.xml app/src/main/res/drawable/ic_launcher_vector_backup.xml
    echo "Original vector icon renamed to: ic_launcher_vector_backup.xml"
fi

# Copy mipmap directories
echo "Installing mipmap icons..."
for density_dir in "$ICON_DIR"/mipmap-*; do
    if [ -d "$density_dir" ]; then
        density_name=$(basename "$density_dir")
        echo "  Installing $density_name..."
        mkdir -p "app/src/main/res/$density_name"
        cp "$density_dir/ic_launcher.png" "app/src/main/res/$density_name/"
    fi
done

# Update AndroidManifest.xml to use mipmap icon
MANIFEST="app/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST" ]; then
    echo "Updating AndroidManifest.xml..."
    
    # Check current icon reference
    current_icon=$(grep 'android:icon=' "$MANIFEST" | head -1)
    echo "Current icon reference: $current_icon"
    
    # Create backup of manifest
    cp "$MANIFEST" "$BACKUP_DIR/AndroidManifest.xml"
    
    # Update to use mipmap
    sed -i 's|@drawable/ic_launcher|@mipmap/ic_launcher|g' "$MANIFEST"
    
    new_icon=$(grep 'android:icon=' "$MANIFEST" | head -1)
    echo "New icon reference: $new_icon"
    
    echo "Manifest backup saved to: $BACKUP_DIR/AndroidManifest.xml"
fi

# Copy notification icons if needed
echo "Copying notification icons..."
mkdir -p "app/src/main/res/drawable"
for notif_icon in "$ICON_DIR"/notification_*.png; do
    if [ -f "$notif_icon" ]; then
        icon_name=$(basename "$notif_icon")
        echo "  Copying $icon_name..."
        cp "$notif_icon" "app/src/main/res/drawable/"
    fi
done

echo
echo "=== Installation Complete ==="
echo
echo "Summary:"
echo "1. Mipmap icons installed to: app/src/main/res/mipmap-*/"
echo "2. Notification icons copied to: app/src/main/res/drawable/"
echo "3. Original files backed up to: $BACKUP_DIR/"
echo "4. AndroidManifest.xml updated to use @mipmap/ic_launcher"
echo
echo "To verify the icon:"
echo "1. Rebuild the app: ./gradlew clean assembleDebug"
echo "2. Install on device/emulator"
echo
echo "To revert changes:"
echo "1. Restore manifest: cp $BACKUP_DIR/AndroidManifest.xml $MANIFEST"
echo "2. Remove mipmap dirs: rm -rf app/src/main/res/mipmap-*"
echo "3. Restore vector icon: cp $BACKUP_DIR/ic_launcher.xml app/src/main/res/drawable/"
echo "4. Remove renamed backup: rm -f app/src/main/res/drawable/ic_launcher_vector_backup.xml"
echo
echo "Note: You may need to clean the project for changes to take effect:"
echo "  ./gradlew clean"
# Guitar Tuner App Icons

Generated monochromatic (white on black) icons for the guitar tuning application.

## Generated Icons

### 512x512 (High Resolution)
1. **assets/guitar_tuner_icon.png** - Original design with detailed headstock and musical note
2. **assets/guitar_tuner_simple.png** - Clean headstock design with tuning indicators
3. **assets/guitar_tuner_with_note.png** - Headstock with subtle musical note
4. **assets/tuning_fork_icon.png** - Tuning fork with musical note design

### 192x192 (Android Launcher Size)
1. **assets/simple_192.png** - Clean headstock design
2. **assets/note_192.png** - Headstock with note  
3. **assets/fork_192.png** - Tuning fork design

## How to Use in Android App

### Option 1: Replace Vector Drawable (Recommended for Monochromatic)

The current app uses a vector drawable at `app/src/main/res/drawable/ic_launcher.xml`. To use one of the generated PNG icons:

1. Copy your chosen PNG file to `app/src/main/res/mipmap-hdpi/`, `app/src/main/res/mipmap-mdpi/`, `app/src/main/res/mipmap-xhdpi/`, etc. for different screen densities.

2. Or replace the vector drawable with a PNG:
   - Delete or rename `app/src/main/res/drawable/ic_launcher.xml`
   - Add PNG files to appropriate mipmap folders:
     ```
     app/src/main/res/mipmap-hdpi/ic_launcher.png (72x72)
     app/src/main/res/mipmap-mdpi/ic_launcher.png (48x48)  
     app/src/main/res/mipmap-xhdpi/ic_launcher.png (96x96)
     app/src/main/res/mipmap-xxhdpi/ic_launcher.png (144x144)
     app/src/main/res/mipmap-xxxhdpi/ic_launcher.png (192x192)
     ```
   - Use `assets/simple_192.png` for xxxhdpi, scale it down for other densities.

### Option 2: Convert to Vector Drawable (Best Quality)

For best quality across all screen sizes, convert the PNG to an Android Vector Drawable:

1. Use online tools like:
   - [SVG to VectorDrawable Converter](https://svg2vector.com/)
   - [Android Studio's Vector Asset Studio](https://developer.android.com/studio/write/vector-asset-studio)

2. Process:
   - Convert PNG to SVG first (using tools like [PNG to SVG](https://www.pngtosvg.com/))
   - Convert SVG to VectorDrawable
   - Replace `app/src/main/res/drawable/ic_launcher.xml`

### Option 3: Keep Current Vector Drawable

The current icon (`ic_launcher.xml`) is a white circle with a musical note. You might want to:
- Update its colors to match your monochromatic theme
- Modify the design using the generated icons as inspiration

## Icon Designs Explained

### 1. Guitar Headstock Design
- Represents a guitar headstock with 6 tuning pegs (3 on each side)
- Clean, recognizable symbol for guitar players
- Tuning indicator lines suggest the tuning function

### 2. Tuning Fork Design  
- Classic symbol for tuning/pitch
- Includes musical note for clarity
- More generic "tuner" symbol (not guitar-specific)

### 3. With/Without Musical Note
- **With note**: Emphasizes musical/tuning aspect
- **Without note**: Cleaner, more modern look

## Regenerating Icons

If you want to modify the icons:

```bash
# Install required library (if not already installed)
pip install Pillow

# Generate all icons
python3 tools/generate_simple_icon.py

# Or generate specific designs
python3 tools/generate_icon.py  # Original detailed design
```

## Customization

Edit the Python scripts to:
- Change colors (swap black/white)
- Adjust sizes and proportions
- Add different design elements
- Create alternative color schemes

## Recommended Choice

For a guitar-specific tuning app, **assets/guitar_tuner_simple.png** (clean headstock) is recommended. It's:
- Clearly guitar-related
- Clean and modern
- Easily recognizable at small sizes
- Monochromatic for consistent theme

For the Android launcher, use **assets/simple_192.png** scaled to appropriate densities.
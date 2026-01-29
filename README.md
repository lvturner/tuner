# Android Guitar Tuner for Monochromatic Display

A minimalist guitar tuner Android app designed for devices with monochromatic displays and limited input capabilities (forward/back scroll, single-touch).

## Features

- **Real-time pitch detection** using YIN algorithm
- **Note detection** for any tuning (chromatic)
- **Visual tuning indicator** with needle display
- **Monochromatic UI** optimized for black/white displays
- **Simple navigation** for limited input devices
- **Reference frequency calibration** (430-450 Hz)
- **Sensitivity adjustment**

## Requirements

- Android 5.0+ (API 21)
- Microphone permission
- Monochromatic or color display

## Architecture

### Core Components

1. **AudioRecorder** - Captures microphone input using AudioRecord
2. **PitchDetector** - Implements YIN pitch detection algorithm
3. **NoteFinder** - Maps frequencies to note names with cents calculation
4. **TunerView** - Custom circular tuner display with needle indicator
5. **SettingsDialog** - Calibration and sensitivity settings

### Key Design Decisions

- **Monochromatic color scheme**: Uses black, white, and grays for maximum contrast
- **Circular tuner interface**: Natural representation of sharp/flat with needle
- **Simple navigation**: Large touch targets, single-screen with popup settings
- **Relative tuning display**: Shows "close enough" indicator rather than exact values
- **Optimized for limited hardware**: Efficient audio processing, minimal UI updates

## Project Structure

```
app/src/main/
├── java/com/rokid/tuner/
│   ├── MainActivity.kt              # Main activity with permission handling
│   ├── audio/
│   │   ├── AudioRecorder.kt         # Audio capture using AudioRecord
│   │   └── AudioConfig.kt           # Audio configuration constants
│   ├── pitch/
│   │   ├── PitchDetector.kt         # Pitch detection (YIN algorithm)
│   │   └── NoteFinder.kt            # Frequency to note mapping
│   ├── ui/
│   │   ├── TunerView.kt             # Custom circular tuner view
│   │   └── SettingsDialog.kt        # Calibration settings dialog
│   └── utils/
│       ├── FrequencyUtils.kt        # Frequency/note conversion utilities
│       └── AudioTestUtils.kt        # Synthetic audio generation for testing
└── res/
    ├── layout/
    │   ├── activity_main.xml        # Main activity layout
    │   └── dialog_settings.xml      # Settings dialog layout
    ├── values/
    │   ├── colors.xml              # Monochromatic color palette
    │   ├── strings.xml             # String resources
    │   └── styles.xml              # App styles and themes
    └── drawable/                   # Vector assets (if needed)
```

## Usage

### Basic Tuning
1. Launch the app
2. Grant microphone permission when prompted
3. Tap "Start Tuning" button
4. Play a guitar string
5. Observe:
   - Note name displayed in center
   - Needle position shows sharp/flat
   - Cents deviation shown numerically
   - Status indicates "In Tune", "Sharp", or "Flat"

### Calibration
1. Tap "Settings" button
2. Adjust reference frequency (default 440 Hz)
3. Adjust sensitivity if needed
4. Tap "Close" to save settings

### Navigation
- **Single touch**: Tap buttons to start/stop tuning or open settings
- **Forward/back scroll**: Hardware navigation keys supported
- **Large touch targets**: All buttons sized for easy targeting

## Technical Implementation

### Pitch Detection
- **Algorithm**: Simplified YIN (Yin-based) auto-correlation
- **Sample rate**: 44100 Hz
- **Buffer size**: 4096 samples
- **Frequency range**: 50-1000 Hz (covers guitar range)

### Note Detection
- **Reference frequency**: Adjustable 430-450 Hz
- **Note calculation**: 12-TET (12-tone equal temperament)
- **Cents calculation**: `1200 × log₂(frequency / target_frequency)`
- **Note names**: International notation (C, C♯, D, D♯, E, F, F♯, G, G♯, A, A♯, B)

### UI Design Principles
- **High contrast**: Black on white for maximum readability
- **Simple geometry**: Circles, lines, minimal ornamentation
- **Clear hierarchy**: Note name largest, status indicators smaller
- **Immediate feedback**: Needle movement shows tuning direction

## Performance Considerations

### Optimized for Limited Hardware
1. **Efficient audio processing**: Batch processing, early rejection
2. **Minimal UI updates**: ~20 FPS update rate
3. **Memory efficient**: Reused buffers, no memory leaks
4. **Power conscious**: Stops processing when not tuning

### Further Optimizations Possible
- Reduce sample rate to 22050 Hz for lower CPU
- Implement fixed-point arithmetic
- Use lookup tables for trigonometric functions
- Cache drawn UI elements

## Testing

### Unit Tests
- Frequency to note conversion
- Cents calculation accuracy
- Pitch detection with synthetic audio

### Integration Tests
- Full audio pipeline (mic → detection → display)
- Permission handling
- UI responsiveness

### Performance Tests
- CPU usage during tuning
- Memory footprint
- Battery impact

## Building

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## Dependencies

- **AndroidX**: Core, AppCompat, ConstraintLayout
- **Kotlin Coroutines**: Async operations
- **TarsosDSP**: Audio processing (pitch detection)
- **Material Components**: UI components

## License

[Add appropriate license]

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to branch
5. Create pull request

## Known Limitations

- Pitch detection accuracy depends on microphone quality
- Background noise may affect tuning
- Very low/high frequencies may not detect accurately
- Mono-only audio input

## Future Enhancements

1. Preset tunings (standard, drop D, etc.)
2. Harmonic analysis for better accuracy
3. Note history/trend display
4. Save/load calibration profiles
5. Support for other instruments
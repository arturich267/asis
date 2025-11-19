# Voice Interaction Module

## Overview
The Voice Interaction Module enables users to interact with the Asis Virtual Companion through voice commands. It records audio, analyzes emotions, generates contextual meme responses, and plays back synthesized or mixed audio responses.

## Architecture

### Components

#### 1. **VoiceFragment**
- UI layer managing the voice interaction screen
- Handles long-press recording on the 🎤 button
- Displays recording visualization with amplitude indicator
- Shows playback controls with seek bar and play/pause/replay buttons
- Mode toggle between Random Meme and Emotion Response

#### 2. **VoiceViewModel**
- Orchestrates the voice interaction workflow
- Manages state for recording, processing, and playback
- Coordinates services: audio recording, emotion analysis, meme generation, TTS, and media playback
- Handles cleanup of temporary files

#### 3. **Services**

##### AudioRecordingService
- Uses Android `AudioRecord` for raw audio capture
- Records in PCM format (44.1kHz, mono, 16-bit)
- Provides real-time amplitude monitoring for visualization
- Converts PCM to WAV format with proper headers

##### EmotionAnalysisService
- Mock implementation analyzing audio/text for emotions
- Returns emotion classifications: happy, sad, excited, calm, angry, neutral, surprised
- Provides confidence scores for each emotion

##### TextToSpeechService
- Android TTS integration for speech synthesis
- Configurable pitch, speed, and language
- Synthesizes text to WAV files for playback
- Supports voice selection

##### FFmpegAudioService
- FFmpeg-based audio manipulation
- **Trim**: Extract segments from audio files
- **Mix**: Combine TTS with real voice snippets at specified ratios
- **Concatenate**: Join multiple audio clips
- **Convert**: Transform between audio formats
- **Duration**: Get audio file duration

##### MediaPlayerController
- Manages MediaPlayer lifecycle
- Provides StateFlow for real-time playback state
- Supports play, pause, seek, and replay operations
- Handles errors and completion events

#### 4. **Data Models**

##### VoiceInteractionMode
```kotlin
enum class VoiceInteractionMode {
    RANDOM_MEME,      // Generate random meme responses
    EMOTION_RESPONSE  // Respond based on detected emotion
}
```

##### AudioRecordingState
```kotlin
data class AudioRecordingState(
    val isRecording: Boolean,
    val audioFile: File?,
    val durationMs: Long,
    val amplitudePercent: Float
)
```

##### AudioPlaybackState
```kotlin
enum class AudioPlaybackState {
    IDLE, LOADING, PLAYING, PAUSED, COMPLETED, ERROR
}
```

## User Flow

### 1. Recording Phase
1. User long-presses the 🎤 button
2. AudioRecordingService starts capturing audio to a PCM file
3. Real-time amplitude is displayed in the visualization card
4. User releases the button to stop recording
5. PCM audio is converted to WAV format

### 2. Processing Phase
1. **Emotion Analysis**: WAV file is analyzed for emotional content
2. **Meme Generation**: Based on the selected mode (Random/Emotion), MemeGenerator creates a response
   - Random: Selects random phrases and styles
   - Emotion: Uses detected emotion to influence response style
3. **Text-to-Speech**: Response text is synthesized to audio
4. **Audio Mixing** (optional): TTS is mixed with real voice snippets if available

### 3. Playback Phase
1. MediaPlayerController prepares the generated audio
2. UI displays the response text and playback controls
3. User can play, pause, seek, or replay the response
4. Progress is tracked and displayed in real-time

## FFmpeg Integration

### Supported Operations

#### Trim Audio
```kotlin
suspend fun trimAudio(
    inputFile: File,
    outputFile: File,
    startTimeMs: Long,
    durationMs: Long
): Result<File>
```

#### Mix Audio
```kotlin
suspend fun mixAudio(
    input1File: File,
    input2File: File,
    outputFile: File,
    mixRatio: Float = 0.5f // 0.0 = only input1, 1.0 = only input2
): Result<File>
```

#### Concatenate Audio
```kotlin
suspend fun concatenateAudio(
    inputFiles: List<File>,
    outputFile: File
): Result<File>
```

#### Convert Audio
```kotlin
suspend fun convertAudio(
    inputFile: File,
    outputFile: File,
    outputFormat: String = "wav",
    sampleRate: Int = 44100,
    channels: Int = 1
): Result<File>
```

## Preferences

### VoiceInteractionPreferences
Stored in DataStore:
- **voiceMode**: Selected interaction mode
- **useRealVoice**: Enable mixing with real voice snippets
- **ttsPitch**: TTS pitch (default 1.0)
- **ttsSpeed**: TTS speed (default 1.0)
- **ttsLanguage**: TTS language (default "en-US")

## Permissions

Required permissions (already declared in AndroidManifest.xml):
- `RECORD_AUDIO`: Required for audio recording
- `MODIFY_AUDIO_SETTINGS`: For audio configuration
- `VIBRATE`: For haptic feedback

## File Management

### Temporary Files
- All audio files are stored in `app.cacheDir/voice_temp/`
- Files older than 1 hour are automatically cleaned up
- Cleanup happens on ViewModel destruction

### File Naming Convention
- Recording: `recording_<timestamp>.pcm` → `recording_<timestamp>.wav`
- TTS: `tts_<timestamp>.wav`
- Mixed: `mixed_<timestamp>.wav`

## Testing

### Unit Tests
- `FFmpegAudioServiceTest`: Tests FFmpeg operations
- `EmotionAnalysisServiceTest`: Tests emotion detection
- `AudioRecordingServiceTest`: Tests audio recording
- `VoiceInteractionModelsTest`: Tests data models

### Integration Tests
The voice interaction workflow can be tested end-to-end by:
1. Simulating button press/release
2. Verifying state transitions
3. Checking audio file generation
4. Validating playback state

## Error Handling

All operations return `Result<T>` types:
- `Result.Success<T>`: Operation succeeded with data
- `Result.Error`: Operation failed with exception
- `Result.Loading`: Operation in progress

Errors are displayed in the UI with dismissible error cards.

## Performance Considerations

1. **Background Processing**: All heavy operations run on `Dispatchers.IO`
2. **Coroutines**: Non-blocking async operations
3. **StateFlow**: Efficient state updates for playback progress
4. **Resource Cleanup**: Services properly released on ViewModel destruction
5. **File Cleanup**: Automatic removal of old temporary files

## Future Enhancements

1. **Voice Training**: Allow users to train with their own voice samples
2. **Cloud TTS**: Optional cloud-based TTS for better quality
3. **Audio Effects**: Add reverb, pitch shifting, or other effects
4. **Voice Recognition**: Speech-to-text for command processing
5. **Batch Processing**: Process multiple recordings at once
6. **Export**: Save favorite responses to device storage

## Dependencies

- **FFmpeg Kit**: `com.arthenica:ffmpeg-kit-full:6.0-2`
- **Android TTS**: Built-in Android TextToSpeech
- **DataStore**: `androidx.datastore:datastore-preferences:1.0.0`
- **Coroutines**: `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`
- **Kotlin Flow**: For reactive data streams

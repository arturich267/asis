# Meme Generator Component

## Overview

The Meme Generator is a comprehensive domain component that creates contextually relevant meme responses based on user input, conversation history, and emotional state. It combines rule-based algorithms with TensorFlow Lite inference to generate varied, engaging responses that maintain a consistent persona.

## Architecture

### Core Components

1. **MemeGenerator** - Main domain orchestrator
2. **SpeakerStyleRepository** - Manages speaker personality profiles
3. **ConversationTopicRepository** - Tracks conversation context
4. **TensorFlowLiteStyleClassifier** - AI-powered style classification
5. **Data Models** - Comprehensive data structures for meme generation

### Data Flow

```
User Input → Style Classification → Template Generation → Response Assembly → MemeResponse
     ↓                ↓                    ↓                    ↓
  Emotion +      TensorFlow Lite     Pattern Detection    Variability
  Context +      Rule-based Fallback  + Style Matching    + Audio Instructions
  Time
```

## Features

### 1. Multi-Mode Generation

- **RANDOM**: Selects random style and generates memes without specific context
- **RESPOND_TO_EMOTION**: Uses detected emotion to guide style selection
- **CONTEXT_AWARE**: Considers time of day and recent conversation topics

### 2. Intelligent Pattern Detection

- **Repetition Detection**: Identifies repeated words/phrases for emphasis
- **Slang Detection**: Recognizes informal language patterns
- **Emoji Pattern Analysis**: Detects existing emoji usage for consistency

### 3. Style Classification

- **TensorFlow Lite Integration**: Primary classification using ML model
- **Rule-based Fallback**: Ensures functionality even without model
- **7 Distinct Styles**: Casual, Energetic, Sarcastic, Friendly, Playful, Serious, Formal

### 4. Context Awareness

- **Time-based Styling**: Different approaches for morning, afternoon, evening
- **Conversation History**: Recent topics influence meme relevance
- **Emotional State**: Detected emotions guide tone and emoji selection

### 5. Variability Controls

- **Seeded RNG**: Reproducible results for testing
- **Variability Percentage**: Controls randomness in output
- **Template Coverage**: Ensures diverse response patterns

## Usage Examples

### Basic Random Meme

```kotlin
val config = MemeGenerationConfig(
    mode = MemeMode.RANDOM,
    seed = 12345L,
    variability = 0.3f
)

val result = memeGenerator.generateMemeResponse(config)
// Result: MemeResponse with text like "hello world again and again 🔥⚡"
```

### Emotion-Responsive Meme

```kotlin
val config = MemeGenerationConfig(
    mode = MemeMode.RESPOND_TO_EMOTION,
    detectedEmotion = "happy",
    includeAudio = true
)

val result = memeGenerator.generateMemeResponse(config)
// Result: Energetic style with celebration emojis and audio instructions
```

### Context-Aware Meme

```kotlin
val config = MemeGenerationConfig(
    mode = MemeMode.CONTEXT_AWARE,
    currentTime = System.currentTimeMillis(), // 9 AM
    variability = 0.2f
)

val result = memeGenerator.generateMemeResponse(config)
// Result: Morning-appropriate energetic style
```

## Speaker Styles

### Casual
- **Characteristics**: Relaxed, friendly, informal
- **Emojis**: 😎, 👌, 🤙, ✌️, 😊
- **Slang**: bro, dude, man, yo, sup
- **Patterns**: you know, like that, for real

### Energetic
- **Characteristics**: High-energy, enthusiastic, lively
- **Emojis**: 🔥, ⚡, 🎉, 💪, 🚀, ✨
- **Slang**: lit, fire, slay, go off, pumped
- **Patterns**: let's gooo, yessir, bang bang

### Sarcastic
- **Characteristics**: Dry, mocking, ironic
- **Emojis**: 🙄, 😏, 🤔, 👀, 💅, 🤷
- **Slang**: sure, right, obviously, clearly
- **Patterns**: if you say so, wow, just wow

### Friendly
- **Characteristics**: Warm, inviting, supportive
- **Emojis**: 😊, 🤗, 💙, 🌈, ☀️, 🌸
- **Slang**: awesome, amazing, wonderful, great
- **Patterns**: you got this, so proud, love that

### Playful
- **Characteristics**: Fun, cheerful, bouncy
- **Emojis**: 😄, 😂, 🤪, 😜, 🎮, 🎭
- **Slang**: lol, lmao, haha, hehe, funny
- **Patterns**: silly goose, fun times, game on

### Serious
- **Characteristics**: Formal, measured, thoughtful
- **Emojis**: 📝, 💭, 🤔, 📊, 🎯, 🔍
- **Slang**: indeed, certainly, precisely, exactly
- **Patterns**: let me think, quite interesting

### Formal
- **Characteristics**: Professional, polished, respectful
- **Emojis**: 🤝, 📊, 📈, 💼, 🎯, ⚖️
- **Slang**: pleasure, excellent, outstanding
- **Patterns**: it would seem, one might say

## Template Generation

### Pattern Detection Algorithm

1. **Repetition Check**: Analyzes phrase for repeated words
2. **Slang Matching**: Compares against style-specific slang terms
3. **Emoji Analysis**: Examines existing emoji hints
4. **Context Integration**: Considers time and topic relevance

### Meme Suffix Generation

Based on detected patterns:
- **Repetition**: "again and again", "on repeat", "broken record"
- **Slang**: "fr fr", "no cap", "bet", "periodt"
- **Emoji**: "vibes only", "meme lord", "emoji master"
- **Default**: "just like that", "you know the vibes"

### Emoji Pack Selection

Uses TensorFlow Lite classification:
- **Input**: Style name + phrase text
- **Output**: Contextually appropriate emoji pack
- **Fallback**: Default emojis for unknown styles

## Audio Instructions

When `includeAudio = true`, generates comprehensive audio guidance:

```
Tone: upbeat, fast-paced, high energy
Speed: 1.1x
Emphasis: 🔥, ⚡, 🎉
```

Tone varies by style:
- **Energetic**: upbeat, fast-paced, high energy
- **Friendly**: warm, gentle, inviting
- **Sarcastic**: dry, slightly mocking, pauses for effect
- **Casual**: relaxed, conversational, natural
- **Serious**: measured, calm, authoritative
- **Playful**: cheerful, bouncy, fun
- **Formal**: professional, clear, measured pace

## Confidence Scoring

Algorithm calculates confidence based on:

1. **Phrase Popularity** (0-0.4): Higher for top-ranked phrases
2. **Topic Relevance** (0-0.1): Boost when recent topics exist
3. **Style Match** (0-0.1): Higher for custom styles
4. **Base Confidence** (0.5): Starting point

Final confidence: `base + phraseBonus + topicBonus + styleBonus`

## Testing

### Unit Tests Coverage

- **MemeGenerator**: All generation modes, error handling, RNG consistency
- **TensorFlowLiteStyleClassifier**: Style classification, emoji pack selection
- **Repository Implementations**: CRUD operations, query methods
- **Integration Tests**: End-to-end flow demonstration

### Test Categories

1. **Happy Path Tests**: Normal operation scenarios
2. **Edge Case Tests**: Empty data, error conditions
3. **Variability Tests**: Seed consistency, randomness
4. **Context Tests**: Time awareness, emotion response
5. **Integration Tests**: Complete workflow verification

## Configuration

### MemeGenerationConfig Parameters

- **mode**: Generation strategy (RANDOM/RESPOND_TO_EMOTION/CONTEXT_AWARE)
- **seed**: RNG seed for reproducible results (optional)
- **variability**: Randomness factor (0.0-1.0, default 0.3)
- **includeAudio**: Whether to generate audio instructions (default false)
- **detectedEmotion**: Emotion for emotion-responsive mode (optional)
- **currentTime**: Timestamp for context awareness (default now)

### Customization Options

1. **Add New Speaker Styles**: Extend SpeakerStyleRepository
2. **Custom Slang Terms**: Modify style characteristics
3. **New Emoji Patterns**: Update classification rules
4. **Template Variations**: Extend suffix generation
5. **Audio Instructions**: Customize tone/speed mappings

## Dependencies

- **TensorFlow Lite**: ML model inference
- **Coroutines**: Asynchronous operations
- **Room Database**: Phrase statistics storage
- **DataStore**: Preferences persistence
- **Kotlin Serialization**: JSON handling (if needed)

## Performance Considerations

- **Lazy Loading**: Styles loaded on demand
- **Caching**: ML model initialized once
- **Efficient Queries**: Database indexes for phrase stats
- **Memory Management**: Proper cleanup of TFLite resources

## Future Enhancements

1. **Dynamic Learning**: Update phrase statistics from usage
2. **User Personalization**: Learn individual preferences
3. **Multi-language Support**: Internationalization
4. **Advanced ML**: More sophisticated style classification
5. **Real-time Adaptation**: Adjust based on conversation flow

## Troubleshooting

### Common Issues

1. **Model Not Found**: Falls back to rule-based classification
2. **Empty Phrase Stats**: Uses default phrase list
3. **No Recent Topics**: Operates without topic context
4. **Invalid Emotion**: Defaults to neutral classification

### Debug Information

- Check confidence scores for generation quality
- Verify template IDs for reproducibility
- Monitor style classification decisions
- Validate emoji pack selections

## API Reference

### Core Methods

```kotlin
suspend fun generateMemeResponse(config: MemeGenerationConfig): Result<MemeResponse>
suspend fun generateMemeResponseSync(config: MemeGenerationConfig): Result<MemeResponse>
```

### Repository Methods

```kotlin
// SpeakerStyleRepository
suspend fun getSpeakerStyleByName(name: String): Result<SpeakerStyle?>
suspend fun getStylesByCharacteristics(characteristics: List<String>): Result<List<SpeakerStyle>>

// ConversationTopicRepository
suspend fun getRecentTopics(limit: Int): Result<List<ConversationTopic>>
suspend fun getTopicsByKeywords(keywords: List<String>): Result<List<ConversationTopic>>

// PhraseStatRepository
suspend fun getTopPhrases(limit: Int): Result<List<PhraseStatEntity>>
```

### Service Methods

```kotlin
// TensorFlowLiteStyleClassifier
suspend fun classifyStyle(inputText: String, detectedEmotion: String?): Result<String>
suspend fun classifyEmojiPack(style: String, inputText: String): Result<List<String>>
```
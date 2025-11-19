# Chat Overlay Module

## Overview

The Chat Overlay module provides a full-featured chat interface for text-based conversations with the Asis Virtual Companion. Messages are powered by the MemeGenerator domain component and persist to the Room database.

## Architecture

### Components

#### UI Layer (`ui/chat/`)
- **ChatFragment**: Main fragment displaying the chat interface
  - RecyclerView with message bubbles
  - Text input field and send button
  - Empty/loading/error states
  - Auto-scroll to latest messages
  
- **ChatMessageAdapter**: RecyclerView adapter with DiffUtil
  - User message view type (right-aligned, primary color)
  - Companion message view type (left-aligned, surface variant color)
  - Efficient updates using DiffUtil
  - Timestamp formatting

- **ChatViewModel**: Manages chat state and business logic
  - Streams conversation history from Room
  - Handles message sending
  - Integrates with MemeGenerator for responses
  - Error/retry state management
  - Loading states

#### Domain Layer
- **MemeGenerator**: Generates contextual responses based on user input
- **ChatMessageRepository**: Interface for data access

#### Data Layer
- **ChatMessageEntity**: Room entity for persisting messages
  - Message text
  - User/companion flag (isFromUser)
  - Timestamp
  - Metadata (confidence, template_id, etc.)
  
- **ChatMessageRepositoryImpl**: Repository implementation with Room DAO

## Features

### Core Functionality
1. **Text Conversations**: Send and receive text messages
2. **Message Persistence**: All messages stored in Room database
3. **Auto-scroll**: Automatically scrolls to latest message
4. **Message Bubbles**: Different styles for user vs companion messages
5. **Timestamps**: Display time for each message
6. **Emoji Support**: Native emoji rendering in messages
7. **Theme Integration**: Uses Material3 theme colors

### State Management
- **Loading State**: Shows progress indicator while loading messages
- **Empty State**: Friendly message when no conversation exists
- **Error State**: Error message with retry button
- **Sending State**: Disables input during message generation

### Navigation
- Integrated with Navigation Component
- Back navigation from toolbar
- Returns to Home screen on dismiss

## UI Design

### Message Bubbles
- **User Messages**: Right-aligned, primary color background, white text
- **Companion Messages**: Left-aligned, surface variant background, dark text
- Both have rounded corners (16dp) and timestamps

### Input Area
- Material3 outlined text field with rounded corners
- Send button (FAB style) with send icon
- Supports multi-line input (max 4 lines)
- IME action "send" for keyboard submission

### Toolbar
- Title: "Chat"
- Close/back button for dismissal
- Elevated design

## Usage

### From Home Screen
```kotlin
// HomeFragment navigates to chat
binding.chatButton.setOnClickListener {
    findNavController().navigate(R.id.action_home_to_chat)
}
```

### Sending Messages
Users type in the input field and tap send or press the keyboard send button. The flow:
1. User message saved to database
2. RecyclerView updates and scrolls to bottom
3. MemeGenerator creates contextual response
4. Companion message saved to database
5. RecyclerView updates again and scrolls to bottom

### Response Generation
```kotlin
val config = MemeGenerationConfig(
    mode = MemeMode.CONTEXT_AWARE,
    currentTime = System.currentTimeMillis(),
    includeAudio = false,
    variability = 0.7f
)
val response = memeGenerator.generateMemeResponse(config)
```

## Testing

### Unit Tests (`test/`)
- **ChatViewModelTest**: ViewModel logic tests
  - Message loading
  - Sending messages
  - Error handling
  - Retry functionality
  
- **ChatMessageAdapterTest**: Adapter tests with Robolectric
  - View type selection
  - DiffUtil callback logic
  - List updates

### Integration Tests (`androidTest/`)
- **ChatFragmentTest**: UI tests with Espresso
  - Fragment display
  - User interactions
  - Navigation
  - Orientation changes
  
- **ChatPersistenceTest**: Database tests
  - Message insertion and retrieval
  - Order preservation
  - Metadata persistence
  - Filtering by type
  
- **ChatIntegrationTest**: End-to-end tests
  - Full conversation flow
  - MemeGenerator integration
  - Multi-message conversations
  - Database persistence across reopens

## Data Flow

```
User Input → ChatFragment
    ↓
ChatViewModel.sendMessage()
    ↓
ChatMessageRepository.insertChatMessage() [User message]
    ↓
Room Database
    ↓
Flow updates → ChatViewModel → ChatFragment [Display user message]
    ↓
MemeGenerator.generateMemeResponse()
    ↓
ChatMessageRepository.insertChatMessage() [Companion message]
    ↓
Room Database
    ↓
Flow updates → ChatViewModel → ChatFragment [Display response]
```

## Configuration

### Dependencies
- androidx.recyclerview (adapter)
- Room (persistence)
- Navigation Component (navigation)
- Material3 (UI components)
- Coroutines & Flow (async operations)

### Resources
- `fragment_chat.xml`: Main chat layout
- `item_message_user.xml`: User message bubble layout
- `item_message_companion.xml`: Companion message bubble layout
- `ic_close.xml`: Close icon drawable
- `ic_send.xml`: Send icon drawable
- String resources in `strings.xml`

## Future Enhancements

### Potential Features
1. **Voice Message Support**: Record and play voice messages
2. **Image/Sticker Support**: Send media beyond text
3. **Rich Text Formatting**: Bold, italic, code blocks
4. **Message Reactions**: React to messages with emoji
5. **Conversation Threading**: Reply to specific messages
6. **Search**: Search through conversation history
7. **Export**: Export conversations as text/PDF
8. **Typing Indicators**: Show when companion is generating response
9. **Message Status**: Sent, delivered, read indicators
10. **Animations**: Smooth message appearance animations

### Planned Improvements
1. Better error recovery mechanisms
2. Offline support with sync queue
3. Message editing and deletion
4. Conversation management (clear, archive)
5. Performance optimization for very long conversations

## Known Issues
- None at initial release

## Performance Considerations
- DiffUtil efficiently handles list updates
- Room Flow streams updates automatically
- View binding avoids findViewById overhead
- LinearLayoutManager with stackFromEnd for efficient scrolling
- Message generation runs on background thread

## Accessibility
- Content descriptions on buttons
- Proper focus handling
- Screen reader support for message bubbles
- Sufficient touch target sizes (48dp minimum)

## Privacy & Data
- All data stored locally in Room database
- No cloud sync (privacy-first design)
- Messages never leave the device
- Can be cleared via settings (future feature)

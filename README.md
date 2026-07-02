# ChatManager-Core

**ChatManager-Core** is a lightweight, event-driven chat library for Minecraft (Fabric / NeoForge, powered by [Architectury](https://github.com/architectury/architectury-api)). It hooks into the client chat pipeline and turns raw chat/system messages into a structured, extensible event system — so other mods and modules don't need to touch mixins or parse vanilla chat packets themselves.

Instead of reading raw `Component` objects off the network, other parts of your mod simply subscribe to typed events like `MessageReceivedEvent`, `PlayerMentionedEvent`, or `MessageAddedToChannelEvent` and react to them.

## Features

- **Custom event bus** (built on [MegaEvents](https://github.com/Ferra13671/MegaEvents)) for all chat-related events — no vanilla mixin juggling required outside the core module.
- **Chat channels** — group messages into named channels (`ChatChannel`) with their own history, visibility, and metadata, similar to tabbed chat systems.
- **Message parsing** — converts vanilla player chat, system chat, and locally-added chat messages into a unified `Message` model (`ChatMessageParser` / `DefaultMessage`), including plain text extraction and message type classification (`PLAYER`, `SYSTEM`, `CLIENT`, `ERROR`, `MENTIONED`, `CUSTOM`).
- **Message pipeline** — `MessageHandlingChannel` provides overridable hooks (`onPreProcess`, `onMessageModify`, `onFilterMessage`, `onPostProcess`, `onMentionedProcess`, `onNetworkDispatch`) to intercept and transform messages as they flow through the system. **Note:** this pipeline does not add the message to a `ChatChannel` for you — call `channel.addMessage(message)` yourself (typically inside `onPostProcess`) once you've decided which channel it belongs to.
- **Mention detection** — automatically detects when a message mentions the local player and fires `PlayerMentionedEvent`.
- **Message stacking** — when the same message is added to a channel twice in quick succession via `ChatChannel#addMessage`, it's automatically stacked (like vanilla's "Message x2") and a `MessageStackEvent` is fired instead of a duplicate `MessageAddedToChannelEvent`.
- **Webhook dispatch** — optional per-channel or global webhook support to forward chat messages to an external HTTP endpoint (e.g. Discord-style webhooks), with either raw JSON `Component` or clean plain-text payloads. Per-channel webhook resolution relies on the message being associated with a channel via `message.setChannel(...)` — this association isn't done automatically by the core pipeline.
- **JSON-based configuration** — behavior like time formatting, webhooks, channel settings, message filters/highlighting, color remapping, aliases, macros, and AI-styled text is all configurable through auto-generated JSON config files via `ChatConfigManager`.
- **Tick & timer utilities** — a simple `TickCounter` that emits a `SecondElapsedEvent` once per second, useful for time-based chat logic (auto-scroll, fading messages, etc.).

## How it works

1. A vanilla chat packet (player message, system message, or a message added directly to the chat HUD) arrives and is intercepted via mixins.
2. `ChatMessageParser` converts it into a `Message` object — extracting plain text, author, timestamp, and message type.
3. `ChatManagerCore.EVENT_BUS` fires a `MessageReceivedEvent`.
4. Any registered `MessageHandlingChannel` (or subclass you create) picks up the event and runs it through the processing pipeline: pre-processing → modification → filtering → post-processing → mention detection → network dispatch.
5. It's up to your `onPostProcess` (or equivalent) implementation to assign the message to a `ChatChannel` by calling `channel.addMessage(message)`. The channel then stores it in its history and fires `MessageAddedToChannelEvent` — or `MessageStackEvent` if it's a duplicate posted within the last 5 seconds.

Everything is decoupled through the event bus, so you can plug in your own logic at almost any stage without touching the core module's internals — but routing a message to a specific channel is a step you implement, not something the core does for you out of the box.

## Installation

ChatManager-Core is published via [JitPack](https://jitpack.io/).

### 1. Add the JitPack repository

**Groovy (`build.gradle`):**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

**Kotlin (`build.gradle.kts`):**

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

### 2. Add the dependency

Pick the module that matches your platform. Available artifacts:

| Module      | Artifact                                          |
|-------------|----------------------------------------------------|
| Common      | `chatmanager_core-common`                          |
| Fabric      | `chatmanager_core-fabric`                           |
| NeoForge    | `chatmanager_core-neoforge`                         |

**Example:**

```groovy
dependencies {
    modImplementation 'com.github.SakaPati.ChatManager-Core:artifact:VERSION'
}
```

Replace `VERSION` with a specific [release tag](https://github.com/SakaPati/ChatManager-Core/releases), or use `master-SNAPSHOT` to always pull the latest commit from `master`.

## Basic usage

### Listening for chat events

```java
import com.ferra13671.megaevents.eventbus.EventSubscriber;
import ru.fozeton.chatmanager.ChatManagerCore;
import ru.fozeton.chatmanager.events.MessageReceivedEvent;
import ru.fozeton.chatmanager.events.PlayerMentionedEvent;

public class MyChatListener {

    public MyChatListener() {
        ChatManagerCore.EVENT_BUS.register(this);
    }

    @EventSubscriber(event = MessageReceivedEvent.class)
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("New message: " + event.getMessage().getPlainText());
    }

    @EventSubscriber(event = PlayerMentionedEvent.class)
    public void onMentioned(PlayerMentionedEvent event) {
        System.out.println("You were mentioned by: " + event.getAuthor());
    }
}
```

### Creating a custom chat channel

```java
import ru.fozeton.chatmanager.channel.ChatChannel;

// Registers itself with ChatManagerCore automatically
ChatChannel guildChannel = new ChatChannel("guild", "Guild Chat");
```

### Extending the message pipeline

```java
import ru.fozeton.chatmanager.channel.ChatChannel;
import ru.fozeton.chatmanager.channel.MessageHandlingChannel;
import ru.fozeton.chatmanager.messages.Message;

public class MyMessageChannel extends MessageHandlingChannel {

    private final ChatChannel channel = new ChatChannel("guild", "Guild Chat");

    @Override
    protected boolean onPreProcess(Message message) {
        // Return false to drop the message entirely
        return !message.getPlainText().contains("badword");
    }

    @Override
    protected void onMessageModify(Message message) {
        // Mutate message content/style before it reaches chat
    }

    @Override
    protected void onPostProcess(Message message) {
        // The core pipeline does not add messages to a channel for you —
        // you decide where each message ends up.
        message.setChannel(channel);
        channel.addMessage(message);
    }
}
```

## Configuration

Configuration files are generated automatically on first run inside your game's config folder, under `ChatManager-Core/`. Available config files:

- **`channels.json`** — time formatting, per-channel webhook settings, scroll/blink colors, and global webhook configuration.
- **`messagesfilter.json`** — regex or plain-text message filters, each with its own highlight/border color.
- **`colorremapper.json`** — remaps vanilla chat colors to a custom palette, plus saturation/brightness tuning for colors without an explicit override.
- **`alias.json`** — chat command/message aliases.
- **`macros.json`** — reusable chat macros.
- **`aistyletext.json`** — optional AI-assisted message styling via a Groq-compatible API endpoint.

Configs are loaded and saved through `ChatConfigManager`, which handles JSON (de)serialization automatically — you don't need to manage file I/O yourself.

## Requirements

- Java 21+
- Minecraft with Fabric or NeoForge (via Architectury)
- [MegaEvents](https://github.com/ferra13671) event bus (pulled in transitively)

## License

Check the [repository](https://github.com/SakaPati/ChatManager-Core?tab=License-1-ov-file) for license details.

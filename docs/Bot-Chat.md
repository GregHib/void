Bots reply to nearby players' public chat using only built-in [Quick Chat](https://runescape.wiki/w/Quick_Chat) phrases.
Players can type anything (up to 80 characters), bots work out what was meant and pick a fitting phrase based on the conversation so far, what they're doing and their personality.

# Overview

```
 "whats ur minnig lvl?"
          │
   ┌──────▼──────┐   slang, stretched words, smileys
   │ Normaliser  │   -> [what, is, your, minnig, level, ?]
   └──────┬──────┘
   ┌──────▼──────┐   items, npcs, skills, locations, quests, minigames
   │EntityTagger │   -> [what, is, your, {skill}, level, ?] + Skill=mining
   └──────┬──────┘
   ┌──────▼──────┐   fastText-style classifier
   │ IntentModel │   -> ask_level (0.99)
   └──────┬──────┘
   ┌──────▼──────┐   follow ups, pending yes/no questions, annoyance, topic
   │Conversation │
   └──────┬──────┘
   ┌──────▼──────┐   botChat("ask_level") { say(...) } handlers
   │ BotChatApi  │   -> weighted candidates, adjusted by persona
   └──────┬──────┘
   ┌──────▼──────┐   phrase text -> id, slot values -> enum indices
   │QuickChat    │   -> QuickChatPublic(phrase = 12, data = [])
   │Phrases      │
   └─────────────┘
          │
 Bot: "My Mining level is 45."
```

The only machine learnt part is the intent classifier, everything else is rules and data, so it's straightforward to see why a bot said something and to change it.

# Runtime (`game/src/main/kotlin/content/bot/chat/`)

| File                                           | Purpose                                                                                                                                                              |
|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BotChatModel.kt`                              | Loads the examples, normaliser and entity tagger on startup, retraining the intent model if its training data changed.                                               |
| `BotChat.kt`                                   | Script with the entry point `BotChat.heard(player, text)` called from public chat and quick chat. Picks which bot replies and sends it after a short "typing" delay. |
| `Normaliser.kt`                                | Lowercases, expands slang (`u` → `you`, `im` → `i am`), turns smileys into tokens and collapses stretched words (`heyyyy` → `hey`).                                  |
| `EntityTagger.kt`                              | Finds entity names and replaces them with placeholders like `{item}`.                                                                                                |
| `IntentModel.kt`                               | Runs the intent classifier and reads/writes the cached model.                                                                                                        |
| `IntentTrainer.kt`                             | Trains the intent classifier.                                                                                                                                        |
| `ChatProcessor.kt`                             | Normaliser → EntityTagger → IntentModel, producing an `Utterance`.                                                                                                   |
| `Conversation.kt`                              | Short-term memory for each bot/player pair.                                                                                                                          |
| `Persona.kt`                                   | Personality derived from the bot's account name.                                                                                                                     |
| `BotChatApi.kt`                                | Script interface for registering replies.                                                                                                                            |
| `ChatContext.kt`                               | Receiver for reply handlers: `say`, `silence`, `asking`, `slot`, `activity`.                                                                                         |
| `QuickChatPhrases.kt`                          | Looks up phrases by text and encodes slot values into quick chat data.                                                                                               |

## Who replies

When a player talks, only one bot replies:
1. Whose name was mentioned
2. The player spoke with in the last 30 seconds
3. Closest within 6 tiles, `chattiness` dependent.

Bots never reply to other bots, and ignore players on their ignore list.

## Entities

The entity tagger is built at startup from game data, so new content is understood without retraining:

| Type | Source |
|---|---|
| Skill | `chat_skills` table (row id + `aka`) |
| Location | Quick chat location enum `1504` + `locations` table `aka` |
| Minigame | Quick chat minigame enum `1503` |
| Quest | `quests.toml` names |
| Item | Item definition names + `aka` |
| Npc | NPC definition names + `aka` |
| Number | Any number |

Aliases are matched longest first, then by type priority (the order above).
Skills, locations, minigames and quests allow a one letter typo (`minnig`, `varrok`), items and npcs only match exactly (or plural) as there are too many for fuzzy matching to be safe.
Words from the training data are never fuzzy matched so "share" doesn't become "shark", and single words too common in chat can be excluded in `chat_stop_words`.

## Intent model

A [fastText](https://arxiv.org/abs/1607.01759) style linear classifier:
- Features are hashed into 65,536 buckets: words, word pairs (keeps "your level" vs "my level") and 3-5 letter character n-grams (so typos land near the correct word)
- Features are averaged into a 32 dimension vector which a linear layer maps to intent probabilities
- Cached as gzipped half-precision floats in `data/.temp/bot_chat.model` (setting `bots.chat.model`), see [Training](#training)

Messages classified with less than 40% confidence are handled by the `unknown` intent, where Quick Chat itself provides the perfect excuse: *"I can't answer that on Quick Chat."*

## Conversations

Each bot/player pair has a `Conversation` which remembers:
- The last 8 turns
- The current topic (last entity of each type), so `"and wc?"` after `"whats your mining level"` reuses the previous question with the new skill
- A pending yes/no question, "Do you need help?" -> "yes"
- Annoyance from insults or repeated messages, escalating to `annoyed` and eventually the ignore list
- The number of separate sessions (5 minutes apart), "Nice to meet you." vs "Welcome back."

## Personas

Every bot has a `Persona` derived from its account name, so it stays the same between logins:
- `friendliness` — Warm vs Curt replies
- `helpfulness` — agreeing to requests
- `chattiness` — joining in when not addressed, laughing along
- `slang` — "np", "lol" vs "No problem.", "Haha!"
- `patience` — rude messages tolerated before ignoring

# Writing replies

Replies are registered in scripts implementing `BotChatApi`:

```kotlin
class SocialChat : Script, BotChatApi {
    init {
        botChat("thanks") {
            say("You're welcome.", style = Style.Formal)
            say("No problem.", style = Style.Formal)
            say("np", style = Style.Slang)
        }
    }
}
```

Every `say` adds a weighted candidate and one is picked at random. `style` scales the weight by the bot's persona, e.g. `Style.Slang` is twice as likely for a bot with `slang = 1.0` and never picked with `slang = 0.0`.

Multiple handlers can respond to the same intent, content can add to replies without touching existing scripts:

```kotlin
botChat("greet") {
    if (now.month == Month.DECEMBER && now.dayOfMonth in 20..26) {
        say("Merry Christmas!", weight = 4f)
    }
}
```

> [!NOTE]
> Phrases are the exact Quick Chat text with typed placeholders, as printed by `./gradlew :tools:model:dumpQuickChat`. Unknown phrases are logged and skipped, `BotChatTest` checks every phrase used exists.

## Slots

Placeholders are filled in different ways:
- **Numbers** — `<SkillLevel>`, `<CombatLevel>`, `<Varp>` etc. are filled automatically from the bot's own stats when sent
- **`<MultipleChoice>`** — pass a string id and it's matched against the enum values, e.g. `"iron_ore"` → `iron`, `"seers_village"` → `Seers' Village`
- **`<AllItems>`/`<TradeItems>`** — pass an item id

```kotlin
botChat("ask_level") {
    val skill = slot(SlotType.Skill) ?: return@botChat
    say("My ${skill.key.replaceFirstChar { it.uppercase() }} level is <SkillLevel>.")
}
botChat("level_up") {
    val skill = utterance.first(SlotType.Skill) ?: return@botChat
    say("Nice level in: <MultipleChoice>.", skill.key)
}
```

`slot(type)` returns the entity from this message, otherwise the conversation topic. `utterance.first(type)` only checks this message.

## Context available

| Property | Description |
|---|---|
| `bot` / `speaker` | The bot and the player who spoke |
| `utterance` | Text, tokens, intent, confidence and entities |
| `conversation` | Turns, topic, sessions, annoyance |
| `persona` | The bot's personality |
| `activity` | Skill and product from the bot's current behaviour `produces` |
| `now` | Real date and time |

## Questions and side effects

`asking` waits for the player's yes or no:

```kotlin
botChat("lost") {
    asking("offer_help", yes = { say("Follow me.") }, no = { say("Okay.") }) {
        say("Do you need help?")
    }
}
```

`then` runs only if that candidate is the one said:

```kotlin
say("I have added you to my ignore list.", then = { bot.ignores.add(speaker.accountName) })
```

## Knowledge tables (`data/bot/chat/`)

| Table | Contents |
|---|---|
| `chat_slang`, `chat_smileys` | Normalisation, row id is the replacement (`[.i_am] words = ["im"]`) |
| `chat_stop_words` | Single words never treated as items/npcs |
| `chat_skills` | Skill `aka`, `activity` phrase, `advice` phrase with `spots` `[level, enum value]`, and `tips` `[level, phrase]` |
| `locations` | Location `aka` and `tile` used for directions (also used by `tele`) |
| `chat_item_sources` | Where to get items |

Advice is based on the *asker's* level, and bots choose between the two best spots so they don't all say the same thing.

# Training

The model is trained from example messages in `data/bot/chat/bot_chat.intents.toml` (any `*.intents.toml` file, so content can keep its own examples):

```toml
[ask_level]
examples = [
    "what is your mining level",
    "whats ur wc lvl",
    "you got 99 fishing?",
]
```

Examples use real names, they're run through the same Normaliser and EntityTagger as live chat, so the model learns `{skill}` rather than individual skills.

## Automatic retraining

The trained model is cached in `data/.temp/bot_chat.model` along with a fingerprint of its training data.
On startup the examples are normalised, tagged and hashed (~20ms) along with the trainer settings. If the fingerprint doesn't match the cache the model is retrained (< 1 second) and saved, otherwise the cached model is loaded (~10ms).

This catches everything which affects training:
- Examples added, removed or edited
- Slang or smiley table changes
- New entity names that change how an example is tagged (e.g. an item called "Will" turning "will" into `{item}`)
- Trainer settings, or `IntentTrainer.VERSION` bumped for code changes

New content which doesn't appear in the examples doesn't trigger retraining, the model already understands `{item}`.

Training is seeded so the same examples always produce the same model on every machine.

`BotChatTest` holds out 15% of the examples, trains on the rest and fails if accuracy drops below 80%, so changes which hurt understanding are caught in CI.

## Tools (`tools/model`)

- `./gradlew :tools:model:evaluateBotChat` — accuracy on held-out examples with every mistake listed, plus training words being tagged as items/npcs (candidates for `chat_stop_words`)
- `./gradlew :tools:model:botChatConsole` — type messages and see the intent, confidence and entities
- `./gradlew :tools:model:dumpQuickChat` — print every Quick Chat phrase and its enum options

## Adding an intent

1. Add a `[new_intent]` section with examples
2. Run `evaluateBotChat` and check the accuracy
3. Add a `botChat("new_intent") { ... }` handler

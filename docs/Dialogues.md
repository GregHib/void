## Statements

Statements are the simplist dialogue type and show only text

```kotlin
statement("You can't use this emote yet. Visit the Stronghold of Player Safety to unlock it.")
```

Line of text that are too long will automatically be wrapped around onto a new line.

![image](https://github.com/GregHib/void/assets/5911414/6b8909c1-0e87-4569-90b6-4b3b48bcc10a)

Line breaks can also be specified manually with the `<br>` tag or with [multi-line string literals](https://kotlinlang.org/docs/strings.html#multiline-strings).
```kotlin
statement("You can't use this emote yet. Visit the Stronghold of Player Safety to<br>unlock it.")
```

```kotlin
statement("""
    You can't use this emote yet. Visit the Stronghold of Player Safety to
    unlock it.
""")
```

## Items

Item dialogues show an inventory item next to text.

```kotlin
item(item = "bank_icon", zoom = 1200, text = "You have some runes in your bank. Climb the stairs in Lumbridge Castle until you see this icon on your minimap. There you will find a bank.")
```

![image](https://github.com/GregHib/void/assets/5911414/b02777fd-c70a-4542-8b87-807fb72edf90)

> [!NOTE]
> Some items exist only as icons for this dialogue.

## Characters

### Players

Player dialogues need a facial expression to go with the text displayed.

```kotlin
player<Unsure>("I'd like to trade.")
```

![image](https://github.com/GregHib/void/assets/5911414/c8b424df-0f1c-4889-b46a-490d863c6108)

> [!NOTE]
> [Full list of facial expressions](https://github.com/GregHib/void/blob/main/game/src/main/kotlin/world/gregs/voidps/world/interact/dialogue/Expression.kt)

### NPCs

To start a dialogue with an NPC `player.talkWith(npc)` must first be called.

This is automatically done when interacting with an npc via an NPCOption such as ItemOnNPC.

All future npc dialogues will refer back to the npc being interacted with.

```kotlin
npc<Talk>("Sorry I don't have any quests for you at the moment.")
```
![image](https://github.com/GregHib/void/assets/5911414/05d22201-eeb8-47fa-926c-967731cb8807)

For dialogues with multiple npcs, the npcs ids can be provided
```kotlin
npc<Furious>(npcId = "wally", text = "Die, foul demon!", clickToContinue = false)
```
![image](https://github.com/GregHib/void/assets/5911414/e6b9514c-9ec1-4dd7-bbd9-81c7c3bc33f1)


## Choices

Multi-choice options

```kotlin
choice {
    option("Yes I'm sure!") {
        npc<Cheerful>("There you go. It's a pleasure doing business with you!")
    }
    option("On second thoughts, no thanks.")
}
```

![image](https://github.com/GregHib/void/assets/5911414/424c7cd3-26ce-4eef-9e66-bd3984b06c3b)

Choices can be given custom titles
```kotlin
choice("Start the Cook's Assistant quest?") {
    option("Yes.") {
    }
    option("No.") {
    }
}
```

### Options

Anthing in an action will be executed after an option has been selected.
This can include a mix of delays and character modifications as well as other dialogues.
```kotlin
option("Yes I'm sure!") {
   player.inventory.remove("coins", cost)
   npc<Cheerful>("There you go. It's a pleasure doing business with you!")
}
```

Dialogues will end if an option without an action specified is selected
```kotlin
option("On second thoughts, no thanks.")
```

Options can be given an expression to have the player repeat the dialogue selected
```kotlin
// ❌ Manual
option("I'd like to trade.") {
    player<Unsure>("I'd like to trade.")
}
// ✅ Automatic
option<Unsure>("I'd like to trade.") {
}
```

Options be filtered so they are only displayed when the condition is met.
```kotlin
if (player.questComplete("rune_mysteries")) {
    option("Can you teleport me to the Rune Essence?") {
        player.tele(2910, 4830)
    }
}
```

When only one options condition is met then that option will be automatically selected
```kotlin
choice {
    option<Talk>("Nothing, thanks.")
}
```

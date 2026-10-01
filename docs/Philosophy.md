There's several key themes followed throughout the code base which aren't necessarily standard, this page provides reasoning behind many of the main choices and the benefits they bring.

# Data in files, logic in code
Keeping as much data as possible in files keeps the code clean and allows focus to be kept on the flow of logic. Data can also be easily re-loaded at runtime allowing for faster fixes and prototyping.

# Flexibility with maps
Maps are a naturally efficient way of storing information taking up no more memory than necessary both in RAM and on disk. Both Players and NPC have [Variables](./character-variables) for storing temporary (discarded on logout) and persistent (saved) data instead of hundreds of individual variables which often leads to messy Player class files. It also means account saves only store data of content the player has accessed and player saves will start small and gradually grow in size over time.

> [Definitions](https://github.com/GregHib/void/tree/main/cache/src/main/kotlin/world/gregs/voidps/cache/definition/decoder) which read static cache data all have an [Extras](https://github.com/GregHib/void/blob/main/cache/src/main/kotlin/world/gregs/voidps/cache/definition/Extra.kt) map for storing custom values provided by the files inside the [`/data/`](https://github.com/GregHib/void/tree/main/data/) directory.

# Strings are readable, special numbers are not

All entities have [unique string identifiers](./identifying-entities) as strings are human friendly not only for reading code but also when inputting data:
Want a yellow whip? `::item abyssal_whip_yellow` no id look-up table required.

Requiring unique strings forces contextual and relational information about entities which might otherwise be identical: `banker_lumbridge`, `banker_varrock` which can simplify transformations `"${door.id}_closed".replace("_closed", "_open")` however be cautious with this as name changes could break these implicit relationships.
Even if all else fails you can still use the integer id as a string `"4151"`.

# Events

Important entities each have a publisher for emitting [events](./events), these events can be subscribed to at any time allowing content to interact without creating complex interweaving code.

# Test content as well as systems

Tests are the proof that code written 6 months ago still works today and provides the confidence to make large fundamental changes without introducing new bugs or issues. The more content that is added over time, the more important this becomes.

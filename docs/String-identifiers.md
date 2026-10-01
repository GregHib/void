In-game and during client-server communications most [entity](entities) types are represented as an integer identifier e.g [17937 - Naïve bloodrager pouch](https://runescape.wiki/w/Na%C3%AFve_bloodrager_pouch)

At the borders between these boundaries the integer is replaced by a human-readable string equivalent.

Removing [magic numbers](https://en.wikipedia.org/wiki/Magic_number_(programming)) makes code immediately more readable.

## Do ✅ 
```kotlin
if (npc.id == "bob" && option == "Trade") {
    player.openShop("bobs_brilliant_axes")
}
```
## Don't ❌
```kotlin
if (npc.id == 519 && option == 2) {
    player.openShop(1)
}
```

> [!NOTE]
> Integer ids can still be obtained from the relevant definition class e.g. `npc.def.id`

# Formatting rules

A string id should follow the following formatting rules to keep them standardised.

1. Lowercase
2. Underscores not spaces
3. Ascii equivalents of diacritics (accents)
4. "and" instead of "&"
5. All symbols removed

> `Naïve bloodrager pouch` -> `naive_bloodrager_pouch`

> `Magic potion (3)` -> `magic_potion_3`

In cases where there is no string id the integer id can be used as a string.

# Generating ids

The majority of ids are generated from formatting their cache names.

If there is no known information to separate duplicate ids the second id onwards has a number suffix (i.e first one has no suffix, second one has 2, third 3, etc...)

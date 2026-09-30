package content.bot.chat.tag

import content.bot.chat.Normaliser
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.Tables

/**
 * Finds game entities in token lists and swaps them for placeholders so the intent model
 * doesn't need to learn thousands of names, and new content needs no retraining.
 *
 * "wheres a gud place to train wc" -> [where, is, a, gud, place, to, train, {skill}] + Skill(woodcutting)
 */
class ChatEntityTagger(private val normaliser: Normaliser) {
    private class Alias(val tokens: List<String>, val entity: ChatEntity)

    private val order = compareByDescending<Alias> { it.tokens.size }.thenBy { it.entity.type.ordinal }

    // First alias token -> aliases starting with it, longest and highest priority first
    private val index = HashMap<String, MutableList<Alias>>()

    // Word length -> first tokens of fuzzy aliases
    private val fuzzy = HashMap<Int, MutableSet<String>>()

    // Ordinary words which are never fuzzy matched ("share" isn't a typo of "shark")
    private val ordinary = HashSet<String>()

    // Single words which are too common in chat to be treated as entities
    private val stopWords = HashSet<String>()

    val size: Int
        get() = index.values.sumOf { it.size }

    fun add(type: SlotType, key: String, names: Collection<String>) {
        for (name in names) {
            val tokens = normaliser.tokens(name).filter { it != "?" }
            if (tokens.isEmpty() || (tokens.size == 1 && tokens[0] in stopWords)) {
                continue
            }
            val list = index.getOrPut(tokens.first()) { mutableListOf() }
            if (list.any { it.tokens == tokens && it.entity.type == type }) {
                continue
            }
            // Keep longest and highest priority first
            val alias = Alias(tokens, ChatEntity(type, key))
            val position = list.indexOfFirst { order.compare(alias, it) < 0 }
            list.add(if (position == -1) list.size else position, alias)
            if (type.fuzzy) {
                fuzzy.getOrPut(tokens.first().length) { mutableSetOf() }.add(tokens.first())
            }
        }
    }

    fun protect(words: Collection<String>) {
        for (word in words) {
            if (!index.containsKey(word)) {
                ordinary.add(word)
            }
        }
    }

    fun tag(tokens: List<String>): Tagged {
        val output = ArrayList<String>(tokens.size)
        val entities = ArrayList<ChatEntity>(2)
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token.all { it.isDigit() }) {
                output.add(SlotType.Number.placeholder)
                entities.add(ChatEntity(SlotType.Number, token))
                i++
                continue
            }
            val match = match(tokens, i)
            if (match == null) {
                output.add(token)
                i++
                continue
            }
            output.add(match.entity.type.placeholder)
            entities.add(match.entity)
            i += match.tokens.size
        }
        return Tagged(output, entities)
    }

    private fun match(tokens: List<String>, start: Int): Alias? {
        val token = tokens[start]
        val exact = index[token]?.firstOrNull { matches(it.tokens, tokens, start) }
        if (exact != null) {
            return exact
        }
        if (token.length > 3 && token.endsWith('s')) {
            val plural = index[token.dropLast(1)]?.firstOrNull { matches(it.tokens, tokens, start) }
            if (plural != null) {
                return plural
            }
        }
        if (token.length < 5 || token in ordinary) {
            return null
        }
        for (length in token.length - 1..token.length + 1) {
            for (word in fuzzy[length] ?: continue) {
                if (!withinOneEdit(word, token)) {
                    continue
                }
                val alias = index[word]?.firstOrNull { it.entity.type.fuzzy && matches(it.tokens, tokens, start) }
                if (alias != null) {
                    return alias
                }
            }
        }
        return null
    }

    private fun matches(alias: List<String>, tokens: List<String>, start: Int): Boolean {
        if (start + alias.size > tokens.size) {
            return false
        }
        for (j in 1 until alias.size) {
            val expected = alias[j]
            val actual = tokens[start + j]
            if (expected == actual || actual == expected + "s") {
                continue
            }
            if (expected.length >= 5 && actual !in ordinary && withinOneEdit(expected, actual)) {
                continue
            }
            return false
        }
        return true
    }

    companion object {
        const val LOCATION_ENUM = 1504
        const val MINIGAME_ENUM = 1503

        /**
         * Damerau-Levenshtein distance <= 1 without allocating a matrix
         */
        fun withinOneEdit(a: String, b: String): Boolean {
            if (a == b) {
                return true
            }
            val diff = a.length - b.length
            if (diff > 1 || diff < -1) {
                return false
            }
            var i = 0
            while (i < a.length && i < b.length && a[i] == b[i]) {
                i++
            }
            return when {
                diff == 1 -> a.regionMatches(i + 1, b, i, b.length - i)
                diff == -1 -> b.regionMatches(i + 1, a, i, a.length - i)
                a.regionMatches(i + 1, b, i + 1, a.length - i - 1) -> true
                // Transposition "mnining"
                else -> i + 1 < a.length && a[i] == b[i + 1] && a[i + 1] == b[i] && a.regionMatches(i + 2, b, i + 2, a.length - i - 2)
            }
        }

        fun key(name: String) = name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

        /**
         * Builds from game definitions: skill aliases, quick chat location + minigame enums, quests, items and npcs (including `aka` names)
         */
        fun load(normaliser: Normaliser, quests: QuestDefinitions, vocabulary: Collection<String>): ChatEntityTagger {
            val entityTagger = ChatEntityTagger(normaliser)
            for (row in Tables.get("chat_stop_words").rows()) {
                entityTagger.stopWords.addAll(row.stringList("words"))
            }
            for (row in Tables.get("chat_skills").rows()) {
                entityTagger.add(SlotType.Skill, row.rowId, row.stringList("aka") + row.rowId)
            }
            for (name in enumValues(LOCATION_ENUM)) {
                val key = key(name)
                entityTagger.add(SlotType.Location, key, listOf(name) + (Tables.stringListOrNull("chat_locations.$key.aka") ?: emptyList()))
            }
            for (name in enumValues(MINIGAME_ENUM)) {
                entityTagger.add(SlotType.Minigame, key(name), listOf(name))
            }
            for (quest in quests.definitions) {
                val name = quest.name
                if (quest.stringId.isEmpty() || name.isNullOrEmpty()) {
                    continue
                }
                entityTagger.add(SlotType.Quest, quest.stringId, listOf(name))
            }
            for (item in ItemDefinitions.definitions) {
                if (item.stringId.isEmpty() || item.name == "null" || item.noted || item.lent) {
                    continue
                }
                entityTagger.add(SlotType.Item, item.stringId, listOf(item.name) + aka(item.params))
            }
            for (npc in NPCDefinitions.definitions) {
                if (npc.stringId.isEmpty() || npc.name == "null") {
                    continue
                }
                entityTagger.add(SlotType.Npc, npc.stringId, listOf(npc.name) + aka(npc.params))
            }
            entityTagger.protect(vocabulary)
            return entityTagger
        }

        private fun enumValues(id: Int): List<String> = EnumDefinitions.get(id).map?.values?.filterIsInstance<String>() ?: emptyList()

        private fun aka(params: Map<Int, Any>?): List<String> {
            val list = params?.get(Params.AKA) as? List<*> ?: return emptyList()
            return list.filterIsInstance<String>().map { it.replace('_', ' ') }
        }
    }
}

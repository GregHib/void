package content.bot.chat.process

import world.gregs.voidps.engine.data.definition.Tables

/**
 * Turns raw player chat into clean lowercase tokens.
 * Expands [slang] ("u" -> "you", "im" -> "i am"), swaps [smileys] for word tokens and shortens stretched words ("heyyy" -> "hey")
 * so the entity tagger and intent model see consistent input.
 */
class Normaliser(
    private val slang: Map<String, List<String>>,
    private val smileys: Map<String, String>,
) {

    fun tokens(text: String): List<String> {
        val output = ArrayList<String>(16)
        for (raw in text.lowercase().split(' ', '\t', '\n')) {
            if (raw.isEmpty()) {
                continue
            }
            val smiley = smileys[raw]
            if (smiley != null) {
                output.add(smiley)
                continue
            }
            val cleaned = collapse(raw.filter { it.isLetterOrDigit() })
            if (cleaned.isNotEmpty()) {
                val replacement = slang[cleaned]
                if (replacement == null) {
                    output.add(cleaned)
                } else {
                    output.addAll(replacement)
                }
            }
            if (raw.contains('?')) {
                output.add("?")
            }
        }
        return output
    }

    /**
     * Collapses 3+ repeated characters into one, "heyyyy" -> "hey", "loool" -> "lol"
     */
    private fun collapse(word: String): String {
        val builder = StringBuilder(word.length)
        var i = 0
        while (i < word.length) {
            var run = 1
            while (i + run < word.length && word[i + run] == word[i]) {
                run++
            }
            if (run >= 3) {
                builder.append(word[i])
            } else {
                builder.append(word, i, i + run)
            }
            i += run
        }
        return builder.toString()
    }

    companion object {
        /**
         * Loads from the `chat_slang` and `chat_smileys` tables where the row id is the replacement
         */
        fun load(): Normaliser {
            val slang = HashMap<String, List<String>>()
            for (row in Tables.get("chat_slang").rows()) {
                val replacement = row.rowId.split('_')
                for (word in row.stringList("words")) {
                    slang[word] = replacement
                }
            }
            val smileys = HashMap<String, String>()
            for (row in Tables.get("chat_smileys").rows()) {
                val token = row.rowId
                for (smiley in row.stringList("words")) {
                    smileys[smiley] = token
                }
            }
            return Normaliser(slang, smileys)
        }
    }
}

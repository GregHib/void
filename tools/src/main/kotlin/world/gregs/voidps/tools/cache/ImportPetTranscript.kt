package world.gregs.voidps.tools.cache

import java.net.URI

/**
 * Fetches a RuneScape Wiki Transcript page and prints `pet_talks.tables.toml`
 * rows derived from its wikitext. The user is responsible for running this
 * and pasting/appending the output into the data file.
 *
 * Usage:
 *   gradle :tools:importPetTranscript --args="Abyssal_minion [pet_key]"
 *
 * The first arg is the wiki page name after `Transcript:` (URL-encoded
 * underscores, not spaces). The optional second arg overrides the row key
 * — defaults to the page name lowercased, useful for pets whose wiki page
 * casing differs from our row id (e.g. "Baby_basilisk" → baby_basilisk).
 *
 * Heuristics:
 *   - Each top-level `==Heading==` section becomes one or more rows.
 *   - Sub-headings like `===Random===` / `===Overheads===` mark overhead
 *     rows; `===Removed===` / `===Hunger===` sub-sections are skipped.
 *   - Conditional sub-headings ("If the player ...", "<Skill> < N", etc.)
 *     are emitted with the condition string left blank and a `# TODO`
 *     comment so you can hand-set it.
 *   - Wiki markup (links, bold/italic, html tags) is stripped from line
 *     bodies; literal `(translation)` parens are preserved verbatim.
 */
object ImportPetTranscript {

    @JvmStatic
    fun main(args: Array<String>) {
        val page = args.getOrNull(0) ?: error("Usage: importPetTranscript <Page_name> [pet_key]")
        val petKey = args.getOrNull(1) ?: page.lowercase()
        val raw = URI("https://runescape.wiki/w/Transcript:$page?action=raw").toURL().readText()
        val displayName = page.replace('_', ' ')
        val rows = parse(raw, displayName)
        for ((i, row) in rows.withIndex()) row.print(petKey, i + 1)
    }

    private data class Row(
        val kind: String,
        val stage: String,
        val conditionHint: String,
        val lines: List<String>,
    ) {
        fun print(petKey: String, index: Int) {
            val suffix = when (kind) {
                "overhead" -> "overhead_$index"
                else -> "${index}"
            }
            println("[.${petKey}_$suffix]")
            println("pet = \"$petKey\"")
            println("stage = \"$stage\"")
            if (conditionHint.isNotBlank()) {
                println("# TODO condition: $conditionHint")
            }
            println("condition = \"\"")
            println("lines = [")
            for (line in lines) println("    \"$line\",")
            println("]")
            println()
        }
    }

    private fun parse(raw: String, npcDisplay: String): List<Row> {
        val rows = mutableListOf<Row>()
        var section = SectionMeta()
        var lines = mutableListOf<String>()

        fun flush() {
            if (lines.isNotEmpty() && !section.skip) {
                rows += Row(section.kind, section.stage, section.conditionHint, lines.toList())
            }
            lines = mutableListOf()
        }

        for (rawLine in raw.lines()) {
            val line = rawLine.trimEnd()
            val heading = headingLevel(line)
            if (heading > 0) {
                flush()
                section = classifyHeading(line.trim('=').trim(), section)
                continue
            }
            if (section.skip) continue
            if (!line.trimStart().startsWith("*")) continue
            val body = line.trimStart().trimStart('*').trim().trimStart(':').trim()
            val rendered = renderLine(body, npcDisplay) ?: continue
            lines += rendered
        }
        flush()
        return rows
    }

    private data class SectionMeta(
        val kind: String = "convo",
        val stage: String = "",
        val conditionHint: String = "",
        val skip: Boolean = false,
    )

    private fun headingLevel(line: String): Int {
        val trimmed = line.trim()
        if (!trimmed.startsWith("=")) return 0
        var lead = 0
        while (lead < trimmed.length && trimmed[lead] == '=') lead++
        var tail = 0
        while (tail < trimmed.length && trimmed[trimmed.length - 1 - tail] == '=') tail++
        return minOf(lead, tail).takeIf { it >= 2 } ?: 0
    }

    private fun classifyHeading(text: String, prev: SectionMeta): SectionMeta {
        val lower = text.lowercase()
        val kind = when {
            "overhead" in lower || "random" in lower -> "overhead"
            else -> "convo"
        }
        val skip = when {
            "removed" in lower -> true
            "hunger" in lower -> true
            "starv" in lower -> true
            "fed" in lower -> true
            "feeding" in lower -> true
            else -> false
        }
        val stage = when {
            "spawn" in lower || "baby" in lower -> "baby"
            "adult" in lower || "grown" in lower -> "grown"
            else -> prev.stage
        }
        val conditionHint = when {
            text.startsWith("If ", ignoreCase = true) -> text.trim()
            "<" in text || ">" in text -> text.trim()
            else -> ""
        }
        return SectionMeta(kind = kind, stage = stage, conditionHint = conditionHint, skip = skip)
    }

    private fun renderLine(body: String, npcDisplay: String): String? {
        val speaker = """^'''([^:']+):'''\s*(.*)$""".toRegex().find(body) ?: return null
        val name = speaker.groupValues[1].trim()
        val text = stripWiki(speaker.groupValues[2]).ifBlank { return null }
        val role = when {
            name.equals("Player", ignoreCase = true) -> "player"
            name.equals(npcDisplay, ignoreCase = true) -> "npc"
            name.contains(npcDisplay, ignoreCase = true) -> "npc"
            else -> "npc"
        }
        return "$role: ${escape(text)}"
    }

    private fun stripWiki(s: String): String =
        s.replace(Regex("""\[\[[^|\]]*\|([^\]]+)\]\]"""), "$1")
            .replace(Regex("""\[\[([^\]]+)\]\]"""), "$1")
            .replace(Regex("""'''([^']+)'''"""), "$1")
            .replace(Regex("""''([^']+)''"""), "$1")
            .replace(Regex("<[^>]+>"), "")
            .trim()

    private fun escape(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"")
}

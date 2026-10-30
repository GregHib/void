package content.quest.miniquest.barbarian_training

import content.entity.player.modal.book.Books
import content.quest.miniquest.barbarian_training.BarbarianTraining.REPORTED
import content.quest.miniquest.barbarian_training.BarbarianTraining.STARTED
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * The journal fills in with each of Otto's lessons as they're told, and their follow-up once reported back.
 * Each lesson is its own book, the first page is the lesson and the second what Otto says once it's done.
 */
class BarbarianSkillsBook(val books: Books) : Script {

    val sections = listOf("rod", "barehand", "bow", "herblore", "spear", "pyre", "hasta")

    init {
        // TODO confirm the layout of lessons after the first, the layout follows the 2009 book
        books.generate("barbarian_skills", ::pages)
    }

    fun pages(player: Player): List<List<String>> {
        val lines = books.get("barbarian_skills").flatten().toMutableList()
        var gap = INTRO_GAP
        for (section in sections) {
            val stage = player.barbarianTraining(section)
            if (stage < STARTED) {
                continue
            }
            val lesson = books.get("barbarian_skills_$section")
            repeat(gap) { lines.add("") }
            gap = 1
            lines.addAll(lesson.first())
            if (stage == REPORTED && lesson.size > 1) {
                lines.add("")
                lines.addAll(lesson[1])
            }
        }
        // The last line of each left-hand page is left empty
        val pages = mutableListOf<List<String>>()
        var index = 0
        while (index < lines.size) {
            val left = lines.subList(index, minOf(index + LINES_PER_SIDE - 1, lines.size))
            index += left.size
            val right = lines.subList(index, minOf(index + LINES_PER_SIDE, lines.size))
            index += right.size
            pages.add(left + List(LINES_PER_SIDE - left.size) { "" } + right)
        }
        return pages
    }

    private companion object {
        const val LINES_PER_SIDE = 15
        const val INTRO_GAP = 3
    }
}

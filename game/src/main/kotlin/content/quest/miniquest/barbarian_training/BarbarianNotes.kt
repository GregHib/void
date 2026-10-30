package content.quest.miniquest.barbarian_training

import content.entity.player.modal.book.Books
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * Ancient pages found in the Ancient Cavern are copied into My notes, which shows each page once copied.
 */
class BarbarianNotes(val books: Books) : Script {

    init {
        itemOption("Copy to log", "ancient_page_*") { (item, slot) ->
            if (!inventory.remove(slot, item.id)) {
                return@itemOption
            }
            if (addVarbit("barbarian_notes", item.id)) {
                message("You copy the scrap of information to your logbook.")
            } else {
                message("You already have this information in your logbook.")
            }
        }

        // TODO confirm the layout, follows 2009scape with one page per side
        books.generate("my_notes", ::pages)
    }

    fun pages(player: Player): List<List<String>> {
        val notes = books.get("my_notes")
        return notes.indices.chunked(2).map { sides ->
            sides.flatMap { index ->
                val lines = if (player.containsVarbit("barbarian_notes", "ancient_page_${index + 1}")) notes[index] else emptyList()
                lines + List(LINES_PER_SIDE - lines.size) { "" }
            }
        }
    }

    private companion object {
        const val LINES_PER_SIDE = 15
    }
}

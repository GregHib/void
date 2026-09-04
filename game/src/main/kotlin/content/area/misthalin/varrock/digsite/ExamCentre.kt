package content.area.misthalin.varrock.digsite

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class ExamCentre : Script {
    init {
        objectOperate("Search", "qip_digsite_bookcase_low,qip_digsite_bookcase_high") {
            message("You search through the bookcase...")
            delay(1)
            statement("The label on this shelf reads 'Earth Sciences'; however, the helpful books have been taken. It looks like the other students got to them first.")
        }

        objectOperate("Search", "elemental_workshop_2_bookcase") {
            if (ownsItem("beaten_book")) {
                statement("The bookcase has nothing in it that interests you.")
            } else if (inventory.add("beaten_book")) {
                item("beaten_book", "You find an old-looking book.")
            } else {
                item("book_on_chemicals", "You find an old-looking book, but you do not have space in your inventory to put it.")
            }
        }

        objectOperate("Search", "qip_digsite_bookcase_low_digbookcase_shorter") {
            if (inventory.add("book_on_chemicals")) {
                item("book_on_chemicals", "You find a book on chemicals.")
            } else {
                player<Sad>("I'd best make sure I have room for it.")
            }
        }

        objectOperate("Search", "qip_digsite_bookcase_low_digbookcase_shorter_m_o") {
            message("You search the bookcase...")
            delay(3)
            if (get("golem_b", 0) < 2 || inventory.contains("varmens_notes")) {
                message("You find nothing of interest.")
                return@objectOperate
            }
            if (inventory.isFull()) {
                message("You find Varmen's expedition notes, but don't have room to take them.")
                return@objectOperate
            }
            set("golem_b", 3)
            inventory.add("varmens_notes")
            item("varmens_notes", "You find Varmen's expedition notes.")
        }
    }
}

package content.area.kandarin.baxtorian_falls

import content.entity.player.inv.item.addOrDrop
import content.entity.player.modal.book.openBook
import content.quest.quest
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory

class TouristInfoCentre : Script {
    init {
        objectOperate("Search", "waterfall_bookcase,waterfall_bookcase_2") { (target) ->
            if (target.tile.level == 1 && target.tile.x in 2516..2521 && target.tile.y in 3423..3431) {
                message("You search the bookcase.")
                delay(2)
                if (questStage("waterfall_quest") < 2) {
                    // TODO need non-quest responses
                    return@objectOperate
                }
                if (inventory.contains("book_on_baxtorian")) {
                    // TODO
                    return@objectOperate
                }
                addOrDrop("book_on_baxtorian")
                message("You find a book named 'Book on Baxtorian' on the bookcase.")
            }
        }

        itemOption("Read", "book_on_baxtorian") {
            openBook("book_on_baxtorian")
            if (quest("waterfall_quest") == "found_hudon") {
                set("waterfall_quest", if (carriesItem("glarials_pebble")) "found_pebble" else "read_book")
            }
        }
    }
}

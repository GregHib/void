package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script

class AlKharidFlyer : Script {

    init {
        itemOption("Read", "al_kharid_flyer") {
            item(
                "al_kharid_flyer",
                """
                Come to the Al Kharid Market place! High quality produce at low, low prices! Show this flyer to a merchant for money off your next purchase, courtesy of Ali Morrisane!
            """,
            )
            statement("You notice that the money off voucher is out of date.")
        }
    }
}

package content.quest.member.mahjarrat.the_dig_site

import content.skill.magic.jewellery.jewelleryTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject

class DigSitePendant : Script {

    val areas: AreaDefinitions by inject()

    val digSite = areas["dig_site_teleport"]

    init {
        itemOption("Rub", "dig_site_pendant_#") {
            if (contains("delay")) {
                return@itemOption
            }
            message("You rub the pendant...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, digSite)
        }
    }
}

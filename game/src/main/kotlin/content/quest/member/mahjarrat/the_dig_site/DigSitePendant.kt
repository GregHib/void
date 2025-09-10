package content.quest.member.mahjarrat.the_dig_site

import content.entity.player.inv.inventoryItem
import content.skill.magic.jewellery.jewelleryTeleport
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.event.Script
@Script
class DigSitePendant {

    val areas: AreaDefinitions by inject()
    
    val digSite = areas["dig_site_teleport"]
    
    init {
        inventoryItem("Rub", "dig_site_pendant_#") {
            if (player.contains("delay")) {
                return@inventoryItem
            }
            player.message("You rub the pendant...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, digSite)
        }

    }

}

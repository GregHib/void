package content.skill.prayer.bone

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.event.Script
@Script
class BoneOffering {

    init {
        itemOnObjectOperate(obj = "altar*") {
            if (!item.def.contains("prayer_xp")) {
                return@itemOnObjectOperate
            }
            val tile = target.nearestTo(player.tile)
            val count = player.inventory.count(item.id)
            if (count > 1) {
                val (_, amount) = makeAmount(listOf(item.id), "", count)
                offer(amount, tile)
            } else {
                offer(1, tile)
            }
        }

    }

    suspend fun ItemOnObject.offer(amount: Int, tile: Tile) {
        val xp = item.def["prayer_xp", 0.0]
        repeat(amount) {
            if (player.inventory.remove(item.id)) {
                player.experience.add(Skill.Prayer, xp)
                player.anim("offer_bones")
                areaGfx("bone_offering", tile)
                player.message(
                    "The gods ${
                        when {
                            xp <= 25 -> "accept"
                            xp <= 100 -> "are pleased with"
                            else -> "are very pleased with"
                        }
                    } your offering.",
                    ChatType.Filter,
                )
                pause(2)
            } else {
                player.mode = EmptyMode
            }
        }
    }
}

package content.skill.prayer.bone

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class BoneOffering : Script {

    init {
        itemOnObjectOperate(obj = "altar*") { (target, item) ->
            if (!item.def.contains("prayer_xp")) {
                return@itemOnObjectOperate
            }
            val tile = target.nearestTo(tile)
            val count = inventory.count(item.id)
            if (count > 1) {
                val (_, amount) = makeAmount(listOf(item.id), "", count)
                offer(item, amount, tile)
            } else {
                offer(item, 1, tile)
            }
        }
    }

    suspend fun Player.offer(item: Item, amount: Int, tile: Tile) {
        val xp = item.def["prayer_xp", 0.0]
        repeat(amount) {
            if (inventory.remove(item.id)) {
                experience.add(Skill.Prayer, xp)
                anim("offer_bones")
                areaGfx("bone_offering", tile)
                message(
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
                mode = EmptyMode
            }
        }
    }
}

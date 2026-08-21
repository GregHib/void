package content.skill.prayer.bone

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class BoneOffering : Script {

    init {
        itemOnObjectOperate(obj = "prayer_altar*") { (target, item) ->
            Tables.intOrNull("bones.${item.id}.xp") ?: return@itemOnObjectOperate
            val tile = target.nearestTo(tile)
            val bonus = bonusPercent(target.tile)
            val count = inventory.count(item.id)
            if (count > 1) {
                val (_, amount) = makeAmount(listOf(item.id), "", count)
                offer(item, amount, tile, bonus)
            } else {
                offer(item, 1, tile, bonus)
            }
        }
    }

    /**
     * Percentage of extra Prayer experience granted by the altar standing on [tile], on top of the
     * base experience for burying the bones. Chaos altars in the Wilderness temples give 250% more.
     */
    fun bonusPercent(tile: Tile): Int = Areas.get(tile.zone)
        .filter { tile in it.area }
        .maxOfOrNull { it["bone_offering_bonus", 0] } ?: 0

    suspend fun Player.offer(item: Item, amount: Int, tile: Tile, bonus: Int) {
        val xp = Tables.intOrNull("bones.${item.id}.xp") ?: return
        repeat(amount) {
            if (inventory.remove(item.id)) {
                exp(Skill.Prayer, (xp / 10.0) * (100 + bonus) / 100.0)
                anim("offer_bones")
                areaGfx("bone_offering", tile)
                message(
                    "The gods ${
                        when {
                            xp <= 250 -> "accept"
                            xp <= 1000 -> "are pleased with"
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

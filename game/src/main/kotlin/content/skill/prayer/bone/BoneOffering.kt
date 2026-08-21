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
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class BoneOffering : Script {

    init {
        itemOnObjectOperate(obj = "prayer_altar*") { (target, item) ->
            Tables.intOrNull("bones.${item.id}.xp") ?: return@itemOnObjectOperate
            val tile = target.nearestTo(tile)
            val bonus = bonusPercent(target)
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
     * Percentage of extra Prayer experience granted by [target], on top of the base experience for
     * burying the bones. Every altar gives [DEFAULT_BONUS], chaos altars in the Wilderness temples
     * give more.
     */
    fun bonusPercent(target: GameObject): Int {
        val bonus = Tables.intOrNull("bone_offering.${target.id}.bonus") ?: return DEFAULT_BONUS
        val wildernessOnly = Tables.boolOrNull("bone_offering.${target.id}.wilderness_only") ?: false
        if (wildernessOnly && target.tile !in Areas["wilderness"]) {
            return DEFAULT_BONUS
        }
        return bonus
    }

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

    companion object {
        private const val DEFAULT_BONUS = 50
    }
}

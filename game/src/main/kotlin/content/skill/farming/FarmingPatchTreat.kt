package content.skill.farming

import content.entity.player.inv.item.addOrDrop
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.weakQueue

class FarmingPatchTreat : Script {

    init {
        objectOperate("Rake", "*_patch_weeds_*", handler = ::rake)
        itemOnObjectOperate("compost,supercompost", "*", handler = ::compost)
        itemOnObjectOperate("watering_can_*", "*", handler = ::water)
    }

    private suspend fun compost(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_")) {
            return
        }
        val id = target.def(player).stringId
        if (id.endsWith("_fullygrown")) {
            player.message("Composting it isn't going to make it get any bigger.")
            return
        }
        val item = interact.item
        val key = if (item.id == "supercompost") "patch_super_compost" else "patch_compost"
        if (player.containsVarbit(key, interact.target.id)) {
            player.message("This allotment has already been treated with ${item.id}.")
            return
        }
        val value = player[interact.target.id, "weeds_life3"]
        if (value.endsWith("_compost") || value.endsWith("_super")) {
            player.message("Composting it isn't going to make it get any bigger.")
            return
        }
        if (!player.inventory.replace(interact.slot, item.id, "bucket")) {
            return
        }
        player.addVarbit(key, interact.target.id)
        player.anim("farming_pour_water")
        player.sound("farming_compost")
        player.delay(2)
        player.message("You treat the ${interact.target.patchName()} with ${item.id}.")
        player.exp(Skill.Farming, item.def["farming_xp", 0.0])
    }

    private suspend fun water(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_veg") && !target.id.startsWith("farming_flower") && !target.id.startsWith("farming_hops")) {
            player.message("This patch doesn't need watering.")
            return
        }
        val id = target.def(player).stringId
        if (id.endsWith("_fullygrown")) {
            player.message("This patch doesn't need watering.")
            return
        }
        val value = player[target.id, "weeds_life3"]
        if (value.startsWith("weeds") || value.contains("watered")) {
            player.message("This patch doesn't need watering.")
            return
        }
        if (!player.inventory.discharge(player, interact.slot)) {
            return
        }
        player.anim("farming_watering")
        player.sound("farming_watering")
        player.delay(2)
        player[target.id] = value.replaceFirst("_", "_watered_")
    }

    private fun rake(player: Player, interact: PlayerOnObjectInteract, count: Int = 3) {
        if (count <= 0) {
            return
        }
        if (!player.inventory.contains("rake")) {
            player.message("You need a rake to weed a farming patch")
            return
        }
        val obj = interact.target
        if (player[obj.id, "weeds_life3"] == "weeds_0") {
            player.message("This ${obj.patchName()} doesn't need weeding right now.")
            return
        }
        player.anim("farming_raking")
        player.weakQueue("farming_rake", 3) {
            val current = player[obj.id, "weeds_life3"]
            val next = when (current) {
                "weeds_life3" -> "weeds_2"
                "weeds_life2" -> "weeds_1"
                "weeds_life1" -> "weeds_0"
                "weeds_2" -> "weeds_1"
                "weeds_1" -> "weeds_0"
                else -> return@weakQueue
            }
            if (Level.success(player.levels.get(Skill.Farming), 180..255)) {
                player[obj.id] = next
                player.addOrDrop("weeds")
                player.timers.startIfAbsent("farming_tick")
                player.exp(Skill.Farming, 8.0)
                rake(player, interact, count - 1)
            } else {
                rake(player, interact, count)
            }
        }
    }
}

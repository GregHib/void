package content.skill.farming

import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.entity.player.stat.Stats
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.*
import world.gregs.voidps.engine.entity.character.mode.interact.ItemOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.weakQueue

class FarmingPatchPlant : Script {

    init {
        playerSpawn {
            for (variable in variables.data.keys) {
                if (variable.startsWith("farming_")) {
                    sendVariable(variable)
                }
            }
        }

        itemOnObjectOperate("*_seed,scarecrow,*_sapling", "*", handler = ::plantSeed)
    }

    private suspend fun plantSeed(player: Player, interact: ItemOnObjectInteract) {
        val target = interact.target
        if (!target.id.startsWith("farming_")) {
            return
        }
        val item = interact.item
        item.def.getOrNull<String>("farming_patch") ?: return
        val id = target.def(player).stringId
        if (id.endsWith("patch_weeded")) {
            plant(player, interact)
            return
        }
        val amount = item.def["farming_amount", 1]
        val stage = player[target.id, "weeds_life3"]
        if (stage != "weeds_0") {
            player.statement("You can only plant ${item.def.name.plural(amount)} in an empty patch.")
            return
        }
        val patch: String = item.def.getOrNull("farming_patch") ?: return
        val patchName = target.patchName()
        if (patchName.removeSuffix(" patch") != patch) {
            player.statement("You can only plant ${item.def.name.plural(amount)} in ${patchName.an()} $patchName.")
            return
        }
    }

    private suspend fun plant(player: Player, interact: ItemOnObjectInteract) {
        val item = interact.item
        val amount = item.def["farming_amount", 1]
        val variable = interact.target.id
        if (variable.startsWith("farming_spirit_tree_patch") && hasSpiritTree(player)) {
            player.message("You can only plant one spirit tree at a time.") // TODO proper message
            return
        }
        val patchName = interact.target.patchName()
        if (patchName.contains("tree") && !player.inventory.contains("spade")) {
            player.message("You need a spade to do that.")
            return
        }
        if (!patchName.contains("tree") && !player.inventory.contains("seed_dibber")) {
            player.message("You need a seed dibber to plant the seed in the dirt.") // TODO proper message
            return
        }
        if (!player.inventory.remove(item.id, amount)) {
            player.message("You need $amount ${item.def.name.plural(amount)} to grow those.")
            return
        }
        val level = item.def["farming_level", 1]
        if (!player.has(Skill.Farming, level)) {
            player.statement("You need to be a level $level to plant that.")
            return
        }
        if (patchName.contains("tree")) {
            player.anim("human_dig")
            player.sound("dig_spade")
        } else {
            player.anim("farming_seed_dibbing")
            player.sound("farming_dibbing")
        }
        player.delay(3)
        player.message("You plant ${if (amount == 1) "a" else amount} ${item.def.name.lowercase().plural(amount)} in the $patchName.", type = ChatType.Filter)
        val crop: String = item.def.getOrNull("farming_crop") ?: return
        player[variable] = "${crop}_0"
        player.exp(Skill.Farming, item.def["farming_xp", 0.0])
    }

    private fun hasSpiritTree(player: Player): Boolean {
        return !player["farming_spirit_tree_patch_port_sarim", "weeds_3"].startsWith("weeds") ||
        !player["farming_spirit_tree_patch_etceteria", "weeds_3"].startsWith("weeds") ||
        !player["farming_spirit_tree_patch_brimhaven", "weeds_3"].startsWith("weeds")
    }

}

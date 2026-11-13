package content.skill.farming

import content.entity.player.stat.Stats
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class FarmingPatchInspect : Script {

    init {
        objectOperate("Inspect", handler = ::inspect)
        objectOperate("Guide", handler = ::guide)
    }

    private fun inspect(player: Player, interact: PlayerOnObjectInteract) {
        if (!interact.target.id.startsWith("farming_")) {
            player.noInterest()
            return
        }
        player.message(
            buildString {
                val name = interact.target.patchName()
                append("This is${name.an()} $name.")
                append(" ")
                when {
                    player.containsVarbit("patch_super_compost", interact.target.id) -> append("The soil has been treated with supercompost.")
                    player.containsVarbit("patch_compost", interact.target.id) -> append("The soil has been treated with compost.")
                    else -> append("The soil has not been treated.")
                }
                append(" ")

                val value = player[interact.target.id, "weeds_life3"]
                if (value == "weeds_0") {
                    append("The patch is empty.")
                } else if (value.contains("weeds")) {
                    append("The patch needs weeding.")
                } else {
                    val ending = value.substringBeforeLast("_")
                    val type = ending.removeSuffix("_watered").removeSuffix("_dead").removeSuffix("_diseased")
                    if (ending.endsWith("_dead")) {
                        append("The patch has become infected by disease and has died.")
                        return@buildString
                    }
                    if (ending.endsWith("_diseased")) {
                        append("The patch has become infected by disease.") // TODO proper message
                        return@buildString
                    }
                    val amount = if (name == "allotment") 3 else 1
                    val stage = value.substringAfterLast("_").toIntOrNull()
                    val stages = growthStages(interact.target.id, type) ?: when (name) {
                        // TODO dynamic patches
                        "allotment", "herb patch", "belladonna patch", "flower patch" -> 5
                        "fruit tree patch", "cactus patch" -> 7
                        "tree patch" -> 11
                        "evil turnip patch" -> 2
                        else -> 0
                    }
                    val fullyGrown = value.substringAfterLast("_").startsWith("life") ||
                        value.substringAfterLast("_") == "claim" ||
                        interact.target.def(player).stringId.contains("_fullygrown")
                    if (value.substringAfterLast("_") == "stump" || type.endsWith("_stump")) {
                        append("You can uproot this stump with a spade.")
                    } else if (fullyGrown) {
                        append("The patch is fully grown.")
                    } else if (stage == null) {
                        append("The patch has ${type.toLowerSpaceCase().plural(amount)} growing in it and is at state $stages/$stages.")
                    } else {
                        append("The patch has ${type.toLowerSpaceCase().plural(amount)} growing in it and is at state ${stage + 1}/$stages.")
                    }
                }
            },
        )
    }

    private fun growthStages(variable: String, crop: String): Int? {
        val values = (VariableDefinitions.get(variable)?.values as? MapValues)?.values ?: return null
        val prefix = "${crop}_"
        val maximum = values.keys
            .asSequence()
            .map { it.toString() }
            .mapNotNull { key ->
                if (!key.startsWith(prefix)) {
                    null
                } else {
                    key.removePrefix(prefix).toIntOrNull()
                }
            }
            .maxOrNull()
        val hasFullyGrownState = values.keys.any { it.toString() == "${crop}_life1" }
        return maximum?.plus(
            when {
                variable.contains("fruit_tree") || variable.contains("bush_patch") || variable.contains("cactus_patch") -> 1
                hasFullyGrownState -> 2
                else -> 1
            },
        )
    }

    private fun guide(player: Player, interact: PlayerOnObjectInteract) {
        if (!interact.target.id.startsWith("farming_")) {
            player.noInterest()
            return
        }
        val name = interact.target.patchName()
        Stats.openGuide(
            player,
            Skill.Farming,
            when (name) {
                "allotment" -> 0
                "hops patch" -> 1
                "tree patch" -> 2
                "fruit tree patch" -> 3
                "bush patch" -> 4
                "flower patch" -> 5
                "herb patch" -> 6
                else -> 7
            },
        )
    }
}

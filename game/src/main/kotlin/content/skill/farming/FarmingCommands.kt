package content.skill.farming

import content.entity.player.command.find
import content.quest.questJournal
import net.pearx.kasechange.toSentenceCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.timer.TICKS

class FarmingCommands(
    val accounts: AccountDefinitions,
) : Script {

    init {
        modCommand("patches", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "List farming patches for the given player", handler = ::listPatches)
        modCommand("growth", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Check the next farming growth tick for the given player", handler = ::growthInfo)
        modCommand("rot", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Completes all rotting compost bins for the given player", handler = ::rot)
    }

    fun listPatches(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(0)) ?: return
        val list = mutableListOf<String>()
        for ((_, vars) in FarmingPatches.patches) {
            for (variable in vars) {
                val value: String = target[variable] ?: continue
                list.add("=== ${variable.removePrefix("farming_").toTitleCase()} ===")
                val type = value.substringBeforeLast("_")
                val stage = value.substringAfterLast("_")
                when {
                    value.endsWith("_watered") -> list.add("<blue>Watered")
                    value.endsWith("_diseased") -> list.add("<orange>Diseased")
                    value.endsWith("_dead") -> list.add("<dark_red>Dead")
                    stage.startsWith("life") -> list.add("<dark_green>Harvestable")
                }
                list.add("Crop: ${type.removeSuffix("_watered").removeSuffix("_diseased").removeSuffix("_dead").toSentenceCase()}")
                if (stage.startsWith("life")) {
                    list.add("Lives: ${stage.removePrefix("life")}")
                } else {
                    var stages = 5
                    if (type.startsWith("weeds")) {
                        stages = 3
                    }
                    val int = stage.toIntOrNull()
                    if (int != null) {
                        list.add("Stage: $int/$stages")
                    } else {
                        list.add("Stage: $stage")
                    }
                }
                if (player.containsVarbit("patch_super_compost", variable)) {
                    list.add("Soil: Super-compost")
                } else if (player.containsVarbit("patch_compost", variable)) {
                    list.add("Soil: Compost")
                } else {
                    list.add("Soil: Normal")
                }
                list.add("")
            }
        }
        player.questJournal("Active Farming Patches", list)
    }

    fun growthInfo(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(0)) ?: return
        val ticks = target.timers.remaining("farming_tick")
        if (ticks == -1) {
            player.message("Player has no patches active.", ChatType.Console)
            return
        }
        player.message("Next growth tick for '${target.name}' in ${TICKS.toMinutes(ticks)}m${TICKS.toSeconds(ticks.rem(100))}s", ChatType.Console)
    }

    fun rot(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(0)) ?: return
        rot(target, "compost_bin_falador")
        rot(target, "compost_bin_catherby")
        rot(target, "compost_bin_port_phasmatys")
        rot(target, "compost_bin_ardougne")
    }

    private fun rot(player: Player, variable: String) {
        val value: String = player[variable] ?: return
        if (!value.contains("_rotting")) {
            return
        }
        val stage = value.substringAfterLast("_").toIntOrNull() ?: return
        player[variable] = value.replace("_$stage", "_ready")
    }
}

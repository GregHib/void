package content.skill.farming

import content.entity.sound.jingle
import content.entity.sound.sound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

@Script
class Farming : Api {

    override fun spawn(player: Player) {
        if (!player.contains("farming_offset_mins")) {
            player["farming_offset_mins"] = random.nextInt(0, 30)
        }
        if (player.contains("last_growth_cycle")) {
            player.timers.start("farming_tick", true)
        }
    }

    init {
        timerStart("farming_tick") { player ->
            // Next farming tick
            val remaining = 5 - (epochMinutes() + player["farming_offset_mins", 0]).rem(5)
            interval = TimeUnit.MINUTES.toTicks(remaining)
        }

        timerTick("farming_tick") { player ->
            nextInterval = 500 // 500
            val epoch = epochMinutes()
            val delta = epoch - player["last_growth_cycle", epoch - 1]
            player["last_growth_cycle"] = epoch
            grow(player, delta / 5)
        }
    }

    val varbits = listOf("allotment_falador_nw")

    val definitions: VariableDefinitions by inject()

    fun grow(player: Player, stages: Int) {
        for (varbit in varbits) {
            val current: String = player[varbit] ?: continue
            val type = current.substringAfterLast("_")
            if (type == "none" || type == "compost" || type == "super" || type == "dead") {
                continue
            }
            val produce = current.substringBeforeLast("_")
            if (produce.endsWith("diseased")) {
                player[varbit] = current.replace("diseased", "dead")
                amuletOfFarming(player, varbit)
                continue
            }
            val stage = type.toInt()
            if (current.startsWith("weeds")) {
                val next = (stage + stages).rem(4)
                player[varbit] = if (next == 3) {
                    "weeds_${
                        when (random.nextInt(3)) {
                            0 -> "none"
                            1 -> "compost"
                            else -> "super"
                        }
                    }"
                } else {
                    "weeds_$next"
                }
                continue
            }
            // TODO use maths and produce to figure out if growth should occur
            val definition = definitions.get(varbit) ?: continue
            val int = definition.values.toInt(current)
            val list = (definition.values as ListValues).values as List<String>
            var next = current
            for (i in int until int + stages) {
                next = list[int + 1]
                if (next.endsWith("_none")) {
                    break
                }
                if (random.nextInt(5) == 0) { // TODO chances
                    val value = list[int + 64] // diseased
                    if (value != current.replace(produce, "${produce}_diseased")) {
                        // TODO check
                        continue
                    }
                    next = value // diseased
                }
            }
            if (next.endsWith("_none")) {
                // complete
                // TODO if compost or super otherwise keep same
            }
            player[varbit] = next
            amuletOfFarming(player, varbit)
        }
    }

    private fun amuletOfFarming(player: Player, patch: String) {
        if (player["amulet_of_farming_patch", ""] != patch) {
            return
        }
        player.jingle("farming_amulet_alert")
        player.sound("farming_amulet")
        player.message("A low hum resonates from the <col=ef1020>Amulet of Nature</col> - it seeks audience with you.")
    }

}
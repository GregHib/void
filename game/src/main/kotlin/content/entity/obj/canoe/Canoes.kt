package content.entity.obj.canoe

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.CanoeDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.type.Direction

class Canoes(val stations: CanoeDefinitions) : Script {

    init {
        playerSpawn {
            sendVariable("canoe_state_lumbridge")
            sendVariable("canoe_state_champions_guild")
            sendVariable("canoe_state_barbarian_village")
            sendVariable("canoe_state_edgeville")
            sendVariable("canoe_state_wilderness_pond")
        }

        objectOperate("Chop-down", "canoe_station") { (target) ->
            if (!has(Skill.Woodcutting, 12, false)) {
                statement("You must have at least level 12 woodcutting to start making canoes.")
                return@objectOperate
            }
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                message("You need a hatchet to chop down this tree.")
                message("You do not have a hatchet which you have the woodcutting level to use.")
                return@objectOperate
            }
            val location = target.id.removePrefix("canoe_station_")
            when (target.rotation) {
                1 -> walkToDelay(target.tile.add(-1, 4))
                2 -> walkToDelay(target.tile.add(3, 2))
                3 -> walkToDelay(target.tile.add(2))
            }
            face(Direction.cardinal[target.rotation])
            delay()
            anim("${hatchet.id}_shape_canoe")
            delay()
            target.anim("canoe_fall")
            clearAnim()
            sound("fell_tree")
            set("canoe_state_$location", "falling")
            delay()
            set("canoe_state_$location", "fallen")
        }

        interfaceOpened("canoe") { id ->
            val dugout = levels.get(Skill.Woodcutting) > 26
            interfaces.sendVisibility(id, "visible_dugout", dugout)
            interfaces.sendVisibility(id, "invisible_dugout", !dugout)

            val stable = levels.get(Skill.Woodcutting) > 41
            interfaces.sendVisibility(id, "visible_stable_dugout", stable)
            interfaces.sendVisibility(id, "invisible_stable_dugout", !stable)

            val waka = levels.get(Skill.Woodcutting) > 56
            interfaces.sendVisibility(id, "visible_waka", waka)
            interfaces.sendVisibility(id, "invisible_waka", !waka)
        }

        interfaceOption("Select", "canoe:a_*") {
            val type = it.component.removePrefix("a_")
            (dialogueSuspension as? StringSuspension)?.resume(type)
        }

        objectOperate("Shape-canoe", "canoe_station_fallen") { (target) ->
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                message("You need a hatchet to shape a canoe.")
                message("You do not have a hatchet which you have the woodcutting level to use.")
                return@objectOperate
            }
            when (target.rotation) {
                1 -> walkToDelay(target.tile.add(-1, 2))
                2 -> walkToDelay(target.tile.add(2, 2))
                3 -> walkToDelay(target.tile.add(2, 2))
            }
            arriveDelay()
            val location = target.id.removePrefix("canoe_station_")
            face(Direction.cardinal[target.rotation])
            open("canoe")
            val canoe = StringSuspension.get(this)
            closeMenu()
            val required = when (canoe) {
                "log" -> 12
                "dugout" -> 26
                "stable_dugout" -> 41
                "waka" -> 56
                else -> return@objectOperate
            }
            if (!has(Skill.Woodcutting, required, message = true)) {
                return@objectOperate
            }
            val level = levels.get(Skill.Woodcutting)
            val min = hatchet.def.getOrNull<Int>("canoe_chance_min") ?: return@objectOperate
            val max = hatchet.def.getOrNull<Int>("canoe_chance_max") ?: return@objectOperate
            val chance: IntRange = min until max
            var count = 0
            while (count++ < 50) {
                anim("${hatchet.id}_shape_canoe")
                delay(3)
                if (Level.success(level, chance)) {
                    break
                }
            }
            set("canoe_state_$location", canoe)
            clearAnim()
            exp(
                Skill.Woodcutting,
                when (canoe) {
                    "log" -> 30.0
                    "dugout" -> 60.0
                    "stable_dugout" -> 90.0
                    "waka" -> 150.0
                    else -> 0.0
                },
            )
        }

        objectOperate("Float Log", "canoe_station_log") { (target) ->
            float(target)
        }

        objectOperate("Float Canoe", "canoe_station_*") { (target) ->
            float(target)
        }

        objectOperate("Paddle Canoe", "canoe_station_water_*") { (target) ->
            face(Direction.cardinal[target.rotation])
            val station = target.id.removePrefix("canoe_station_")
            val canoe = target.def(this).stringId.removePrefix("canoe_station_water_")
            val destination = canoeStationMap(canoe, station)
            if (destination == null || destination == station) {
                return@objectOperate
            }
            if (destination == "wilderness_pond" && get("wilderness_canoe_warning", true)) {
                statement("<red>Warning</col> This canoe will take you deep into the <red>Wilderness</col>. There are no trees suitable to make a canoe there. You will have to walk back.")
                choice("Are you sure you wish to travel") {
                    option("Yes, I'm brave.")
                    option("Eeep! The Wilderness... No thank you.") {
                        return@option
                    }
                    option("Yes, and don't show this warning again.") {
                        set("wilderness_canoe_warning", false)
                    }
                }
            }
            canoeTravel(canoe, station, destination)
            val definition = stations.get(destination)
            tele(definition.destination)
            set("canoe_state_$station", "tree")
            set("canoe_state_$destination", "tree")
            GameObjects.add("a_sinking_canoe_$canoe", tile = definition.sink, rotation = 1, ticks = 3)
            sound("canoe_sink")
            message(definition.message, type = ChatType.Filter)
        }
    }

    suspend fun Player.float(target: GameObject) {
        when (target.rotation) {
            1 -> walkToDelay(target.tile.add(-1, 2))
            2 -> walkToDelay(target.tile.add(2, 2))
            3 -> walkToDelay(target.tile.add(2, 2))
        }
        val location = target.id.removePrefix("canoe_station_")
        val canoe = target.def(this).stringId.removePrefix("canoe_station_")
        set("canoe_state_$location", "float_$canoe")
        anim("canoe_push")
        face(Direction.cardinal[target.rotation])
        target.anim("canoe_fall")
        sound("canoe_roll")
        delay(2)
        set("canoe_state_$location", "water_$canoe")
    }
}

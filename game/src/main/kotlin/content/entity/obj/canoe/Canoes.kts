package content.entity.obj.canoe

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.entity.sound.playSound
import content.skill.woodcutting.Hatchet
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

playerSpawn { player ->
    player.sendVariable("canoe_state_lumbridge")
    player.sendVariable("canoe_state_champions_guild")
    player.sendVariable("canoe_state_barbarian_village")
    player.sendVariable("canoe_state_edgeville")
    player.sendVariable("canoe_state_wilderness_pond")
}

val objects: GameObjects by inject()

objectOperate("Chop-down", "canoe_station") {
    if (!player.has(Skill.Woodcutting, 12, false)) {
        statement("You must have at least level 12 woodcutting to start making canoes.")
        return@objectOperate
    }
    val hatchet = Hatchet.best(player)
    if (hatchet == null) {
        player.message("You need a hatchet to chop down this tree.")
        player.message("You do not have a hatchet which you have the woodcutting level to use.")
        return@objectOperate
    }
    val location = target.id.removePrefix("canoe_station_")
    when (target.rotation) {
        1 -> player.walkToDelay(target.tile.add(-1, 4))
        2 -> player.walkToDelay(target.tile.add(3, 2))
        3 -> player.walkToDelay(target.tile.add(2))
    }
    player.face(Direction.cardinal[target.rotation])
    delay()
    player.anim("${hatchet.id}_shape_canoe")
    delay()
    target.anim("canoe_fall")
    player.clearAnim()
    player.playSound("tree_fall")
    player["canoe_state_${location}"] = "falling"
    delay()
    player["canoe_state_${location}"] = "fallen"
}

interfaceOpen("canoe") { player ->
    val dugout = player.levels.get(Skill.Woodcutting) > 26
    player.interfaces.sendVisibility(id, "visible_dugout", dugout)
    player.interfaces.sendVisibility(id, "invisible_dugout", !dugout)

    val stable = player.levels.get(Skill.Woodcutting) > 41
    player.interfaces.sendVisibility(id, "visible_stable_dugout", stable)
    player.interfaces.sendVisibility(id, "invisible_stable_dugout", !stable)

    val waka = player.levels.get(Skill.Woodcutting) > 56
    player.interfaces.sendVisibility(id, "visible_waka", waka)
    player.interfaces.sendVisibility(id, "invisible_waka", !waka)
}

interfaceOption("Select", "a_*", "canoe") {
    val type = component.removePrefix("a_")
    (player.dialogueSuspension as? StringSuspension)?.resume(type)
}

objectOperate("Shape-canoe", "canoe_station_fallen") {
    val hatchet = Hatchet.best(player)
    if (hatchet == null) {
        player.message("You need a hatchet to shape a canoe.")
        player.message("You do not have a hatchet which you have the woodcutting level to use.")
        return@objectOperate
    }
    when (target.rotation) {
        1 -> player.walkToDelay(target.tile.add(-1, 2))
        2 -> player.walkToDelay(target.tile.add(2, 2))
        3 -> player.walkToDelay(target.tile.add(2, 2))
    }
    arriveDelay()
    val location = target.id.removePrefix("canoe_station_")
    player.face(Direction.cardinal[target.rotation])
    player.open("canoe")
    val canoe = StringSuspension.get(player)
    player.closeMenu()
    val level = player.levels.get(Skill.Woodcutting)
    val chance: IntRange = hatchet.def.getOrNull<String>("canoe_chance")?.toIntRange() ?: return@objectOperate
    var count = 0
    while (count++ < 50) {
        player.anim("${hatchet.id}_shape_canoe")
        delay(3) // TODO can it be cancelled?
        if (Level.success(level, chance)) {
            break
        }
    }
    player["canoe_state_${location}"] = canoe
    player.clearAnim()
    player.exp(
        Skill.Woodcutting, when (canoe) {
            "log" -> 30.0
            "dugout" -> 60.0
            "stable_dugout" -> 90.0
            "waka" -> 150.0
            else -> 0.0
        }
    )
}

objectOperate("Float Log", "canoe_station_log") {
    float()
}

objectOperate("Float Canoe", "canoe_station_*") {
    float()
}

suspend fun ObjectOption<Player>.float() {
    when (target.rotation) {
        1 -> player.walkToDelay(target.tile.add(-1, 2))
        2 -> player.walkToDelay(target.tile.add(2, 2))
        3 -> player.walkToDelay(target.tile.add(2, 2))
    }
    val location = target.id.removePrefix("canoe_station_")
    val canoe = def.stringId.removePrefix("canoe_station_")
    player["canoe_state_${location}"] = "float_$canoe"
    player.anim("canoe_push")
    player.face(Direction.cardinal[target.rotation])
    target.anim("canoe_fall")
    player.playSound("canoe_roll")
    delay(2)
    player["canoe_state_${location}"] = "water_$canoe"
}

// TODO config file
val destinations = mapOf(
    "lumbridge" to Tile(3231, 3250),
    "champions_guild" to Tile(3199, 3344),
    "barbarian_village" to Tile(3109, 3415),
    "edgeville" to Tile(3129, 3501),
    "wilderness_pond" to Tile(3142, 3796),
)

val names = mapOf(
    "lumbridge" to "Lumbridge",
    "champions_guild" to "the Champions' Guild",
    "barbarian_village" to "the Barbarian's Village",
    "edgeville" to "Edgeville",
    "wilderness_pond" to "the Wilderness",
)

val sink = mapOf(
    "lumbridge" to Tile(3235, 3248),
    "champions_guild" to Tile(3197, 3341),
    "barbarian_village" to Tile(3107, 3414),
    "edgeville" to Tile(3129, 3505),
    "wilderness_pond" to Tile(3142, 3795),
)

objectOperate("Paddle Canoe", "canoe_station_water_*") {
    player.face(Direction.cardinal[target.rotation])
    val station = target.id.removePrefix("canoe_station_")
    val canoe = def.stringId.removePrefix("canoe_station_water_")
    val destination = canoeStationMap(canoe, station)
    if (destination == null || destination == station) {
        return@objectOperate
    }
    if (destination == "wilderness_pond" && player["wilderness_canoe_warning", true]) {
        statement("<red>Warning</col> This canoe will take you deep into the <red>Wilderness</col>. There are no trees suitable to make a canoe there. You will have to walk back.")
        choice("Are you sure you wish to travel") {
            option("Yes, I'm brave.")
            option("Eeep! The Wilderness... No thank you.") {
                return@option
            }
            option("Yes, and don't show this warning again.") {
                player["wilderness_canoe_warning"] = false
            }
        }
    }
    canoeTravel(canoe, station, destination)
    player.tele(destinations[destination]!!)
    player["canoe_state_${station}"] = "tree"
    player["canoe_state_${destination}"] = "tree"
    objects.add("a_sinking_canoe_${canoe}", tile = sink[destination]!!, rotation = 1, ticks = 3)
    player.playSound("canoe_sink")
    if (destination == "wilderness_pond") {
        player.message("You arrive in the Wilderness. There are no trees suitable to make a canoe.")
        player.message("Your canoe sinks into the water after the hard journey. Looks like you're walking back.")
    } else {
        player.message("You arrive at ${names[destination]}.<br>Your canoe sinks into the water after the hard journey.", type = ChatType.Filter)
    }
}
package content.skill.prayer.active

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.getActivePrayerVarKey
import content.entity.sound.sound

val definitions: PrayerDefinitions by inject()
val variableDefinitions: VariableDefinitions by inject()

timerStart("prayer_drain") {
    interval = 1
}

timerTick("prayer_drain") { player ->
    val equipmentBonus = player["prayer", 0]
    var prayerDrainCounter = player["prayer_drain_counter", 0]

    prayerDrainCounter += getTotalDrainEffect(player)
    val prayerDrainResistance = 60 + (equipmentBonus * 2)
    while (prayerDrainCounter > prayerDrainResistance) {
        player.levels.drain(Skill.Prayer, 1)
        prayerDrainCounter -= prayerDrainResistance
        if (player.levels.get(Skill.Prayer) == 0) {
            player.sound("prayer_drain")
            player.message("You have run out of Prayer points; you can recharge at an altar.")
            cancel()
            break
        }
    }
    player["prayer_drain_counter"] = prayerDrainCounter
}

timerStop("prayer_drain") { player ->
    player.clear(player.getActivePrayerVarKey())
    player[PrayerConfigs.USING_QUICK_PRAYERS] = false
}

fun getTotalDrainEffect(player: Player): Int {
    val listKey = player.getActivePrayerVarKey()
    val variable = variableDefinitions.get(listKey)
    val values = (variable?.values as BitwiseValues).values
    var total = 0
    for (prayer in values) {
        if (player.containsVarbit(listKey, prayer)) {
            val definition = definitions.get(prayer as String)
            total += definition.drain
        }
    }
    return total
}
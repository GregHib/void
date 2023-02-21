package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.interact.entity.sound.playSound

on<TimerStart>({ timer == "prayer_drain" }) { _: Player ->
    interval = 1
}

on<TimerTick>({ timer == "prayer_drain" }) { player: Player ->
    val equipmentBonus = player["prayer", 0]
    var prayerDrainCounter = player["prayer_drain_counter", 0]

    prayerDrainCounter += getTotalDrainEffect(player)
    val prayerDrainResistance = 60 + (equipmentBonus * 2)
    while (prayerDrainCounter > prayerDrainResistance) {
        player.levels.drain(Skill.Prayer, 1)
        prayerDrainCounter -= prayerDrainResistance
        if (player.levels.get(Skill.Prayer) == 0) {
            player.playSound("prayer_drain")
            player.message("You have run out of Prayer points; you can recharge at an altar.")
            player.clearVar(player.getActivePrayerVarKey())
            player.setVar(PrayerConfigs.USING_QUICK_PRAYERS, false)
            cancel()
            break
        }
    }
    player["prayer_drain_counter", true] = prayerDrainCounter
}

val prayerDrainEffects = mapOf(
    "rapid_restore" to 1,
    "protect_item" to 2,
    "rapid_heal" to 2,
    "burst_of_strength" to 3,
    "clarity_of_thought" to 3,
    "mystic_will" to 3,
    "retribution" to 3,
    "sharp_eye" to 3,
    "thick_skin" to 3,
    "hawk_eye" to 6,
    "improved_reflexes" to 6,
    "mystic_lore" to 6,
    "redemption" to 6,
    "rock_skin" to 6,
    "superhuman_strength" to 6,
    "eagle_eye" to 12,
    "mystic_might" to 12,
    "protect_from_magic" to 12,
    "protect_from_melee" to 12,
    "protect_from_missiles" to 12,
    "protect_from_summoning" to 12,
    "steel_skin" to 12,
    "ultimate_strength" to 12,
    "incredible_reflexes" to 12,
    "rapid_renewal" to 14,
    "rigour" to 16,
    "chivalry" to 18,
    "smite" to 18,
    "augury" to 18,
    "piety" to 20
)

val curseDrainEffects = mapOf(
    "berserker" to 2,
    "protect_item" to 2,
    "wrath" to 3,
    "leech_attack" to 10,
    "leech_defence" to 10,
    "leech_energy" to 10,
    "leech_magic" to 10,
    "leech_ranged" to 10,
    "leech_special_attack" to 10,
    "leech_strength" to 10,
    "deflect_magic" to 12,
    "deflect_melee" to 12,
    "deflect_missiles" to 12,
    "deflect_summoning" to 12,
    "sap_mage" to 14,
    "sap_ranger" to 14,
    "sap_spirit" to 14,
    "sap_warrior" to 14,
    "soul_split" to 16,
    "turmoil" to 16
)

fun getTotalDrainEffect(player: Player): Int {
    val effects = if (player.isCurses()) curseDrainEffects else prayerDrainEffects
    val listKey = player.getActivePrayerVarKey()
    var total = 0
    for ((effect, drain) in effects) {
        if (player.hasVar(listKey, effect)) {
            total += drain
        }
    }
    return total
}
package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on


fun set(effect: String, bonus: String, value: Int) {
    on<EffectStart>({ this.effect == effect }) { player: Player ->
        player.setVar(bonus, player.getVar(bonus, 0) + value, refresh = false)
    }
    on<EffectStop>({ this.effect == effect }) { player: Player ->
        player.setVar(bonus, player.getVar(bonus, 0) - value, refresh = false)
    }
}

set("prayer_clarity_of_thought", "attack_bonus", 5)
set("prayer_improved_reflexes", "attack_bonus", 10)
set("prayer_incredible_reflexes", "attack_bonus", 15)
set("prayer_chivalry", "attack_bonus", 15)
set("prayer_chivalry", "strength_bonus", 18)
set("prayer_chivalry", "defence_bonus", 20)
set("prayer_piety", "attack_bonus", 20)
set("prayer_piety", "strength_bonus", 23)
set("prayer_piety", "defence_bonus", 25)
set("prayer_sharp_eye", "range_bonus", 5)
set("prayer_hawk_eye", "range_bonus", 10)
set("prayer_eagle_eye", "range_bonus", 15)
set("prayer_rigour", "range_bonus", 23)
set("prayer_rigour", "defence_bonus", 25)
set("prayer_mystic_will", "magic_bonus", 5)
set("prayer_mystic_lore", "magic_bonus", 10)
set("prayer_mystic_might", "magic_bonus", 15)
set("prayer_augury", "magic_bonus", 25)
set("prayer_augury", "defence_bonus", 25)
set("prayer_thick_skin", "defence_bonus", 5)
set("prayer_rock_skin", "defence_bonus", 10)
set("prayer_steel_skin", "defence_bonus", 15)
set("prayer_burst_of_strength", "strength_bonus", 5)
set("prayer_superhuman_strength", "strength_bonus", 10)
set("prayer_ultimate_strength", "strength_bonus", 15)
set("prayer_leech_attack", "attack_bonus", 5)
set("prayer_leech_ranged", "range_bonus", 5)
set("prayer_leech_magic", "magic_bonus", 5)
set("prayer_leech_defence", "defence_bonus", 5)
set("prayer_leech_strength", "strength_bonus", 5)
set("prayer_turmoil", "attack_bonus", 15)
set("prayer_turmoil", "strength_bonus", 23)
set("prayer_turmoil", "defence_bonus", 15)

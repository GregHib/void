package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.getPrayerBonus
import world.gregs.voidps.world.interact.entity.combat.HitEffectiveLevelOverride
import kotlin.math.floor

on<HitEffectiveLevelOverride>({ type == "spell" && defence && target is NPC }, priority = Priority.HIGH) { _: Character ->
    level = (target as NPC).levels.get(Skill.Magic)
}

on<HitEffectiveLevelOverride>({ type == "spell" && defence && target is Player }, priority = Priority.LOW) { _: Character ->
    target as Player
    var level = floor(target.levels.get(Skill.Magic) * target.getPrayerBonus(Skill.Magic))
    level = floor(level * 0.7)
    this.level = (floor(this.level * 0.3) + level).toInt()
}

on<InterfaceOption>({ name.endsWith("_spellbook") && component == "defensive_cast" && option == "Defensive Casting" }) { player: Player ->
    player.toggleVar(component)
}

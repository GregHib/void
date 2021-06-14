import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.BooleanVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.getPrayerBonus
import world.gregs.voidps.world.interact.entity.combat.HitChanceLevelOverride
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.spell
import kotlin.math.floor

fun isArenaSpell(spell: String): Boolean = spell == "saradomin_strike" || spell == "claws_of_guthix" || spell == "flames_of_zamorak"

on<HitDamageModifier>(
    { player -> skill == Skill.Magic && player.hasEffect("charge") && isArenaSpell(player.spell) },
    priority = Priority.HIGHEST
) { _: Player ->
    damage += 100.0
}

on<HitChanceLevelOverride>({ skill == Skill.Magic && defence && target is NPC }, priority = Priority.HIGH) { _: Player ->
    level = (target as NPC).levels.get(skill)
}

on<HitChanceLevelOverride>({ skill == Skill.Magic && defence && target is NPC }, priority = Priority.HIGH) { _: NPC ->
    level = (target as NPC).levels.get(skill)
}

on<HitChanceLevelOverride>({ skill == Skill.Magic && defence && target is Player }, priority = Priority.LOW) { _: Player ->
    this.level = getPlayerMagicDefence(level, target as Player)
}

on<HitChanceLevelOverride>({ skill == Skill.Magic && defence && target is Player }, priority = Priority.LOW) { _: NPC ->
    this.level = getPlayerMagicDefence(level, target as Player)
}

fun getPlayerMagicDefence(defenceLevel: Int, target: Character): Int {
    var level = floor(target.levels.get(Skill.Magic) * target.getPrayerBonus("magic_bonus"))
    level = floor(level * 0.7)
    return (floor(defenceLevel * 0.3) + level).toInt()
}

on<InterfaceOption>({ name.endsWith("_spellbook") && component == "defensive_cast" && option == "Defensive Casting" }) { player: Player ->
    player.toggleVar(component)
}

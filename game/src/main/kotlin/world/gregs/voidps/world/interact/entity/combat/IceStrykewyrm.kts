import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.spell
import kotlin.math.floor

on<HitDamageModifier>({ skill == Skill.Magic && target is NPC && target.name == "ice_strykewyrm" }, priority = Priority.LOWER) { player: Player ->
    val fireCape = player.equipped(EquipSlot.Cape).name == "fire_cape"
    if (fireCape) {
        damage += 40
    }
    if (player.spell.startsWith("fire_")) {
        damage = floor(damage * if (fireCape) 2.0 else 1.5)
    }
}
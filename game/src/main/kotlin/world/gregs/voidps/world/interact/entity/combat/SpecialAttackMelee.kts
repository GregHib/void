import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitChanceModifier
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.specialAttack
import kotlin.math.floor

on<HitChanceModifier>({ player -> offense && skill == Skill.Attack && player.specialAttack && player.equipped(EquipSlot.Weapon).def.has("spec_acc_multiplier") }, priority = Priority.HIGH) { player: Player ->
    val multiplier = player.equipped(EquipSlot.Weapon).def["spec_acc_multiplier", 1.0]
    chance = floor(chance * multiplier)
}

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.specialAttack && player.equipped(EquipSlot.Weapon).def.has("spec_dmg_multiplier") }, Priority.HIGH) { player: Player ->
    val multiplier = player.equipped(EquipSlot.Weapon).def["spec_dmg_multiplier", 1.0]
    damage = floor(damage * multiplier)
}

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.specialAttack && player.equipped(EquipSlot.Weapon).name == "armadyl_godsword" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.25)
}

on<HitDamageModifier>({ player -> skill == Skill.Strength && player.specialAttack && player.equipped(EquipSlot.Weapon).name == "bandos_godsword" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.1)
}
package content.skill.melee

import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class Block(
    val combatDefinitions: CombatDefinitions,
) : Script {

    val styleDefinitions: WeaponStyleDefinitions by inject()
    val weaponDefinitions: WeaponAnimationDefinitions by inject()
    val animationDefinitions: AnimationDefinitions by inject()

    init {
        combatAttack(handler = ::attack)
        npcCombatAttack(handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val target = attack.target
        val delay = attack.delay
        if (target is Player) {
            source.sound(calculateHitSound(target), delay)
            target.sound(calculateHitSound(target), delay)
            val shield = target.equipped(EquipSlot.Shield).id
            if (shield.endsWith("shield")) {
                target.anim("shield_block", delay)
            } else if (shield.endsWith("defender")) {
                target.anim("defender_block", delay)
            } else if (shield.endsWith("book")) {
                target.anim("book_block", delay)
            } else {
                val type: String? = target.weapon.def.getOrNull("weapon_type")
                val definition = if (type != null) weaponDefinitions.get(type) else null
                var animation = definition?.attackTypes?.get("defend")
                if (animation == null) {
                    val id = target.weapon.def["weapon_style", -1]
                    val style = styleDefinitions.get(id)
                    animation = if (id != -1 && animationDefinitions.contains("${style.stringId}_defend")) "${style.stringId}_defend" else "human_defend"
                }
                target.anim(animation, delay)
            }
        } else if (target is NPC) {
            val id = if (source is Player) target.def(source).stringId else target.id
            val definition = combatDefinitions.get(target.def["combat_def", id])
            target.anim(definition.defendAnim, delay)
            source.sound(definition.defendSound?.id ?: return, delay)
        }
    }

    fun calculateHitSound(target: Character): String {
        if (target is Player) {
            return if (target.male) {
                "male_defend_${random.nextInt(0, 3)}"
            } else {
                "female_defend_${random.nextInt(0, 1)}"
            }
        }
        return "human_defend"
    }
}

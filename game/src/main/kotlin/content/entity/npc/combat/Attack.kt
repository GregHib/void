package content.entity.npc.combat

import content.entity.combat.hit.hit
import content.skill.slayer.categories
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.inject

class Attack : Script {

    val definitions: WeaponStyleDefinitions by inject()
    val animationDefinitions: AnimationDefinitions by inject()
    val soundDefinitions: SoundDefinitions by inject()

    init {
        npcCombatSwing { target ->
            if (tile.distanceTo(target) > def["attack_radius", 8]) {
                mode = Retreat(this, target)
                return@npcCombatSwing
            }
            anim(attackAnimation(this))
            (target as? Player)?.sound(NPCAttack.sound(soundDefinitions, this, "attack"))
            hit(target)
        }
    }

    fun attackAnimation(npc: NPC): String {
        if (npc.categories.contains("human") && npc.def.contains("weapon_style")) {
            val id = npc.def["weapon_style", "unarmed"]
            val styleDefinition = definitions.get(id)
            val styleName: String? = npc.def.getOrNull("style")
            var style = styleName?.let { styleDefinition.combatStyles.indexOf(it.removePrefix("typeless_")) } ?: -1
            if (style == -1) {
                style = 0
            }

            val animation = "${styleDefinition.stringId}_${styleDefinition.attackTypes[style]}"
            if (animationDefinitions.contains(animation)) {
                return animation
            }
        }
        return NPCAttack.anim(animationDefinitions, npc, "attack")
    }
}

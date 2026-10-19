package content.area.misthalin.varrock

import content.skill.melee.weapon.attackType
import content.skill.melee.weapon.fightStyle
import content.skill.melee.weapon.weapon
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.WeaponAnimationDefinitions
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.random

class VarrockDummy(
    private val weaponAnimations: WeaponAnimationDefinitions,
    private val weaponStyles: WeaponStyleDefinitions,
) : Script {

    init {
        npcSpawn("trainee_guard") {
            if (dummy() == null) {
                return@npcSpawn
            }
            softTimers.start("trainee_guard_dummy_attack")
        }

        npcTimerStart("trainee_guard_dummy_attack") {
            random.nextInt(7, 11)
        }

        npcTimerTick("trainee_guard_dummy_attack") {
            val dummy = dummy() ?: return@npcTimerTick random.nextInt(7, 11)
            anim("varrock_guard_attack")
            dummy.anim("dummy_hit")
            random.nextInt(7, 11)
        }

        objectOperate("Attack") { (target) ->
            if (target.intId == 23921) {
                if (fightStyle == "range") {
                    message("You can't use ranged attacks on the dummy.")
                    return@objectOperate
                }
                val canLearn = levels.getMax(Skill.Attack) < 8
                anim(attackAnimation())
                target.anim("dummy_hit")
                message("You hit the dummy.")
                if (canLearn) {
                    exp(Skill.Attack, 5.0)
                } else {
                    delay(1)
                    message("There is nothing more you can learn from hitting a dummy.")
                    return@objectOperate
                }
                delay(1)
            }
        }
    }

    private fun Player.attackAnimation(): String {
        val style = weaponStyles.get(weapon.def["weapon_style", 0])
        val type = weapon.def.getOrNull("weapon_type") ?: style.stringId
        val attack = if (fightStyle == "magic" || fightStyle == "blaze") {
            style.attackTypes.firstOrNull() ?: attackType
        } else {
            attackType
        }
        val definition = weaponAnimations.getOrNull(type)
        val animation = definition?.attackTypes?.getOrDefault(Params.id(attack), definition.attackTypes[Params.DEFAULT])
        return animation ?: "${style.stringId}_$attack"
    }

    private fun NPC.dummy(): GameObject? = GameObjects.findOrNull(tile.add(direction), 23921)
}

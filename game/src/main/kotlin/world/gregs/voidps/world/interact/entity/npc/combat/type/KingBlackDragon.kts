package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.player.toxin.poison
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound


val specials = listOf("toxic", "ice", "shock")

on<CombatSwing>({ npc -> !swung() && npc.id == "king_black_dragon" }, Priority.HIGHEST) { npc: NPC ->
    val canMelee = CharacterTargetStrategy(npc).reached(target)
    println("Can melee: $canMelee")
    when (random.nextInt(if (canMelee) 3 else 2)) {
        0 -> {
            npc.setAnimation("dragon_breath")
            target.playSound("dragon_breath")
            npc.hit(target, type = "dragonfire")
        }
        1 -> {
            val type = specials.random()
            npc.setAnimation("dragon_breath_$type")
            target.playSound("dragon_breath_$type")
            npc.shoot("dragon_breath_$type", target)
            npc.hit(target, type = "dragonfire", spell = type, special = true)
        }
        else -> {
            npc.setAnimation("dragon_attack_${random.nextInt(3)}")
            target.playSound("dragon_attack")
            npc.hit(target, type = "melee")
        }
    }
    delay = npc.def["attack_speed", 4]
}

on<CombatHit>({ source is NPC && source.id == "king_black_dragon" }) { player: Player ->
    when (spell) {
        "toxic" -> source.poison(player, 80)
        "ice" -> source.freeze(player, 10)
        "shock" -> {
            player.message("You're shocked and weakened!")
            for (skill in Skill.all) {
                player.levels.drain(skill, 2)
            }
        }
    }
}
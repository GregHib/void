package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.fightStyle

on<NPCOption>({ approach && npc.id == "magic_dummy" && option == "Attack" && it.fightStyle != "magic" }, Priority.HIGH) { player: Player ->
    player.message("You can only use Magic against this dummy.")
    cancel()
}

on<HitDamageModifier>({ target is NPC && target.id == "magic_dummy" }, Priority.LOWEST) { _: Player ->
    target as NPC
    damage = damage.coerceAtMost(target.levels.get(Skill.Constitution) - 1.0)
}

on<CurrentLevelChanged>({ it.id == "magic_dummy" && skill == Skill.Constitution && to <= 10 }, Priority.HIGH) { npc: NPC ->
    npc.levels.clear()
    npc.attackers.forEach {
        it.queue.clearWeak()
    }
}

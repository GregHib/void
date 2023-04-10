package world.gregs.voidps.world.map.lumbridge.combat_hall

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.fightStyle

for (type in listOf("magic", "melee")) {
    on<NPCOption>({ approach && npc.id == "${type}_dummy" && option == "Attack" && it.fightStyle != type }, Priority.HIGH) { player: Player ->
        player.message("You can only use ${type.toTitleCase()} against this dummy.")
        player.approachRange(10, false)
        player.mode = EmptyMode
        cancel()
    }

    on<InterfaceOnNPC>({ approach && npc.id == "${type}_dummy" && it.fightStyle != type }) { player: Player ->
        player.message("You can only use ${type.toTitleCase()} against this dummy.")
        player.approachRange(10, false)
        player.mode = EmptyMode
        cancel()
    }

    on<CombatSwing>({ target is NPC && target.id == "${type}_dummy" && it.fightStyle != type }, Priority.HIGHER) { player: Player ->
        player.message("You can only use ${type.toTitleCase()} against this dummy.")
        player.mode = EmptyMode
        delay = -1
    }

    on<HitDamageModifier>({ target is NPC && target.id == "${type}_dummy" }, Priority.LOWEST) { _: Player ->
        target as NPC
        damage = damage.coerceAtMost(target.levels.get(Skill.Constitution) - 1.0)
    }

    on<CurrentLevelChanged>({ it.id == "${type}_dummy" && skill == Skill.Constitution && to <= 10 }, Priority.HIGH) { npc: NPC ->
        npc.levels.clear()
        npc.attackers.forEach {
            it.mode = EmptyMode
        }
    }
}

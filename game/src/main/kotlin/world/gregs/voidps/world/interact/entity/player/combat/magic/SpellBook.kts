import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNpcClick
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remove
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

on<InterfaceOnNpcClick>({ id.endsWith("_spellbook") && canAttack(it, npc) }) { player: Player ->
    cancel()
    if (player.hasClock("in_combat") && player.target == npc) {
        player.spell = component
        player.attackRange = 8
        player["attack_speed"] = 5
    } else {
        player.closeDialogue()
        player["queued_spell"] = component
        player.attackRange = 8
        player.mode = CombatMovement(player, npc)
    }
}

on<CombatSwing>({ it.contains("queued_spell") }, Priority.HIGHEST) { player: Player ->
    player.spell = player.remove("queued_spell") ?: return@on
    player["attack_speed"] = 5
}
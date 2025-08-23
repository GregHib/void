package content.area.asgarnia.falador

import content.entity.player.combat.special.SpecialAttackPrepare
import content.skill.magic.spell.spell
import content.skill.melee.weapon.attackRange
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnItem
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.handle.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile

private val acceptedTiles = listOf(
    Tile(3005, 3376, 0),
    Tile(2999, 3375, 0),
    Tile(2996, 3377, 0),
    Tile(2989, 3378, 0),
)

@Option("Climb", "giant_mole_lair_escape_rope")
fun ObjectOption<Player>.exitMoleLair() {
    player.anim("climb_up")
    player.tele(acceptedTiles.random())
}

@Option(option = "Examine")
fun ObjectOption<Player>.examineObject() {
    player.message(def.getOrNull("examine") ?: return, ChatType.ObjectExamine)
}

@UseOn(use = ["modern_spellbook:superheat_item"], on = ["*"])
fun InterfaceOnItem.superHeat() {

}

@Spawn("giant_mole")
fun moleSpawn(mole: NPC) {
    println("Holey moley!")
}

@Handle("special_attack_prepare", "brine_sabre")
fun SpecialAttackPrepare.brineSabre() {

}

@VarSet("in_multi_combat", toBool = true)
fun VariableSet.enterMulti(player: Player) {
    player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
}

@UseOn(use = ["fishbowl_water"], on = ["seaweed"])
@UseOn(use = ["seaweed"], on = ["fishbowl_water"])
fun ItemOnItem.makeFishbowl(player: Player) {
    player.inventory.transaction {
        remove(fromItem.id)
        remove(toItem.id)
        add("fishbowl_seaweed")
    }
    when (player.inventory.transaction.error) {
        TransactionError.None -> player.message("You carefully place the seaweed into the fishbowl.")
        else -> {}
    }
}

@UseOn(on = ["*_spellbook"])
suspend fun InterfaceOnNPC.examineObject() {
    if (!player.has(Skill.Slayer, target.def["slayer_level", 0])) {
        player.message("You need a higher slayer level to know how to wound this monster.")
        cancel()
        return
    }
    approachRange(8, update = false)
    player.spell = component
    if (target.id.endsWith("_dummy")/* && !handleCombatDummies()*/) {
        player.clear("spell")
        return
    }
    player["attack_speed"] = 5
    player["one_time"] = true
    player.attackRange = 8
    player.face(target)
//    combatInteraction(player, target)
    cancel()
}
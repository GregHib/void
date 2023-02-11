package world.gregs.voidps.world.interact.entity.death

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.DropGreen
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.world.community.clan.clan
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.damageDealers
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.item.tradeable
import world.gregs.voidps.world.interact.entity.sound.playSound

val npcs: NPCs by inject()
val floorItems: FloorItems by inject()
val tables: DropTables by inject()

on<Registered> { character: Character ->
    character.damageDealers = mutableMapOf()
    character.attackers = ObjectArrayList()
}

on<Death> { npc: NPC ->
    npc.start("dead")
    npc.strongQueue {
        val dealer = npc.damageDealers.maxByOrNull { it.value }
        val killer = dealer?.key
        val tile = npc.tile
        npc["death_tile"] = tile
        npc.setAnimation(deathAnimation(npc))
        val name = npc.def.name.toSnakeCase()
        (killer as? Player)?.playSound("${name}_death", delay = 40)
        pause(4)
        dropLoot(npc, killer, name, tile)
        npc.attackers.clear()
        npc.stopAllEffects()
        npcs.remove(npc)
        npcs.removeIndex(npc)
        val respawn = npc.getOrNull<Tile>("respawn_tile")
        if (respawn != null) {
            pause(npc["respawn_delay", 60])
            npc.damageDealers.clear()
            npc.levels.clear()
            npc.tele(respawn)
            npc.face(npc["respawn_direction", Direction.NORTH], update = false)
            npcs.add(npc)
            npc.stop("dead")
        } else {
            npcs.releaseIndex(npc)
            npc.events.emit(Unregistered)
        }
    }
}

fun deathAnimation(npc: NPC): String {
    val race: String? = npc.def.getOrNull("race")
    if (race != null) {
        return "${race}_death"
    }
    return npc.def["death_anim"]
}

fun dropLoot(npc: NPC, killer: Character?, name: String, tile: Tile) {
    var table = tables.get("${name}_drop_table")
    if (table == null) {
        table = tables.get("${npc.def["race", ""]}_drop_table")
        if (table == null) {
            return
        }
    }
    val combatLevel = if (killer is Player) killer.combatLevel else if (killer is NPC) killer.def.combat else -1
    val drops = table.role(maximumRoll = if (combatLevel > 0) combatLevel * 10 else -1)
        .filterNot { it.id == "nothing" }
        .reversed()
        .map { it.toItem() }
    if (npc.inMultiCombat && killer is Player && killer.getVar("loot_share", false)) {
        shareLoot(killer, npc, tile, drops)
    } else {
        drops.forEach { item ->
            if (!item.id.contains("clue_scroll") && item.amount > 0) {
                floorItems.add(item.id, item.amount, tile, revealTicks = if (item.tradeable) 60 else -1, disappearTicks = 120, owner = if (killer is Player) killer else null)
            }
        }
    }
}

var Player.lootSharePotential: Int
    get() = get("loot_share_potential", 0)
    set(value) = set("loot_share_potential", true, value)

fun shareLoot(killer: Player, npc: NPC, tile: Tile, drops: List<Item>) {
    val clan = killer.clan ?: return
    val members = npc.damageDealers.keys
        .filterIsInstance<Player>()
        .filter { it.tile.within(tile, 16) && clan.members.contains(it) && it.getVar("loot_share", false) }
    drops.forEach { item ->
        if (item.amount <= 0) {
            return@forEach
        }
        if (clan.coinShare && item.def.cost > 100000) {
            shareCoin(item, members, tile)
        } else {
            val awardee = getAwardee(item, killer, members)
            notify(members, awardee, item)
            floorItems.add(item.id, item.amount, tile, revealTicks = if (item.tradeable) 60 else -1, disappearTicks = 120, owner = awardee)
            awardee.message(DropGreen { "You received: ${item.amount} ${item.def.name.plural(item.amount)}." }, ChatType.ClanChat)
        }
    }
}

fun shareCoin(item: Item, members: List<Player>, tile: Tile) {
    val total = item.def.cost * item.amount
    val split = total / members.size
    for (member in members) {
        floorItems.add("coins", split, tile, revealTicks = 60, disappearTicks = 120, owner = member)
        member.message(DropGreen { "You received $split gold as your split of this drop: ${item.amount} x ${item.def.name}." }, ChatType.ClanChat)
    }
}

fun getAwardee(item: Item, killer: Player, members: List<Player>) =
    if (item.tradeable) weightedSample(members.map { member -> member to member.lootSharePotential }) ?: killer else killer

fun notify(members: List<Player>, awardee: Player, item: Item) {
    for (member in members) {
        if (member == awardee) {
            continue
        }
        member.message("${awardee.name} received: ${item.amount} ${item.def.name.plural(item.amount)}.", ChatType.ClanChat)
        member.lootSharePotential += item.def.cost * item.amount
        member.message("Your chance of receiving loot has improved.", ChatType.Filter)
    }
}
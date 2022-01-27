import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.engine.utility.weightedSample
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
    character.attackers = mutableListOf()
}

on<Death> { npc: NPC ->
    npc.action(ActionType.Dying) {
        withContext(NonCancellable) {
            val damageDealers: MutableMap<Character, Int> = npc["damage_dealers"]
            val dealer = damageDealers.maxByOrNull { it.value }
            val killer = dealer?.key
            val tile = npc.tile
            npc["death_tile"] = tile
            val name = npc.def.name.toUnderscoreCase()
            npc.setAnimation("${name}_death")
            (killer as? Player)?.playSound("${name}_death", delay = 40)
            delay(4)
            dropLoot(npc, killer, name, tile)
            npc.attackers.clear()
            npc.stopAllEffects()
            npcs.remove(npc)
            val respawn = npc.getOrNull<Tile>("respawn_tile")
            if (respawn != null) {
                delay(npc["respawn_delay", 60])
                damageDealers.clear()
                npc.levels.clear()
                npc.move(respawn)
                npc.turn(npc["respawn_direction", Direction.NORTH], update = false)
                npcs.add(npc)
            } else {
                npc.events.emit(Unregistered)
                await(Suspension.Infinite)
            }
        }
    }
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
            awardee.message(Colour.ChatColour.DropGreen { "You received: ${item.amount} ${item.def.name.plural(item.amount)}." }, ChatType.ClanChat)
        }
    }
}

fun shareCoin(item: Item, members: List<Player>, tile: Tile) {
    val total = item.def.cost * item.amount
    val split = total / members.size
    for (member in members) {
        floorItems.add("coins", split, tile, revealTicks = 60, disappearTicks = 120, owner = member)
        member.message(Colour.ChatColour.DropGreen { "You received $split gold as your split of this drop: ${item.amount} x ${item.def.name}." }, ChatType.ClanChat)
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
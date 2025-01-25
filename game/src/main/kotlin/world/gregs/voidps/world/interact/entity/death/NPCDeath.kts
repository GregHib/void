package world.gregs.voidps.world.interact.entity.death

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.community.clan.clan
import world.gregs.voidps.world.interact.entity.combat.*
import content.entity.player.inv.item.tradeable
import content.entity.sound.playSound

val npcs: NPCs by inject()
val floorItems: FloorItems by inject()
val tables: DropTables by inject()
val animationDefinitions: AnimationDefinitions by inject()
val soundDefinitions: SoundDefinitions by inject()

npcDeath { npc ->
    npc.mode = PauseMode
    npc.dead = true
    npc.steps.clear()
    npc.strongQueue(name = "death", 1) {
        val killer = npc.killer
        val tile = npc.tile
        npc["death_tile"] = tile
        npc.anim(deathAnimation(npc))
        val name = npc.def.name.toSnakeCase()
        (killer as? Player)?.playSound(deathSound(npc))
        delay(4)
        dropLoot(npc, killer, name, tile)
        npc.attackers.clear()
        npc.softTimers.stopAll()
        npcs.removeIndex(npc)
        val respawn = npc.get<Tile>("respawn_tile")
        if (respawn != null) {
            npc.tele(respawn)
            delay(npc["respawn_delay", 60])
            npc.damageDealers.clear()
            npc.levels.clear()
            npc.face(npc["respawn_direction", Direction.NORTH], update = false)
            npcs.index(npc)
            npc.dead = false
            npc.mode = EmptyMode
            npc.emit(Spawn)
        } else {
            World.queue("remove_npc") {
                npcs.remove(npc)
            }
            npc.emit(Despawn)
        }
    }
}

fun deathAnimation(npc: NPC): String {
    var animation = "${npc.id}_death"
    if (animationDefinitions.contains(animation)) {
        return animation
    }
    if (npc.def.contains("death_anim")) {
        animation = npc.def["death_anim", ""]
        if (animationDefinitions.contains(animation)) {
            return animation
        }
    }
    if (npc.race.isNotEmpty()) {
        animation = "${npc.race}_death"
        if (animationDefinitions.contains(animation)) {
            return animation
        }
    }
    return ""
}


fun deathSound(npc: NPC): String {
    var sound: String
    if (npc.race.isNotEmpty()) {
        sound = "${npc.race}_death"
        if (soundDefinitions.contains(sound)) {
            return sound
        }
    }
    sound = "${npc.id}_death"
    if (soundDefinitions.contains(sound)) {
        return sound
    }
    return ""
}

fun dropLoot(npc: NPC, killer: Character?, name: String, tile: Tile) {
    var table = tables.get("${npc.def["drop_table", name]}_drop_table")
    if (table == null) {
        table = tables.get("${npc.race}_drop_table")
        if (table == null) {
            return
        }
    }
    val combatLevel = if (killer is Player) killer.combatLevel else if (killer is NPC) killer.def.combat else -1
    val drops = table.role(maximumRoll = if (combatLevel > 0) combatLevel * 10 else -1, members = World.members, player = killer as? Player)
        .filterNot { it.id == "nothing" }
        .reversed()
        .map { it.toItem() }
        .filter { World.members || !it.def.members }
        .toMutableList()
    npc.emit(DropItems(killer, drops))
    if (npc.inMultiCombat && killer is Player && killer["loot_share", false]) {
        shareLoot(killer, npc, tile, drops)
    } else {
        drops.forEach { item ->
            if (!item.id.contains("clue_scroll") && item.amount > 0) {
                floorItems.add(tile, item.id, item.amount, charges = item.charges(), revealTicks = if (item.tradeable) 60 else FloorItems.NEVER, disappearTicks = 120, owner = if (killer is Player) killer else null)
            }
        }
    }
}

var Player.lootSharePotential: Int
    get() = get("loot_share_potential", 0)
    set(value) = set("loot_share_potential", value)

fun shareLoot(killer: Player, npc: NPC, tile: Tile, drops: List<Item>) {
    val clan = killer.clan ?: return
    val members = npc.damageDealers.keys
        .filterIsInstance<Player>()
        .filter { it.tile.within(tile, 16) && clan.members.contains(it) && it["loot_share", false] }
    drops.forEach { item ->
        if (item.amount <= 0) {
            return@forEach
        }
        if (clan.coinShare && item.def.cost > 100000) {
            shareCoin(item, members, tile)
        } else {
            val awardee = getAwardee(item, killer, members)
            notify(members, awardee, item)
            floorItems.add(tile, item.id, item.amount, charges = item.charges(), revealTicks = if (item.tradeable) 60 else FloorItems.NEVER, disappearTicks = 120, owner = awardee)
            awardee.message("<dark_green>You received: ${item.amount} ${item.def.name.plural(item.amount)}.", ChatType.ClanChat)
        }
    }
}

fun shareCoin(item: Item, members: List<Player>, tile: Tile) {
    val total = item.def.cost * item.amount
    val split = total / members.size
    for (member in members) {
        floorItems.add(tile, "coins", split, revealTicks = 60, disappearTicks = 120, owner = member)
        member.message("<dark_green>You received $split gold as your split of this drop: ${item.amount} x ${item.def.name}.", ChatType.ClanChat)
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
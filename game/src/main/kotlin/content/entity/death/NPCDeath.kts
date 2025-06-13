package content.entity.death

import com.github.michaelbull.logging.InlineLogger
import content.area.wilderness.inMultiCombat
import content.entity.combat.*
import content.entity.npc.combat.NPCAttack
import content.entity.player.inv.item.tradeable
import content.entity.sound.sound
import content.skill.slayer.*
import content.social.clan.clan
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
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

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
        npc.anim(NPCAttack.anim(animationDefinitions, npc, "death"))
        (killer as? Player)?.sound(NPCAttack.sound(soundDefinitions, npc, "death"))
        delay(4)
        if (killer is Player) {
            slay(killer, npc)
        }
        dropLoot(npc, killer, tile)
        npc.attackers.clear()
        npc.softTimers.stopAll()
        npc.hide = true
        val respawn = npc.get<Tile>("respawn_tile")
        if (respawn != null) {
            npc.tele(respawn)
            delay(npc["respawn_delay", 60])
            npc.damageDealers.clear()
            npc.levels.clear()
            npc.face(npc["respawn_direction", Direction.NORTH], update = false)
            npc.hide = false
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

fun dropLoot(npc: NPC, killer: Character?, tile: Tile) {
    val table = tables.get("${npc.def["drop_table", npc.id]}_drop_table") ?: return
    val combatLevel = if (killer is Player) {
        killer.combatLevel
    } else if (killer is NPC) {
        killer.def.combat
    } else {
        -1
    }
    val drops = table.role(maximumRoll = if (combatLevel > 0) combatLevel * 10 else -1, player = killer as? Player)
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
            floorItems.add(
                tile,
                item.id,
                item.amount,
                charges = item.charges(),
                revealTicks = if (item.tradeable) 60 else FloorItems.NEVER,
                disappearTicks = if (item.tradeable) 120 else 300,
                owner = awardee,
            )
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

fun getAwardee(item: Item, killer: Player, members: List<Player>) = if (item.tradeable) weightedSample(members.map { member -> member to member.lootSharePotential }) ?: killer else killer

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

val logger = InlineLogger()

fun slay(player: Player, npc: NPC) {
    if (player.slayerTask == "nothing" || !npc.categories.contains(player.slayerTask)) {
        return
    }
    val slayerExp = npc.def["slayer_xp", 0.0]
    if (slayerExp == 0.0) {
        logger.warn { "No slayer exp found for slain monster: $npc" }
        return
    }
    player.exp(Skill.Slayer, slayerExp)
    player.slayerTaskRemaining--
    if (player.slayerTaskRemaining == 0) {
        player.slayerStreak++
        var points = when (player.slayerMaster) {
            "mazchna" -> 15
            "vannaka" -> 60
            "chaeldar" -> 150
            "sumona" -> 180
            "duradel", "lapalok" -> 225
            "kuradal" -> 270
            else -> 0
        }
        when {
            player.slayerStreak.rem(50) == 0 -> {}
            player.slayerStreak.rem(10) == 0 -> points /= 3
            else -> points /= 15
        }
        player.slayerPoints += points
        player.inc("slayer_tasks_completed")
        player.clear("slayer_target")
        // TODO dif message for 0 points?
        player.message("You've completed ${player.slayerStreak} tasks in a row and gain $points points. Return to a Slayer Master.")
    } else if (player.slayerTaskRemaining.rem(10) == 0) {
        player.message("You still need to kill ${player.slayerTaskRemaining} monsters to completed your current Slayer assignment.", ChatType.Filter)
    }
}

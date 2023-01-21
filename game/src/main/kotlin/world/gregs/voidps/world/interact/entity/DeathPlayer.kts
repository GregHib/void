import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.contain.*
import world.gregs.voidps.engine.entity.character.event.Death
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.suspend.pause
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.utility.getIntProperty
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.getActivePrayerVarKey
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.inWilderness
import world.gregs.voidps.world.interact.entity.item.tradeable
import world.gregs.voidps.world.interact.entity.player.equip.ItemsKeptOnDeath
import world.gregs.voidps.world.interact.entity.sound.playJingle

val floorItems: FloorItems by inject()
val enums: EnumDefinitions by inject()

on<Registered> { character: Character ->
    character["damage_dealers"] = mutableMapOf<Character, Int>()
    character["attackers"] = mutableListOf<Character>()
    character["hits"] = mutableListOf<CombatHit>()
}

val Character.damageDealers: MutableMap<Character, Int>
    get() = this["damage_dealers"]

val x = getIntProperty("homeX", 0)
val y = getIntProperty("homeY", 0)
val plane = getIntProperty("homePlane", 0)
val respawnTile = Tile(x, y, plane)

on<Death> { player: Player ->
    player.start("dead")
    player.strongQueue {
        val dealer = player.damageDealers.maxByOrNull { it.value }
        val killer = dealer?.key
        player.instructions.resetReplayCache()
        val tile = player.tile.copy()
        val wilderness = player.inWilderness
        player.message("Oh dear, you are dead!")
        player.setAnimation("player_death")
        pause(5)
        player.clearAnimation()
        player.attackers.clear()
        player.damageDealers.clear()
        player.playJingle("death")
        player.stopAllEffects()
        player.clearVar(player.getActivePrayerVarKey())
        player.stopAllEffects()
        dropItems(player, killer, tile, wilderness)
        player.levels.clear()
        player.move(respawnTile)
        player.face(Direction.SOUTH, update = false)
        player.stop("dead")
    }
}

fun dropItems(player: Player, killer: Character?, tile: Tile, inWilderness: Boolean) {
    if (player.isAdmin()) {
        return
    }
    val items = ItemsKeptOnDeath.getAllOrdered(player)
    val kept = ItemsKeptOnDeath.kept(player, items, enums)

    for (item in kept) {
        if (player.inventory.remove(item.id, item.amount) || player.equipment.remove(item.id, item.amount)) {
            continue
        }
    }
    drop(player, Item("bones", 1), tile, inWilderness, killer)
    drop(player, player.inventory, tile, inWilderness, killer)
    drop(player, player.equipment, tile, inWilderness, killer)
    player.inventory.clear()
    player.equipment.clear()

    for (item in kept) {
        player.inventory.add(item.id, item.amount)
    }
}

fun drop(player: Player, container: Container, tile: Tile, inWilderness: Boolean, killer: Character?) {
    for (item in container.items) {
        if (item.isEmpty()) {
            continue
        }
        drop(player, item, tile, inWilderness, killer)
    }
}

fun drop(
    player: Player,
    item: Item,
    tile: Tile,
    inWilderness: Boolean,
    killer: Character?
) {
    if (item.tradeable) {
        floorItems.add(item.id, item.amount, tile, revealTicks = 180, disappearTicks = 240, owner = if (inWilderness && killer is Player) killer else player)
    } else {
        floorItems.add("coins", item.amount * item.def.cost, tile, revealTicks = 180, disappearTicks = 240, owner = if (inWilderness && killer is Player) killer else player)
    }
}
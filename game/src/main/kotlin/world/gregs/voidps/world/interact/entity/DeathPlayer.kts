import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.clear
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.event.Death
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.utility.getIntProperty
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.activity.combat.prayer.getActivePrayerVarKey
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.sound.playJingle

val floorItems: FloorItems by inject()

on<Registered> { character: Character ->
    character["damage_dealers"] = mutableMapOf<Character, Int>()
    character["attackers"] = mutableListOf<Character>()
}

val Character.damageDealers: MutableMap<Character, Int>
    get() = this["damage_dealers"]

val x = getIntProperty("homeX", 0)
val y = getIntProperty("homeY", 0)
val plane = getIntProperty("homePlane", 0)
val respawnTile = Tile(x, y, plane)

on<Death> { player: Player ->
    player.start("dead")
    player.action(ActionType.Dying) {
        withContext(NonCancellable) {
            player.instructions.resetReplayCache()
            val tile = player.tile
            player.message("Oh dear, you are dead!")
            player.setAnimation("player_death")
            delay(5)
            player.clearAnimation()
            player.attackers.clear()
            player.damageDealers.clear()
            player.playJingle("death")
            player.clearVar(player.getActivePrayerVarKey())
            player.stopAllEffects()
            if (!player.isAdmin()) {
                dropAll(player, player.equipment, tile)
                dropAll(player, player.inventory, tile)
            }
            player.levels.clear()
            player.move(respawnTile)
            player.face(Direction.SOUTH, update = false)
            player.stop("dead")
        }
    }
}

fun dropAll(player: Player, container: Container, tile: Tile) {
    for (slot in container.indices) {
        val item = container[slot]
        if (item.isNotEmpty()) {
            floorItems.add(item.id, item.amount, tile, revealTicks = 180, disappearTicks = 240, owner = player)
        }
    }
    container.clear()
}
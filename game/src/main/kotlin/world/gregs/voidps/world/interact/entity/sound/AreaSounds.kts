import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.entity.sound.Sounds
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.network.encode.areaMIDI
import world.gregs.voidps.network.encode.areaSound
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.sound.PlaySound

val batcher: ChunkBatcher by inject()
val sounds: Sounds by inject()

on<World, PlaySound> {
    val sound = AreaSound(tile, id, radius, repeat, delay, volume, speed, midi, owner)
    batcher.update(tile.chunk, sound.toMessage())
    sound.events.emit(Registered)
}

fun AreaSound.toMessage(): (Player) -> Unit = { player ->
    if (midi) {
        player.client?.areaMIDI(tile.offset(), id, radius, rotation, delay, volume, speed)
    } else {
        player.client?.areaSound(tile.offset(), id, radius, rotation, delay, volume, speed)
    }
}

batcher.addInitial { player, chunk, messages ->
    sounds[chunk].forEach {
        if (it.visible(player)) {
            messages += it.toMessage()
        }
    }
}
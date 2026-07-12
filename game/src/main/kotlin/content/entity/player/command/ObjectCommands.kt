package content.entity.player.command

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAnimation
import java.util.concurrent.TimeUnit

class ObjectCommands : Script {

    init {
        adminCommand(
            "obj",
            stringArg("id"),
            intArg("shape", optional = true),
            intArg("rotation", optional = true),
            intArg("ticks", optional = true),
            desc = "Spawn an object",
            handler = ::spawn,
        )

        adminCommand(
            "objanim",
            intArg("from"),
            intArg("to", optional = true),
            desc = "Play an animation id (or a range, cycling every 5s) on nearby objects",
            handler = ::objanim,
        )
    }

    suspend fun objanim(player: Player, args: List<String>) {
        val from = args[0].toIntOrNull() ?: return
        val to = args.getOrNull(1)?.toIntOrNull() ?: from
        // Scan a small radius so multi-tile objects (stored at their base tile) are
        // still found when standing inside them.
        val objects = (-2..2).flatMap { dx -> (-2..2).flatMap { dy -> GameObjects.at(player.tile.add(dx, dy)) } }
        if (objects.isEmpty()) {
            player.message("No objects found nearby.")
            return
        }
        for (animation in from..to) {
            for (obj in objects) {
                play(obj, animation)
            }
            player.message("Playing anim $animation on ${objects.size} nearby object(s).")
            if (animation != to) {
                player.delay(TimeUnit.SECONDS.toTicks(5))
            }
        }
    }

    private fun play(obj: GameObject, animation: Int) {
        ZoneBatchUpdates.add(obj.tile.zone, ObjectAnimation(obj.tile.id, animation, obj.shape, obj.rotation))
    }

    fun spawn(player: Player, args: List<String>) {
        val id = args[0]
        val shape = args.getOrNull(1)?.toIntOrNull() ?: 10
        val rotation = args.getOrNull(2)?.toIntOrNull() ?: 0
        val ticks = args.getOrNull(3)?.toIntOrNull() ?: -1
        GameObjects.add(id, player.tile, shape, rotation, ticks)
    }
}

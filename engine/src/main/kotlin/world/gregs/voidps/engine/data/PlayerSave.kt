package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.entity.character.player.Player
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PlayerSave(
    private val factory: PlayerFactory
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val queued = mutableSetOf<String>()

    fun queue(player: Player) {
        queued.add(player.accountName)
        executor.submit {
            factory.save(player.accountName, player)
            queued.remove(player.accountName)
        }
    }

    fun saving(name: String) = queued.contains(name)

    fun shutdown() {
        executor.awaitTermination(15, TimeUnit.SECONDS)
    }
}
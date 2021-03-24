import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.character.remove
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on

on<Command>({ prefix == "record" }) { player: Player ->
    val all = mutableListOf<Event>()
    player["recording"] = all
    player.events.all = {
        all.add(it)
    }
}

on<Command>({ prefix == "stop" }) { player: Player ->
    player.events.all = null
    val recording: MutableList<Event> = player.remove("recording") ?: return@on
    for (record in recording) {
        println(record)
    }
}
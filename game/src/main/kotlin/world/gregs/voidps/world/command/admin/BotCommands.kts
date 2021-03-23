import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on

on<Command>({ prefix == "record" }) { player: Player ->
    player["recording"] = true
    player["record"] = mutableListOf<PlayerEvent>()
}

on<Command>({ prefix == "stop" }) { player: Player ->
    player["recording"] = false
    val recording: MutableList<PlayerEvent> = player["record"]
    for (record in recording) {
        println(record)
    }
}

on<ObjectOption>({ player -> player["recording", false] }) { player: Player ->
    val list: MutableList<PlayerEvent> = player["record"]
    list.add(this)
}
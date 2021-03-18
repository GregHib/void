import world.gregs.voidps.engine.entity.character.get
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where

Command where { prefix == "record" } then {
    player["recording"] = true
    player["record"] = mutableListOf<PlayerEvent>()
}

Command where { prefix == "stop" } then {
    player["recording"] = false
    val recording: MutableList<PlayerEvent> = player["record"]
    for (record in recording) {
        println(record)
    }
}

ObjectOption where { player["recording", false] } then {
    val list: MutableList<PlayerEvent> = player["record"]
    list.add(this)
}
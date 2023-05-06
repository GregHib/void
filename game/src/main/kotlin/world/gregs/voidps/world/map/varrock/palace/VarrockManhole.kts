import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.on

on<ObjectOption>({ obj.id == "varrock_manhole" && option == "Open" }) { player: Player ->
    obj.replace("varrock_manhole_open")
}

on<ObjectOption>({ obj.id == "varrock_manhole_open" && option == "Close" }) { player: Player ->
    obj.replace("varrock_manhole")
}
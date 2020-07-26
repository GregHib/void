package rs.dusk.world.entity.player.ui.tab

import rs.dusk.engine.client.ui.event.InterfaceInteraction
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then

on(InterfaceInteraction) {
    where {
        name == "options" && component == "graphics" && option == "Graphics Settings"
    }
    then {
        player.open("graphics_options")
    }
}

on(InterfaceInteraction) {
    where {
        name == "options" && component == "audio" && option == "Audio Settings"
    }
    then {
        player.open("audio_options")
    }
}
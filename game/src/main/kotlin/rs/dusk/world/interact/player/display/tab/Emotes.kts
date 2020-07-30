package rs.dusk.world.interact.player.display.tab

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.variable.BitwiseVariable
import rs.dusk.engine.model.engine.variable.StringMapVariable
import rs.dusk.engine.model.engine.variable.Variable
import rs.dusk.engine.model.engine.variable.sendVar
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSettingsMessage

StringMapVariable(
    465, Variable.Type.VARP, true, mapOf(
        0 to "locked",
        7 to "unlocked"
    )
).register("lost_tribe_emotes")

StringMapVariable(
    1085, Variable.Type.VARP, true, mapOf(
        0 to "locked",
        249852 to "unlocked"
    )
).register("zombie_hand_emote")

BitwiseVariable(
    802, Variable.Type.VARP, true, values = listOf(
        "Flap",
        "Slap Head",
        "Idea",
        "Stomp"
    )
).register("stronghold_of_security_emotes")

BitwiseVariable(
    313, Variable.Type.VARP, true, values = listOf(
        "Glass Wall",
        "Glass Box",
        "Climb Rope",
        "Lean",
        "Scared",
        "Zombie Dance",
        "Zombie Walk",
        "Bunny-hop",
        "Skillcape",
        "Snowman Dance",
        "Air Guitar",
        "Safety First",
        "Explore",
        "Trick",
        "Give Thanks",
        "Freeze"
    )
).register("event_emotes")

InterfaceOpened where { name == "emotes" } then {
    for (index in 11..14) {
        player.send(InterfaceSettingsMessage(id, index, -1, 190, 2150))
    }

    player.sendVar("lost_tribe_emotes")
    player.sendVar("stronghold_of_security_emotes")
    player.sendVar("zombie_hand_emote")
    player.sendVar("event_emotes")
}
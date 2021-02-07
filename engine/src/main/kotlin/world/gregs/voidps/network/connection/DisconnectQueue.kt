package world.gregs.voidps.network.connection

import world.gregs.voidps.engine.entity.character.player.Player
import java.util.*

interface DisconnectQueue : Queue<Player>, Runnable
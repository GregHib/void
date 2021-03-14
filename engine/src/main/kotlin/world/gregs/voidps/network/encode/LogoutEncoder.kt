package world.gregs.voidps.network.encode

import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes.LOGOUT

fun Client.logout() = send(LOGOUT, 0) {}
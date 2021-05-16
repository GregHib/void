package world.gregs.voidps.network.encode

import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol.LOGOUT

fun Client.logout() = send(LOGOUT) {}
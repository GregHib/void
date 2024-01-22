package world.gregs.voidps.network.encode

import world.gregs.voidps.network.Protocol.LOGOUT
import world.gregs.voidps.network.client.Client

fun Client.logout() = send(LOGOUT) {}
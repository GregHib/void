package rs.dusk.network.rs.session

import io.netty.channel.Channel
import rs.dusk.core.network.model.session.Session

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since April 09, 2020
 */
class ServiceSession(channel: Channel) : Session(channel)
package org.redrune.network.session

import io.netty.channel.Channel
import org.redrune.network.codec.service.Service
import org.redrune.network.codec.service.ServiceRequest
import org.redrune.network.codec.service.ServiceResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class ServiceSession(channel: Channel) : Session(channel) {
    override fun messageReceived(msg: Any) {
        val serviceRequest = msg as ServiceRequest
        when (val service = serviceRequest.service) {
            Service.FILE_SERVICE -> {
                send(ServiceResponse(service))
            }
            Service.LOGIN_SERVICE -> {

            }
        }
    }
}
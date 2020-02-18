package org.redrune.network.codec.service.message.impl

import org.redrune.network.codec.service.message.ServiceType
import org.redrune.network.model.message.Message

/**
 * This class encapsulates a service message containing the type of service requested as well as the build number of the requesting client
 *
 * @author Tyluur <contact@kiaira.tech>
 * @property type The type of service being requested
 * @property majorBuild Int The major build version of the client
 * @since 2020-02-02
 */
data class VersionBoundServiceMessage(
    val type: ServiceType,
    val majorBuild: Int
) : Message
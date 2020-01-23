package org.redrune.network.codec.service

/**
 * The [Service] that should be handled next in the pipeline is wrapped by this class
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:24 a.m.
 */
class ServiceResponse(
    /**
     * The next service
     */
    val service: Service
)
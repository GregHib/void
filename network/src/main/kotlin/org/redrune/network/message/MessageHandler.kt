package org.redrune.network.message

import org.redrune.network.Session
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 5:18 p.m.
 */
abstract class MessageHandler<T : Message> {

    fun getGenericTypeClass(): KClass<T> {
        return try {
            val className =
                (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0].typeName
            val clazz = Class.forName(className).kotlin
            clazz as KClass<T>
        } catch (e: Exception) {
            throw IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ", e)
        }
    }

    /**
     * Handling the message, invoked after a message has been decoded and passed to the pipeline
     */
    abstract fun handle(session: Session, msg: T)
}
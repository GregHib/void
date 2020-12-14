package rs.dusk.core.network.codec

import io.github.classgraph.ClassGraph
import java.util.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 02, 2020
 */
object ReflectionUtils {

    val result = ClassGraph().enableClassInfo().scan()
    /**
     * Finds all subclasses of the parameterized type, and stores them into the returned list
     */
    inline fun <reified T> findSubclasses() : ArrayList<T> {
        val name = T::class.qualifiedName
        val list = result.getSubclasses(name).loadClasses() as MutableList<Class<*>>?
        val classes = ArrayList<T>()
        list?.forEach { classes.add(it.newInstance() as T) }
        return classes
    }

}
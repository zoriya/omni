package dev.zoriya.omni.utils

import android.os.Handler
import android.os.Looper
import com.margelo.nitro.NitroModules
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
import kotlin.reflect.KProperty

object ThreadHelper {
    @JvmStatic
    fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
            return
        }

        val ctx = NitroModules.applicationContext ?: throw Error("Missing context")
        Handler(ctx.mainLooper).post { action() }
    }

    @JvmStatic
    fun <T> runOnMainThreadSync(action: Callable<T>): T {
        return if (Looper.myLooper() == Looper.getMainLooper()) {
            action.call()
        } else {
            val futureTask = FutureTask(action)
            Handler(Looper.getMainLooper()).post(futureTask)
            futureTask.get()
        }
    }

    class MainThreadProperty<Reference, Type>(
        private val get: Reference.() -> Type,
        private val set: (Reference.(Type) -> Unit)? = null
    ) {
        operator fun getValue(thisRef: Reference, property: KProperty<*>): Type {
            return runOnMainThreadSync { thisRef.get() }
        }

        operator fun setValue(thisRef: Reference, property: KProperty<*>, value: Type) {
            val setter = set ?: throw IllegalStateException("Property ${property.name} is read-only")
            runOnMainThread { thisRef.setter(value) }
        }
    }

    fun <Reference, T> mainThreadProperty(get: Reference.() -> T) = MainThreadProperty(get)
    fun <Reference, T> mainThreadProperty(
        get: Reference.() -> T,
        set: Reference.(T) -> Unit
    ) = MainThreadProperty(get, set)
}
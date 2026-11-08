package cl.duoc.tacticaldev.testutil

import android.content.SharedPreferences

/**
 * SharedPreferences "falso" que guarda todo en un mapa en memoria.
 *
 * Un *fake* es una implementación simple y funcional de una interfaz, pensada
 * solo para pruebas. A diferencia de un *mock* (que solo responde lo que le
 * programamos), el fake se comporta como el real: lo que guardas, lo puedes leer.
 * Así podemos probar TecnicoRepository sin un teléfono.
 */
class FakeSharedPreferences : SharedPreferences {

    private val datos = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = datos.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = datos[key.orEmpty()] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (datos[key.orEmpty()] as? MutableSet<String>) ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = datos[key.orEmpty()] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = datos[key.orEmpty()] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = datos[key.orEmpty()] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = datos[key.orEmpty()] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = datos.containsKey(key.orEmpty())
    override fun edit(): SharedPreferences.Editor = Editor()
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    private inner class Editor : SharedPreferences.Editor {
        private val cambios = mutableMapOf<String, Any?>()
        private var limpiar = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            cambios[key.orEmpty()] = value
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            cambios[key.orEmpty()] = values
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            cambios[key.orEmpty()] = value
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            cambios[key.orEmpty()] = value
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            cambios[key.orEmpty()] = value
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            cambios[key.orEmpty()] = value
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            cambios[key.orEmpty()] = null
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            limpiar = true
            return this
        }

        override fun commit(): Boolean {
            if (limpiar) datos.clear()
            cambios.forEach { (k, v) -> if (v == null) datos.remove(k) else datos[k] = v }
            return true
        }
        override fun apply() {
            commit()
        }
    }
}

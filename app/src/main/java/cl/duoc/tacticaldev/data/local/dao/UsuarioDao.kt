package cl.duoc.tacticaldev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>): List<Long>
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long
}

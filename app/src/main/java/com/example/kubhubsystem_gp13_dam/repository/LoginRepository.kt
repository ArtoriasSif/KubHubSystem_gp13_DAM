package com.example.kubhubsystem_gp13_dam.data.repository

import com.example.kubhubsystem_gp13_dam.model.loginUsers.User
import com.example.kubhubsystem_gp13_dam.model.loginUsers.UserRole
import kotlinx.coroutines.delay

/**
 * Clase encargada de manejar la lógica de autenticación y datos de usuario.
 * Simula una fuente de datos local (sin conexión a una API real).
 */
class LoginRepository {

    // =====================================================================
    // 🔹 LISTA DE USUARIOS SIMULADA
    // =====================================================================
    /***
     * Esta lista representa los usuarios disponibles dentro del sistema.
     * Cada usuario contiene:
     *  - username: correo del usuario
     *  - password: contraseña asociada
     *  - role: rol del sistema (enum UserRole)
     *  - displayName: nombre mostrado en la UI
     ***/
    private val users = listOf(
        User(
            username = "admin@kubhub.com",
            password = "admin123",
            role = UserRole.ADMIN,
            displayName = "Administrador"
        ),
        User(
            username = "coadmin@kubhub.com",
            password = "coadmin123",
            role = UserRole.CO_ADMIN,
            displayName = "Co-Administrador"
        ),
        User(
            username = "gestor@kubhub.com",
            password = "gestor123",
            role = UserRole.GESTOR_PEDIDOS,
            displayName = "Gestor de Pedidos"
        ),
        User(
            username = "profesor@kubhub.com",
            password = "profesor123",
            role = UserRole.PROFESOR,
            displayName = "Profesor"
        ),
        User(
            username = "bodega@kubhub.com",
            password = "bodega123",
            role = UserRole.BODEGA,
            displayName = "Bodeguero"
        ),
        User(
            username = "asistente@kubhub.com",
            password = "asistente123",
            role = UserRole.ASISTENTE,
            displayName = "Asistente"
        )
    )

    // =====================================================================
    // 🔸 FUNCIÓN DE LOGIN (SIMULACIÓN DE AUTENTICACIÓN)
    // =====================================================================
    /**
     * Simula el inicio de sesión con delay (como si fuera una llamada a servidor).
     *
     * @param username Correo del usuario
     * @param password Contraseña ingresada
     * @return String? → Devuelve:
     *  - `"username"` si el usuario no existe
     *  - `"password"` si la contraseña es incorrecta
     *  - `null` si la autenticación es exitosa
     */
    suspend fun login(username: String, password: String): String? {
        /***
         * ✅ delay() suspende la coroutine SIN BLOQUEAR el hilo principal.
         * Esto simula el tiempo de espera de una petición HTTP real.
         ***/
        delay(1500) // 1.5 segundos de simulación de red

        /*** Busca el usuario en la lista según el username ingresado ***/
        val user = users.find { it.username == username }

        /*** Valida el resultado y retorna el tipo de error o null si es correcto ***/
        return when {
            user == null -> "username"              // Usuario no encontrado
            user.password != password -> "password" // Contraseña incorrecta
            else -> null                            // Inicio de sesión exitoso
        }
    }

    // =====================================================================
    // 🔹 FUNCIÓN: OBTENER CREDENCIALES DEMO
    // =====================================================================
    /**
     * Obtiene las credenciales (usuario y contraseña) según el rol seleccionado.
     *
     * @param role Rol del usuario (UserRole)
     * @return Pair<String, String>? → (username, password)
     */
    fun getDemoCredentials(role: UserRole): Pair<String, String>? {
        /*** Busca el usuario correspondiente al rol indicado ***/
        val user = users.find { it.role == role }
        /*** Si lo encuentra, retorna un par con username y password ***/
        return user?.let { it.username to it.password }
    }

    // =====================================================================
    // 🔹 FUNCIÓN: BUSCAR USUARIO POR USERNAME
    // =====================================================================
    /**
     * Retorna el objeto User asociado a un username específico.
     *
     * @param username Correo electrónico del usuario
     * @return User? → El usuario encontrado o null si no existe
     */
    fun getUserByUsername(username: String): User? { //No ultilizado actualmente
        return users.find { it.username == username }
    }

    // =====================================================================
    // 🔸 SINGLETON: INSTANCIA ÚNICA DE REPOSITORIO
    // =====================================================================
    /**
     * Asegura que solo exista una única instancia de LoginRepository.
     * Evita múltiples cargas de datos innecesarias.
     */
    companion object {
        /*** @Volatile
         *  instance como @Volatile hace que Todos los hilos ven el valor actualizado inmediatamente,
         *  Si Hilo A crea la instancia y la asigna a instance, Hilo B la verá inmediatamente al leer la variable.
         *  synchronized(this) → asegura que solo un hilo a la vez ejecute ese bloque.
         *  @Volatile → asegura que todos los hilos vean el resultado inmediatamente después de crear la instancia.
         *
         *  BURDO
         *  @Volatile = “Oye JVM, cualquier cambio que haga un hilo aquí debe ser visible para todos los demás hilos inmediatamente”.
         *  synchronized = “No dejes que más de un hilo entre aquí a la vez”.
         *
         *  Combinados, garantizan que: 1 Solo se cree una instancia del singleton. 2 Todos los hilos vean esa misma instancia.
         *  __________________________________
         *  Sin @Volatile y sin synchronized
         *  instance = null  → “No le estoy diciendo a los demás hilos que miren esto de inmediato.”
         *  Varias llamadas concurrentes pueden crear más de una instancia del singleton, porque cada hilo ve instance como null y entra a crear su propia instancia.
         *
         *  BURDO
         *  Cada hilo puede pensar: “¡Uy, no hay instancia aún! Voy a crearla yo mismo”, y varios hilos pueden hacerlo al mismo tiempo.
         *  Resultado: rompes el patrón singleton, porque hay múltiples objetos en vez de uno solo compartido.
         *
         *  En resumen: @Volatile + synchronized = seguridad y visibilidad; sin ellos = riesgo de inconsistencias en entornos multihilo.
         ***/
        @Volatile
        private var instance: LoginRepository? = null

        /***
         * Retorna la instancia existente o crea una nueva si no existe.
         * Usa 'synchronized' para asegurar acceso concurrente seguro.
         ***/
        fun getInstance(): LoginRepository {
            return instance ?: synchronized(this) {
                instance ?: LoginRepository().also { instance = it }
            }
        }
    }
}
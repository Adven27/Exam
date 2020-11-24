package env.core

import env.core.SocketUtils.SocketType.TCP
import java.net.InetAddress
import java.util.Random
import java.util.SortedSet
import java.util.TreeSet
import javax.net.ServerSocketFactory

internal object SocketUtils {
    private const val PORT_RANGE_MIN = 1024
    private const val PORT_RANGE_MAX = 65535
    private val random = Random(System.currentTimeMillis())

    /**
     * Find an available TCP port randomly selected from the range
     * [`minPort`, `maxPort`].
     * @param minPort the minimum port number
     * @param maxPort the maximum port number
     * @return an available TCP port number
     * @throws IllegalStateException if no available port could be found
     */
    @JvmOverloads
    fun findAvailableTcpPort(minPort: Int = PORT_RANGE_MIN, maxPort: Int = PORT_RANGE_MAX): Int =
        TCP.findAvailablePort(minPort, maxPort)

    /**
     * Find the requested number of available TCP ports, each randomly selected
     * from the range [`minPort`, `maxPort`].
     * @param numRequested the number of available ports to find
     * @param minPort the minimum port number
     * @param maxPort the maximum port number
     * @return a sorted set of available TCP port numbers
     * @throws IllegalStateException if the requested number of available ports could not be found
     */
    @JvmOverloads
    fun findAvailableTcpPorts(
        numRequested: Int,
        minPort: Int = PORT_RANGE_MIN,
        maxPort: Int = PORT_RANGE_MAX
    ): SortedSet<Int> = TCP.findAvailablePorts(numRequested, minPort, maxPort)

    private enum class SocketType {
        TCP {
            override fun isPortAvailable(port: Int) = try {
                val serverSocket = ServerSocketFactory.getDefault().createServerSocket(
                    port, 1, InetAddress.getByName("localhost")
                )
                serverSocket.close()
                true
            } catch (expected: Exception) {
                false
            }
        };

        /**
         * Determine if the specified port for this `SocketType` is
         * currently available on `localhost`.
         */
        protected abstract fun isPortAvailable(port: Int): Boolean

        /**
         * Find a pseudo-random port number within the range
         * [`minPort`, `maxPort`].
         * @param minPort the minimum port number
         * @param maxPort the maximum port number
         * @return a random port number within the specified range
         */
        private fun findRandomPort(minPort: Int, maxPort: Int): Int = minPort + random.nextInt(maxPort - minPort + 1)

        /**
         * Find an available port for this `SocketType`, randomly selected
         * from the range [`minPort`, `maxPort`].
         * @param minPort the minimum port number
         * @param maxPort the maximum port number
         * @return an available port number for this socket type
         * @throws IllegalStateException if no available port could be found
         */
        fun findAvailablePort(minPort: Int, maxPort: Int): Int {
            val portRange = maxPort - minPort
            var candidatePort: Int
            var searchCounter = 0
            do {
                check(searchCounter <= portRange) {
                    "Could not find an available $name port in the range [$minPort, $maxPort] after $searchCounter attempts"
                }
                candidatePort = findRandomPort(minPort, maxPort)
                searchCounter++
            } while (!isPortAvailable(candidatePort))
            return candidatePort
        }

        /**
         * Find the requested number of available ports for this `SocketType`,
         * each randomly selected from the range [`minPort`, `maxPort`].
         * @param numRequested the number of available ports to find
         * @param minPort the minimum port number
         * @param maxPort the maximum port number
         * @return a sorted set of available port numbers for this socket type
         * @throws IllegalStateException if the requested number of available ports could not be found
         */
        @Suppress("MagicNumber")
        fun findAvailablePorts(numRequested: Int, minPort: Int, maxPort: Int): SortedSet<Int> {
            val availablePorts: SortedSet<Int> = TreeSet()
            var attemptCount = 0
            while (++attemptCount <= numRequested + 100 && availablePorts.size < numRequested) {
                availablePorts.add(findAvailablePort(minPort, maxPort))
            }
            check(availablePorts.size == numRequested) {
                "Could not find $numRequested available $name ports in the range [$minPort, $maxPort]"
            }
            return availablePorts
        }
    }
}

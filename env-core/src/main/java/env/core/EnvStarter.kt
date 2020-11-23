package env.core

import java.util.Scanner
import kotlin.system.exitProcess

/**
 * Starts env for convenience of local development.
 */
object EnvStarter {

    @JvmStatic
    fun main(args: Array<String>) {
        require(args.isNotEmpty()) {
            """Provide implementation full class name of support.env.Environment. For example in gradle.build:
                envRun {
                    args 'specs.AccountingEnvironment'
                }
            """
        }
        val sn = Scanner(System.`in`)
        args[0].asEnvironment().apply {
            up()
            do {
                printHelp()
                exec(sn.nextLine(), this)
            } while (true)
        }
    }

    private fun String.asEnvironment() =
        Class.forName(this).getDeclaredConstructor().apply { isAccessible = true }.newInstance() as Environment

    @Suppress("SpreadOperator")
    private fun exec(cmd: String, env: Environment) {
        val s = cmd.split(" ").toTypedArray()
        when (s[0]) {
            "u" -> if (s.size > 1) env.up(*s.copyOfRange(1, s.size)) else {
                env.up()
            }
            "d" -> if (s.size > 1) env.down(*s.copyOfRange(1, s.size)) else {
                env.down()
            }
            "e" -> exitProcess(0)
            else -> printHelp()
        }
    }

    private fun printHelp() {
        println("\nType: ")
        println("u - environment up")
        println("d - environment down")
        println("e - for exit")
    }
}

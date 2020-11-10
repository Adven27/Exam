package specs.fl

import org.concordion.api.BeforeSpecification
import org.junit.rules.TemporaryFolder
import specs.Specs

open class Fl : Specs() {
    val dir: String
        get() = TEMP_FOLDER.root.path

    fun addFile(name: String?): Boolean {
        return TEMP_FOLDER.newFile(name).exists()
    }

    companion object {
        private val TEMP_FOLDER = TemporaryFolder()

        @JvmStatic
        @BeforeSpecification
        fun beforeSpec() {
            TEMP_FOLDER.create()
        }
    }
}

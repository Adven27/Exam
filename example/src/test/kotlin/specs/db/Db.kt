package specs.db

import io.github.adven27.concordion.extensions.exam.core.utils.findResource
import specs.Specs

open class Db : Specs()

class DbPlugin : Specs()
class DbCheck : Specs()
class DbClean : Specs()
class DbShow : Specs()
class DbSet : Specs()
class DbExecute : Specs() {
    val dir: String
        get() = "/specs/db/data".findResource().path
}

class DbVerify : Specs() {
    val dir: String
        get() = "/specs/db/data".findResource().path
}

package specs.db.dbexecute

import io.github.adven27.concordion.extensions.exam.core.utils.findResource
import specs.Specs

class DbExecute:Specs() {
    val dir: String
        get() = "/specs/db/data".findResource().path

}
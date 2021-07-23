package specs.db.dbverify

import io.github.adven27.concordion.extensions.exam.core.utils.findResource
import specs.Specs

class DbVerify : Specs() {
    val dir: String
        get() = "/specs/db/data".findResource().path

}
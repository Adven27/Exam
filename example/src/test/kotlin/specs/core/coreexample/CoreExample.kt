package specs.core.coreexample

import specs.Specs
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CoreBeforeExample : Specs() {
    fun now() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
}

class CoreExample : Specs()

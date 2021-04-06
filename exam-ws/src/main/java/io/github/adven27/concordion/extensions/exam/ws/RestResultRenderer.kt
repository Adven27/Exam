package io.github.adven27.concordion.extensions.exam.ws

import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import io.github.adven27.concordion.extensions.exam.core.toHtml
import org.concordion.api.listener.AssertSuccessEvent

open class RestResultRenderer : ExamResultRenderer() {
    override fun successReported(event: AssertSuccessEvent) {
        val el = event.element
        if (el.getChildElements("exp").isEmpty()) {
            el.addStyleClass("rest-success").appendNonBreakingSpaceIfBlank()
        } else {
            val id = System.currentTimeMillis()
            val tmpl = template(id).toHtml()
            val exp = el.getChildElements("exp")[0]
            val act = el.getChildElements("act")[0]
            tmpl.findBy("e-$id")!!.text(exp.text).css(exp.getAttributeValue("class")).el.appendNonBreakingSpaceIfBlank()
            tmpl.findBy("a-$id")!!.text(act.text).css(act.getAttributeValue("class")).el.appendNonBreakingSpaceIfBlank()
            el.removeChild(exp)
            el.removeChild(act)
            el.appendChild(tmpl.el)
        }
    }
}

private fun template(id: Long) = //language=xml
    """
    <div>
        <nav>
          <div class="nav nav-tabs" role="tablist">
            <a class="nav-item nav-link active small text-success" id="nav-et-$id-tab" data-toggle="tab" 
            href="#nav-et-$id" role="tab" aria-controls="nav-et-$id" aria-selected="true"> Expected </a>
            <a class="nav-item nav-link small" id="nav-at-$id-tab" data-toggle="tab" href="#nav-at-$id" 
            role="tab" aria-controls="nav-at-$id" aria-selected="false" 
            onclick="setTimeout(() => { window.dispatchEvent(new Event('resize')); }, 200)"> Actual </a>
          </div>
        </nav>
        <div class="tab-content">
          <div class="tab-pane fade show active" id="nav-et-$id" role="tabpanel" aria-labelledby="nav-et-$id-tab">
            <pre id='e-$id' class='rest-success'/>
          </div>
          <div class="tab-pane fade" id="nav-at-$id" role="tabpanel" aria-labelledby="nav-at-$id-tab">
            <pre id='a-$id' class='rest-success'/>
          </div>
        </div>
    </div>
    """

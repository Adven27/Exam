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
          <ul class="nav nav-tabs" role="tablist">
            <li class="nav-item" role="presentation"> 
                <button class="nav-link active small text-success" type="button" role="tab"
                id="nav-et-$id-tab"
                data-bs-toggle="tab" data-bs-target="#nav-et-$id"  
                aria-controls="nav-et-$id" aria-selected="true">Expected</button>
            </li>
            <li class="nav-item" role="presentation"> 
                <button class="nav-link small" type="button" role="tab"
                onclick="setTimeout(() => { window.dispatchEvent(new Event('resize')); }, 200)"
                id="nav-at-$id-tab" 
                data-bs-toggle="tab" data-bs-target="#nav-at-$id" 
                aria-controls="nav-at-$id" aria-selected="false">Actual</button>
            </li>
          </ul>
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

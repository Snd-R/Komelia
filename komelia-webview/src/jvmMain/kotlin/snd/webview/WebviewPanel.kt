package snd.webview

import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.Color
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

class WebviewPanel(
    onCreated: (Webview) -> Unit
) : JPanel() {

    private val drawSurface: Component = Canvas().apply { background = Color.black }
    private var webview: Webview? = null

    init {
        background = Color.black
        layout = BorderLayout()
        add(drawSurface, BorderLayout.CENTER)
        addAncestorListener(
            object : AncestorListener {
                override fun ancestorAdded(event: AncestorEvent) {
                    Webview.webview(drawSurface) { webview ->
                        this@WebviewPanel.webview = webview
                        webview.updateSize(event.component.width, event.component.height)
                        onCreated(webview)
                    }
                }

                override fun ancestorRemoved(event: AncestorEvent) {}

                override fun ancestorMoved(event: AncestorEvent) {}
            }
        )
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                webview?.updateSize(e.component.width, e.component.height)
            }
        })
    }
}
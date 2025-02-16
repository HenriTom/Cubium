package me.henritom.cubium.ui.impl

import me.henritom.cubium.CubiumClient
import me.henritom.cubium.util.RenderUtil
import me.henritom.cubium.ui.UIColors
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Color

class SelectSEScreen(val parent: Screen?) : Screen(Text.translatable("cubium.ui.select_se.title")) {

    private var scroll = 0
    private val searchEngines = CubiumClient.searchEngineManager.searchEngines.shuffled()

    override fun init() {
        super.init()
        scroll = 0
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (context == null || client == null)
            return

        context.drawText(textRenderer, Text.translatable("cubium.ui.select_se.title"), (width - textRenderer.getWidth(Text.translatable("cubium.ui.select_se.title"))) / 2, 20, UIColors.WHITE.rgb, true)

        context.fill(width / 2 - 125, 40, width / 2 + 125, (height / 40) * 40 - 40, UIColors.BACKGROUND.rgb)

        for ((index, searchEngine) in searchEngines.withIndex()) {
            val y = 40 + (index * 40)
            if (y + (scroll * 40) >= 40 && y + (scroll * 40) <= height - 40) {
                val fillStartX = width / 2 - 125
                val fillEndX = width / 2 + 125
                val fillStartY = y + (scroll * 40)
                val fillEndY = y + 38 + (scroll * 40)

                if (fillStartX >= 0 && fillEndX <= width && fillStartY >= 40 && fillEndY <= height - 40) {
                    context.fill(fillStartX, fillStartY, fillEndX, fillEndY, if (mouseX in fillStartX..fillEndX && mouseY >= fillStartY && mouseY <= fillEndY) Color(255, 255, 255, 32).rgb else Color(255, 255, 255, 32).darker().darker().rgb)
                    context.drawText(textRenderer, Text.translatable(searchEngine.title), fillStartX + 4, fillStartY + 4, UIColors.WHITE.rgb, true)
                    RenderUtil.drawTextWithWrap(context, textRenderer, Text.translatable(searchEngine.description), fillStartX + 4, fillStartY + 8 + textRenderer.fontHeight, 242, UIColors.WHITE.rgb, true)
                }
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, button)

        if (client == null)
            return false

        for ((index, searchEngine) in searchEngines.withIndex()) {
            val y = 40 + (index * 40)
            val fillStartX = width / 2 - 125
            val fillEndX = width / 2 + 125
            val fillStartY = y + (scroll * 40)
            val fillEndY = y + 38 + (scroll * 40)

            if (mouseX.toInt() in fillStartX..fillEndX && mouseY.toInt() in fillStartY..fillEndY) {
                if (mouseY > (height / 40) * 40 - 40)
                    return false

                CubiumClient.searchEngineManager.defaultSearchEngine = searchEngine
                client!!.setScreen(BrowserScreen(null))
                return true
            }
        }

        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        scroll = (scroll + verticalAmount.toInt()).coerceIn((-(searchEngines.size - (height / 40)) - 2).coerceAtMost(0), 0)
        return true
    }

    override fun close() {
        client!!.setScreen(this.parent)
    }
}
package me.henritom.cubium.ui.impl

import me.henritom.cubium.CubiumClient
import me.henritom.cubium.ui.UIColors
import me.henritom.cubium.util.RenderUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import java.awt.Color

class SelectSEScreen(private val parent: Screen?, private val shuffle: Boolean) : Screen(Text.translatable("cubium.ui.select_se.title")) {

    private var scroll = 0
    private var searchEngines = CubiumClient.searchEngineManager.shuffledSearchEngines

    private var resetButton: ButtonWidget? = null
    private var addButton: ButtonWidget? = null

    override fun init() {
        super.init()
        scroll = 0

        resetButton = null
        addButton = null

        if (shuffle)
            searchEngines = CubiumClient.searchEngineManager.shuffleList()
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (context == null || client == null)
            return

        context.drawText(textRenderer, Text.translatable("cubium.ui.select_se.title"), (width - textRenderer.getWidth(Text.translatable("cubium.ui.select_se.title"))) / 2, 20, UIColors.WHITE.rgb, true)
        context.drawText(textRenderer, Text.translatable("cubium.ui.select_se.deletion_note"), (width - textRenderer.getWidth(Text.translatable("cubium.ui.select_se.deletion_note"))) / 2, 20 + textRenderer.fontHeight + 2, UIColors.RED.rgb, true)

        context.fill(width / 2 - 125, 40, width / 2 + 125, (height / 40) * 40 - 40, UIColors.BACKGROUND.rgb)

        if (resetButton == null) {
            resetButton = ButtonWidget.builder(Text.translatable("cubium.ui.select_se.reset_button")) {
                CubiumClient.searchEngineManager.searchEngines.clear()
                CubiumClient.searchEngineManager.shuffledSearchEngines.clear()

                CubiumClient.configManager.deleteSearchEngines()
                CubiumClient.configManager.checkForSearchEngines()
                CubiumClient.configManager.loadSearchEngines()

                searchEngines = CubiumClient.searchEngineManager.shuffleList()

                client!!.setScreen(SelectSEScreen(parent, false))
            }
                .dimensions(width / 2 - 125, (height / 40) * 40 - 40, 120, 20)
                .build()

            this.addDrawableChild(resetButton)
        }

        if (addButton == null) {
            addButton = ButtonWidget.builder(Text.translatable("cubium.ui.select_se.add_button")) {
                if (this.client != null)
                    client!!.setScreen(AddSEScreen(this))
            }
                .dimensions(width / 2 + 5, (height / 40) * 40 - 40, 120, 20)
                .build()

            this.addDrawableChild(addButton)
        }

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

                if (button == 2) {
                    if (CubiumClient.searchEngineManager.removeSearchEngine(searchEngine)) {
                        searchEngines = CubiumClient.searchEngineManager.searchEngines
                        client!!.setScreen(SelectSEScreen(parent, false))

                        return true
                    }

                    return false
                }

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
package me.henritom.cubium.ui

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*

class RenderUtil {

    companion object {
        fun drawTextWithWrap(context: DrawContext, textRenderer: TextRenderer, text: Text, x: Int, y: Int, maxWidth: Int, color: Int, shadow: Boolean) {
            val words = text.string.split(" ")
            val lines = mutableListOf<String>()
            var currentLine = ""

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"

                if (textRenderer.getWidth(testLine) <= maxWidth)
                    currentLine = testLine
                else {
                    lines.add(currentLine)
                    currentLine = word
                }
            }

            if (currentLine.isNotEmpty()) lines.add(currentLine)

            var currentY = y
            for (line in lines) {
                context.drawText(textRenderer, line, x, currentY, color, shadow)
                currentY += textRenderer.fontHeight + 2
            }
        }

        fun drawIcon(drawContext: DrawContext, iconName: String, x: Int, y: Int, width: Int, height: Int) {
            val icon = Identifier.of("cubium", "icons/" + iconName.lowercase(Locale.getDefault()))
            drawContext.drawTexture(RenderLayer::getGuiTextured, icon, x, y, 0F, 0F, width, height, width, height)
        }
    }
}
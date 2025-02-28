package me.henritom.cubium.ui.impl

import me.henritom.cubium.CubiumClient
import me.henritom.cubium.search.SearchEngine
import me.henritom.cubium.ui.UIColors
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text

class AddSEScreen(private val parent: Screen?) : Screen(Text.translatable("cubium.ui.add_se.title")) {

    private var cancelButton: ButtonWidget? = null
    private var addButton: ButtonWidget? = null

    private var titleField: TextFieldWidget? = null
    private var descriptionField: TextFieldWidget? = null
    private var urlField: TextFieldWidget? = null
    private var searchURLField: TextFieldWidget? = null

    override fun init() {
        super.init()

        cancelButton = null
        addButton = null

        titleField = null
        descriptionField = null
        urlField = null
        searchURLField = null
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (context == null || client == null)
            return

        context.drawText(textRenderer, Text.translatable("cubium.ui.add_se.title"), (width - textRenderer.getWidth(Text.translatable("cubium.ui.add_se.title"))) / 2, 20, UIColors.WHITE.rgb, true)

        context.fill(width / 2 - 125, 40, width / 2 + 125, textRenderer.fontHeight * 18 + 21, UIColors.BACKGROUND.rgb)

        if (cancelButton == null) {
            cancelButton = ButtonWidget.builder(Text.translatable("cubium.ui.add_se.cancel_button")) {
                client!!.setScreen(parent)
            }
                .dimensions(width / 2 - 125, textRenderer.fontHeight * 18 + 38, 120, 20)
                .build()

            this.addDrawableChild(cancelButton)
        }

        if (addButton == null) {
            addButton = ButtonWidget.builder(Text.translatable("cubium.ui.add_se.add_button")) {
                val searchEngine = SearchEngine(
                    titleField?.text?.takeIf { it.isNotEmpty() } ?: "Unknown",
                    descriptionField?.text?.takeIf { it.isNotEmpty() } ?: "No description provided",
                    urlField?.text?.takeIf { it.isNotEmpty() } ?: "cubium://settings",
                    searchURLField?.text?.takeIf { it.isNotEmpty() } ?: "cubium://"
                )

                CubiumClient.searchEngineManager.addSearchEngine(searchEngine)

                client!!.setScreen(SelectSEScreen(null, true))
            }
                .dimensions(width / 2 + 5, textRenderer.fontHeight * 18 + 38, 120, 20)
                .build()

            this.addDrawableChild(addButton)
        }

        // Title
        context.drawText(textRenderer, Text.translatable("cubium.ui.add_se.se_title").append(":"), width / 2 - 120, 45, UIColors.WHITE.rgb, true)

        if (titleField == null) {
            titleField = TextFieldWidget(textRenderer, width / 2 - 120, 45 + textRenderer.fontHeight, 240, textRenderer.fontHeight + 8, Text.translatable("cubium.ui.add_se.se_title"))
            titleField?.setMaxLength(128)

            addDrawableChild(titleField)
        }

        // Description
        context.drawText(textRenderer, Text.translatable("cubium.ui.add_se.se_description").append(":"), width / 2 - 120, 45 + textRenderer.fontHeight * 4, UIColors.WHITE.rgb, true)

        if (descriptionField == null) {
            descriptionField = TextFieldWidget(textRenderer, width / 2 - 120, textRenderer.fontHeight * 9 + 8, 240, textRenderer.fontHeight + 8, Text.translatable("cubium.ui.add_se.se_description"))
            descriptionField?.setMaxLength(128)

            addDrawableChild(descriptionField)
        }

        // URL
        context.drawText(textRenderer, Text.translatable("cubium.ui.add_se.se_url").append(":"), width / 2 - 120, 45 + textRenderer.fontHeight * 8, UIColors.WHITE.rgb, true)

        if (urlField == null) {
            urlField = TextFieldWidget(textRenderer, width / 2 - 120, textRenderer.fontHeight * 13 + 8, 240, textRenderer.fontHeight + 8, Text.translatable("cubium.ui.add_se.se_url"))
            urlField?.setMaxLength(128)

            addDrawableChild(urlField)
        }

        // Search URL
        context.drawText(textRenderer, Text.translatable("cubium.ui.add_se.se_search_url").append(":"), width / 2 - 120, 45 + textRenderer.fontHeight * 12, UIColors.WHITE.rgb, true)

        if (searchURLField == null) {
            searchURLField = TextFieldWidget(textRenderer, width / 2 - 120, textRenderer.fontHeight * 17 + 8, 240, textRenderer.fontHeight + 8, Text.translatable("cubium.ui.add_se.se_search_url"))
            searchURLField?.setMaxLength(128)

            addDrawableChild(searchURLField)
        }
    }

    override fun close() {
        client!!.setScreen(this.parent)
    }
}
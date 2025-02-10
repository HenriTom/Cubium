package me.henritom.cubium.ui.impl

import com.cinemamod.mcef.MCEF
import com.cinemamod.mcef.MCEFBrowser
import com.mojang.blaze3d.systems.RenderSystem
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.ui.UIColors
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.*
import net.minecraft.text.Text

class BrowserScreen(val parent: Screen?) : Screen(Text.translatable("cubium.ui.browser.title")) {

    private val browserOffset = 40f
    private var browser: MCEFBrowser? = null

    private var reloadButton: ButtonWidget? = null
    private var homeButton: ButtonWidget? = null
    private var closeButton: ButtonWidget? = null

    private var urlBox: TextFieldWidget? = null
    private var urlBoxWasSelected = false

    override fun init() {
        super.init()

        reloadButton = null
        homeButton = null
        closeButton = null

        urlBox = null

        if (CubiumClient.searchEngineManager.defaultSearchEngine == null) {
            client!!.setScreen(SelectSEScreen(null))
            return
        }

        if (browser == null) {
            browser = MCEF.createBrowser(CubiumClient.historyManager.lastUrl.ifEmpty { Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine!!.url).string }, false)

            resizeBrowser()
        }
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (context == null || client == null)
            return

        if (CubiumClient.searchEngineManager.defaultSearchEngine == null) {
            client!!.setScreen(SelectSEScreen(null))
            return
        }

        context.drawText(textRenderer, Text.translatable("cubium.ui.browser.title"), (width - textRenderer.getWidth(Text.translatable("cubium.ui.browser.title"))) / 2, 6, UIColors.WHITE.rgb, true)

        // Reload Button
        if (reloadButton == null) {
            reloadButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.reload")) {
                browser?.loadURL(browser?.getURL() ?: "")

                return@builder
            }
                .dimensions(browserOffset.toInt(), 20, 20, 20)
                .build()

            addDrawableChild(reloadButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == reloadButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            reloadButton!!.isFocused = false
        }

        // Home Button
        if (homeButton == null) {
            homeButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.home")) {
                browser?.loadURL(Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine!!.url).string ?: "")

                return@builder
            }
                .dimensions((browserOffset + 20).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(homeButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == homeButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            homeButton!!.isFocused = false
        }

        // Close Button
        if (closeButton == null) {
            closeButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.close")) {
                close()

                return@builder
            }
                .dimensions((width - browserOffset - 20).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(closeButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == closeButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            closeButton!!.isFocused = false
        }

        // Address bar
        if (urlBox == null) {
            urlBox = TextFieldWidget(
                textRenderer,
                (browserOffset + 40).toInt(),
                (browserOffset - 20).toInt(),
                (width - 60 - browserOffset * 2).toInt(),
                20,
                Text.translatable("cubium.ui.browser.title")
            )
            urlBox!!.setMaxLength(Int.MAX_VALUE)

            addDrawableChild(urlBox)
        } else
            if (!urlBox!!.isFocused) {
                urlBox!!.text = browser?.getURL().toString()
                urlBoxWasSelected = false
            }

        if (urlBox != null && urlBox!!.isFocused && urlBox!!.selectedText.isEmpty() && !urlBoxWasSelected) {
            urlBoxWasSelected = true

            urlBox!!.setSelectionStart(0)
            urlBox!!.setSelectionEnd(urlBox!!.text.length)
        }

        super.render(context, mouseX, mouseY, delta)

        RenderSystem.disableDepthTest()
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR)
        RenderSystem.setShaderTexture(0, browser!!.renderer.textureID)

        val tessellator = Tessellator.getInstance()
        val buffer: BufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)

        buffer.vertex(browserOffset, height - browserOffset, 0f).texture(0.0f, 1.0f).color(255, 255, 255, 255)
        buffer.vertex(width - browserOffset, height - browserOffset, 0f).texture(1.0f, 1.0f).color(255, 255, 255, 255)
        buffer.vertex(width - browserOffset, browserOffset, 0f).texture(1.0f, 0.0f).color(255, 255, 255, 255)
        buffer.vertex(browserOffset, browserOffset, 0f).texture(0.0f, 0.0f).color(255, 255, 255, 255)

        BufferRenderer.drawWithGlobalProgram(buffer.end())
        RenderSystem.setShaderTexture(0, 0)
        RenderSystem.enableDepthTest()
    }

    override fun close() {
        CubiumClient.historyManager.lastUrl = browser?.getURL().toString()

        browser!!.close()

        super.close()
    }

    private fun mouseX(x: Double): Int {
        return (((x - browserOffset) * client?.window?.scaleFactor!!).toInt())
    }

    private fun mouseY(y: Double): Int {
        return (((y - browserOffset) * client?.window?.scaleFactor!!).toInt())
    }

    private fun scaleX(x: Double): Int {
        return (((x - browserOffset * 2) * client?.window?.scaleFactor!!).toInt())
    }

    private fun scaleY(y: Double): Int {
        return (((y - browserOffset * 2) * client?.window?.scaleFactor!!).toInt())
    }

    private fun resizeBrowser() {
        if (width > 100 && height > 100)
            browser!!.resize(scaleX(width.toDouble()), scaleY(height.toDouble()))
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        super.resize(client, width, height)

        resizeBrowser()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (urlBox == null || !urlBox!!.isFocused) {
            browser!!.sendMousePress(mouseX(mouseX), mouseY(mouseY), button)
            browser!!.setFocus(true)
        } else
            if (!urlBox!!.isMouseOver(mouseX, mouseY))
                urlBox!!.isFocused = false

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (urlBox == null || !urlBox!!.isFocused) {
            browser!!.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), button)
            browser!!.setFocus(true)
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (urlBox == null || !urlBox!!.isFocused)
            browser!!.sendMouseMove(mouseX(mouseX), mouseY(mouseY))

        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double, verticalAmount: Double): Boolean {
        if (urlBox == null || !urlBox!!.isFocused)
            browser!!.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), verticalAmount, 0)

        return super.mouseScrolled(mouseX, mouseY, delta, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (urlBox != null && urlBox!!.isFocused)
            if (keyCode == 257) {
                urlBox!!.isFocused = false

                var url = urlBox!!.text

                if (url.contains(".") && !url.contains(" ")) {
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "https://$url"
                } else {
                    url.replace(" ", "+")
                    url = "${Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine?.searchUrl).string ?: ""}$url"
                }

                browser?.loadURL(url)

                return true
            }

        browser!!.sendKeyPress(keyCode, scanCode.toLong(), modifiers)
        browser!!.setFocus(true)

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (urlBox == null || !urlBox!!.isFocused) {
            browser!!.sendKeyRelease(keyCode, scanCode.toLong(), modifiers)
            browser!!.setFocus(true)
        }

        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (urlBox == null || !urlBox!!.isFocused) {
            if (codePoint == 0.toChar())
                return false

            browser!!.sendKeyTyped(codePoint, modifiers)
            browser!!.setFocus(true)
        }

        return super.charTyped(codePoint, modifiers)
    }
}

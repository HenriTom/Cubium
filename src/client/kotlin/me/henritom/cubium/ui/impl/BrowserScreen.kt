package me.henritom.cubium.ui.impl

import com.cinemamod.mcef.MCEF
import com.cinemamod.mcef.MCEFBrowser
import com.mojang.blaze3d.systems.RenderSystem
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.features.bookmark.Bookmark
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

    private var defaultBrowserOffset = 40f
    private var browserOffset = defaultBrowserOffset
    private var browser: MCEFBrowser? = null

    private var lastButton: ButtonWidget? = null
    private var reloadButton: ButtonWidget? = null
    private var homeButton: ButtonWidget? = null
    private var bookmarkButton: ButtonWidget? = null
    private var fullscreenButton: ButtonWidget? = null

    private var addressBar: TextFieldWidget? = null
    private var addressBarWasSelected = false

    private var closeButton: ButtonWidget? = null

    override fun init() {
        super.init()

        if (browserOffset == defaultBrowserOffset) {
            lastButton = null
            reloadButton = null
            homeButton = null
            bookmarkButton = null

            addressBar = null

            fullscreenButton = null
            closeButton = null
        }

        if (CubiumClient.searchEngineManager.defaultSearchEngine == null) {
            client!!.setScreen(SelectSEScreen(null))
            return
        }

        if (browser == null) {
            val defaultUrl = Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine!!.url).string
            browser = MCEF.createBrowser(CubiumClient.historyManager.history.lastOrNull()?.ifEmpty { defaultUrl } ?: defaultUrl, false)

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

        if (browserOffset == defaultBrowserOffset)
            context.drawText(textRenderer, Text.translatable("cubium.ui.browser.title"), (width - textRenderer.getWidth(Text.translatable("cubium.ui.browser.title"))) / 2, 6, UIColors.WHITE.rgb, true)

        // Last Button
        if (lastButton == null) {
            lastButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.previous")) {
                val last = CubiumClient.historyManager.lastUrl
                CubiumClient.historyManager.lastUrl = browser?.getURL().toString()
                browser?.loadURL(last)

                return@builder
            }
                .dimensions(defaultBrowserOffset.toInt(), 20, 20, 20)
                .build()

            addDrawableChild(lastButton)
        } else
            lastButton!!.active = (CubiumClient.historyManager.lastUrl != browser?.getURL() && CubiumClient.historyManager.lastUrl != "")

        if (MinecraftClient.getInstance().currentScreen?.focused == lastButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            lastButton!!.isFocused = false
        }

        // Reload Button
        if (reloadButton == null) {
            reloadButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.reload")) {
                browser?.loadURL(browser?.getURL() ?: "")

                return@builder
            }
                .dimensions(defaultBrowserOffset.toInt() + 20, 20, 20, 20)
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
                .dimensions((defaultBrowserOffset + 40).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(homeButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == homeButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            homeButton!!.isFocused = false
        }

        // Bookmark Button
        val isBookmarked = CubiumClient.bookmarkManager.bookmarks.any { it.url == browser?.getURL() }

        if (bookmarkButton == null) {
            bookmarkButton = ButtonWidget.builder(if (isBookmarked) Text.translatable("cubium.ui.browser.bookmarked") else Text.translatable("cubium.ui.browser.bookmark")) {
                if (!CubiumClient.bookmarkManager.bookmarks.removeIf { it.url == browser?.getURL() })
                    CubiumClient.bookmarkManager.bookmarks.add(Bookmark(browser?.getURL().toString(), browser?.getURL().toString(), "Bookmarks"))

                return@builder
            }
                .dimensions((defaultBrowserOffset + 60).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(bookmarkButton)
        } else
            bookmarkButton!!.message = if (isBookmarked) Text.translatable("cubium.ui.browser.bookmarked") else Text.translatable("cubium.ui.browser.bookmark")

        if (MinecraftClient.getInstance().currentScreen?.focused == bookmarkButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            bookmarkButton!!.isFocused = false
        }

        // Address bar
        if (addressBar == null) {
            addressBar = TextFieldWidget(
                textRenderer,
                (defaultBrowserOffset + 80).toInt(),
                (defaultBrowserOffset - 20).toInt(),
                (width - 120 - defaultBrowserOffset * 2).toInt(),
                20,
                Text.translatable("cubium.ui.browser.title")
            )
            addressBar!!.setMaxLength(Int.MAX_VALUE)

            addDrawableChild(addressBar)
        } else
            if (!addressBar!!.isFocused) {
                addressBar!!.text = browser?.getURL().toString()
                addressBarWasSelected = false
            }

        if (addressBar != null && addressBar!!.isFocused && addressBar!!.selectedText.isEmpty() && !addressBarWasSelected) {
            addressBarWasSelected = true

            addressBar!!.setSelectionStart(0)
            addressBar!!.setSelectionEnd(addressBar!!.text.length)
        }

        // FullScreen Button
        if (fullscreenButton == null) {
            fullscreenButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.fullscreen")) {
                enterFullscreen()

                return@builder
            }
                .dimensions((width - defaultBrowserOffset - 40).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(fullscreenButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == fullscreenButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            fullscreenButton!!.isFocused = false
        }

        // Close Button
        if (closeButton == null) {
            closeButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.close")) {
                close()

                return@builder
            }
                .dimensions((width - defaultBrowserOffset - 20).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(closeButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == closeButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            closeButton!!.isFocused = false
        }

        // History Saver
        if (CubiumClient.historyManager.history.lastOrNull() != browser?.getURL())
            CubiumClient.historyManager.add(browser?.getURL().toString())

        // Browser
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

    private fun enterFullscreen() {
        browserOffset = 0f

        buttonVisibility(false)
        resizeBrowser()
    }

    private fun exitFullscreen() {
        browserOffset = 40f
        init()

        buttonVisibility(true)
        resizeBrowser()
    }

    private fun buttonVisibility(visible: Boolean) {
        lastButton?.active = visible
        lastButton?.visible = visible

        reloadButton?.active = visible
        reloadButton?.visible = visible

        homeButton?.active = visible
        homeButton?.visible = visible

        bookmarkButton?.active = visible
        bookmarkButton?.visible = visible

        addressBar?.active = visible
        addressBar?.visible = visible

        fullscreenButton?.active = visible
        fullscreenButton?.visible = visible

        closeButton?.active = visible
        closeButton?.visible = visible
    }

    override fun close() {
        if (browser == null)
            return

        if (browserOffset == 0f) {
            exitFullscreen()
            return
        }

        CubiumClient.historyManager.add(browser?.getURL().toString())

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
        if (addressBar == null || !addressBar!!.isFocused) {
            browser!!.sendMousePress(mouseX(mouseX), mouseY(mouseY), button)
            browser!!.setFocus(true)
        } else
            if (!addressBar!!.isMouseOver(mouseX, mouseY))
                addressBar!!.isFocused = false

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (addressBar == null || !addressBar!!.isFocused) {
            browser!!.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), button)
            browser!!.setFocus(true)
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (addressBar == null || !addressBar!!.isFocused)
            browser!!.sendMouseMove(mouseX(mouseX), mouseY(mouseY))

        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double, verticalAmount: Double): Boolean {
        if (addressBar == null || !addressBar!!.isFocused)
            browser!!.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), verticalAmount, 0)

        return super.mouseScrolled(mouseX, mouseY, delta, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (addressBar != null && addressBar!!.isFocused)
            if (keyCode == 257) {
                addressBar!!.isFocused = false

                var url = addressBar!!.text

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
        if (addressBar == null || !addressBar!!.isFocused) {
            browser!!.sendKeyRelease(keyCode, scanCode.toLong(), modifiers)
            browser!!.setFocus(true)
        }

        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (addressBar == null || !addressBar!!.isFocused) {
            if (codePoint == 0.toChar())
                return false

            browser!!.sendKeyTyped(codePoint, modifiers)
            browser!!.setFocus(true)
        }

        return super.charTyped(codePoint, modifiers)
    }
}

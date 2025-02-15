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
import org.cef.CefApp
import org.cef.callback.CefCallback
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


class BrowserScreen(val parent: Screen?) : Screen(Text.translatable("cubium.ui.browser.title")) {

    private var defaultBrowserOffset = 40f
    private var browserOffset = defaultBrowserOffset
    private var browser: MCEFBrowser? = null
    private var menuOpened = false

    private var lastButton: ButtonWidget? = null
    private var reloadButton: ButtonWidget? = null
    private var homeButton: ButtonWidget? = null
    private var bookmarkButton: ButtonWidget? = null

    private var addressBar: TextFieldWidget? = null
    private var addressBarWasSelected = false

    private var menuButton: ButtonWidget? = null
    private var fullscreenButton: ButtonWidget? = null
    private var closeButton: ButtonWidget? = null

    override fun init() {
        super.init()

        if (browserOffset == defaultBrowserOffset) {
            lastButton = null
            reloadButton = null
            homeButton = null
            bookmarkButton = null

            addressBar = null

            menuButton = null
            fullscreenButton = null
            closeButton = null
        }

        if (CubiumClient.searchEngineManager.defaultSearchEngine == null) {
            client!!.setScreen(SelectSEScreen(null))
            return
        }

        if (browser == null) {
            val defaultUrl = Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine!!.url).string
            browser = MCEF.createBrowser(CubiumClient.historyManager.history.lastOrNull()?.url?.ifEmpty { defaultUrl } ?: defaultUrl, false)

            resizeBrowser()
        }

        initSchemeHandler()
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
                    CubiumClient.bookmarkManager.bookmarks.add(Bookmark(CubiumClient.bookmarkManager.getNextAvailableId(), browser?.getURL().toString(), browser?.getURL().toString(), "Bookmarks"))

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
                (width - 140 - defaultBrowserOffset * 2).toInt(),
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

        // Menu Button
        if (menuButton == null) {
            menuButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.menu")) {
                menuOpened = !menuOpened

                return@builder
            }
                .dimensions((width - defaultBrowserOffset - 60).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(menuButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == menuButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            menuButton!!.isFocused = false
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
        if (CubiumClient.historyManager.history.lastOrNull()?.url != browser?.getURL())
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

        // Menu Overlay
        if (menuOpened)
            renderMenuOverlay(context, mouseX, mouseY)
    }

    private fun enterFullscreen() {
        browserOffset = 0f
        menuOpened = false

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

        menuButton?.active = visible
        menuButton?.visible = visible

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
        if (menuOpened) {
            val buttons = mapOf(
                Pair("Bookmarks", "cubium://bookmarks"),
                Pair("History", "cubium://history"),
                Pair("Settings", "cubium://settings")
            )

            val menuWidth = buttons.keys.maxOf { textRenderer.getWidth(it) } + 20

            val menuX = (width - browserOffset - menuWidth).toInt()
            val menuY = browserOffset.toInt()

            val buttonHeight = textRenderer.fontHeight + 10
            val buttonWidth = menuWidth - 10

            val buttonX = menuX + 5
            var buttonY = menuY + 5

            for (url in buttons.values) {
                if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight)
                    browser?.loadURL(url)

                buttonY += buttonHeight + 5
            }

            return super.mouseClicked(mouseX, mouseY, button)
        }

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

                val domainRegex = Regex("^(https?://)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$")
                val specialSchemes = listOf("chrome://", "cubium://", "about:", "file:", "http://", "https://")

                url = when {
                    specialSchemes.any { url.startsWith(it) } -> url.replace("cubium-urls", "chrome-urls")
                    domainRegex.matches(url) -> if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                    else -> "${Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine?.searchUrl).string ?: ""}${url.replace(" ", "+")}"
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

    private fun renderMenuOverlay(context: DrawContext, mouseX: Int, mouseY: Int) {
        val buttons = listOf(
            "Bookmarks",
            "History",
            "Settings"
        )

        val menuWidth = buttons.maxOf { textRenderer.getWidth(it) } + 20
        val menuHeight = buttons.size * (textRenderer.fontHeight + 10) + 20

        val menuX = (width - browserOffset - menuWidth).toInt()
        val menuY = browserOffset.toInt()

        context.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, UIColors.BACKGROUND.rgb)

        val buttonHeight = textRenderer.fontHeight + 10
        val buttonWidth = menuWidth - 10

        val buttonX = menuX + 5
        var buttonY = menuY + 5

        for (button in buttons) {
            context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) UIColors.BACKGROUND2.rgb else UIColors.BACKGROUND.rgb)
            context.drawText(textRenderer, Text.translatable(button), buttonX + 5, buttonY + 5, UIColors.WHITE.rgb, false)

            buttonY += buttonHeight + 5
        }
    }

    private fun initSchemeHandler() {
        CefApp.getInstance().registerSchemeHandlerFactory("cubium", "") { _, _, _, request ->
            var url = request.url.lowercase(Locale.getDefault())

            val cubiumURLs = listOf(
                "cubium://bookmarks",
                "cubium://history",
                "cubium://settings"
            )

            if (url.startsWith("cubium://delete_bookmark?id=")) {
                val id = request.url.split("id=")[1]
                CubiumClient.bookmarkManager.deleteBookmark(id.toIntOrNull() ?: -1)
                url = "cubium://bookmarks"
            }

            if (url == "cubium://clear_history") {
                CubiumClient.historyManager.history.clear()
                url = "cubium://history"
            }

            if (cubiumURLs.contains(url)) {
                val customPage = when (url) {
                    "cubium://bookmarks" -> {
                        val bookmarks = CubiumClient.bookmarkManager.bookmarks
                        val folders = mutableMapOf<String, MutableList<Triple<String, String, String>>>()

                        for (bookmark in bookmarks) {
                            val folder = bookmark.folder
                            folders.computeIfAbsent(folder) { mutableListOf() }.add(Triple(bookmark.id.toString(), bookmark.name, bookmark.url))
                        }

                        buildString {
                            append("""
                                <html>
                                    <body>
                                        <h1>Cubium Bookmarks</h1>
                            """.trimIndent())

                            for ((folder, bookmarksInFolder) in folders) {
                                append("<h2>$folder</h2><ul>")

                                for ((id, title, bUrl) in bookmarksInFolder) {
                                    append("""
                                        <li>
                                            <a href="javascript:void(0);" onclick="window.location.href='$bUrl';">$title</a>
                                            <button onclick="deleteBookmark('$id')">Delete</button>
                                        </li>
                                    """.trimIndent())
                                }

                                append("</ul>")
                            }

                            append("""
                                <script>
                                    function deleteBookmark(id) {
                                        window.location.href = "cubium://delete_bookmark?id=" + encodeURIComponent(id);
                                    }
                                </script>
                                </body>
                            </html>
                            """.trimIndent())
                        }
                    }

                    "cubium://history" -> {
                        val history = CubiumClient.historyManager.history

                        val groupedHistory = history.groupBy {
                            val date = Date(it.time)
                            val sdf = SimpleDateFormat("yyyy-MM-dd")
                            sdf.format(date)
                        }

                        buildString {
                            append("<html><body><h1>Cubium History</h1>")
                            append("<button onclick=\"clearHistory()\">Clear History</button>")

                            for ((date, entries) in groupedHistory) {
                                append("<h2>$date</h2>")

                                append("<ul>")

                                for (entry in entries.reversed()) {
                                    val dateTime = Date(entry.time)
                                    val sdfTime = SimpleDateFormat("HH:mm:ss")
                                    val formattedTime = sdfTime.format(dateTime)

                                    append("""
                                        <li><span>$formattedTime</span> - <a href="javascript:void(0);" onclick="window.location.href='${entry.url}';">${entry.url}</a></li>
                                    """)
                                }

                                append("</ul>")
                            }

                            append("""
                                <script>
                                    function clearHistory() {
                                        window.location.href = 'cubium://clear_history';
                                    }
                                </script>
                            """)

                            append("</body></html>")
                        }
                    }

                    else -> "<html><body><h1>Unknown Cubium Page</h1><p>No content for this URL.</p></body></html>"
                }

                val stream = customPage.byteInputStream(StandardCharsets.UTF_8)
                val size = customPage.length

                return@registerSchemeHandlerFactory object : CefResourceHandler {
                    override fun processRequest(request: CefRequest, callback: CefCallback): Boolean {
                        callback.Continue()
                        return true
                    }

                    override fun getResponseHeaders(response: CefResponse, responseLength: IntRef, redirectUrl: StringRef) {
                        response.mimeType = "text/html"
                        response.status = 200
                        responseLength.set(size)
                    }

                    override fun readResponse(dataOut: ByteArray, bytesToRead: Int, bytesRead: IntRef, callback: CefCallback): Boolean {
                        try {
                            val available = stream.available()

                            if (available > 0) {
                                val toRead = min(available.toDouble(), bytesToRead.toDouble()).toInt()
                                bytesRead.set(stream.read(dataOut, 0, toRead))
                                return true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return false
                    }

                    override fun cancel() {}
                }
            } else {
                browser?.loadURL(url.replace("cubium://", "chrome://"))

                return@registerSchemeHandlerFactory null
            }
        }
    }
}

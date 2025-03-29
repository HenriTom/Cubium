package me.henritom.cubium.ui.impl

import CubiumDownloadHandler
import com.cinemamod.mcef.MCEF
import com.cinemamod.mcef.MCEFBrowser
import com.mojang.blaze3d.systems.RenderSystem
import me.henritom.cubium.CubiumClient
import me.henritom.cubium.features.bookmark.Bookmark
import me.henritom.cubium.features.minimize.BrowserSaver.Companion.browser
import me.henritom.cubium.overlay.BrowserOverlay
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
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefCallback
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.net.URI
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class BrowserScreen(val parent: Screen?, private val loadUrl: String? = null) : Screen(Text.translatable("cubium.ui.browser.title")) {

    companion object {
        @JvmField
        val specialSchemes = listOf("chrome://", "cubium://", "about:", "file:", "http://", "https://")
    }

    private var defaultBrowserOffset = 40f
    private var browserOffset = defaultBrowserOffset
    private var menuOpened = false
    private var zoomPercentage = 100

    private var lastButton: ButtonWidget? = null
    private var reloadButton: ButtonWidget? = null
    private var homeButton: ButtonWidget? = null
    private var bookmarkButton: ButtonWidget? = null

    private var addressBar: TextFieldWidget? = null
    private var addressBarWasSelected = false

    private var menuButton: ButtonWidget? = null
    private var minimizeButton: ButtonWidget? = null
    private var fullscreenButton: ButtonWidget? = null
    private var closeButton: ButtonWidget? = null

    override fun init() {
        super.init()

        if (browserOffset == defaultBrowserOffset) {
            BrowserOverlay.enabled = false

            lastButton = null
            reloadButton = null
            homeButton = null
            bookmarkButton = null

            addressBar = null

            menuButton = null
            minimizeButton = null
            fullscreenButton = null
            closeButton = null
        }

        if (CubiumClient.searchEngineManager.defaultSearchEngine == null) {
            client!!.setScreen(SelectSEScreen(null, true))
            return
        }

        if (browser == null) {
            val defaultUrl = Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine!!.url).string
            browser = MCEF.createBrowser(loadUrl ?: if (CubiumClient.featureManager.features["history"] == true) CubiumClient.historyManager.history.lastOrNull()?.url?.ifEmpty { defaultUrl } ?: defaultUrl else defaultUrl, false)

            browser!!.client.addDownloadHandler(CubiumDownloadHandler())

            resizeBrowser()
        }

        initWarden(browser!!)
        initSchemeHandler()
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (context == null || client == null)
            return

        // Zoom
        if (browser != null) {
            browser!!.zoomLevel = CubiumClient.zoom.toDouble() / 10

            val zoomMultiplier = browser?.zoomLevel?.times(1.25) ?: 1.0
            zoomPercentage = (100.0.times(if (zoomMultiplier > 0) (zoomMultiplier + 1.0) else if (zoomMultiplier < 0) 0.1 * (10 + zoomMultiplier) else 1.0)).toInt()

            if (browserOffset == defaultBrowserOffset)
                context.drawText(textRenderer, "$zoomPercentage%", browserOffset.toInt() + 4, (height - browserOffset).toInt() + 4, UIColors.WHITE.rgb, true)
        }

        // Main Screen
        if (CubiumClient.searchEngineManager.defaultSearchEngine == null) {
            client!!.setScreen(SelectSEScreen(null, true))
            return
        }

        // Title
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
            lastButton!!.active = (CubiumClient.historyManager.lastUrl != browser?.getURL() && CubiumClient.historyManager.lastUrl != "") && CubiumClient.featureManager.features["history"] == true

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
                (width - 160 - defaultBrowserOffset * 2).toInt(),
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
                .dimensions((width - defaultBrowserOffset - 80).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(menuButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == menuButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            menuButton!!.isFocused = false
        }

        // Minimize Button
        if (minimizeButton == null) {
            minimizeButton = ButtonWidget.builder(Text.translatable("cubium.ui.browser.minimize")) {
                close()

                return@builder
            }
                .dimensions((width - defaultBrowserOffset - 60).toInt(), 20, 20, 20)
                .build()

            addDrawableChild(minimizeButton)
        }

        if (MinecraftClient.getInstance().currentScreen?.focused == minimizeButton) {
            MinecraftClient.getInstance().currentScreen?.focused = null
            minimizeButton!!.isFocused = false
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
                exit()

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
            if (CubiumClient.featureManager.features["history"] == true)
                CubiumClient.historyManager.add(browser?.getURL().toString())

        if (CubiumClient.featureManager.features["darkreader"] == true)
            browser!!.executeJavaScript(
                """
                    (function() {
                        let script = document.createElement('script');
                        script.src = 'https://cdn.jsdelivr.net/npm/darkreader@4.9.58/darkreader.min.js';
                        script.onload = () => DarkReader.enable();
                        document.head.appendChild(script);
                    })();
                """,
                "https://darkreader.org",
                1
            )


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

    private fun exit() {
        browser!!.close()

        close()

        browser = null
    }

    override fun close() {
        if (browser == null)
            return

        if (browserOffset == 0f) {
            exitFullscreen()
            return
        }

        if (CubiumClient.featureManager.features["history"] == true)
            CubiumClient.historyManager.add(browser?.getURL().toString())

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
                Pair(Text.translatable("cubium.ui.browser.bookmarks").string, "cubium://bookmarks"),
                Pair(Text.translatable("cubium.ui.browser.history").string, "cubium://history"),
                Pair(Text.translatable("cubium.ui.browser.settings").string, "cubium://settings"),
                Pair(Text.translatable("cubium.ui.browser.pop_out").string, "cubium://popout")
            )

            val menuWidth = buttons.keys.maxOf { textRenderer.getWidth(it) } + 20

            val menuX = (width - browserOffset - menuWidth).toInt()
            val menuY = browserOffset.toInt()

            val buttonHeight = textRenderer.fontHeight + 10
            val buttonWidth = menuWidth - 10

            val buttonX = menuX + 5
            var buttonY = menuY + 5

            for (url in buttons.values) {
                if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                    if (url == "cubium://popout") {
                        menuOpened = false
                        BrowserOverlay.enabled = true
                        close()
                        return true
                    }

                    browser?.loadURL(url)
                    menuOpened = false
                }

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
        if (addressBar == null || !addressBar!!.isFocused) {
            if (hasControlDown()) {
                CubiumClient.zoom += verticalAmount.toInt() * 8
                CubiumClient.zoom = Math.clamp(CubiumClient.zoom.toLong(), -72, 72)
                return super.mouseScrolled(mouseX, mouseY, delta, verticalAmount)
            }

            browser!!.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), verticalAmount, 0)
        }

        return super.mouseScrolled(mouseX, mouseY, delta, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == 294)
            browser?.loadURL(browser?.getURL() ?: "")

        if (addressBar != null && addressBar!!.isFocused)
            if (keyCode == 257) {
                addressBar!!.isFocused = false

                var url = addressBar!!.text

                val domainRegex = Regex("^(https?://)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$")

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
            Text.translatable("cubium.ui.browser.bookmarks").string,
            Text.translatable("cubium.ui.browser.history").string,
            Text.translatable("cubium.ui.browser.settings").string,
            Text.translatable("cubium.ui.browser.pop_out").string
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

    private fun initWarden(browser: MCEFBrowser) {
        browser.client.addRequestHandler(object : CefRequestHandlerAdapter() {
            override fun getResourceRequestHandler(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?, isNavigation: Boolean, isDownload: Boolean, requestInitiator: String?, disableDefaultHandling: BoolRef?): CefResourceRequestHandler {
                return object : CefResourceRequestHandlerAdapter() {
                    override fun onBeforeResourceLoad(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): Boolean {
                        if (CubiumClient.featureManager.features["warden"] == false)
                            return super.onBeforeResourceLoad(browser, frame, request)

                        val rawUrl = request?.url ?: return super.onBeforeResourceLoad(browser, frame, request)
                        val host = try {
                            URI(rawUrl).host?.removePrefix("www.") ?: return super.onBeforeResourceLoad(browser, frame, request)
                        } catch (e: Exception) {
                            return super.onBeforeResourceLoad(browser, frame, request)
                        }

                        if (CubiumClient.warden.blockedDomains.isNotEmpty()) {
                            val blocked = CubiumClient.warden.blockedDomains.any { domain ->
                                if (domain.startsWith("*.")) {
                                    val domainWithoutWildcard = domain.removePrefix("*.")
                                    host.endsWith(domainWithoutWildcard)
                                } else {
                                    host == domain
                                }
                            }

                            if (blocked) {
                                println("[Cubium Warden] Blocked request to $host")
                                return true
                            }
                        }

                        return super.onBeforeResourceLoad(browser, frame, request)
                    }
                }
            }
        })
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

            if (url == "cubium://reset_search_engine") {
                if (CubiumClient.historyManager.history.isNotEmpty() && CubiumClient.historyManager.history.last().url == Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine?.url).string)
                    CubiumClient.historyManager.history[CubiumClient.historyManager.history.size - 1].url = ""

                CubiumClient.searchEngineManager.defaultSearchEngine = null
                url = "cubium://settings"
            }

            if (url.startsWith("cubium://set_user_agent?ua=")) {
                val ua = request.url.split("ua=")[1]

                CubiumClient.userAgentManager.updateUserAgent(ua)
                url = "cubium://settings"
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
                                        <h1>${Text.translatable("cubium.ui.browser.bookmarks.title").string}</h1>
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

                        val sortedGroupedHistory = groupedHistory.toSortedMap(reverseOrder())

                        buildString {
                            append("<html><body><h1>${Text.translatable("cubium.ui.browser.history.title").string}</h1>")
                            append("<button onclick=\"clearHistory()\">${Text.translatable("cubium.ui.browser.history.clear").string}</button>")

                            for ((date, entries) in sortedGroupedHistory) {
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

                    "cubium://settings" -> {
                        buildString {
                            val currentSearchEngine = Text.translatable(CubiumClient.searchEngineManager.defaultSearchEngine?.title ?: "cubium.default_se.none.title").string
                            val userAgent = CubiumClient.userAgentManager.mcefAgent

                            append("""
                                <html>
                                    <body>
                                        <h1>${Text.translatable("cubium.ui.browser.settings.title").string}</h1>
                                        
                                        <h2>${Text.translatable("cubium.ui.browser.settings.search_engine").string}</h2>
                                        <p>${Text.translatable("cubium.ui.browser.settings.search_engine.current").string} $currentSearchEngine</p>
                                        <button onclick="resetSearchEngine()">${Text.translatable("cubium.ui.browser.settings.search_engine.change").string}</button>
                                        
                                        <h2>${Text.translatable("cubium.ui.browser.settings.user_agent").string}</h2>
                                        <textarea id="userAgentInput" placeholder="${Text.translatable("cubium.ui.browser.settings.user_agent").string}" rows="3" style="width: 100%;">$userAgent</textarea> 
                                        <br>
                                        <button onclick="saveUserAgent()">${Text.translatable("cubium.ui.browser.settings.user_agent.save").string}</button>
                                        <p>${Text.translatable("cubium.ui.browser.settings.user_agent.note").string}</p>
                                        <p>${Text.translatable("cubium.ui.browser.settings.user_agent.info").string}</p>
                                        
                                        <script>
                                            function resetSearchEngine() {
                                                window.location.href = "cubium://reset_search_engine";
                                            }
                                            
                                            function saveUserAgent() {
                                                let userAgent = document.getElementById("userAgentInput").value;
                                                window.location.href = "cubium://set_user_agent?ua=" + encodeURIComponent(userAgent);
                                            }
                                        </script>
                                    </body>
                                </html>
                            """.trimIndent())
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

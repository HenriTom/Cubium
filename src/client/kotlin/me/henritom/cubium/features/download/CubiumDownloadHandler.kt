import org.cef.browser.CefBrowser
import org.cef.callback.CefBeforeDownloadCallback
import org.cef.callback.CefDownloadItem
import org.cef.handler.CefDownloadHandlerAdapter

class CubiumDownloadHandler : CefDownloadHandlerAdapter() {
    override fun onBeforeDownload(browser: CefBrowser, downloadItem: CefDownloadItem, suggestedName: String, callback: CefBeforeDownloadCallback) {
        callback.Continue(System.getProperty("user.home") + "/Downloads/" + suggestedName, true)
    }
}
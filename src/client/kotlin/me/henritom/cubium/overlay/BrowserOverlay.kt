package me.henritom.cubium.overlay

import com.mojang.blaze3d.systems.RenderSystem
import me.henritom.cubium.features.minimize.BrowserSaver.Companion.browser
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.render.*
import org.lwjgl.glfw.GLFW

class BrowserOverlay : HudRenderCallback {

    private var client = MinecraftClient.getInstance()

    private var posX = 20F
    private var posY = 20F

    companion object {
        var enabled = false

        fun scroll(amount: Int) {
            if (browser != null)
                browser!!.sendMouseWheel(MinecraftClient.getInstance()?.window?.width?.div(2) ?: 100, MinecraftClient.getInstance()?.window?.height?.div(2) ?: 100, amount.toDouble(), 0)
        }
    }

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!enabled || client.currentScreen !is ChatScreen)
                return@register

            val dragging = GLFW.glfwGetMouseButton(client.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
            val mouseX = client.mouse.x / client.window.scaleFactor
            val mouseY = client.mouse.y / client.window.scaleFactor

            if (mouseX in posX..(posX + client.window.width / 6) && mouseY in posY..(posY + client.window.height / 6) && dragging) {
                posX = (mouseX - client.window.width / 12).toFloat()
                posY = (mouseY - client.window.height / 12).toFloat()
            }
        }
    }

    override fun onHudRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        if (browser == null || !enabled)
            return

        val width = posX + client.window.width / 6
        val height = posY + client.window.height / 6

        RenderSystem.disableDepthTest()
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR)
        RenderSystem.setShaderTexture(0, browser!!.renderer.textureID)

        val tessellator = Tessellator.getInstance()
        val buffer: BufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)

        buffer.vertex(posX, height, 0f).texture(0.0f, 1.0f).color(255, 255, 255, 255)
        buffer.vertex(width, height, 0f).texture(1.0f, 1.0f).color(255, 255, 255, 255)
        buffer.vertex(width, posY, 0f).texture(1.0f, 0.0f).color(255, 255, 255, 255)
        buffer.vertex(posX, posY, 0f).texture(0.0f, 0.0f).color(255, 255, 255, 255)

        BufferRenderer.drawWithGlobalProgram(buffer.end())
        RenderSystem.setShaderTexture(0, 0)
        RenderSystem.enableDepthTest()
    }
}
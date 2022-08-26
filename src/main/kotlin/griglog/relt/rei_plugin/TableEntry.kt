package griglog.relt.rei_plugin

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import griglog.relt.RELT
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import net.minecraft.client.gui.GuiComponent
import net.minecraft.resources.ResourceLocation

class TableEntry : Renderer {
    var blitOffset: Int = 0
    override fun render(matrices: PoseStack, bounds: Rectangle, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.setShaderTexture(0, ResourceLocation(RELT.id, "icon.png"))
        GuiComponent.blit(matrices, bounds.x, bounds.y, 0f, 0f, 16, 16, 16, 16)
    }

    override fun getTooltip(context: TooltipContext?): Tooltip? {
        return super.getTooltip(context)
    }

    override fun getZ() = blitOffset

    override fun setZ(z: Int) { blitOffset = z }
}
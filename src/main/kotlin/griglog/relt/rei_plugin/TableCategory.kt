package griglog.relt.rei_plugin

import com.mojang.blaze3d.vertex.PoseStack
import griglog.relt.RELT
import me.shedaniel.clothconfig2.ClothConfigInitializer
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.REIRuntime
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.entry.EntryIngredient
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.Mth

val categoryId: CategoryIdentifier<TableDisplay> = CategoryIdentifier.of(RELT.id, "plugin")

class BarteringCategory : DisplayCategory<TableDisplay> {
    override fun getCategoryIdentifier() = categoryId

    override fun getTitle() = TranslatableComponent(RELT.id + ".category")

    override fun getIcon() = TableEntry()

    override fun getDisplayHeight() = 150

    override fun getFixedDisplaysPerPage() = 1

    override fun setupDisplay(display: TableDisplay, bounds: Rectangle): MutableList<Widget> {
        val center = Point(bounds.centerX, bounds.centerY)
        val widgets = mutableListOf<Widget>()

        widgets.add(Widgets.createLabel(Point(center.x, bounds.y),
            TextComponent(display.name.toString()))
            .noShadow()
            .color(0x666666))

        val hasInputs = display.inputEntries.size == 2
        if (hasInputs) {
            widgets.add(Widgets.createSlot(Point(center.x+1, bounds.y + 10))
                .entries(display.inputEntries[1]).markInput())
        }
        widgets.add(Widgets.createSlot(Point(center.x + if (hasInputs) -20 else -10, bounds.y + 10))
            .entries(tableEntry))

        val outBounds = Rectangle(bounds.x, bounds.y + 30, bounds.width, bounds.height - 30)
        widgets.add(Widgets.createSlotBase(outBounds))
        widgets.add(ScrollableSlotsWidget(outBounds, display.outputEntries))

        return widgets
    }
}

//copy-paste from Beacon Payment
class ScrollableSlotsWidget : WidgetWithBounds {
    private val bounds: Rectangle
    private val widgets: List<Slot>
    private val scrolling: ScrollingContainer

    constructor(bounds: Rectangle, ings: Collection<EntryIngredient>){
        this.bounds = bounds
        widgets = ings.map{
            Widgets.createSlot(Point(0, 0))
                .entries(it)
                .disableBackground()
        }
        scrolling = object : ScrollingContainer() {
            override fun getBounds(): Rectangle {
                val r = this@ScrollableSlotsWidget.getBounds()
                return Rectangle(r)
            }

            override fun getMaxScrollHeight(): Int {
                return Mth.ceil(widgets.size / 8f) * 18
            }
        }
    }

    override fun getBounds(): Rectangle = bounds

    override fun children(): List<GuiEventListener?> = widgets

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
        if (containsMouse(mouseX, mouseY)) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -delta, true)
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        scrolling.updateDraggingState(mouseX, mouseY, button) ||
                super.mouseClicked(mouseX, mouseY, button)

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean =
        scrolling.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
                || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)


    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        scrolling.updatePosition(delta)
        val innerBounds = scrolling.scissorBounds
        scissor(matrices, innerBounds).use { scissors ->
            for (y in 0 until Mth.ceil(widgets.size / 8f)) {
                for (x in 0..7) {
                    val index = y * 8 + x
                    if (widgets.size <= index)
                        return@use
                    val widget = widgets[index]
                    widget.bounds.setLocation(bounds.x + 1 + x * 18, bounds.y + 1 + y * 18 - scrolling.scrollAmountInt())
                    widget.render(matrices, mouseX, mouseY, delta)
                }
            }
        }
        scissor(matrices, scrolling.bounds).use { scissors ->
            scrolling.renderScrollBar(
                -0x1000000,
                1f,
                if (REIRuntime.getInstance().isDarkThemeEnabled) 0.8f else 1f
            )
        }
    }
}
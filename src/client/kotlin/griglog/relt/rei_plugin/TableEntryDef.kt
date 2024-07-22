package griglog.relt.rei_plugin

import griglog.relt.RELT
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext
import me.shedaniel.rei.api.common.entry.type.EntryDefinition
import me.shedaniel.rei.api.common.entry.type.EntryType
import net.minecraft.client.gui.DrawContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.stream.Stream

/** Definition of an entry type that looks like
icon.png (if it is a loot table) or
icons.png (if it is a link to all loot tables, effectively a fake table with id "roughly_enough_loot_tables:root")*/
class TableEntryDef : EntryDefinition<Identifier> {
    companion object Constants {
        val type: EntryType<Identifier> = EntryType.deferred(Identifier.of(RELT.MOD_ID, "table"))
        val serializer = Serializer()
        val renderer = Renderer()

        val rootId: Identifier = Identifier.of(RELT.MOD_ID, "root")
    }

    override fun equals(a: Identifier, b: Identifier, context: ComparisonContext): Boolean {
        return a == b
    }

    override fun getValueType(): Class<Identifier> = Identifier::class.java

    override fun getType(): EntryType<Identifier> = Constants.type

    override fun getRenderer(): EntryRenderer<Identifier> = Constants.renderer

    override fun getSerializer(): EntrySerializer<Identifier> = Constants.serializer

    override fun getTagsFor(entry: EntryStack<Identifier>?, value: Identifier?): Stream<out TagKey<*>> =
        Stream.empty()


    override fun asFormattedText(entry: EntryStack<Identifier>, value: Identifier): Text {
        return Text.literal("formatted.$value")
    }

    override fun hash(entry: EntryStack<Identifier>, value: Identifier, context: ComparisonContext): Long =
        value.hashCode().toLong()

    override fun wildcard(entry: EntryStack<Identifier>, value: Identifier): Identifier =
        value

    override fun normalize(entry: EntryStack<Identifier>, value: Identifier): Identifier =
        value

    override fun copy(entry: EntryStack<Identifier>, value: Identifier): Identifier =
        value

    override fun isEmpty(entry: EntryStack<Identifier>, value: Identifier): Boolean =
        false

    override fun getIdentifier(entry: EntryStack<Identifier>, value: Identifier): Identifier? =
        value

    //no clue what it is for
    class Serializer : EntrySerializer<Identifier> {
        override fun supportSaving() = true

        override fun supportReading() = true

        override fun read(tag: NbtCompound): Identifier {
            return Identifier.of(tag.getString("rl"))
        }

        override fun save(entry: EntryStack<Identifier>, value: Identifier): NbtCompound {
            return NbtCompound().apply { putString("rl", value.toString()) }
        }
    }

    class Renderer : EntryRenderer<Identifier> {
        companion object Constants {
            val textureSingle: Identifier = Identifier.of(RELT.MOD_ID, "icon.png")
            val textureMany: Identifier = Identifier.of(RELT.MOD_ID, "icons.png")
        }

        override fun render(
            entry: EntryStack<Identifier>,
            graphics: DrawContext,
            bounds: Rectangle,
            mouseX: Int,
            mouseY: Int,
            delta: Float
        ) {
            val tex = if (entry.value == rootId) textureMany else textureSingle
            graphics.drawTexture(tex, bounds.x, bounds.y, 0f, 0f, 16, 16, 16, 16)
        }

        override fun getTooltip(estack: EntryStack<Identifier>, context: TooltipContext): Tooltip? =
            if (estack.value != rootId) Tooltip.create(Text.literal(estack.value.toString())) else null
    }
}

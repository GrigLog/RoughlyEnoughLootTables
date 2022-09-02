package griglog.relt.rei_plugin

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import griglog.relt.RELT
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext
import me.shedaniel.rei.api.common.entry.type.EntryDefinition
import me.shedaniel.rei.api.common.entry.type.EntryType
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.client.gui.GuiComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import java.util.stream.Stream

class TableEntryDef: EntryDefinition<ResourceLocation>{
    companion object Constants{
        val type: EntryType<ResourceLocation> = EntryType.deferred<ResourceLocation>(ResourceLocation(RELT.id, "table"))
        val serializer = Serializer()
        val renderer = Renderer()

        val rootId = ResourceLocation(RELT.id, "root")
    }
    override fun equals(a: ResourceLocation, b: ResourceLocation, context: ComparisonContext): Boolean {
        return a == b
    }

    override fun getValueType(): Class<ResourceLocation> = ResourceLocation::class.java

    override fun getType(): EntryType<ResourceLocation> = Constants.type

    override fun getRenderer(): EntryRenderer<ResourceLocation> = Constants.renderer

    override fun getSerializer(): EntrySerializer<ResourceLocation>? = Constants.serializer

    override fun getTagsFor(entry: EntryStack<ResourceLocation>?, value: ResourceLocation?): Stream<out TagKey<*>> =
        Stream.empty()


    override fun asFormattedText(entry: EntryStack<ResourceLocation>, value: ResourceLocation): Component {
        return Component.literal("formatted.$value")
    }

    override fun hash(entry: EntryStack<ResourceLocation>, value: ResourceLocation, context: ComparisonContext): Long =
        value.hashCode().toLong()

    override fun wildcard(entry: EntryStack<ResourceLocation>, value: ResourceLocation): ResourceLocation =
        value

    override fun normalize(entry: EntryStack<ResourceLocation>, value: ResourceLocation): ResourceLocation =
        value

    override fun copy(entry: EntryStack<ResourceLocation>, value: ResourceLocation): ResourceLocation =
        value

    override fun isEmpty(entry: EntryStack<ResourceLocation>, value: ResourceLocation): Boolean =
        false

    override fun getIdentifier(entry: EntryStack<ResourceLocation>, value: ResourceLocation): ResourceLocation? =
        value

    //no clue what it is for
    class Serializer : EntrySerializer<ResourceLocation>{
        override fun supportSaving() = true

        override fun supportReading() = true

        override fun read(tag: CompoundTag): ResourceLocation {
            return ResourceLocation(tag.getString("rl"))
        }

        override fun save(entry: EntryStack<ResourceLocation>, value: ResourceLocation): CompoundTag {
            return CompoundTag().apply{putString("rl", value.toString())}
        }
    }

    class Renderer : EntryRenderer<ResourceLocation>{
        companion object Constants{
            val textureSingle = ResourceLocation(RELT.id, "icon.png")
            val textureMany = ResourceLocation(RELT.id, "icons.png")
        }

        override fun render(entry: EntryStack<ResourceLocation>, matrices: PoseStack, bounds: Rectangle, mouseX: Int, mouseY: Int, delta: Float) {
            RenderSystem.setShaderTexture(0, if (entry.value == rootId) textureMany else textureSingle)
            GuiComponent.blit(matrices, bounds.x, bounds.y, 0f, 0f, 16, 16, 16, 16)
        }

        override fun getTooltip(estack: EntryStack<ResourceLocation>, context: TooltipContext): Tooltip? =
            if (estack.value != rootId) Tooltip.create(Component.literal(estack.value.toString())) else null
    }
}

fun tableIng(lootTableName: ResourceLocation): EntryIngredient{
    return EntryIngredients.of(TableEntryDef.type, listOf(lootTableName))
}
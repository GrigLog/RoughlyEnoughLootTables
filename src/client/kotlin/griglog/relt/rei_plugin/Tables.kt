package griglog.relt.rei_plugin

import griglog.relt.mixin.CombinedEntryAccessor
import griglog.relt.mixin.ItemEntryAccessor
import griglog.relt.mixin.LeafEntryAccessor
import griglog.relt.mixin.LootTableAccessor
import griglog.relt.mixin.LootTableEntryAccessor
import griglog.relt.mixin.TagEntryAccessor
import griglog.relt.table_resolving.ItemLike
import griglog.relt.table_resolving.resolveItem
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.loot.LootTable
import net.minecraft.loot.entry.CombinedEntry
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.entry.LootPoolEntry
import net.minecraft.loot.entry.LootTableEntry
import net.minecraft.loot.entry.TagEntry
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.world.World


fun LootTable.resolve(world: World): Pair<Collection<ItemLike>, Collection<EntryStack<Identifier>>> {
    val items = hashSetOf<ItemLike>()
    val tables = hashSetOf<EntryStack<Identifier>>()
    for (pool in (this as LootTableAccessor).pools) {
        for (entry in pool.entries) {
            resolveEntry(entry, items, tables, world)
        }
    }
    return Pair(items, tables)
}

fun resolveEntry(
    entry: LootPoolEntry,
    items: MutableSet<ItemLike>,
    tables: MutableSet<EntryStack<Identifier>>,
    world: World
) {
    when (entry) {
        is ItemEntry -> resolveItem(
            (entry as ItemEntryAccessor).item.value(),
            (entry as LeafEntryAccessor).functions,
            world
        )?.let { items.add(it) }

        is LootTableEntry -> (entry as LootTableEntryAccessor).value.map({ key ->
            tables.add(
                EntryStack.of(
                    TableEntryDef.type,
                    key.value
                )
            )
        },
            { table ->
                val (i, t) = table.resolve(world)
                items.addAll(i)
                tables.addAll(t)
            })

        is TagEntry -> Registries.ITEM.iterateEntries((entry as TagEntryAccessor).name).forEach { item ->
            resolveItem(item.value(), (entry as LeafEntryAccessor).functions, world)?.let { items.add(it) }
        }

        is CombinedEntry -> (entry as CombinedEntryAccessor).children.forEach { resolveEntry(it, items, tables, world) }
    }
}

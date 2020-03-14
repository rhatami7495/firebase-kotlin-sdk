package dev.teamhub.firebase

import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.StructureKind
import kotlin.js.json

actual fun FirebaseEncoder.structureEncoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder = when(desc.kind as StructureKind) {
    StructureKind.LIST -> Array<Any?>(desc.elementsCount) { null }
        .also { value = it }
        .let { FirebaseCompositeEncoder(positiveInfinity) { _, index, value -> it[index] = value } }
    StructureKind.MAP, StructureKind.CLASS -> json()
        .also { value = it }
        .let { FirebaseCompositeEncoder(positiveInfinity) { _, index, value -> it[desc.getElementName(index)] = value } }
}


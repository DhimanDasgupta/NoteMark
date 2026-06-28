package com.dhimandasgupta.notemark.common

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class ImmutableListSerializer<T>(elementSerializer: KSerializer<T>) :
  KSerializer<ImmutableList<T>> {

  private val listSerializer = ListSerializer(elementSerializer)

  override val descriptor: SerialDescriptor = listSerializer.descriptor

  override fun serialize(
    encoder: Encoder,
    value: ImmutableList<T>,
  ) {
    listSerializer.serialize(encoder, value)
  }

  override fun deserialize(decoder: Decoder): ImmutableList<T> {
    return listSerializer.deserialize(decoder).toPersistentList()
  }
}

open class ImmutableSetSerializer<T>(elementSerializer: KSerializer<T>) :
  KSerializer<ImmutableSet<T>> {

  private val setSerializer = SetSerializer(elementSerializer)

  override val descriptor: SerialDescriptor = setSerializer.descriptor

  override fun serialize(
    encoder: Encoder,
    value: ImmutableSet<T>,
  ) {
    setSerializer.serialize(encoder, value)
  }

  override fun deserialize(decoder: Decoder): ImmutableSet<T> {
    return setSerializer.deserialize(decoder).toPersistentSet()
  }
}

open class ImmutableMapSerializer<K, V>(
  keySerializer: KSerializer<K>,
  valueSerializer: KSerializer<V>,
) : KSerializer<ImmutableMap<K, V>> {

  private val mapSerializer =
    MapSerializer(
      keySerializer = keySerializer,
      valueSerializer = valueSerializer,
    )

  override val descriptor: SerialDescriptor = mapSerializer.descriptor

  override fun serialize(
    encoder: Encoder,
    value: ImmutableMap<K, V>,
  ) {
    mapSerializer.serialize(encoder, value)
  }

  override fun deserialize(decoder: Decoder): ImmutableMap<K, V> {
    return mapSerializer.deserialize(decoder).toPersistentMap()
  }
}

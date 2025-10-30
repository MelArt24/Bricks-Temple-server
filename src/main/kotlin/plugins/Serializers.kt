package com.brickstemple.plugins

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return if (decoder is JsonDecoder) {
            when (val el = decoder.decodeJsonElement()) {
                is JsonPrimitive -> when {
                    el.isString -> BigDecimal(el.content)
                    el.booleanOrNull != null -> error("BigDecimal can't be boolean")
                    el.longOrNull != null -> BigDecimal(el.long)
                    el.doubleOrNull != null -> BigDecimal(el.double.toString())
                    else -> error("Unsupported JSON for BigDecimal: $el")
                }
                else -> error("Unsupported JSON element for BigDecimal: $el")
            }
        } else {
            BigDecimal(decoder.decodeString())
        }
    }
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

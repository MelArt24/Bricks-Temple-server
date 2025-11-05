import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CreateOrderItemRequest(
    val productId: Int,
    val quantity: Int,
)
import com.brickstemple.dto.wishlist.WishlistDto
import com.brickstemple.dto.wishlist.WishlistItemDto
import kotlinx.serialization.Serializable

@Serializable
data class WishlistResponse(
    val wishlist: WishlistDto,
    val items: List<WishlistItemDto>
)

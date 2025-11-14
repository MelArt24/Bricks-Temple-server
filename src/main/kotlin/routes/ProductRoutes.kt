package com.brickstemple.routes

import com.brickstemple.dto.CreatedResponse
import com.brickstemple.dto.ErrorResponse
import com.brickstemple.dto.products.PagedResponse
import com.brickstemple.dto.products.ProductDto
import com.brickstemple.dto.products.ProductUpdateDto
import com.brickstemple.models.Products
import com.brickstemple.repositories.ProductRepository
import com.brickstemple.util.toProductDto
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*


fun Route.productRoutes(repo: ProductRepository) {

    route("/products") {

        get {
            try {
                val type = call.request.queryParameters["type"]
                val category = call.request.queryParameters["category"]
                val search = call.request.queryParameters["search"]

                val minPrice = call.request.queryParameters["minPrice"]?.toBigDecimalOrNull()
                val maxPrice = call.request.queryParameters["maxPrice"]?.toBigDecimalOrNull()

                val year = call.request.queryParameters["year"]
                val page = call.request.queryParameters["page"]?.toIntOrNull()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()

                val products = repo.filter(
                    type = type,
                    category = category,
                    search = search,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    year = year,
                    page = page,
                    limit = limit
                )

                if (products.isEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "No products found"))
                } else {
                    call.respond(HttpStatusCode.OK, products)
                }


            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Unexpected error: ${e.message}")
                )
            }
        }


        get("/type/{type}") {
            val type = call.parameters["type"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Missing category parameter")
                )

            try {
                val products = repo.getByType(type)

                if (products.isEmpty()) {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "No products found in type '$type'")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        products
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Unexpected error: ${e.message}")
                )
            }
        }


        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid or missing ID")
                )

            try {
                val product = repo.getById(id)
                if (product == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Product with id=$id not found")
                    )
                } else {
                    call.respond(HttpStatusCode.OK, product)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Unexpected error: ${e.message}")
                )
            }
        }

        authenticate("auth-jwt") {
            post {
                try {
                    val body = call.receive<ProductDto>()

                    if (body.name.isBlank()) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Missing required fields: name or price")
                        )
                    }

                    val newId = repo.create(body)

                    if (newId > 0) {
                        call.respond(HttpStatusCode.Created, CreatedResponse("Product created successfully", newId))

                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Failed to create product")
                        )
                    }

                } catch (e: ContentTransformationException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid JSON: ${e.message}")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Unexpected error: ${e.message}")
                    )
                }
            }


            put("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid or missing ID")
                    )

                try {
                    val product = call.receive<ProductUpdateDto>()
                    val updated = repo.update(id, product)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Updated successfully"))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("Product with id=$id not found")
                        )
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid JSON: ${e.message}")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Unexpected error: ${e.message}")
                    )
                }
            }

            delete("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid or missing ID")
                    )

                try {
                    val ok = repo.delete(id)
                    if (ok) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("Product with id=$id not found")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Unexpected error: ${e.message}")
                    )
                }
            }
        }
    }
}

package loch.golden.waytogo.routes.model

data class RouteListResponse(  val content: List<Route>,
                               val pageable: Pageable,
                               val last: Boolean,
                               val totalPages: Int,
                               val totalElements: Int,
                               val size: Int,
                               val number: Int,
                               val sort: Sort,
                               val first: Boolean,
                               val numberOfElements: Int,
                               val empty: Boolean)

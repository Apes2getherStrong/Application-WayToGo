package loch.golden.waytogo.routes.model

data class Pageable( val pageNumber: Int,
                     val pageSize: Int,
                     val sort: Sort,
                     val offset: Int,
                     val unpaged: Boolean,
                     val paged: Boolean)

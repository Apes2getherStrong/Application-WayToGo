package loch.golden.waytogo.routes.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.repository.RouteRepository
import retrofit2.HttpException

class RoutePagingSource(private val repository: RouteRepository) : PagingSource<Int, Route>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Route> {
        val pageNumber = params.key ?: 1
        val pageSize = params.loadSize

        return try {
            val response = repository.getRoutes(pageNumber, pageSize)
            if (response.isSuccessful) {
                val routes = response.body()?.content ?: emptyList()
                LoadResult.Page(
                    data = routes,
                    prevKey = if (pageNumber == 1) null else pageNumber - 1,
                    nextKey = if (routes.isEmpty()) null else pageNumber + 1
                )
            } else {
                LoadResult.Error(Exception("Failed to get routes."))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        } catch (httpE: HttpException) {
            LoadResult.Error(httpE)
        }

    }

    override fun getRefreshKey(state: PagingState<Int, Route>): Int? {

        return state.anchorPosition?.let { anchorPosition ->
            val closestPage = state.closestPageToPosition(anchorPosition)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)

        }

    }


}
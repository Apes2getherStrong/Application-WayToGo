package loch.golden.waytogo.routes.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import loch.golden.waytogo.routes.model.Route
import loch.golden.waytogo.routes.repository.RouteRepository
import retrofit2.HttpException

class RoutePagingSource(private val repository: RouteRepository) : PagingSource<Int,Route>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Route> {
        val pageNumber = params.key ?: 1
        val pageSize = params.loadSize

        return try {
            val response = repository.getRoutes(pageNumber, pageSize)
            if(response.isSuccessful) {
                val routes = response.body()?.content ?: emptyList()
                LoadResult.Page(
                    data = routes,
                    prevKey = if(pageNumber==1)null else pageNumber -1,
                    nextKey = if (routes.isEmpty()) null else pageNumber+1
                )
            }else{
                LoadResult.Error(Exception("Failed to get routes."))
            }
        }catch (e: Exception) {
            LoadResult.Error(e)
        } catch (httpE: HttpException) {
            LoadResult.Error(httpE)
        }

    }
    override fun getRefreshKey(state: PagingState<Int, Route>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null
        val prevKey = closestPage.prevKey
        val nextKey = closestPage.nextKey

        return when {
            prevKey != null -> prevKey + 1
            nextKey != null -> nextKey - 1
            else -> null
        }

    }


}
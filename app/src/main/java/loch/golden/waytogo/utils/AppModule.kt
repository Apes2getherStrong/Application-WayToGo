package loch.golden.waytogo.utils

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import loch.golden.waytogo.access.services.components.AuthInterceptor
import loch.golden.waytogo.access.services.services.ApiService
import loch.golden.waytogo.access.services.services.GoogleApiService
import loch.golden.waytogo.data.room.WayToGoDatabase
import loch.golden.waytogo.data.room.dao.RouteDao
import loch.golden.waytogo.presentation.fragments.map.components.navigation.LocationManager
import loch.golden.waytogo.presentation.fragments.map.components.navigation.NavigationManager
import loch.golden.waytogo.presentation.fragments.user.components.TokenManager
import loch.golden.waytogo.utils.Constants.Companion.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .pingInterval(3, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGoogleApiService(): GoogleApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_API_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(GoogleApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideWayToGoDatabase(
        @ApplicationContext context: Context,
    ): WayToGoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,  // Use the application context
            WayToGoDatabase::class.java,
            "way-to-go-database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWayToGoDao(database: WayToGoDatabase): RouteDao {
        return database.getRouteDao()
    }

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        val locationManager = LocationManager(context)
        locationManager.startLocationUpdates()
        return locationManager
    }

    @Provides
    @Singleton
    fun provideNavigationManager(apiService: GoogleApiService): NavigationManager {
        return NavigationManager(apiService)
    }

}
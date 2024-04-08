package loch.golden.waytogo.routes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_table")
data class Route(
    @ColumnInfo(name = "route_uid") @PrimaryKey val routeUid: Int,
    val name: String,
    val description: String,

)
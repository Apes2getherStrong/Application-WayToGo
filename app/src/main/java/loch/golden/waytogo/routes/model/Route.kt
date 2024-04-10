package loch.golden.waytogo.routes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import javax.annotation.processing.Generated

@Entity(tableName = "route_table")
data class Route(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "route_uid")
    val routeUid: Int,
    val name: String,
    val description: String,

)
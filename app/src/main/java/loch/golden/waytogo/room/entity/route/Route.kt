package loch.golden.waytogo.room.entity.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "route_table")
data class Route(
    @PrimaryKey
    @ColumnInfo(name = "route_uid")
    @SerializedName("id")
    var routeUid: String,
    var name: String,
    var description: String,
    @Expose(serialize = false, deserialize = true) var externalId: String?

    )


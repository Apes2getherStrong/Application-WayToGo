package loch.golden.waytogo.routes.model.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.UUID
import javax.annotation.processing.Generated

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


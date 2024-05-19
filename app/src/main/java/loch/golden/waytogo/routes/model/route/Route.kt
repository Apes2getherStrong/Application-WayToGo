package loch.golden.waytogo.routes.model.route

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID
import javax.annotation.processing.Generated

@Entity(tableName = "route_table")
data class Route(
    @PrimaryKey
    @ColumnInfo(name = "route_uid")
    @SerializedName("id")
    val routeUid: String,
    var name: String,
    var description: String,

    )

//TODO narazie routUUid jest  wpostaci stringa, nwm czy tak napewno powinno byc i nie trzeba bedzie zrobic jakiegos mappera
//tymczasowe rozwiazanie
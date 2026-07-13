package ai.hnu.kr.termproject_navercalander_duplicate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String,
    val color: String
)

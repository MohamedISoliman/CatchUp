/*
 * Copyright (c) 2017 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sweers.catchup.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import android.support.annotation.Keep
import io.reactivex.Maybe
import io.sweers.catchup.ui.base.HasStableId
import org.threeten.bp.Instant

@Dao
interface ServiceDao {

  @Query("SELECT * FROM pages WHERE type = :type AND page = 0 AND expiration > :expiration")
  fun getFirstServicePage(type: String, expiration: Instant): Maybe<ServicePage>

  @Query("SELECT * FROM pages WHERE type = :type AND page = 0 ORDER BY expiration DESC")
  fun getFirstServicePage(type: String): Maybe<ServicePage>

  @Query("SELECT * FROM pages WHERE type = :type AND page = :page AND sessionId = :sessionId")
  fun getServicePage(type: String, page: Int, sessionId: Long): Maybe<ServicePage>

  @Query("SELECT * FROM items WHERE id = :id")
  fun getItemById(id: Long): Maybe<CatchUpItem>

  @Query("SELECT * FROM items WHERE id IN(:ids)")
  fun getItemByIds(ids: Array<Long>): Maybe<List<CatchUpItem>>

  @Insert(onConflict = REPLACE)
  fun putPage(page: ServicePage)

  @Insert(onConflict = REPLACE)
  fun putItem(item: CatchUpItem)

  @Insert(onConflict = REPLACE)
  fun putItems(vararg item: CatchUpItem)

  @Query("DELETE FROM pages")
  fun nukePages()

  @Query("DELETE FROM items")
  fun nukeItems()

}

@Keep
@Entity(tableName = "pages")
data class ServicePage(
    /**
     * Combination of the sessionId and type
     */
    @PrimaryKey val id: String,
    val type: String,
    val expiration: Instant,
    val sessionId: Long = -1,
    val page: Int = 0,
    val items: List<Long>
)

@Keep
@Entity(tableName = "items")
data class CatchUpItem(
    @PrimaryKey var id: Long,
    val title: String,
    val timestamp: Instant,
    val score: Pair<String, Int>? = null,
    val tag: String? = null,
    val author: String? = null,
    val source: String? = null,
    val commentCount: Int = 0,
    val hideComments: Boolean = false,
    val itemClickUrl: String? = null,
    val itemCommentClickUrl: String? = null
) : HasStableId {
  override fun stableId() = id
}
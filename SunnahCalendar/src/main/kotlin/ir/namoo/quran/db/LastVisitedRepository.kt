package ir.namoo.quran.db

import com.byagowi.persiancalendar.utils.logException

class LastVisitedRepository(
    private val lastVisitedDB: LastVisitedDB,
    private val lastVisitedPageDB: LastVisitedPageDB
) {

    suspend fun insert(ayaID: Int, suraID: Int) {
        runCatching {
            val lastVisitedEntity = LastVisitedEntity(
                ayaID = ayaID, suraID = suraID
            )
            lastVisitedDB.lastVisitedDao().insert(lastVisitedEntity)
        }.onFailure(logException)
    }

    suspend fun update(lastVisitedEntity: LastVisitedEntity) {
        lastVisitedDB.lastVisitedDao().update(lastVisitedEntity)
    }

    suspend fun delete(lastVisitedEntity: LastVisitedEntity) {
        lastVisitedDB.lastVisitedDao().delete(lastVisitedEntity)
    }

    suspend fun getAllLastVisited(): List<LastVisitedEntity> {
        runCatching {
            return lastVisitedDB.lastVisitedDao().getAllVisited()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }

    // ############################################################### Pages
    suspend fun insertPage(page: Int) {
        runCatching {
            val lastVisitedPageEntity = LastVisitedPageEntity(page = page)
            lastVisitedPageDB.lastVisitedPageDao().insert(lastVisitedPageEntity)
        }.onFailure(logException)
    }

    suspend fun update(lastVisitedPageEntity: LastVisitedPageEntity) =
        lastVisitedPageDB.lastVisitedPageDao().update(lastVisitedPageEntity)

    suspend fun delete(lastVisitedPageEntity: LastVisitedPageEntity) =
        lastVisitedPageDB.lastVisitedPageDao().delete(lastVisitedPageEntity)

    suspend fun getAllVisitedPages(): List<LastVisitedPageEntity> {
        runCatching {
            return lastVisitedPageDB.lastVisitedPageDao().getAllVisitedPages()
        }.onFailure { return emptyList() }.getOrElse { return emptyList() }
    }
}//end of class LastVisitedRepository

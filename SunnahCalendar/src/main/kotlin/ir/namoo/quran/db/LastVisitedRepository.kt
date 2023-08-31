package ir.namoo.quran.db

class LastVisitedRepository(private val lastVisitedDB: LastVisitedDB) {

    suspend fun insert(ayaID: Int, suraID: Int) {
        runCatching {
            val lastVisitedEntity = LastVisitedEntity(
                ayaID = ayaID, suraID = suraID
            )
            lastVisitedDB.lastVisitedDao().insert(lastVisitedEntity)
        }.onFailure { }
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
}//end of class LastVisitedRepository

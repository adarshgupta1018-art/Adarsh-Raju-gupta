package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY startHour ASC, startMinute ASC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentById(id: String): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<TournamentEntity>)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("UPDATE tournaments SET roomId = :roomId, roomPassword = :roomPassword WHERE id = :id")
    suspend fun updateRoomDetails(id: String, roomId: String, roomPassword: String)

    @Query("UPDATE tournaments SET status = :status WHERE id = :id")
    suspend fun updateTournamentStatus(id: String, status: com.example.data.model.TournamentStatus)

    @Query("DELETE FROM tournaments")
    suspend fun clearAll()
}

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registrations ORDER BY registeredAt DESC")
    fun getAllRegistrations(): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId ORDER BY registeredAt ASC")
    fun getRegistrationsForTournament(tournamentId: String): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE ffUid = :ffUid ORDER BY registeredAt DESC")
    fun getRegistrationsForPlayer(ffUid: String): Flow<List<RegistrationEntity>>

    @Query("SELECT COUNT(*) FROM registrations WHERE tournamentId = :tournamentId AND status != 'REJECTED' AND status != 'CANCELLED'")
    suspend fun getActiveCountForTournament(tournamentId: String): Int

    @Query("SELECT * FROM registrations WHERE id = :id LIMIT 1")
    suspend fun getRegistrationById(id: String): RegistrationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(registration: RegistrationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistrations(registrations: List<RegistrationEntity>)

    @Query("UPDATE registrations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: com.example.data.model.RegistrationStatus)

    @Query("DELETE FROM registrations")
    suspend fun clearAll()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE targetUid = :targetUid OR targetUid = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForUser(targetUid: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetUid = :targetUid OR targetUid = 'ALL'")
    suspend fun markAllAsReadForUser(targetUid: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

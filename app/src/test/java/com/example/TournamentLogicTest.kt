package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.RegistrationStatus
import com.example.data.model.TournamentStatus
import com.example.data.repository.TournamentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TournamentLogicTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TournamentRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TournamentRepository(
            tournamentDao = database.tournamentDao(),
            registrationDao = database.registrationDao(),
            notificationDao = database.notificationDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `seedInitialData populates eight tournaments`() = runBlocking {
        repository.seedInitialDataIfEmpty()
        val all = repository.allTournaments.first()
        assertEquals(8, all.size)
        assertTrue(all.any { it.startTime.contains("7:00 PM") })
    }

    @Test
    fun `secure 5-minute room reveal rule locks and unlocks accurately`() = runBlocking {
        repository.seedInitialDataIfEmpty()
        val tournament = repository.allTournaments.first().first { it.startTime.contains("7:00 PM") }

        // Set room credentials
        repository.updateCustomRoomDetails(tournament.id, "ROOM_888999", "PASS_ACE")

        // 1. Simulate 6:54:00 PM (6 minutes before 7:00 PM match -> LOCKED)
        val calBefore = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.MINUTE, -6)
        }
        val credsLocked = repository.getSecureRoomCredentials(
            tournamentId = tournament.id,
            playerUid = "UID_TEST_1",
            simulatedTimeMillis = calBefore.timeInMillis
        )
        assertFalse("Credentials must be locked before 5-minute threshold", credsLocked.isRevealed)
        assertEquals("", credsLocked.roomId)
        assertEquals("", credsLocked.roomPassword)

        // 2. Simulate 6:55:00 PM (Exactly 5 minutes before 7:00 PM match -> UNLOCKED)
        val calUnlocked = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.MINUTE, -5)
        }
        val credsUnlocked = repository.getSecureRoomCredentials(
            tournamentId = tournament.id,
            playerUid = "UID_TEST_1",
            simulatedTimeMillis = calUnlocked.timeInMillis
        )
        assertTrue("Credentials must unlock at 5 minutes prior to match", credsUnlocked.isRevealed)
        assertEquals("ROOM_888999", credsUnlocked.roomId)
        assertEquals("PASS_ACE", credsUnlocked.roomPassword)
    }

    @Test
    fun `player registration creates pending status and unique ID`() = runBlocking {
        repository.seedInitialDataIfEmpty()
        val tournament = repository.allTournaments.first().first()

        val regResult = repository.registerPlayer(
            tournamentId = tournament.id,
            playerName = "Rohan Sharma",
            ffIgn = "ACE_ROHAN",
            ffUid = "7722119900",
            contactNumber = "+91 9988776655",
            paymentRef = "UPI12345"
        )

        assertTrue(regResult.isSuccess)
        val reg = regResult.getOrNull()
        assertNotNull(reg)
        assertTrue(reg!!.id.startsWith("ACE-"))
        assertEquals(RegistrationStatus.PENDING, reg.status)

        // Admin confirms registration
        repository.confirmRegistration(reg.id)
        val updatedTourney = repository.allTournaments.first().first { it.id == tournament.id }
        assertTrue(updatedTourney.confirmedCount >= 1)
    }
}

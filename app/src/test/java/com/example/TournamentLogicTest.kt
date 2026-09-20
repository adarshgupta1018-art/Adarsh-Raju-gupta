package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.RegistrationStatus
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
            context = context,
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

        // Setup custom room details
        repository.updateCustomRoomDetails(tournament.id, "ROOM_888999", "PASS_ACE")

        // 10 minutes prior to match: credentials MUST BE LOCKED
        val calLocked = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 50)
            set(Calendar.SECOND, 0)
        }
        val credsLocked = repository.getSecureRoomCredentials(
            tournamentId = tournament.id,
            playerUid = "test_player",
            simulatedTimeMillis = calLocked.timeInMillis
        )
        assertFalse("Credentials must remain strictly locked 10 mins prior", credsLocked.isRevealed)

        // 4 minutes prior to match: credentials MUST BE UNLOCKED
        val calUnlocked = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 56)
            set(Calendar.SECOND, 0)
        }
        val credsUnlocked = repository.getSecureRoomCredentials(
            tournamentId = tournament.id,
            playerUid = "test_player",
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
            teamName = "Solo",
            selectedSlot = 1,
            paymentRef = "UPI12345"
        )

        assertTrue(regResult.isSuccess)
        val reg = regResult.getOrNull()
        assertNotNull(reg)
        assertTrue(reg!!.id.startsWith("ACE-"))
        assertEquals(RegistrationStatus.PENDING, reg.status)
        assertEquals(1, reg.selectedSlot)

        // Admin confirms registration
        repository.confirmRegistration(reg.id)
        val updatedTourney = repository.allTournaments.first().first { it.id == tournament.id }
        assertTrue(updatedTourney.confirmedCount >= 1)
    }

    @Test
    fun `duplicate slot booking prevention prevents booking same slot`() = runBlocking {
        repository.seedInitialDataIfEmpty()
        val tournament = repository.allTournaments.first().first()

        // Book slot 5 first
        val firstBooking = repository.registerPlayer(
            tournamentId = tournament.id,
            playerName = "Player One",
            ffIgn = "IGN_1",
            ffUid = "UID_1",
            contactNumber = "+91 9100000001",
            teamName = "Team A",
            selectedSlot = 5,
            paymentRef = "REF1"
        )
        assertTrue(firstBooking.isSuccess)

        // Try booking slot 5 again
        val duplicateBooking = repository.registerPlayer(
            tournamentId = tournament.id,
            playerName = "Player Two",
            ffIgn = "IGN_2",
            ffUid = "UID_2",
            contactNumber = "+91 9100000002",
            teamName = "Team B",
            selectedSlot = 5,
            paymentRef = "REF2"
        )
        assertTrue("Duplicate slot booking must fail", duplicateBooking.isFailure)
    }

    @Test
    fun `seedInitialData contains zero hardcoded Adarsh Gupta records`() = runBlocking {
        repository.seedInitialDataIfEmpty()
        val allRegistrations = repository.allRegistrations.first()
        assertFalse(allRegistrations.any { it.playerName.contains("Adarsh", ignoreCase = true) })
        assertFalse(allRegistrations.any { it.ffUid == "1298471203" })
        assertFalse(allRegistrations.any { it.ffIgn == "ACE_ADARSH_99" })
        assertFalse(allRegistrations.any { it.contactNumber == "+91 9811223344" })
    }

    @Test
    fun `sequential registrations generate distinct unique IDs`() = runBlocking {
        repository.seedInitialDataIfEmpty()
        val tournament = repository.allTournaments.first().first { it.startTime.contains("8:00 PM") }

        val reg1 = repository.registerPlayer(
            tournamentId = tournament.id,
            playerName = "Player One",
            ffIgn = "IGN_1",
            ffUid = "UID_1",
            contactNumber = "+91 9100000001",
            teamName = "Team A",
            selectedSlot = 2,
            paymentRef = "REF1"
        ).getOrThrow()

        val reg2 = repository.registerPlayer(
            tournamentId = tournament.id,
            playerName = "Player Two",
            ffIgn = "IGN_2",
            ffUid = "UID_2",
            contactNumber = "+91 9100000002",
            teamName = "Team B",
            selectedSlot = 3,
            paymentRef = "REF2"
        ).getOrThrow()

        assertFalse("Registration IDs must not collide", reg1.id == reg2.id)
        assertTrue(reg1.id.startsWith("ACE-"))
        assertTrue(reg2.id.startsWith("ACE-"))
    }
}

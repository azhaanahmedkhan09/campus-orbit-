package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CampusOrbitRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createStudentAndObserve_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = CampusOrbitRepository(firestore)
        val uniqueCode = "STU-${UUID.randomUUID().toString().take(8)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createStudent(
                studentCode = uniqueCode,
                fullName = "Aarav Mehta",
                email = "aarav@campusorbit.edu",
                department = "Computer Science & Engineering",
                semester = 6L,
                cgpa = 9.2,
                attendancePct = 91L,
                feeStatus = "PAID",
                guardianName = "Rajesh Mehta",
                guardianPhone = "+1-555-0101",
                mentorName = "Dr. Elena Rostova"
            )
        }
        assertTrue("Expected student creation to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)
        val studentId = createResult.getOrThrow()
        assertTrue(studentId.isNotEmpty())

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStudents().first { list -> list.any { it.id == studentId } }
        }
        assertTrue(emitted.any { it.id == studentId && it.studentCode == uniqueCode })
    }

    @Test
    fun getStudentById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = CampusOrbitRepository(firestore)
        val studentId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createStudent(
                studentCode = "STU-ALICE-01",
                fullName = "Alice Student",
                email = "alice.std@campusorbit.edu",
                department = "CSE",
                semester = 4L,
                cgpa = 8.8,
                attendancePct = 89L,
                feeStatus = "PAID",
                guardianName = "Guardian Alice",
                guardianPhone = "+1-555-0102",
                mentorName = "Mentor A"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = CampusOrbitRepository(firestore)
        val bobReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getStudentById(studentId)
        }
        assertTrue("Expected cross-user read to fail", bobReadResult.isFailure)
        val firestoreEx = bobReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeStudents_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val fakeUid = "unauth_uid_test"
        auth.signOut()
        val repository = CampusOrbitRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeStudents(userId = fakeUid).first()
            }
            fail("Expected unauthenticated observeStudents to throw")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    @Test
    fun createAndUpdateGrievanceAndCampusItem_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = CampusOrbitRepository(firestore)

        val grvId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitGrievance(
                ticketNumber = "GRV-${UUID.randomUUID().toString().take(6)}",
                title = "Lab Projector Calibration",
                category = "Infrastructure",
                description = "Smartboard projector in Room 302 needs alignment.",
                priority = "MEDIUM",
                submittedBy = "Aarav Mehta"
            ).getOrThrow()
        }
        assertTrue(grvId.isNotEmpty())

        val itemId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCampusItem(
                moduleType = CampusModuleType.NOTICE,
                code = "NOT-${UUID.randomUUID().toString().take(6)}",
                title = "Convocation 2026 Announcement",
                subtitle = "Main Auditorium",
                department = "All Departments",
                semester = 0L,
                status = "PUBLISHED",
                category = "EVENT",
                primaryMetric = 1.0,
                secondaryMetric = 500L,
                metadataText = "Annual convocation ceremony registration open.",
                ownerName = "Academic Registrar"
            ).getOrThrow()
        }
        assertTrue(itemId.isNotEmpty())
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}

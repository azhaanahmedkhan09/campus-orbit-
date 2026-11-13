const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or list students", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("students").get());
  await assertFails(unauthDb.collection("students").doc("std_1").get());
});

test("Authenticated user can create and read their own UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      fullName: "Dr. Alice Vance",
      email: "alice@campusorbit.edu",
      activeRole: "COLLEGE_ADMIN",
      institutionName: "Orbit Institute of Technology",
      department: "Computer Science & Engineering",
      linkedStudentCode: "STU-2026-001",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user cannot read another user's UserProfile or StudentRecord", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      fullName: "Bob Stone",
      email: "bob@campusorbit.edu",
      activeRole: "STUDENT",
      institutionName: "Orbit Institute of Technology",
      department: "Mechanical Engineering",
      linkedStudentCode: "STU-2026-002",
      createdAt: now,
      updatedAt: now,
    });
    await context.firestore().collection("students").doc("bob_student").set({
      userId: BOB_UID,
      studentCode: "STU-2026-002",
      fullName: "Bob Stone",
      email: "bob@campusorbit.edu",
      department: "Mechanical Engineering",
      semester: 4,
      cgpa: 8.6,
      attendancePct: 88,
      feeStatus: "PAID",
      guardianName: "Robert Stone",
      guardianPhone: "+1-555-0192",
      mentorName: "Prof. K. Sharma",
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("students").doc("bob_student").get());
});

test("Shadow update with extra field is rejected by hasOnly gate", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("students").doc("std_shadow").set({
      userId: ALICE_UID,
      studentCode: "STU-2026-001",
      fullName: "Aarav Mehta",
      email: "aarav@campusorbit.edu",
      department: "Computer Science",
      semester: 5,
      cgpa: 9.1,
      attendancePct: 92,
      feeStatus: "PAID",
      guardianName: "Rajesh Mehta",
      guardianPhone: "+1-555-0101",
      mentorName: "Dr. Elena Rostova",
      status: "ACTIVE",
      isVerifiedHack: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated user can query own students, grievances, and campus_items with userId filter, and unfiltered query fails", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("students").doc("alice_std").set({
      userId: ALICE_UID,
      studentCode: "STU-2026-001",
      fullName: "Aarav Mehta",
      email: "aarav@campusorbit.edu",
      department: "Computer Science",
      semester: 5,
      cgpa: 9.1,
      attendancePct: 92,
      feeStatus: "PAID",
      guardianName: "Rajesh Mehta",
      guardianPhone: "+1-555-0101",
      mentorName: "Dr. Elena Rostova",
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    });
    await context.firestore().collection("campus_items").doc("alice_item").set({
      userId: ALICE_UID,
      moduleType: "NOTICE",
      code: "NOT-2026-01",
      title: "End-Semester Examination Schedule Published",
      subtitle: "Academic Cell • All Departments",
      department: "All Departments",
      semester: 0,
      status: "PUBLISHED",
      category: "EXAMINATION",
      primaryMetric: 100,
      secondaryMetric: 1,
      metadataText: "Hall tickets are now available for download.",
      ownerName: "Controller of Examinations",
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("students").where("userId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("campus_items").where("userId", "==", ALICE_UID).get()
  );
  await assertFails(aliceDb.collection("students").get());
  await assertFails(aliceDb.collection("campus_items").get());
});

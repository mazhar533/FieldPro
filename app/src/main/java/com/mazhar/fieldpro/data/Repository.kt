package com.mazhar.fieldpro.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FieldProRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fieldpro_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val KEY_USER = "user_data"
        private const val KEY_JOBS = "jobs_data"
        private const val KEY_NOTIFICATIONS = "notifications_data"
        private const val KEY_LOGGED_IN = "is_logged_in"
    }

    init {
        // Initialize with mock data if run for the first time
        if (!prefs.contains(KEY_USER)) {
            val defaultUser = User(
                fullName = "Alex Johnson",
                email = "alex@fieldservice.com",
                contactNumber = "+1 555-0199",
                role = "TECHNICIAN"
            )
            saveUser(defaultUser)
            saveJobs(getMockJobs())
            saveNotifications(getMockNotifications())
        }
    }

    // User Session
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }

    fun registerUser(
        user: User,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(user.email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        val userMap = hashMapOf(
                            "fullName" to user.fullName,
                            "email" to user.email,
                            "contactNumber" to user.contactNumber,
                            "role" to user.role
                        )
                        firestore.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                saveUser(user)
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.localizedMessage ?: "Failed to save profile details")
                            }
                    } else {
                        onFailure("User ID is null")
                    }
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Registration failed")
                }
            }
    }

    fun authenticateUser(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val user = User(
                                        fullName = document.getString("fullName") ?: "",
                                        email = document.getString("email") ?: email,
                                        contactNumber = document.getString("contactNumber") ?: "",
                                        role = document.getString("role") ?: "TECHNICIAN"
                                    )
                                    saveUser(user)
                                    onSuccess(user)
                                } else {
                                    val defaultUser = User(
                                        fullName = auth.currentUser?.displayName ?: "Alex Johnson",
                                        email = email,
                                        contactNumber = "+1 555-0199",
                                        role = "TECHNICIAN"
                                    )
                                    saveUser(defaultUser)
                                    onSuccess(defaultUser)
                                }
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.localizedMessage ?: "Failed to retrieve profile details")
                            }
                    } else {
                        onFailure("User ID is null")
                    }
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Authentication failed")
                }
            }
    }

    fun updatePassword(
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure(task.exception?.localizedMessage ?: "Failed to update password")
                    }
                }
        } else {
            onFailure("User is not signed in")
        }
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Failed to send reset email")
                }
            }
    }

    fun getUser(): User {
        val userStr = prefs.getString(KEY_USER, null) ?: return User("Alex Johnson", "alex@fieldservice.com", "+1 555-0199", "TECHNICIAN")
        val obj = JSONObject(userStr)
        return User(
            fullName = obj.getString("fullName"),
            email = obj.getString("email"),
            contactNumber = obj.getString("contactNumber"),
            role = obj.getString("role")
        )
    }

    fun saveUser(user: User) {
        val obj = JSONObject()
        obj.put("fullName", user.fullName)
        obj.put("email", user.email)
        obj.put("contactNumber", user.contactNumber)
        obj.put("role", user.role)
        prefs.edit().putString(KEY_USER, obj.toString()).apply()
    }

    // Jobs
    fun getJobs(): List<ServiceRequest> {
        val jobsStr = prefs.getString(KEY_JOBS, null) ?: return emptyList()
        val list = mutableListOf<ServiceRequest>()
        val arr = JSONArray(jobsStr)
        for (i in 0 until arr.length()) {
            list.add(jsonToServiceRequest(arr.getJSONObject(i)))
        }
        return list
    }

    fun saveJobs(jobs: List<ServiceRequest>) {
        val arr = JSONArray()
        for (job in jobs) {
            arr.put(serviceRequestToJSON(job))
        }
        prefs.edit().putString(KEY_JOBS, arr.toString()).apply()
    }

    fun updateJobStatus(jobId: String, status: JobStatus) {
        val jobs = getJobs().toMutableList()
        val index = jobs.indexOfFirst { it.id == jobId }
        if (index != -1) {
            jobs[index].status = status
            saveJobs(jobs)
            
            // Generate notification for status update
            addNotification(
                title = "Job Status Updated",
                description = "Job ${jobId} status is now ${status.name.replace("_", " ")}.",
                icon = NotificationIcon.CLOCK
            )
        }
    }

    fun submitServiceReport(jobId: String, findings: String, actionsTaken: String, remarks: String) {
        val jobs = getJobs().toMutableList()
        val index = jobs.indexOfFirst { it.id == jobId }
        if (index != -1) {
            val sdf = SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a", Locale.getDefault())
            val currentTimestamp = sdf.format(Date())
            
            jobs[index].status = JobStatus.COMPLETED
            jobs[index].findings = findings
            jobs[index].actionsTaken = actionsTaken
            jobs[index].completionRemarks = remarks
            jobs[index].reportTimestamp = currentTimestamp
            saveJobs(jobs)
            
            // Add notification
            addNotification(
                title = "Service Report Submitted",
                description = "Report for ${jobId} has been successfully saved.",
                icon = NotificationIcon.BRIEFCASE
            )
        }
    }

    // Notifications
    fun getNotifications(): List<AlertNotification> {
        val notifStr = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        val list = mutableListOf<AlertNotification>()
        val arr = JSONArray(notifStr)
        for (i in 0 until arr.length()) {
            list.add(jsonToNotification(arr.getJSONObject(i)))
        }
        return list
    }

    fun saveNotifications(notifications: List<AlertNotification>) {
        val arr = JSONArray()
        for (notif in notifications) {
            arr.put(notificationToJSON(notif))
        }
        prefs.edit().putString(KEY_NOTIFICATIONS, arr.toString()).apply()
    }

    fun markNotificationAsRead(notifId: String) {
        val notifications = getNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == notifId }
        if (index != -1) {
            notifications[index].isRead = true
            saveNotifications(notifications)
        }
    }

    fun addNotification(title: String, description: String, icon: NotificationIcon) {
        val notifications = getNotifications().toMutableList()
        val sdf = SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a", Locale.getDefault())
        val newNotif = AlertNotification(
            id = "NOTIF-${1000 + notifications.size + 1}",
            title = title,
            description = description,
            timestamp = sdf.format(Date()),
            iconType = icon,
            isRead = false
        )
        notifications.add(0, newNotif) // Add to top
        saveNotifications(notifications)
    }

    fun clearData() {
        auth.signOut()
        prefs.edit().clear().apply()
        // Re-initialize default session data but keep logged out
        val defaultUser = User(
            fullName = "Alex Johnson",
            email = "alex@fieldservice.com",
            contactNumber = "+1 555-0199",
            role = "TECHNICIAN"
        )
        saveUser(defaultUser)
        saveJobs(getMockJobs())
        saveNotifications(getMockNotifications())
    }

    // Helpers JSON
    private fun serviceRequestToJSON(req: ServiceRequest): JSONObject {
        val obj = JSONObject()
        obj.put("id", req.id)
        obj.put("customerName", req.customerName)
        obj.put("serviceType", req.serviceType)
        obj.put("issueDescription", req.issueDescription)
        obj.put("status", req.status.name)
        obj.put("assignedTechnician", req.assignedTechnician)
        obj.put("serviceDate", req.serviceDate)
        obj.put("serviceTime", req.serviceTime)
        obj.put("contactNumber", req.contactNumber)
        obj.put("location", req.location)
        obj.put("findings", req.findings ?: JSONObject.NULL)
        obj.put("actionsTaken", req.actionsTaken ?: JSONObject.NULL)
        obj.put("completionRemarks", req.completionRemarks ?: JSONObject.NULL)
        obj.put("reportTimestamp", req.reportTimestamp ?: JSONObject.NULL)
        return obj
    }

    private fun jsonToServiceRequest(obj: JSONObject): ServiceRequest {
        return ServiceRequest(
            id = obj.getString("id"),
            customerName = obj.getString("customerName"),
            serviceType = obj.getString("serviceType"),
            issueDescription = obj.getString("issueDescription"),
            status = JobStatus.valueOf(obj.getString("status")),
            assignedTechnician = obj.getString("assignedTechnician"),
            serviceDate = obj.getString("serviceDate"),
            serviceTime = obj.getString("serviceTime"),
            contactNumber = obj.getString("contactNumber"),
            location = obj.getString("location"),
            findings = if (obj.isNull("findings")) null else obj.getString("findings"),
            actionsTaken = if (obj.isNull("actionsTaken")) null else obj.getString("actionsTaken"),
            completionRemarks = if (obj.isNull("completionRemarks")) null else obj.getString("completionRemarks"),
            reportTimestamp = if (obj.isNull("reportTimestamp")) null else obj.getString("reportTimestamp")
        )
    }

    private fun notificationToJSON(notif: AlertNotification): JSONObject {
        val obj = JSONObject()
        obj.put("id", notif.id)
        obj.put("title", notif.title)
        obj.put("description", notif.description)
        obj.put("timestamp", notif.timestamp)
        obj.put("iconType", notif.iconType.name)
        obj.put("isRead", notif.isRead)
        return obj
    }

    private fun jsonToNotification(obj: JSONObject): AlertNotification {
        return AlertNotification(
            id = obj.getString("id"),
            title = obj.getString("title"),
            description = obj.getString("description"),
            timestamp = obj.getString("timestamp"),
            iconType = NotificationIcon.valueOf(obj.getString("iconType")),
            isRead = obj.getBoolean("isRead")
        )
    }

    // Mock Data Generator
    private fun getMockJobs(): List<ServiceRequest> {
        return listOf(
            ServiceRequest(
                id = "REQ-1001",
                customerName = "Sarah Connor",
                serviceType = "Internet Installation",
                issueDescription = "New fiber optic installation requested for residential property.",
                status = JobStatus.ASSIGNED,
                assignedTechnician = "Alex Johnson",
                serviceDate = "6/23/2026",
                serviceTime = "08:16 AM",
                contactNumber = "+1 555-1029",
                location = "123 Tech Blvd, Cityville"
            ),
            ServiceRequest(
                id = "REQ-1002",
                customerName = "John Smith",
                serviceType = "Appliance Repair",
                issueDescription = "HVAC unit making unusual noises and not cooling properly.",
                status = JobStatus.IN_PROGRESS,
                assignedTechnician = "Alex Johnson",
                serviceDate = "6/23/2026",
                serviceTime = "07:16 AM",
                contactNumber = "+1 555-3049",
                location = "456 Business Rd, Townsburg"
            ),
            ServiceRequest(
                id = "REQ-1003",
                customerName = "Acme Corp.",
                serviceType = "Network Setup",
                issueDescription = "Set up office local network and Wi-Fi access points.",
                status = JobStatus.COMPLETED,
                assignedTechnician = "Alex Johnson",
                serviceDate = "6/22/2026",
                serviceTime = "02:30 PM",
                contactNumber = "+1 555-9088",
                location = "789 Corporate Way, Metro City",
                findings = "Router was misconfigured and fiber link was down.",
                actionsTaken = "Reconfigured router, patched fiber lines, and validated 1Gbps throughput.",
                completionRemarks = "Successfully resolved. Client confirmed all systems online.",
                reportTimestamp = "06/22/2026, 03:45:10 PM"
            ),
            ServiceRequest(
                id = "REQ-1005",
                customerName = "Global Logistics",
                serviceType = "Security System",
                issueDescription = "Install new CCTV cameras in the main loading dock.",
                status = JobStatus.PENDING,
                assignedTechnician = "Alex Johnson",
                serviceDate = "6/25/2026",
                serviceTime = "08:52 AM",
                contactNumber = "+1 555-4433",
                location = "Warehouse 4, Industrial Park"
            )
        )
    }

    private fun getMockNotifications(): List<AlertNotification> {
        return listOf(
            AlertNotification(
                id = "NOTIF-1",
                title = "New Job Assigned",
                description = "You have been assigned REQ-1003 for Acme Corp.",
                timestamp = "6/23/2026, 8:16:21 AM",
                iconType = NotificationIcon.BRIEFCASE,
                isRead = false
            ),
            AlertNotification(
                id = "NOTIF-2",
                title = "Reminder: Upcoming Job",
                description = "REQ-1001 starts in 2 hours.",
                timestamp = "6/23/2026, 7:16:21 AM",
                iconType = NotificationIcon.CLOCK,
                isRead = true
            )
        )
    }
}

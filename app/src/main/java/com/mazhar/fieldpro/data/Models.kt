package com.mazhar.fieldpro.data

enum class JobStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    REJECTED
}

enum class NotificationIcon {
    BRIEFCASE,
    CLOCK
}

data class User(
    val fullName: String,
    val email: String,
    val contactNumber: String,
    val role: String,
    val expertise: String = ""
)

data class ServiceRequest(
    val id: String,
    val customerName: String,
    val serviceType: String,
    val issueDescription: String,
    var status: JobStatus,
    val assignedTechnician: String,
    val serviceDate: String,
    val serviceTime: String,
    val contactNumber: String,
    val location: String,
    var findings: String? = null,
    var actionsTaken: String? = null,
    var completionRemarks: String? = null,
    var reportTimestamp: String? = null,
    var createdTimestamp: String? = null,
    var assignedTimestamp: String? = null,
    var inProgressTimestamp: String? = null,
    var evidenceImageBase64: String? = null,
    var startLatitude: Double? = null,
    var startLongitude: Double? = null,
    var completionLatitude: Double? = null,
    var completionLongitude: Double? = null
)

data class AlertNotification(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val iconType: NotificationIcon,
    var isRead: Boolean
)

# Keep data models used for Firestore serialization to avoid reflection crashes
-keep class com.mazhar.fieldpro.data.ServiceRequest { *; }
-keep class com.mazhar.fieldpro.data.User { *; }
-keep class com.mazhar.fieldpro.data.AlertNotification { *; }
-keep class com.mazhar.fieldpro.data.JobStatus { *; }

# Keep Firebase / Firestore internal classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

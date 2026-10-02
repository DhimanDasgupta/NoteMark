# Keep app network DTO names/members stable for JSON serialization safety.
# If all DTOs are @Serializable and referenced directly, R8 can often shrink safely,
# but this avoids release-only serialization surprises.
-keep class com.dhimandasgupta.notemark.data.remote.model.** { *; }

# Keep protobuf generated message classes used by DataStore/protobuf-lite.
-keep class com.dhimandasgupta.notemark.proto.** { *; }

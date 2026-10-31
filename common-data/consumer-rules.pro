# The @Serializable network models need no rules: kotlinx-serialization bundles its own.

# protobuf-lite resolves message fields reflectively by name (e.g. "syncing_"), so keep the
# generated fields of the DataStore proto messages. Methods and builders stay optimizable.
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

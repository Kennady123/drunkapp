// Firebase Functions v2
const { onValueCreated } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");
const { getDatabase } = require("firebase-admin/database");
const haversine = require("haversine-distance");

// Initialize Firebase Admin
initializeApp({
  databaseURL: "https://drunkalertapp-default-rtdb.asia-southeast1.firebasedatabase.app/"
});

// Trigger when a new alert is created
exports.onAlertCreated = onValueCreated(
  { ref: "/locations/{userId}/alerts/{alertId}", region: "asia-southeast1" },
  async (event) => {
    const senderId = event.params.userId;
    const alertId = event.params.alertId;
    const alert = event.data.val();

    if (!alert || !alert.lat || !alert.lon) {
      console.log("❌ Invalid alert data");
      return null;
    }

    const { lat: alertLat, lon: alertLon, message: alertMessage } = alert;

    try {
      const db = getDatabase();
      const usersSnapshot = await db.ref("/locations").once("value");
      const users = usersSnapshot.val() || {};
      const nearbyTokens = [];

      for (const userId in users) {
        if (userId === senderId) continue;
        const user = users[userId];
        if (!user.latitude || !user.longitude || !user.fcmToken) continue;

        const distance = haversine(
          { lat: alertLat, lon: alertLon },
          { lat: user.latitude, lon: user.longitude }
        );

        if (distance <= 5000) {
          nearbyTokens.push(user.fcmToken);
          console.log(`✅ User ${userId} is ${distance}m away`);
        }
      }

      if (nearbyTokens.length === 0) {
        console.log("ℹ️ No nearby users found.");
        return null;
      }

      // ✅ Send DATA-ONLY messages
      const messagePayload = {
        data: {
          title: "🚨 Nearby Alert",
          message: alertMessage || "Emergency reported nearby",
          lat: String(alertLat),
          lon: String(alertLon),
          alertId: alertId,
          type: "alert"
        },
        tokens: nearbyTokens
      };

      const response = await getMessaging().sendEachForMulticast(messagePayload);
      console.log(`📢 Notifications sent. Success: ${response.successCount}, Failures: ${response.failureCount}`);

      return null;
    } catch (err) {
      console.error("❌ Error sending alert:", err);
      return null;
    }
  }
);

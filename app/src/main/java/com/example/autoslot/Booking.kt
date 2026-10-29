package com.example.autoslot

import android.content.Intent
import org.json.JSONObject

/** Data satu booking antrean servis. */
data class Booking(
    val code: String,
    val owner: String,
    val plate: String,
    val vehicleType: String,
    val serviceName: String,
    val durationMinutes: Int,
    val price: Int,
    val slot: String,
    val queueNumber: Int,
    val createdAt: Long
) {

    /** Memasukkan seluruh data ke Intent (explicit intent + extras). */
    fun putInto(intent: Intent): Intent = intent.apply {
        putExtra(EXTRA_CODE, code)
        putExtra(EXTRA_OWNER, owner)
        putExtra(EXTRA_PLATE, plate)
        putExtra(EXTRA_VEHICLE, vehicleType)
        putExtra(EXTRA_SERVICE, serviceName)
        putExtra(EXTRA_DURATION, durationMinutes)
        putExtra(EXTRA_PRICE, price)
        putExtra(EXTRA_SLOT, slot)
        putExtra(EXTRA_QUEUE, queueNumber)
        putExtra(EXTRA_CREATED, createdAt)
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("code", code)
        put("owner", owner)
        put("plate", plate)
        put("vehicle", vehicleType)
        put("service", serviceName)
        put("duration", durationMinutes)
        put("price", price)
        put("slot", slot)
        put("queue", queueNumber)
        put("created", createdAt)
    }

    fun toShareText(): String = buildString {
        appendLine("TIKET ANTREAN AUTOSLOT")
        appendLine("Kode Booking : $code")
        appendLine("Antrean No.  : ${"%02d".format(queueNumber)}")
        appendLine("Pemilik      : $owner")
        appendLine("Plat Nomor   : $plate ($vehicleType)")
        appendLine("Layanan      : $serviceName")
        appendLine("Jam Datang   : $slot")
        appendLine("Durasi       : ${Catalog.formatDuration(durationMinutes)}")
        append("Estimasi Biaya: ${Catalog.formatRupiah(price)}")
    }

    companion object {
        const val EXTRA_CODE = "extra_code"
        const val EXTRA_OWNER = "extra_owner"
        const val EXTRA_PLATE = "extra_plate"
        const val EXTRA_VEHICLE = "extra_vehicle"
        const val EXTRA_SERVICE = "extra_service"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_SLOT = "extra_slot"
        const val EXTRA_QUEUE = "extra_queue"
        const val EXTRA_CREATED = "extra_created"

        fun fromIntent(intent: Intent): Booking? {
            val code = intent.getStringExtra(EXTRA_CODE) ?: return null
            return Booking(
                code = code,
                owner = intent.getStringExtra(EXTRA_OWNER).orEmpty(),
                plate = intent.getStringExtra(EXTRA_PLATE).orEmpty(),
                vehicleType = intent.getStringExtra(EXTRA_VEHICLE).orEmpty(),
                serviceName = intent.getStringExtra(EXTRA_SERVICE).orEmpty(),
                durationMinutes = intent.getIntExtra(EXTRA_DURATION, 0),
                price = intent.getIntExtra(EXTRA_PRICE, 0),
                slot = intent.getStringExtra(EXTRA_SLOT).orEmpty(),
                queueNumber = intent.getIntExtra(EXTRA_QUEUE, 1),
                createdAt = intent.getLongExtra(EXTRA_CREATED, System.currentTimeMillis())
            )
        }

        fun fromJson(o: JSONObject): Booking = Booking(
            code = o.getString("code"),
            owner = o.optString("owner"),
            plate = o.optString("plate"),
            vehicleType = o.optString("vehicle"),
            serviceName = o.optString("service"),
            durationMinutes = o.optInt("duration"),
            price = o.optInt("price"),
            slot = o.optString("slot"),
            queueNumber = o.optInt("queue", 1),
            createdAt = o.optLong("created")
        )
    }
}

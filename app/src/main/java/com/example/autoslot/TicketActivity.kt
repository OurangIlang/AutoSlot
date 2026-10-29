package com.example.autoslot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TicketActivity : AppCompatActivity() {

    private lateinit var booking: Booking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Menerima data dari Intent.
        val data = Booking.fromIntent(intent)
        if (data == null) {
            Toast.makeText(this, R.string.toast_invalid_ticket, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        booking = data

        bindTicket()

        findViewById<Button>(R.id.btnMore).setOnClickListener { showMorePopup(it) }
        findViewById<Button>(R.id.btnNewBooking).setOnClickListener { startNewBooking() }
    }

    private fun bindTicket() {
        findViewById<TextView>(R.id.tvBookingCode).text = booking.code
        findViewById<TextView>(R.id.tvQueue).text = getString(
            R.string.ticket_queue_format,
            "%02d".format(booking.queueNumber),
            booking.slot
        )

        val customer = findViewById<LinearLayout>(R.id.llCustomer)
        addRow(customer, getString(R.string.row_owner), booking.owner)
        addRow(customer, getString(R.string.row_plate), booking.plate)
        addRow(customer, getString(R.string.row_vehicle), booking.vehicleType)

        val service = findViewById<LinearLayout>(R.id.llService)
        addRow(service, getString(R.string.row_package), booking.serviceName)
        addRow(service, getString(R.string.row_slot), booking.slot)
        addRow(service, getString(R.string.row_duration), Catalog.formatDuration(booking.durationMinutes))
        addRow(service, getString(R.string.row_created), Catalog.formatDate(booking.createdAt))

        val cost = findViewById<LinearLayout>(R.id.llCost)
        addRow(cost, getString(R.string.row_total), Catalog.formatRupiah(booking.price))
    }

    private fun addRow(container: LinearLayout, label: String, value: String) {
        val row = layoutInflater.inflate(R.layout.item_ticket_row, container, false)
        row.findViewById<TextView>(R.id.tvRowLabel).text = label
        row.findViewById<TextView>(R.id.tvRowValue).text = value
        container.addView(row)
    }

    // ---- Popup menu: Bagikan / Salin ----
    private fun showMorePopup(anchor: View) {
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(R.menu.menu_popup_ticket, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.pop_share -> {
                        shareTicket()
                        true
                    }
                    R.id.pop_copy -> {
                        copyCode()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    /** Implicit Intent (ACTION_SEND) untuk membagikan tiket ke aplikasi lain. */
    private fun shareTicket() {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Tiket AutoSlot ${booking.code}")
            putExtra(Intent.EXTRA_TEXT, booking.toShareText())
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)))
    }

    private fun copyCode() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("booking_code", booking.code))
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }

    /** Kembali ke halaman depan dengan form kosong (MainActivity dibuat ulang). */
    private fun startNewBooking() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

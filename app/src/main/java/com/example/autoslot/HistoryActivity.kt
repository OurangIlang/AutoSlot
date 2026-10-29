package com.example.autoslot

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/** Menampilkan riwayat booking tersimpan (SharedPreferences) memakai ListView. */
class HistoryActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var listView: ListView
    private lateinit var tvEmpty: TextView
    private var items: List<Booking> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        prefs = PrefsManager(this)
        listView = findViewById(R.id.listHistory)
        tvEmpty = findViewById(R.id.tvEmpty)
        listView.emptyView = tvEmpty

        // Context menu pada item ListView (tekan lama).
        registerForContextMenu(listView)
        listView.setOnItemClickListener { _, _, position, _ -> openTicket(items[position]) }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        items = prefs.getHistory()
        val data = items.map {
            mapOf(
                "title" to "${it.code}  (No. ${"%02d".format(it.queueNumber)})",
                "sub" to "${it.owner} | ${it.serviceName} | Jam ${it.slot}"
            )
        }
        listView.adapter = SimpleAdapter(
            this,
            data,
            android.R.layout.simple_list_item_2,
            arrayOf("title", "sub"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
    }

    private fun openTicket(booking: Booking) {
        startActivity(booking.putInto(Intent(this, TicketActivity::class.java)))
    }

    // ---- Options menu ----
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.action_clear_history -> {
            confirmClearHistory()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // ---- Context menu ----
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val info = menuInfo as? AdapterView.AdapterContextMenuInfo ?: return
        if (v.id == R.id.listHistory) {
            menu.setHeaderTitle(items[info.position].code)
            menuInflater.inflate(R.menu.menu_context_history, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as? AdapterView.AdapterContextMenuInfo
            ?: return super.onContextItemSelected(item)
        val booking = items.getOrNull(info.position) ?: return true
        return when (item.itemId) {
            R.id.ctx_view_ticket -> {
                openTicket(booking)
                true
            }
            R.id.ctx_delete -> {
                prefs.removeBooking(booking.code)
                refreshList()
                Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_clear_history_title)
            .setMessage(R.string.confirm_clear_history_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                prefs.clearHistory()
                refreshList()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}

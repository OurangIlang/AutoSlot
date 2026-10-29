package com.example.autoslot

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var auth: AuthManager
    private lateinit var prefs: PrefsManager

    private lateinit var tvFormTitle: TextView
    private lateinit var etName: EditText
    private lateinit var etPlate: EditText
    private lateinit var rgVehicle: RadioGroup
    private lateinit var spService: Spinner
    private lateinit var spSlot: Spinner
    private lateinit var tvPreview: TextView
    private lateinit var cbRemember: CheckBox
    private lateinit var btnQuick: Button
    private lateinit var btnReset: Button
    private lateinit var btnBook: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = AuthManager(this)
        prefs = PrefsManager(this)
        bindViews()

        // Halaman utama hanya untuk pengguna yang sudah login.
        if (!auth.isLoggedIn) {
            goToLogin()
            return
        }
        supportActionBar?.subtitle = getString(R.string.greeting_format, auth.getFullName())
        setupListeners()

        // Context menu: tekan lama pada judul form.
        registerForContextMenu(tvFormTitle)

        // Isi otomatis dari SharedPreferences hanya saat start baru (bukan saat rotasi/ganti tema).
        if (savedInstanceState == null && prefs.rememberMe) {
            restoreLastCustomer()
        }
        updatePreview()
        showOnboardingIfNeeded()
    }

    // ------------------------------------------------------------------ setup

    private fun bindViews() {
        tvFormTitle = findViewById(R.id.tvFormTitle)
        etName = findViewById(R.id.etName)
        etPlate = findViewById(R.id.etPlate)
        rgVehicle = findViewById(R.id.rgVehicle)
        spService = findViewById(R.id.spService)
        spSlot = findViewById(R.id.spSlot)
        tvPreview = findViewById(R.id.tvPreview)
        cbRemember = findViewById(R.id.cbRemember)
        btnQuick = findViewById(R.id.btnQuick)
        btnReset = findViewById(R.id.btnReset)
        btnBook = findViewById(R.id.btnBook)
    }

    private fun setupListeners() {
        spService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePreview()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        btnQuick.setOnClickListener { showQuickPopup(it) }
        btnReset.setOnClickListener { resetForm() }
        btnBook.setOnClickListener { submitBooking() }
    }

    // ------------------------------------------------------------------ form

    private fun selectedVehicle(): String =
        if (rgVehicle.checkedRadioButtonId == R.id.rbMotor) {
            getString(R.string.vehicle_motor)
        } else {
            getString(R.string.vehicle_car)
        }

    private fun setVehicle(vehicle: String) {
        rgVehicle.check(if (vehicle == getString(R.string.vehicle_motor)) R.id.rbMotor else R.id.rbMobil)
    }

    private fun updatePreview() {
        val index = spService.selectedItemPosition - 1 // posisi 0 = placeholder
        val service = Catalog.services.getOrNull(index)
        tvPreview.text = if (service == null) {
            getString(R.string.preview_empty)
        } else {
            getString(
                R.string.preview_format,
                service.name,
                Catalog.formatDuration(service.durationMinutes),
                Catalog.formatRupiah(service.price)
            )
        }
    }

    private fun resetForm() {
        etName.text.clear()
        etPlate.text.clear()
        etName.error = null
        etPlate.error = null
        rgVehicle.check(R.id.rbMobil)
        spService.setSelection(0)
        spSlot.setSelection(0)
        etName.requestFocus()
    }

    private fun fillSample(vehicle: String) {
        if (vehicle == getString(R.string.vehicle_motor)) {
            etName.setText("Siti Aminah")
            etPlate.setText("D 5678 AB")
        } else {
            etName.setText("Budi Santoso")
            etPlate.setText("B 1234 XYZ")
        }
        setVehicle(vehicle)
    }

    private fun restoreLastCustomer() {
        cbRemember.isChecked = true
        etName.setText(prefs.lastOwner)
        etPlate.setText(prefs.lastPlate)
        if (prefs.lastVehicle.isNotEmpty()) setVehicle(prefs.lastVehicle)
    }

    /** Validasi seluruh isian. Mengembalikan true bila semua valid. */
    private fun validateForm(): Boolean {
        var valid = true
        var firstInvalid: View? = null

        val name = etName.text.toString().trim()
        val plate = Catalog.normalizePlate(etPlate.text.toString())

        when {
            name.isEmpty() -> {
                etName.error = getString(R.string.error_owner_empty)
                firstInvalid = etName
                valid = false
            }
            name.length < 3 -> {
                etName.error = getString(R.string.error_owner_short)
                firstInvalid = etName
                valid = false
            }
            else -> etName.error = null
        }

        when {
            plate.isEmpty() -> {
                etPlate.error = getString(R.string.error_plate_empty)
                if (firstInvalid == null) firstInvalid = etPlate
                valid = false
            }
            !Catalog.isPlateValid(plate) -> {
                etPlate.error = getString(R.string.error_plate_invalid)
                if (firstInvalid == null) firstInvalid = etPlate
                valid = false
            }
            else -> etPlate.error = null
        }

        if (spService.selectedItemPosition == 0) {
            (spService.selectedView as? TextView)?.error = getString(R.string.error_service_empty)
            if (firstInvalid == null) firstInvalid = spService
            valid = false
        }

        if (spSlot.selectedItemPosition == 0) {
            (spSlot.selectedView as? TextView)?.error = getString(R.string.error_slot_empty)
            if (firstInvalid == null) firstInvalid = spSlot
            valid = false
        }

        if (!valid) {
            Toast.makeText(this, R.string.toast_form_invalid, Toast.LENGTH_SHORT).show()
            firstInvalid?.requestFocus()
        }
        return valid
    }

    private fun submitBooking() {
        if (!validateForm()) return

        val owner = etName.text.toString().trim()
        val plate = Catalog.normalizePlate(etPlate.text.toString())
        val vehicle = selectedVehicle()
        val service = Catalog.services[spService.selectedItemPosition - 1]
        val slot = spSlot.selectedItem.toString()
        val code = Catalog.makeBookingCode(slot, plate)

        val history = prefs.getHistory()
        if (history.any { it.code == code }) {
            etPlate.error = getString(R.string.error_duplicate_booking)
            etPlate.requestFocus()
            return
        }

        val booking = Booking(
            code = code,
            owner = owner,
            plate = plate,
            vehicleType = vehicle,
            serviceName = service.name,
            durationMinutes = service.durationMinutes,
            price = service.price,
            slot = slot,
            queueNumber = history.count { it.slot == slot } + 1,
            createdAt = System.currentTimeMillis()
        )

        // SharedPreferences: simpan riwayat + (opsional) data pelanggan terakhir.
        prefs.addBooking(booking)
        prefs.rememberMe = cbRemember.isChecked
        if (cbRemember.isChecked) {
            prefs.saveLastCustomer(owner, plate, vehicle)
        }

        // Explicit Intent -> TicketActivity.
        startActivity(booking.putInto(Intent(this, TicketActivity::class.java)))
    }

    // ------------------------------------------------------------------ options menu

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_dark_mode)?.isChecked = prefs.darkMode
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_history -> {
            startActivity(Intent(this, HistoryActivity::class.java))
            true
        }
        R.id.action_dark_mode -> {
            val enable = !item.isChecked
            item.isChecked = enable
            prefs.darkMode = enable
            AppCompatDelegate.setDefaultNightMode(
                if (enable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            true
        }
        R.id.action_clear_data -> {
            confirmClearData()
            true
        }
        R.id.action_logout -> {
            confirmLogout()
            true
        }
        R.id.action_about -> {
            showAbout()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // ------------------------------------------------------------------ context menu

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id == R.id.tvFormTitle) {
            menu.setHeaderTitle(R.string.context_title)
            menuInflater.inflate(R.menu.menu_context_form, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.ctx_clear_form -> {
            resetForm()
            true
        }
        R.id.ctx_fill_sample -> {
            fillSample(getString(R.string.vehicle_car))
            true
        }
        else -> super.onContextItemSelected(item)
    }

    // ------------------------------------------------------------------ popup menu

    private fun showQuickPopup(anchor: View) {
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(R.menu.menu_popup_quick, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.pop_use_last -> {
                        if (prefs.lastOwner.isEmpty()) {
                            Toast.makeText(this@MainActivity, R.string.toast_no_last_data, Toast.LENGTH_SHORT).show()
                        } else {
                            restoreLastCustomer()
                        }
                        true
                    }
                    R.id.pop_sample_car -> {
                        fillSample(getString(R.string.vehicle_car))
                        true
                    }
                    R.id.pop_sample_motor -> {
                        fillSample(getString(R.string.vehicle_motor))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    // ------------------------------------------------------------------ dialogs

    private fun showOnboardingIfNeeded() {
        if (prefs.onboardingDone) return
        AlertDialog.Builder(this)
            .setTitle(R.string.onboarding_title)
            .setMessage(R.string.onboarding_message)
            .setPositiveButton(R.string.ok, null)
            .show()
        prefs.onboardingDone = true
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_logout_title)
            .setMessage(R.string.confirm_logout_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                auth.logout()
                goToLogin()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    private fun confirmClearData() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_clear_title)
            .setMessage(R.string.confirm_clear_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                prefs.clearAll()
                cbRemember.isChecked = false
                resetForm()
                invalidateOptionsMenu()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, R.string.toast_data_cleared, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}

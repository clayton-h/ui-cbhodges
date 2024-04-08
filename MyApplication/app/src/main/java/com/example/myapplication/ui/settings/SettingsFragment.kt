package com.example.myapplication.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.myapplication.R
import java.util.Locale

class SettingsFragment : Fragment() {

    private lateinit var switchNotifications: SwitchCompat
    private lateinit var radioGroup: RadioGroup
    private lateinit var languageSpinner: Spinner
    private var isInitialSpinnerSelection = true
    private var selectedTheme: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        switchNotifications = view.findViewById(R.id.switch_notifications)
        radioGroup = view.findViewById(R.id.radio_group)
        languageSpinner = view.findViewById(R.id.language_spinner)

        initSwitch()
        initRadioGroup()
        initSpinner()

        return view
    }

    private fun initSwitch() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        updateNotificationsLabel(switchNotifications.isChecked)

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            updateNotificationsLabel(isChecked)
        }
    }

    private fun updateNotificationsLabel(isChecked: Boolean) {
        val notificationsLabel = view?.findViewById<TextView>(R.id.text_notifications_label)
        notificationsLabel?.text = getString(
            if (isChecked) R.string.notifications_on else R.string.notifications_off
        )
    }


    private fun initRadioGroup() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        selectedTheme = prefs.getString("theme", "system")?.toIntOrNull() ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        radioGroup.check(
            when (selectedTheme) {
                AppCompatDelegate.MODE_NIGHT_NO -> R.id.radio_button1
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.id.radio_button2
                AppCompatDelegate.MODE_NIGHT_YES -> R.id.radio_button3
                else -> R.id.radio_button2
            }
        )

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedTheme = when (checkedId) {
                R.id.radio_button1 -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radio_button2 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                R.id.radio_button3 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            prefs.edit().putString("theme", selectedTheme.toString()).apply()
            AppCompatDelegate.setDefaultNightMode(selectedTheme)
        }
    }

    private fun initSpinner() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val selectedLanguage = prefs.getString("language", "en") // Use language codes like "en" for English

        val languages = resources.getStringArray(R.array.language_options)
        val languageCodes = arrayOf("en", "es", "fr") // Add corresponding language codes
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        languageSpinner.adapter = languageAdapter

        languageSpinner.setSelection(languageCodes.indexOf(selectedLanguage))

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isInitialSpinnerSelection) {
                    val selectedLangCode = languageCodes[position]
                    prefs.edit().putString("language", selectedLangCode).apply()
                    setLocale(selectedLangCode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateViewWithNewLanguage() {
        // Update the text for the notifications switch
        val notificationsLabel = view?.findViewById<TextView>(R.id.text_notifications_label)
        notificationsLabel?.text = getString(R.string.notifications)

        // Update the text for the theme label
        val themeLabel = view?.findViewById<TextView>(R.id.text_theme_label)
        themeLabel?.text = getString(R.string.theme)

        // Update the text for the radio buttons (theme options)
        val radioButtonLight = view?.findViewById<RadioButton>(R.id.radio_button1)
        radioButtonLight?.text = getString(R.string.light)

        val radioButtonSystem = view?.findViewById<RadioButton>(R.id.radio_button2)
        radioButtonSystem?.text = getString(R.string.system)

        val radioButtonDark = view?.findViewById<RadioButton>(R.id.radio_button3)
        radioButtonDark?.text = getString(R.string.dark)

        // Update the text for the language label
        val languageLabel = view?.findViewById<TextView>(R.id.text_language_label)
        languageLabel?.text = getString(R.string.language)
    }

    private fun setLocale(languageCode: String) {
        val currentLanguage = Locale.getDefault().language
        if (currentLanguage != languageCode) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            activity?.recreate()
        }
    }

    companion object {
        fun applyTheme(fragment: SettingsFragment) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
            val selectedTheme = prefs.getString("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())?.toIntOrNull() ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppCompatDelegate.setDefaultNightMode(selectedTheme)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SettingsFragment.applyTheme(this)

        initSwitch()
        initRadioGroup()
        initSpinner()
        updateViewWithNewLanguage()

        view.post { isInitialSpinnerSelection = false }

        // Set the switch to checked
        switchNotifications.isChecked = true
        updateNotificationsLabel(true)
    }
}
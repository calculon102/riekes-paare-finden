package de.pixelgerecht.riekespaarefinden.android

import android.app.Fragment
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initAvatarSelector(resources.getString(R.string.prefs_style_avatar_player_one))
        initAvatarSelector(resources.getString(R.string.prefs_style_avatar_player_two))
    }

    private fun initAvatarSelector(pref: String) {
        Intent.ACTION_GET_CONTENT
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
                ?: throw IllegalStateException("No shared preferences available!")

            initCardBack(R.string.prefs_style_card_back_key, prefs)

            initAvatarSetting(R.string.prefs_style_avatar_player_one, prefs)
            initAvatarSetting(R.string.prefs_style_avatar_player_two, prefs)
        }

        private fun initCardBack(prefId: Int, prefs: SharedPreferences) {
            val cardBackPref = getPref(prefId)

            val currentVal = prefs.getString(cardBackPref.key, null)
            setAssetAsIcon(cardBackPref, "card-back-$currentVal.png")

            cardBackPref.setOnPreferenceChangeListener { preference, newValue ->
                setAssetAsIcon(preference, "card-back-$newValue.png")
                true
            }

        }

        private fun initAvatarSetting(prefId: Int, prefs: SharedPreferences) {
            val avatarPref = getPref(prefId)

            val currentVal = prefs.getString(avatarPref.key, null)
            setDrawableAsIcon(avatarPref, currentVal)

            avatarPref.setOnPreferenceChangeListener { preference, newValue ->
                setDrawableAsIcon(preference, newValue)
                true
            }
        }

        private fun getPref(prefId: Int) =
            (findPreference(resources.getString(prefId)) as Preference?
                ?: throw IllegalArgumentException("Could not find preference with id $prefId"))

        private fun setDrawableAsIcon(avatarPref: Preference, newValue: Any?) {
            val id = R.drawable::class.java.getField("ic_$newValue").getInt(null)
            avatarPref.icon = resources.getDrawable(id, null)
        }

        private fun setAssetAsIcon(pref: Preference, iconPath: String) {
            pref.icon = Drawable.createFromStream(
                resources.assets.open(iconPath), null
            )
        }
    }
}
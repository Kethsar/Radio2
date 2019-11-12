package io.r_a_d.radio2.preferences

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.preference.*
import io.r_a_d.radio2.R
import io.r_a_d.radio2.alarm.RadioAlarm

class AlarmFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.alarm_preferences, rootKey)

        val timeSet = findPreference<Preference>("timeSet")

        val hourOfDayDefault = 7
        val minuteDefault = 0
        if (!PreferenceManager.getDefaultSharedPreferences(context!!).contains("alarmTimeFromMidnight"))
        {
            PreferenceManager.getDefaultSharedPreferences(context!!).edit {
                putInt("alarmTimeFromMidnight", (60*hourOfDayDefault+minuteDefault))
                commit()
            }
        }
        val time = PreferenceManager.getDefaultSharedPreferences(context!!).getInt("alarmTimeFromMidnight", (60*hourOfDayDefault+minuteDefault))
        val hourOfDay = time / 60
        val minute = time % 60
        timeSet?.summary = "${if (hourOfDay < 10) "0" else ""}$hourOfDay:${if (minute < 10) "0" else ""}$minute"


        timeSet?.setOnPreferenceClickListener {
            val timePickerOnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                PreferenceManager.getDefaultSharedPreferences(context!!).edit {
                    putInt("alarmTimeFromMidnight", (60*hourOfDay+minute))
                    commit()
                }
                timeSet.summary = "${if (hourOfDay < 10) "0" else ""}$hourOfDay:${if (minute < 10) "0" else ""}$minute"
                RadioAlarm.instance.cancelAlarm(context!!)
                RadioAlarm.instance.setNextAlarm(context!!, isForce = true, forceTime = hourOfDay*60+minute)
            }
            val timeNew = PreferenceManager.getDefaultSharedPreferences(context!!).getInt("alarmTimeFromMidnight", 7*60*60) / 60
            val hourOfDayNew = timeNew / 60
            val minuteNew = timeNew % 60
            val timePicker = TimePickerDialog(context, timePickerOnTimeSetListener, hourOfDayNew, minuteNew, true)

            timePicker.show()
            true
        }

        fun updateDays(preference : MultiSelectListPreference?, newValue : Set<String>?)
        {
            Log.d(tag, newValue.toString())
            val listOfDays : String
            val fullWeek = context!!.resources.getStringArray(R.array.weekdays).toSet()
            val workingWeek = context!!.resources.getStringArray(R.array.weekdays).toSet().minusElement("Saturday").minusElement("Sunday")
            listOfDays = when (newValue) {
                fullWeek -> context!!.getString(R.string.every_day)
                workingWeek -> context!!.getString(R.string.working_days)
                else -> {
                    // build ORDERED LIST of days... I don't know why the original one is in shambles!!
                    val fullWeekOrdered = arrayListOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    val selectedDays = arrayListOf<String>()
                    for (item in fullWeekOrdered) {
                        if (newValue!!.contains(item))
                            selectedDays.add(item)
                    }
                    "$selectedDays".drop(1).dropLast(1) // dropping '[' and ']'
                }
            }
            preference?.summary = listOfDays
        }

        val alarmDays = findPreference<MultiSelectListPreference>("alarmDays")

        updateDays(alarmDays, PreferenceManager.getDefaultSharedPreferences(context).getStringSet("alarmDays", setOf()))
        alarmDays?.setOnPreferenceChangeListener { preference, newValue ->
            @Suppress("UNCHECKED_CAST")
            updateDays(preference as MultiSelectListPreference, newValue as Set<String>)
            RadioAlarm.instance.cancelAlarm(context!!)
            RadioAlarm.instance.setNextAlarm(context!!, isForce = true, forceDays = newValue)
            true
        }

        //

        val isWakingUp = findPreference<SwitchPreferenceCompat>("isWakingUp")
        isWakingUp?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                RadioAlarm.instance.setNextAlarm(context!!, isForce = true)
                timeSet?.isEnabled = true
                alarmDays?.isEnabled = true
            } else {
                RadioAlarm.instance.cancelAlarm(context!!)
                timeSet?.isEnabled = false
                alarmDays?.isEnabled = false
            }

            true
        }

        alarmDays?.isEnabled = isWakingUp?.isChecked ?: false
        timeSet?.isEnabled = isWakingUp?.isChecked ?: false
    }


}
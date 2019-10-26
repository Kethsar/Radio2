package io.r_a_d.radio2.ui.nowplaying

import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import io.r_a_d.radio2.PlayerStore
import io.r_a_d.radio2.R

class NowPlayingViewModel: ViewModel() {

    /* Note : ViewModels do not have any kind of data persistence, which is a bit of a shame.
       Data persistence is currently in beta, and poorly documented (some pages don't even match!)
       For the moment, we will store data related to playback state in PlayerStore.
    */

    var seekBarChangeListener: SeekBar.OnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // updated continuously as the user slides the thumb
                PlayerStore.instance.volume.value = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // called when the user first touches the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // called after the user finishes moving the SeekBar
            }
        }

    override fun onCleared() {
        super.onCleared()
    }
}

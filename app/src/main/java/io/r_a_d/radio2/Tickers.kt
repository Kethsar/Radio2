package io.r_a_d.radio2

import android.support.v4.media.session.PlaybackStateCompat
import io.r_a_d.radio2.playerstore.PlayerStore
import java.util.*


class Tick  : TimerTask() {
    override fun run() {
        PlayerStore.instance.currentTime.postValue(PlayerStore.instance.currentTime.value!! + 500)
    }
}

class ApiFetchTick  : TimerTask() {
    private val apiFetchTickTag = "======apiTick======"
    override fun run() {
        if (PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_STOPPED)
        {
            PlayerStore.instance.fetchApi()
            //Log.d(apiFetchTickTag, "mainApiData fetch from object #${this.hashCode()}")
        }
    }
}
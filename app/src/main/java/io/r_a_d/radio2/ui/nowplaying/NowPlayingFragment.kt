package io.r_a_d.radio2.ui.nowplaying

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import io.r_a_d.radio2.*
import io.r_a_d.radio2.playerstore.PlayerStore
import io.r_a_d.radio2.playerstore.Song





class NowPlayingFragment : Fragment() {

    /*
    companion object {
        fun newInstance() = NowPlayingFragment()
    }

     */

    //private val nowPlayingFragmentTag = NowPlayingFragment::class.java.name


    private lateinit var nowPlayingViewModel: NowPlayingViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        nowPlayingViewModel = ViewModelProviders.of(this).get(NowPlayingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_nowplaying, container, false)

        // View bindings to the ViewModel
        val songTitleText: TextView = root.findViewById(R.id.text_song_title)
        val songArtistText: TextView = root.findViewById(R.id.text_song_artist)
        val seekBarVolume: SeekBar = root.findViewById(R.id.seek_bar_volume)
        val volumeText: TextView = root.findViewById(R.id.volume_text)
        val progressBar: ProgressBar = root.findViewById(R.id.progressBar)
        val streamerPictureImageView: ImageView = root.findViewById(R.id.streamerPicture)
        val streamerNameText : TextView = root.findViewById(R.id.streamerName)
        val songTitleNextText: TextView = root.findViewById(R.id.text_song_title_next)
        val songArtistNextText: TextView = root.findViewById(R.id.text_song_artist_next)
        val volumeIconImage : ImageView = root.findViewById(R.id.volume_icon)


        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            streamerNameText,4, 24, 2, TypedValue.COMPLEX_UNIT_SP)
        /*
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            songTitleText,4, 24, 2, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            songArtistText,4, 24, 2, TypedValue.COMPLEX_UNIT_SP)
         */

        PlayerStore.instance.currentSong.title.observe(viewLifecycleOwner, Observer {
                songTitleText.text = it
        })

        PlayerStore.instance.currentSong.artist.observe(viewLifecycleOwner, Observer {
                songArtistText.text = it
        })

        PlayerStore.instance.playbackState.observe(viewLifecycleOwner, Observer {
            syncPlayPauseButtonImage(root)
        })

        // trick : I can't observe the queue because it's an ArrayDeque that doesn't trigger any change...
        // so I observe a dedicated Mutable that gets set when the queue is updated.
        PlayerStore.instance.isQueueUpdated.observe(viewLifecycleOwner, Observer {
            val t = if (PlayerStore.instance.queue.size > 0) PlayerStore.instance.queue[0] else Song("No queue - ") // (it.peekFirst != null ? it.peekFirst : Song() )
            songTitleNextText.text = t.title.value
            songArtistNextText.text = t.artist.value
        })

        PlayerStore.instance.volume.observe(viewLifecycleOwner, Observer {
            volumeText.text = "$it%"
            when {
                it > 66 -> volumeIconImage.setImageResource(R.drawable.ic_volume_high)
                it in 33..66 -> volumeIconImage.setImageResource(R.drawable.ic_volume_medium)
                it in 0..33 -> volumeIconImage.setImageResource(R.drawable.ic_volume_low)
                else -> volumeIconImage.setImageResource(R.drawable.ic_volume_off)
            }
        })

        PlayerStore.instance.isMuted.observe(viewLifecycleOwner, Observer {
            if (it)
                volumeIconImage.setImageResource(R.drawable.ic_volume_off)
            else
                PlayerStore.instance.volume.value = PlayerStore.instance.volume.value // force trigger volume observer
        })


        PlayerStore.instance.streamerPicture.observe(viewLifecycleOwner, Observer { pic ->
            streamerPictureImageView.setImageBitmap(pic)
        })

        PlayerStore.instance.streamerName.observe(viewLifecycleOwner, Observer {
            streamerNameText.text = it
        })

        // fuck it, do it on main thread
        PlayerStore.instance.currentTime.observe(viewLifecycleOwner, Observer {
            val dd = (PlayerStore.instance.currentTime.value!! - PlayerStore.instance.currentSong.startTime.value!!).toInt()
            progressBar.progress = dd
        })

        PlayerStore.instance.currentSong.stopTime.observe(viewLifecycleOwner, Observer {
            val dd = (PlayerStore.instance.currentSong.stopTime.value!! - PlayerStore.instance.currentSong.startTime.value!!).toInt()
            progressBar.max = dd
        })

        PlayerStore.instance.currentSong.stopTime.observe(viewLifecycleOwner, Observer {
            val t : TextView= root.findViewById(R.id.endTime)
            val minutes: String = ((PlayerStore.instance.currentSong.stopTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/60/1000).toString()
            val seconds: String = ((PlayerStore.instance.currentSong.stopTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/1000%60).toString()
            t.text = "$minutes:${if (seconds.toInt() < 10) "0" else ""}$seconds"
        })

        PlayerStore.instance.currentTime.observe(viewLifecycleOwner, Observer {
            val t : TextView= root.findViewById(R.id.currentTime)
            val minutes: String = ((PlayerStore.instance.currentTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/60/1000).toString()
            val seconds: String = ((PlayerStore.instance.currentTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/1000%60).toString()
            t.text = "$minutes:${if (seconds.toInt() < 10) "0" else ""}$seconds"
        })

        seekBarVolume.setOnSeekBarChangeListener(nowPlayingViewModel.seekBarChangeListener)
        seekBarVolume.progress = PlayerStore.instance.volume.value!!
        progressBar.max = 1000
        progressBar.progress = 0

        syncPlayPauseButtonImage(root)

        // initialize the value for isPlaying when displaying the fragment
        PlayerStore.instance.isPlaying.value = PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_PLAYING

        val button: ImageButton = root.findViewById(R.id.play_pause)
        button.setOnClickListener{
            PlayerStore.instance.isPlaying.value = PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_STOPPED
        }

        volumeIconImage.setOnClickListener{
            PlayerStore.instance.isMuted.value = !PlayerStore.instance.isMuted.value!!
        }

        return root
    }

    private fun syncPlayPauseButtonImage(v: View)
    {
        val img = v.findViewById<ImageButton>(R.id.play_pause)

        if (PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_STOPPED) {
            img.setImageResource(R.drawable.exo_controls_play)
        } else {
            img.setImageResource(R.drawable.exo_controls_pause)
        }
    }



}

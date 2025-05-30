package com.example.rhythmease

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.example.rhythmease.R

// Data definition for available instruments
data class Instrument(val name: String, val iconRes: Int? = null)

/**
 * PUBLIC_INTERFACE
 * RhythmEase MainActivity: Lays out sequencer grid, instrument selection, and playback controls.
 * Styled using theme colors and enables beat programming and playback.
 */
class MainActivity : AppCompatActivity() {

    // Sequencer definition
    private val NUM_INSTRUMENTS = 5
    private val NUM_STEPS = 16
    private val instruments = listOf(
        Instrument("Kick"),
        Instrument("Snare"),
        Instrument("Hi-Hat"),
        Instrument("Clap"),
        Instrument("Perc"),
    )

    // 2D Array: instruments x steps (row major order)
    private val sequencerGrid = Array(NUM_INSTRUMENTS) { BooleanArray(NUM_STEPS) { false } }

    private lateinit var gridLayout: GridLayout
    private lateinit var instrumentListLayout: LinearLayout
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var stepIndicators: Array<View>
    private var isPlaying = false
    private var currentStep = 0
    private var tempoBpm = 110 // static for simplicity

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var playbackRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.hide()

        gridLayout = findViewById(R.id.sequencer_grid)
        instrumentListLayout = findViewById(R.id.instrument_list)
        playButton = findViewById(R.id.btn_play)
        pauseButton = findViewById(R.id.btn_pause)
        stopButton = findViewById(R.id.btn_stop)

        setupInstrumentList()
        setupSequencerGrid()
        setupPlaybackControls()

        playbackRunnable = object : Runnable {
            override fun run() {
                if (!isPlaying) return
                highlightStep(currentStep)
                // (Optional: Play sound here for each active cell)
                currentStep = (currentStep + 1) % NUM_STEPS
                handler.postDelayed(this, (60000.0 / tempoBpm / 4).toLong())
            }
        }
    }

    /**
     * Initializes the vertical instrument selection bar.
     */
    private fun setupInstrumentList() {
        instrumentListLayout.removeAllViews()
        for ((index, instrument) in instruments.withIndex()) {
            val btn = Button(this)
            btn.text = instrument.name
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            btn.setTextColor(ContextCompat.getColor(this, R.color.accent))
            btn.textSize = 16f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
            params.setMargins(8, 8, 8, 8)
            btn.layoutParams = params
            btn.isAllCaps = false
            btn.isEnabled = false
            instrumentListLayout.addView(btn)
        }
    }

    /**
     * Initializes the main grid for beat sequencing.
     * Each cell is toggleable and visually styled according to state.
     */
    private fun setupSequencerGrid() {
        gridLayout.removeAllViews()
        gridLayout.columnCount = NUM_STEPS
        gridLayout.rowCount = NUM_INSTRUMENTS

        // Create cells as ToggleButtons
        for (row in 0 until NUM_INSTRUMENTS) {
            for (col in 0 until NUM_STEPS) {
                val cell = ToggleButton(this)
                cell.isChecked = false
                cell.setBackgroundResource(R.drawable.beat_cell_selector)
                cell.setTextColor(ContextCompat.getColor(this, R.color.accent))
                cell.textOn = ""
                cell.textOff = ""
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = GridLayout.LayoutParams.WRAP_CONTENT
                params.columnSpec = GridLayout.spec(col, 1f)
                params.rowSpec = GridLayout.spec(row, 1f)
                params.setMargins(3, 6, 3, 6)
                cell.layoutParams = params
                cell.setOnCheckedChangeListener { _, isChecked ->
                    sequencerGrid[row][col] = isChecked
                }
                gridLayout.addView(cell)
            }
        }
    }

    /**
     * Sets up playback controls and their interactivity.
     * Handles the sequencer timing on Play, Pause, Stop.
     */
    private fun setupPlaybackControls() {
        playButton.setOnClickListener {
            if (!isPlaying) {
                isPlaying = true
                handler.post(playbackRunnable)
                playButton.setColorFilter(ContextCompat.getColor(this, R.color.accent))
                pauseButton.setColorFilter(ContextCompat.getColor(this, R.color.secondary))
                stopButton.setColorFilter(ContextCompat.getColor(this, R.color.secondary))
            }
        }
        pauseButton.setOnClickListener {
            if (isPlaying) {
                isPlaying = false
                playButton.setColorFilter(ContextCompat.getColor(this, R.color.secondary))
                pauseButton.setColorFilter(ContextCompat.getColor(this, R.color.accent))
                stopButton.setColorFilter(ContextCompat.getColor(this, R.color.secondary))
            }
        }
        stopButton.setOnClickListener {
            if (isPlaying || currentStep != 0) {
                isPlaying = false
                currentStep = 0
                highlightStep(-1)
                playButton.setColorFilter(ContextCompat.getColor(this, R.color.secondary))
                pauseButton.setColorFilter(ContextCompat.getColor(this, R.color.secondary))
                stopButton.setColorFilter(ContextCompat.getColor(this, R.color.accent))
            }
        }
    }

    /**
     * Highlights the column currently being played back.
     */
    private fun highlightStep(step: Int) {
        val count = gridLayout.childCount
        for (col in 0 until NUM_STEPS) {
            for (row in 0 until NUM_INSTRUMENTS) {
                val idx = row * NUM_STEPS + col
                val cell = gridLayout.getChildAt(idx)
                if (cell is ToggleButton) {
                    if (col == step) {
                        cell.background.setTint(ContextCompat.getColor(this, R.color.accent))
                    } else {
                        cell.background.setTint(ContextCompat.getColor(this, R.color.cell_bg))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(playbackRunnable)
    }
}

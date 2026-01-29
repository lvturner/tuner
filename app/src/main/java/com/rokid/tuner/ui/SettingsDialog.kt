package com.rokid.tuner.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import com.rokid.tuner.R
import com.rokid.tuner.audio.AudioConfig
import kotlin.math.roundToInt

class SettingsDialog : DialogFragment() {

    companion object {
         private const val ARG_REFERENCE_FREQUENCY = "reference_frequency"
         private const val ARG_SENSITIVITY = "sensitivity"
          private const val ARG_DISPLAY_DELAY_MS = "display_delay_ms"
          private const val ARG_PITCH_UPDATE_DELAY_MS = "pitch_update_delay_ms"
        
          fun newInstance(
              referenceFrequency: Double = AudioConfig.DEFAULT_REFERENCE_FREQUENCY,
              sensitivity: Int = 50,
                displayDelayMs: Long = 1000,
               pitchUpdateDelayMs: Long = 200,
              onReferenceFrequencyChanged: ((Double) -> Unit)? = null,
              onSensitivityChanged: ((Int) -> Unit)? = null,
              onDisplayDelayChanged: ((Long) -> Unit)? = null,
              onPitchUpdateDelayChanged: ((Long) -> Unit)? = null
          ): SettingsDialog {
              val fragment = SettingsDialog()
              fragment.onReferenceFrequencyChanged = onReferenceFrequencyChanged
              fragment.onSensitivityChanged = onSensitivityChanged
              fragment.onDisplayDelayChanged = onDisplayDelayChanged
              fragment.onPitchUpdateDelayChanged = onPitchUpdateDelayChanged
              val args = Bundle().apply {
                  putDouble(ARG_REFERENCE_FREQUENCY, referenceFrequency)
                  putInt(ARG_SENSITIVITY, sensitivity)
                  putLong(ARG_DISPLAY_DELAY_MS, displayDelayMs)
                  putLong(ARG_PITCH_UPDATE_DELAY_MS, pitchUpdateDelayMs)
              }
              fragment.arguments = args
              return fragment
          }
    }

      private var onReferenceFrequencyChanged: ((Double) -> Unit)? = null
      private var onSensitivityChanged: ((Int) -> Unit)? = null
      private var onDisplayDelayChanged: ((Long) -> Unit)? = null
      private var onPitchUpdateDelayChanged: ((Long) -> Unit)? = null

    var onDismissListener: (() -> Unit)? = null

    private lateinit var frequencyEditText: EditText
    private lateinit var frequencySeekBar: SeekBar
    private lateinit var frequencyValueText: TextView
     private lateinit var sensitivitySeekBar: SeekBar
     private lateinit var sensitivityValueText: TextView
      private lateinit var displayDelaySeekBar: SeekBar
      private lateinit var displayDelayValueText: TextView
      private lateinit var pitchUpdateDelaySeekBar: SeekBar
      private lateinit var pitchUpdateDelayValueText: TextView
      private lateinit var rmsValueText: TextView
      private lateinit var closeButton: Button

      private var currentFrequency = AudioConfig.DEFAULT_REFERENCE_FREQUENCY
      private var currentSensitivity = 50 // 0-100 scale
         private var currentDisplayDelayMs = 1000L // 0-1000 ms scale
        private var currentPitchUpdateDelayMs = 200L // 0-1000 ms scale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         arguments?.let {
              currentFrequency = it.getDouble(ARG_REFERENCE_FREQUENCY, AudioConfig.DEFAULT_REFERENCE_FREQUENCY)
              currentSensitivity = it.getInt(ARG_SENSITIVITY, 50)
                currentDisplayDelayMs = it.getLong(ARG_DISPLAY_DELAY_MS, 1000)
               currentPitchUpdateDelayMs = it.getLong(ARG_PITCH_UPDATE_DELAY_MS, 200)
          }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.TunerDialog)
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_settings, null)
        
        builder.setView(view)
        builder.setTitle(R.string.settings)
        
        initViews(view)
        setupListeners()
        
        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun initViews(view: View) {
        frequencyEditText = view.findViewById(R.id.frequency_edit_text)
        frequencySeekBar = view.findViewById(R.id.frequency_seek_bar)
        frequencyValueText = view.findViewById(R.id.frequency_value_text)
         sensitivitySeekBar = view.findViewById(R.id.sensitivity_seek_bar)
         sensitivityValueText = view.findViewById(R.id.sensitivity_value_text)
          displayDelaySeekBar = view.findViewById(R.id.display_delay_seek_bar)
          displayDelayValueText = view.findViewById(R.id.display_delay_value_text)
          pitchUpdateDelaySeekBar = view.findViewById(R.id.pitch_update_delay_seek_bar)
          pitchUpdateDelayValueText = view.findViewById(R.id.pitch_update_delay_value_text)
          rmsValueText = view.findViewById(R.id.rms_value_text)
         closeButton = view.findViewById(R.id.close_button)
         
         // Set initial values
         frequencyEditText.setText(currentFrequency.toString())
         frequencySeekBar.progress = ((currentFrequency - 430) * 2).roundToInt() // 430-450 Hz range
         frequencyValueText.text = "${currentFrequency} Hz"
         
         sensitivitySeekBar.progress = currentSensitivity
         sensitivityValueText.text = "$currentSensitivity%"
         
          // Display delay: 0-1000 ms
          displayDelaySeekBar.progress = currentDisplayDelayMs.coerceIn(0L, 1000L).toInt()
          displayDelayValueText.text = "$currentDisplayDelayMs ms"
          
          // Pitch update delay: 0-1000 ms
          pitchUpdateDelaySeekBar.progress = currentPitchUpdateDelayMs.coerceIn(0L, 1000L).toInt()
          pitchUpdateDelayValueText.text = "$currentPitchUpdateDelayMs ms"
          
          rmsValueText.text = "--"
    }

    private fun setupListeners() {
        frequencyEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateFrequencyFromEditText()
            }
        }
        
        frequencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentFrequency = 430 + (progress / 2.0)
                    frequencyEditText.setText(String.format("%.1f", currentFrequency))
                    frequencyValueText.text = "${String.format("%.1f", currentFrequency)} Hz"
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onReferenceFrequencyChanged?.invoke(currentFrequency)
            }
        })
        
        sensitivitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentSensitivity = progress
                    sensitivityValueText.text = "$progress%"
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Update sensitivity in audio processing
                onSensitivityChanged?.invoke(currentSensitivity)
            }
         })
         
         displayDelaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
             override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                 if (fromUser) {
                     currentDisplayDelayMs = progress.toLong()
                     displayDelayValueText.text = "$progress ms"
                 }
             }
             
             override fun onStartTrackingTouch(seekBar: SeekBar) {}
             override fun onStopTrackingTouch(seekBar: SeekBar) {
                 // Update display delay in main activity
                 onDisplayDelayChanged?.invoke(currentDisplayDelayMs)
             }
          })
          
          pitchUpdateDelaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
              override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                  if (fromUser) {
                      currentPitchUpdateDelayMs = progress.toLong()
                      pitchUpdateDelayValueText.text = "$progress ms"
                  }
              }
              
              override fun onStartTrackingTouch(seekBar: SeekBar) {}
              override fun onStopTrackingTouch(seekBar: SeekBar) {
                  // Update pitch update delay in main activity
                  onPitchUpdateDelayChanged?.invoke(currentPitchUpdateDelayMs)
              }
          })
          
          closeButton.setOnClickListener {
              dismiss()
          }
    }

    private fun updateFrequencyFromEditText() {
        try {
            val newFrequency = frequencyEditText.text.toString().toDouble()
            if (newFrequency in 430.0..450.0) {
                currentFrequency = newFrequency
                frequencySeekBar.progress = ((newFrequency - 430) * 2).roundToInt()
                frequencyValueText.text = "${String.format("%.1f", newFrequency)} Hz"
                onReferenceFrequencyChanged?.invoke(newFrequency)
            } else {
                frequencyEditText.setText(currentFrequency.toString())
            }
        } catch (e: NumberFormatException) {
            frequencyEditText.setText(currentFrequency.toString())
        }
    }

    fun setInitialFrequency(frequency: Double) {
        currentFrequency = frequency.coerceIn(430.0, 450.0)
    }

     fun setInitialSensitivity(sensitivity: Int) {
         currentSensitivity = sensitivity.coerceIn(0, 100)
     }

      fun setInitialDisplayDelay(delayMs: Long) {
          currentDisplayDelayMs = delayMs.coerceIn(0L, 1000L)
      }

      fun setInitialPitchUpdateDelay(delayMs: Long) {
          currentPitchUpdateDelayMs = delayMs.coerceIn(0L, 1000L)
      }

     fun updateRmsValue(rms: Double) {
        if (::rmsValueText.isInitialized) {
            rmsValueText.text = String.format("%.4f", rms)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
}
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
import com.rokid.tuner.constants.UiConstants
import kotlin.math.roundToInt

class SettingsDialog : DialogFragment() {

    companion object {
         private const val ARG_REFERENCE_FREQUENCY = "reference_frequency"
         private const val ARG_SENSITIVITY = "sensitivity"
          private const val ARG_DISPLAY_DELAY_MS = "display_delay_ms"
           private const val ARG_PITCH_UPDATE_DELAY_MS = "pitch_update_delay_ms"
           private const val ARG_IN_TUNE_THRESHOLD_CENTS = "in_tune_threshold_cents"
        
          fun newInstance(
               referenceFrequency: Double = AudioConfig.DEFAULT_REFERENCE_FREQUENCY,
                sensitivity: Int = UiConstants.SETTINGS_DEFAULT_SENSITIVITY,
                 displayDelayMs: Long = UiConstants.DEFAULT_DISPLAY_DELAY_MS,
                 pitchUpdateDelayMs: Long = UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS,
                 inTuneThresholdCents: Double = UiConstants.DEFAULT_IN_TUNE_THRESHOLD_CENTS,
               onReferenceFrequencyChanged: ((Double) -> Unit)? = null,
               onSensitivityChanged: ((Int) -> Unit)? = null,
               onDisplayDelayChanged: ((Long) -> Unit)? = null,
               onPitchUpdateDelayChanged: ((Long) -> Unit)? = null,
               onInTuneThresholdChanged: ((Double) -> Unit)? = null
          ): SettingsDialog {
              val fragment = SettingsDialog()
               fragment.onReferenceFrequencyChanged = onReferenceFrequencyChanged
               fragment.onSensitivityChanged = onSensitivityChanged
               fragment.onDisplayDelayChanged = onDisplayDelayChanged
               fragment.onPitchUpdateDelayChanged = onPitchUpdateDelayChanged
               fragment.onInTuneThresholdChanged = onInTuneThresholdChanged
              val args = Bundle().apply {
                  putDouble(ARG_REFERENCE_FREQUENCY, referenceFrequency)
                  putInt(ARG_SENSITIVITY, sensitivity)
                   putLong(ARG_DISPLAY_DELAY_MS, displayDelayMs)
                   putLong(ARG_PITCH_UPDATE_DELAY_MS, pitchUpdateDelayMs)
                   putDouble(ARG_IN_TUNE_THRESHOLD_CENTS, inTuneThresholdCents)
              }
              fragment.arguments = args
              return fragment
          }
    }

      private var onReferenceFrequencyChanged: ((Double) -> Unit)? = null
      private var onSensitivityChanged: ((Int) -> Unit)? = null
       private var onDisplayDelayChanged: ((Long) -> Unit)? = null
       private var onPitchUpdateDelayChanged: ((Long) -> Unit)? = null
       private var onInTuneThresholdChanged: ((Double) -> Unit)? = null

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
       private lateinit var inTuneThresholdSeekBar: SeekBar
       private lateinit var inTuneThresholdValueText: TextView
       private lateinit var rmsValueText: TextView
       private lateinit var closeButton: Button

      private var currentFrequency = AudioConfig.DEFAULT_REFERENCE_FREQUENCY
       private var currentSensitivity = UiConstants.SETTINGS_DEFAULT_SENSITIVITY // 0-100 scale
           private var currentDisplayDelayMs = UiConstants.DEFAULT_DISPLAY_DELAY_MS // 0-1000 ms scale
          private var currentPitchUpdateDelayMs = UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS // 0-1000 ms scale
          private var currentInTuneThresholdCents = UiConstants.DEFAULT_IN_TUNE_THRESHOLD_CENTS // 0-50 cents scale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         arguments?.let {
              currentFrequency = it.getDouble(ARG_REFERENCE_FREQUENCY, AudioConfig.DEFAULT_REFERENCE_FREQUENCY)
               currentSensitivity = it.getInt(ARG_SENSITIVITY, UiConstants.SETTINGS_DEFAULT_SENSITIVITY)
                  currentDisplayDelayMs = it.getLong(ARG_DISPLAY_DELAY_MS, UiConstants.DEFAULT_DISPLAY_DELAY_MS)
                 currentPitchUpdateDelayMs = it.getLong(ARG_PITCH_UPDATE_DELAY_MS, UiConstants.DEFAULT_PITCH_UPDATE_DELAY_MS)
                 currentInTuneThresholdCents = it.getDouble(ARG_IN_TUNE_THRESHOLD_CENTS, UiConstants.DEFAULT_IN_TUNE_THRESHOLD_CENTS)
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
           inTuneThresholdSeekBar = view.findViewById(R.id.in_tune_threshold_seek_bar)
           inTuneThresholdValueText = view.findViewById(R.id.in_tune_threshold_value_text)
           rmsValueText = view.findViewById(R.id.rms_value_text)
         closeButton = view.findViewById(R.id.close_button)
         
         // Set initial values
         frequencyEditText.setText(currentFrequency.toString())
          frequencySeekBar.progress = ((currentFrequency - UiConstants.MIN_REFERENCE_FREQUENCY) * UiConstants.FREQUENCY_SCALE_FACTOR).roundToInt() // 430-450 Hz range
         frequencyValueText.text = "${currentFrequency} Hz"
         
         sensitivitySeekBar.progress = currentSensitivity
         sensitivityValueText.text = "$currentSensitivity%"
         
          // Display delay: 0-1000 ms
           displayDelaySeekBar.progress = currentDisplayDelayMs.coerceIn(UiConstants.MIN_DISPLAY_DELAY_MS, UiConstants.MAX_DISPLAY_DELAY_MS).toInt()
          displayDelayValueText.text = "$currentDisplayDelayMs ms"
          
          // Pitch update delay: 0-1000 ms
           pitchUpdateDelaySeekBar.progress = currentPitchUpdateDelayMs.coerceIn(UiConstants.MIN_PITCH_UPDATE_DELAY_MS, UiConstants.MAX_PITCH_UPDATE_DELAY_MS).toInt()
           pitchUpdateDelayValueText.text = "$currentPitchUpdateDelayMs ms"
           
           // In-tune threshold: 0-50 cents
           inTuneThresholdSeekBar.progress = currentInTuneThresholdCents.coerceIn(UiConstants.MIN_IN_TUNE_THRESHOLD_CENTS, UiConstants.MAX_IN_TUNE_THRESHOLD_CENTS).roundToInt()
           inTuneThresholdValueText.text = "${currentInTuneThresholdCents.roundToInt()} cents"
           
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
                     currentFrequency = UiConstants.MIN_REFERENCE_FREQUENCY + (progress / UiConstants.FREQUENCY_SCALE_FACTOR)
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
           
           inTuneThresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
               override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                   if (fromUser) {
                       currentInTuneThresholdCents = progress.toDouble()
                       inTuneThresholdValueText.text = "$progress cents"
                   }
               }
               
               override fun onStartTrackingTouch(seekBar: SeekBar) {}
               override fun onStopTrackingTouch(seekBar: SeekBar) {
                   // Update in-tune threshold in main activity
                   onInTuneThresholdChanged?.invoke(currentInTuneThresholdCents)
               }
           })
           
           closeButton.setOnClickListener {
               dismiss()
           }
    }

    private fun updateFrequencyFromEditText() {
        try {
            val newFrequency = frequencyEditText.text.toString().toDouble()
             if (newFrequency in UiConstants.MIN_REFERENCE_FREQUENCY..UiConstants.MAX_REFERENCE_FREQUENCY) {
                currentFrequency = newFrequency
                 frequencySeekBar.progress = ((newFrequency - UiConstants.MIN_REFERENCE_FREQUENCY) * UiConstants.FREQUENCY_SCALE_FACTOR).roundToInt()
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
         currentFrequency = frequency.coerceIn(UiConstants.MIN_REFERENCE_FREQUENCY, UiConstants.MAX_REFERENCE_FREQUENCY)
    }

     fun setInitialSensitivity(sensitivity: Int) {
          currentSensitivity = sensitivity.coerceIn(UiConstants.MIN_SENSITIVITY, UiConstants.MAX_SENSITIVITY)
     }

      fun setInitialDisplayDelay(delayMs: Long) {
           currentDisplayDelayMs = delayMs.coerceIn(UiConstants.MIN_DISPLAY_DELAY_MS, UiConstants.MAX_DISPLAY_DELAY_MS)
      }

       fun setInitialPitchUpdateDelay(delayMs: Long) {
            currentPitchUpdateDelayMs = delayMs.coerceIn(UiConstants.MIN_PITCH_UPDATE_DELAY_MS, UiConstants.MAX_PITCH_UPDATE_DELAY_MS)
       }

       fun setInitialInTuneThreshold(thresholdCents: Double) {
            currentInTuneThresholdCents = thresholdCents.coerceIn(UiConstants.MIN_IN_TUNE_THRESHOLD_CENTS, UiConstants.MAX_IN_TUNE_THRESHOLD_CENTS)
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
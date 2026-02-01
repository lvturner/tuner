package com.rokid.tuner

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rokid.tuner.constants.UiConstants
import com.rokid.tuner.pitch.PitchDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for TunerViewModel class.
 * Tests state management, tuning status calculation, and lifecycle behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class TunerViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: TunerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TunerViewModel()
    }

    @After
    fun tearDown() {
        viewModel.stopTuning()
        Dispatchers.resetMain()
    }

    // ========== Initial state tests ==========

    @Test
    fun `initial tuning state is Idle`() {
        assertEquals(TunerViewModel.TuningState.Idle, viewModel.tuningState.value)
    }

    @Test
    fun `initial isRunning is false`() {
        assertFalse(viewModel.isRunning.value)
    }

    @Test
    fun `initial currentRms is 0`() {
        assertEquals(0.0, viewModel.currentRms.value, 0.001)
    }

    // ========== TuningState sealed class tests ==========

    @Test
    fun `TuningState Idle is singleton`() {
        val idle1 = TunerViewModel.TuningState.Idle
        val idle2 = TunerViewModel.TuningState.Idle
        
        assertSame(idle1, idle2)
    }

    @Test
    fun `TuningState Listening is singleton`() {
        val listening1 = TunerViewModel.TuningState.Listening
        val listening2 = TunerViewModel.TuningState.Listening
        
        assertSame(listening1, listening2)
    }

    @Test
    fun `TuningState Detected holds PitchResult`() {
        val pitchResult = PitchDetector.PitchResult(440.0, "A4", 0f, 0.9f)
        val state = TunerViewModel.TuningState.Detected(pitchResult)
        
        assertEquals(pitchResult, state.result)
        assertEquals(440.0, state.result.frequency, 0.001)
        assertEquals("A4", state.result.noteName)
    }

    @Test
    fun `TuningState Error holds message`() {
        val state = TunerViewModel.TuningState.Error("Test error message")
        
        assertEquals("Test error message", state.message)
    }

    @Test
    fun `TuningState types are distinguishable`() {
        val idle = TunerViewModel.TuningState.Idle
        val listening = TunerViewModel.TuningState.Listening
        val detected = TunerViewModel.TuningState.Detected(
            PitchDetector.PitchResult(440.0, "A4", 0f, 0.9f)
        )
        val error = TunerViewModel.TuningState.Error("Error")
        
        assertTrue(idle is TunerViewModel.TuningState.Idle)
        assertTrue(listening is TunerViewModel.TuningState.Listening)
        assertTrue(detected is TunerViewModel.TuningState.Detected)
        assertTrue(error is TunerViewModel.TuningState.Error)
        
        // Verify states are distinct by checking their types at runtime
        val states = listOf<TunerViewModel.TuningState>(idle, listening, detected, error)
        assertEquals(4, states.filterIsInstance<TunerViewModel.TuningState.Idle>().size + 
            states.filterIsInstance<TunerViewModel.TuningState.Listening>().size +
            states.filterIsInstance<TunerViewModel.TuningState.Detected>().size +
            states.filterIsInstance<TunerViewModel.TuningState.Error>().size)
    }

    // ========== TuningStatus enum tests ==========

    @Test
    fun `TuningStatus enum has all expected values`() {
        val values = TunerViewModel.TuningStatus.values()
        
        assertEquals(4, values.size)
        assertTrue(values.contains(TunerViewModel.TuningStatus.IN_TUNE))
        assertTrue(values.contains(TunerViewModel.TuningStatus.SHARP))
        assertTrue(values.contains(TunerViewModel.TuningStatus.FLAT))
        assertTrue(values.contains(TunerViewModel.TuningStatus.LISTENING))
    }

    @Test
    fun `TuningStatus valueOf works correctly`() {
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, 
            TunerViewModel.TuningStatus.valueOf("IN_TUNE"))
        assertEquals(TunerViewModel.TuningStatus.SHARP, 
            TunerViewModel.TuningStatus.valueOf("SHARP"))
        assertEquals(TunerViewModel.TuningStatus.FLAT, 
            TunerViewModel.TuningStatus.valueOf("FLAT"))
        assertEquals(TunerViewModel.TuningStatus.LISTENING, 
            TunerViewModel.TuningStatus.valueOf("LISTENING"))
    }

    // ========== getTuningStatus() tests ==========

    @Test
    fun `getTuningStatus returns IN_TUNE for 0 cents`() {
        val result = PitchDetector.PitchResult(440.0, "A4", 0f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, status)
    }

    @Test
    fun `getTuningStatus returns IN_TUNE for small positive cents`() {
        // 5 cents sharp is still in tune (threshold is 10)
        val result = PitchDetector.PitchResult(441.0, "A4", 5f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, status)
    }

    @Test
    fun `getTuningStatus returns IN_TUNE for small negative cents`() {
        // 5 cents flat is still in tune (threshold is 10)
        val result = PitchDetector.PitchResult(439.0, "A4", -5f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, status)
    }

    @Test
    fun `getTuningStatus returns SHARP for positive cents above threshold`() {
        // 15 cents sharp is out of tune
        val result = PitchDetector.PitchResult(443.0, "A4", 15f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.SHARP, status)
    }

    @Test
    fun `getTuningStatus returns FLAT for negative cents below threshold`() {
        // 15 cents flat is out of tune
        val result = PitchDetector.PitchResult(437.0, "A4", -15f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.FLAT, status)
    }

    @Test
    fun `getTuningStatus returns IN_TUNE at exactly threshold`() {
        // At exactly 10 cents (the threshold), should be considered in tune
        // because the condition is absCents < threshold
        val result = PitchDetector.PitchResult(440.0, "A4", 9.9f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, status)
    }

    @Test
    fun `getTuningStatus returns SHARP at just above threshold`() {
        val result = PitchDetector.PitchResult(440.0, "A4", 10.1f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.SHARP, status)
    }

    @Test
    fun `getTuningStatus returns FLAT at just below negative threshold`() {
        val result = PitchDetector.PitchResult(440.0, "A4", -10.1f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.FLAT, status)
    }

    @Test
    fun `getTuningStatus handles extreme sharp values`() {
        val result = PitchDetector.PitchResult(500.0, "A4", 50f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.SHARP, status)
    }

    @Test
    fun `getTuningStatus handles extreme flat values`() {
        val result = PitchDetector.PitchResult(400.0, "A4", -50f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.FLAT, status)
    }

    // ========== setSensitivity() tests ==========

    @Test
    fun `setSensitivity accepts valid values`() {
        viewModel.setSensitivity(0)
        viewModel.setSensitivity(50)
        viewModel.setSensitivity(100)
        // No exception means success
    }

    @Test
    fun `setSensitivity clamps values below minimum`() {
        viewModel.setSensitivity(-10)
        // Should clamp to 0, no exception
    }

    @Test
    fun `setSensitivity clamps values above maximum`() {
        viewModel.setSensitivity(150)
        // Should clamp to 100, no exception
    }

    // ========== stopTuning() tests ==========

    @Test
    fun `stopTuning sets state to Idle`() {
        viewModel.stopTuning()
        
        assertEquals(TunerViewModel.TuningState.Idle, viewModel.tuningState.value)
    }

    @Test
    fun `stopTuning sets isRunning to false`() {
        viewModel.stopTuning()
        
        assertFalse(viewModel.isRunning.value)
    }

    @Test
    fun `stopTuning resets currentRms to 0`() {
        viewModel.stopTuning()
        
        assertEquals(0.0, viewModel.currentRms.value, 0.001)
    }

    @Test
    fun `stopTuning can be called multiple times safely`() {
        viewModel.stopTuning()
        viewModel.stopTuning()
        viewModel.stopTuning()
        
        // No exception means success
        assertFalse(viewModel.isRunning.value)
    }

    @Test
    fun `stopTuning works when not running`() {
        // Not started, should not throw
        assertFalse(viewModel.isRunning.value)
        viewModel.stopTuning()
        assertFalse(viewModel.isRunning.value)
    }

    // ========== StateFlow observation tests ==========

    @Test
    fun `tuningState StateFlow is observable`() {
        val observedStates = mutableListOf<TunerViewModel.TuningState>()
        
        // Collect initial state
        observedStates.add(viewModel.tuningState.value)
        
        assertEquals(1, observedStates.size)
        assertEquals(TunerViewModel.TuningState.Idle, observedStates[0])
    }

    @Test
    fun `isRunning StateFlow is observable`() {
        val observedValues = mutableListOf<Boolean>()
        
        observedValues.add(viewModel.isRunning.value)
        
        assertEquals(1, observedValues.size)
        assertFalse(observedValues[0])
    }

    @Test
    fun `currentRms StateFlow is observable`() {
        val observedValues = mutableListOf<Double>()
        
        observedValues.add(viewModel.currentRms.value)
        
        assertEquals(1, observedValues.size)
        assertEquals(0.0, observedValues[0], 0.001)
    }

    // ========== Data class tests for PitchResult used with ViewModel ==========

    @Test
    fun `Detected state can be destructured`() {
        val pitchResult = PitchDetector.PitchResult(440.0, "A4", 5f, 0.9f)
        val state = TunerViewModel.TuningState.Detected(pitchResult)
        
        val (result) = state
        
        assertEquals(pitchResult, result)
    }

    @Test
    fun `Error state can be destructured`() {
        val state = TunerViewModel.TuningState.Error("Test error")
        
        val (message) = state
        
        assertEquals("Test error", message)
    }

    // ========== Boundary condition tests ==========

    @Test
    fun `getTuningStatus handles exactly 0 cents correctly`() {
        val result = PitchDetector.PitchResult(440.0, "A4", 0.0f, 0.9f)
        val status = viewModel.getTuningStatus(result)
        
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, status)
    }

    @Test
    fun `getTuningStatus boundary at positive threshold`() {
        // Test values around the 10 cent threshold
        val justUnder = PitchDetector.PitchResult(440.0, "A4", 9.99f, 0.9f)
        val justOver = PitchDetector.PitchResult(440.0, "A4", 10.01f, 0.9f)
        
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, viewModel.getTuningStatus(justUnder))
        assertEquals(TunerViewModel.TuningStatus.SHARP, viewModel.getTuningStatus(justOver))
    }

    @Test
    fun `getTuningStatus boundary at negative threshold`() {
        // Test values around the -10 cent threshold
        val justUnder = PitchDetector.PitchResult(440.0, "A4", -9.99f, 0.9f)
        val justOver = PitchDetector.PitchResult(440.0, "A4", -10.01f, 0.9f)
        
        assertEquals(TunerViewModel.TuningStatus.IN_TUNE, viewModel.getTuningStatus(justUnder))
        assertEquals(TunerViewModel.TuningStatus.FLAT, viewModel.getTuningStatus(justOver))
    }

    // ========== Multiple ViewModel instance tests ==========

    @Test
    fun `multiple ViewModel instances are independent`() {
        val viewModel1 = TunerViewModel()
        val viewModel2 = TunerViewModel()
        
        viewModel1.setSensitivity(25)
        viewModel2.setSensitivity(75)
        
        // Each has independent state
        assertEquals(TunerViewModel.TuningState.Idle, viewModel1.tuningState.value)
        assertEquals(TunerViewModel.TuningState.Idle, viewModel2.tuningState.value)
        
        viewModel1.stopTuning()
        viewModel2.stopTuning()
    }

    // ========== Edge cases for PitchResult in tuning status ==========

    @Test
    fun `getTuningStatus works with various note names`() {
        val notes = listOf("C4", "D4", "E4", "F4", "G4", "A4", "B4")
        
        notes.forEach { noteName ->
            val inTuneResult = PitchDetector.PitchResult(440.0, noteName, 0f, 0.9f)
            val sharpResult = PitchDetector.PitchResult(440.0, noteName, 25f, 0.9f)
            val flatResult = PitchDetector.PitchResult(440.0, noteName, -25f, 0.9f)
            
            assertEquals(TunerViewModel.TuningStatus.IN_TUNE, viewModel.getTuningStatus(inTuneResult))
            assertEquals(TunerViewModel.TuningStatus.SHARP, viewModel.getTuningStatus(sharpResult))
            assertEquals(TunerViewModel.TuningStatus.FLAT, viewModel.getTuningStatus(flatResult))
        }
    }

    @Test
    fun `getTuningStatus ignores frequency value for status calculation`() {
        // Status is based purely on cents, not frequency
        val result1 = PitchDetector.PitchResult(100.0, "A4", 5f, 0.9f)
        val result2 = PitchDetector.PitchResult(1000.0, "A4", 5f, 0.9f)
        
        assertEquals(viewModel.getTuningStatus(result1), viewModel.getTuningStatus(result2))
    }

    @Test
    fun `getTuningStatus ignores probability value for status calculation`() {
        // Status is based purely on cents, not probability
        val result1 = PitchDetector.PitchResult(440.0, "A4", 5f, 0.1f)
        val result2 = PitchDetector.PitchResult(440.0, "A4", 5f, 0.9f)
        
        assertEquals(viewModel.getTuningStatus(result1), viewModel.getTuningStatus(result2))
    }
}

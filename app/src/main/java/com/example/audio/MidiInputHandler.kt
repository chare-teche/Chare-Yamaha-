package com.example.audio

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Handler
import android.os.Looper

/**
 * Real Android MIDI API (android.media.midi) input bridge for external USB/Bluetooth MIDI keyboards.
 * Routes incoming Note On, Note Off, Sustain Pedal (CC64), and Pitch Bend messages live.
 */
class MidiInputHandler(
    private val context: Context,
    private val onNoteOn: (midiNote: Int, velocity: Float) -> Unit,
    private val onNoteOff: (midiNote: Int) -> Unit,
    private val onSustainChanged: (down: Boolean) -> Unit,
    private val onPitchBendChanged: (semitones: Float) -> Unit,
    private val onDevicesChanged: (List<String>) -> Unit
) {
    private val midiManager: MidiManager? =
        context.getSystemService(Context.MIDI_SERVICE) as? MidiManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val openDevices = mutableListOf<MidiDevice>()
    private val openPorts = mutableListOf<MidiOutputPort>()
    private var deviceCallback: MidiManager.DeviceCallback? = null

    private val receiver = object : MidiReceiver() {
        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
            var i = offset
            val end = offset + count
            while (i < end) {
                val status = msg[i].toInt() and 0xFF
                if (status < 0x80) {
                    i++
                    continue
                }
                val command = status and 0xF0
                when (command) {
                    0x90 -> { // Note On
                        if (i + 2 < end) {
                            val note = msg[i + 1].toInt() and 0x7F
                            val vel = msg[i + 2].toInt() and 0x7F
                            if (vel > 0) {
                                onNoteOn(note, vel / 127f)
                            } else {
                                onNoteOff(note)
                            }
                            i += 3
                        } else break
                    }
                    0x80 -> { // Note Off
                        if (i + 2 < end) {
                            val note = msg[i + 1].toInt() and 0x7F
                            onNoteOff(note)
                            i += 3
                        } else break
                    }
                    0xB0 -> { // Control Change (CC64 Sustain)
                        if (i + 2 < end) {
                            val cc = msg[i + 1].toInt() and 0x7F
                            val value = msg[i + 2].toInt() and 0x7F
                            if (cc == 64) {
                                onSustainChanged(value >= 64)
                            }
                            i += 3
                        } else break
                    }
                    0xE0 -> { // Pitch Bend
                        if (i + 2 < end) {
                            val lsb = msg[i + 1].toInt() and 0x7F
                            val msb = msg[i + 2].toInt() and 0x7F
                            val bend14 = (msb shl 7) or lsb
                            val normalized = (bend14 - 8192) / 8192f
                            onPitchBendChanged(normalized * 2f)
                            i += 3
                        } else break
                    }
                    else -> i++
                }
            }
        }
    }

    fun start() {
        val manager = midiManager ?: return
        refreshConnectedDevices(manager)
        val callback = object : MidiManager.DeviceCallback() {
            override fun onDeviceAdded(device: MidiDeviceInfo) {
                refreshConnectedDevices(manager)
            }

            override fun onDeviceRemoved(device: MidiDeviceInfo) {
                refreshConnectedDevices(manager)
            }
        }
        deviceCallback = callback
        try {
            manager.registerDeviceCallback(callback, mainHandler)
        } catch (_: Throwable) {
            // Device may not support MIDI callbacks
        }
    }

    @Suppress("DEPRECATION")
    private fun refreshConnectedDevices(manager: MidiManager) {
        closeOpenPorts()
        val infos = try {
            manager.devices.toList()
        } catch (_: Throwable) {
            emptyList()
        }

        val names = mutableListOf<String>()
        for (info in infos) {
            if (info.outputPortCount > 0) {
                val name = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME)
                    ?: info.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT)
                    ?: "MIDI Controller"
                names.add(name)
                try {
                    manager.openDevice(info, { device ->
                        if (device != null) {
                            openDevices.add(device)
                            val port = device.openOutputPort(0)
                            if (port != null) {
                                port.connect(receiver)
                                openPorts.add(port)
                            }
                        }
                    }, mainHandler)
                } catch (_: Throwable) {
                    // ignore busy device
                }
            }
        }
        onDevicesChanged(names)
    }

    private fun closeOpenPorts() {
        openPorts.forEach { runCatching { it.disconnect(receiver); it.close() } }
        openPorts.clear()
        openDevices.forEach { runCatching { it.close() } }
        openDevices.clear()
    }

    fun stop() {
        deviceCallback?.let { cb ->
            runCatching { midiManager?.unregisterDeviceCallback(cb) }
        }
        deviceCallback = null
        closeOpenPorts()
    }
}

package com.example.audio

import com.example.domain.model.ChordQuality
import com.example.domain.model.DetectedChord
import com.example.domain.model.SplitChordMode

/**
 * Real-time chord detection engine supporting Yamaha-style Single Finger,
 * Fingered (root + inversion detection), and Full Keyboard modes.
 */
object ChordDetector {

    private val blackPitchClasses = setOf(1, 3, 6, 8, 10)

    fun detect(
        activeNotes: Set<Int>,
        mode: SplitChordMode,
        previousChord: DetectedChord
    ): DetectedChord {
        if (activeNotes.isEmpty()) return previousChord

        val sorted = activeNotes.sorted()
        val bassPitchClass = sorted.first().mod12()
        val pitchClasses = sorted.map { it.mod12() }.distinct()

        // Single-note press in Single Finger or Fingered: trigger root Major chord
        if (pitchClasses.size == 1) {
            return DetectedChord(
                rootPitchClass = bassPitchClass,
                quality = ChordQuality.MAJOR,
                bassPitchClass = bassPitchClass
            )
        }

        // Single Finger Yamaha convention:
        // Highest note = root; black key to left = minor; white key to left = 7th; both = min7
        if (mode == SplitChordMode.SINGLE_FINGER && sorted.size in 2..3) {
            val highest = sorted.last()
            val rootPc = highest.mod12()
            val leftKeys = sorted.dropLast(1)
            val allBelowRootClose = leftKeys.all { highest - it in 1..4 }
            if (allBelowRootClose) {
                val hasBlack = leftKeys.any { it.mod12() in blackPitchClasses }
                val hasWhite = leftKeys.any { it.mod12() !in blackPitchClasses }
                val quality = when {
                    hasBlack && hasWhite -> ChordQuality.MINOR_7
                    hasBlack -> ChordQuality.MINOR
                    else -> ChordQuality.DOMINANT_7
                }
                return DetectedChord(
                    rootPitchClass = rootPc,
                    quality = quality,
                    bassPitchClass = rootPc
                )
            }
        }

        // Evaluate all candidate roots among the active pitch classes (supports inversions)
        var bestChord: DetectedChord? = null
        var bestScore = Int.MIN_VALUE

        val templates = listOf(
            ChordQuality.MAJOR_7 to setOf(0, 4, 7, 11),
            ChordQuality.MINOR_7 to setOf(0, 3, 7, 10),
            ChordQuality.DOMINANT_7 to setOf(0, 4, 7, 10),
            ChordQuality.MAJOR to setOf(0, 4, 7),
            ChordQuality.MINOR to setOf(0, 3, 7),
            ChordQuality.SUS4 to setOf(0, 5, 7),
            ChordQuality.SUS2 to setOf(0, 2, 7),
            ChordQuality.DIM to setOf(0, 3, 6),
            ChordQuality.AUG to setOf(0, 4, 8),
            ChordQuality.POWER_5 to setOf(0, 7)
        )

        val pcSet = pitchClasses.toSet()

        for (candidateRoot in pitchClasses) {
            val intervals = pcSet.map { (it - candidateRoot).mod12() }.toSet()
            for ((quality, template) in templates) {
                val matched = intervals.intersect(template).size
                val missing = template.size - matched
                val extra = intervals.size - matched

                // Require at least 2 notes matched for a triad/7th, or exact match for dyads
                if (matched < 2) continue
                if (pcSet.size == 2 && quality !in setOf(ChordQuality.MAJOR, ChordQuality.MINOR, ChordQuality.POWER_5, ChordQuality.DOMINANT_7)) {
                    continue
                }

                var score = matched * 20 - missing * 12 - extra * 15
                // Prefer root position when bass equals candidate root
                if (candidateRoot == bassPitchClass) score += 10
                // Exact match bonus
                if (missing == 0 && extra == 0) score += 25
                // Two-note minor third (0,3) or major third (0,4) shortcuts
                if (pcSet.size == 2 && intervals == setOf(0, 3) && quality == ChordQuality.MINOR) score += 18
                if (pcSet.size == 2 && intervals == setOf(0, 4) && quality == ChordQuality.MAJOR) score += 18

                if (score > bestScore) {
                    bestScore = score
                    bestChord = DetectedChord(
                        rootPitchClass = candidateRoot,
                        quality = quality,
                        bassPitchClass = bassPitchClass
                    )
                }
            }
        }

        return bestChord ?: DetectedChord(
            rootPitchClass = bassPitchClass,
            quality = ChordQuality.MAJOR,
            bassPitchClass = bassPitchClass
        )
    }

    /**
     * Resolves a chord-tone index (0=root, 1=3rd, 2=5th, 3=7th/octave) plus semitone offset
     * into an actual playable MIDI note number around octaveBase.
     */
    fun resolveChordNote(
        chord: DetectedChord,
        chordToneIndex: Int,
        semitoneOffset: Int,
        octaveBase: Int
    ): Int {
        val intervals = chord.quality.intervals
        val baseMidi = (octaveBase + 1) * 12 + chord.rootPitchClass
        val octaveLift = (chordToneIndex / intervals.size) * 12
        val interval = intervals[chordToneIndex % intervals.size]
        return (baseMidi + interval + octaveLift + semitoneOffset).coerceIn(24, 108)
    }

    private fun Int.mod12(): Int = ((this % 12) + 12) % 12
}

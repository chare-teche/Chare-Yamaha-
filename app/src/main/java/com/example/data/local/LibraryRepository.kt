package com.example.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.example.audio.BuiltInStyleBank
import com.example.audio.LoadedWavSample
import com.example.data.parser.OrgSetParser
import com.example.data.parser.YamahaStyleParser
import com.example.domain.model.ImportedSourceUi
import com.example.domain.model.LibraryStyleItemUi
import com.example.domain.model.StyleDefinition
import com.example.domain.model.SynthEngineType
import com.example.domain.model.VoiceCategory
import com.example.domain.model.VoicePreset
import com.example.domain.model.WorshipSongUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Local storage repository combining Room persistence with strictly read-only Android SAF
 * traversal and separate Yamaha SET and ORG.SET parsing pipelines.
 */
class LibraryRepository(
    private val context: Context,
    private val dao: AppDao
) {
    private val parsedStyleCache = mutableMapOf<String, StyleDefinition>()

    val importedSourcesFlow: Flow<List<ImportedSourceUi>> = dao.observeSources().map { list ->
        list.map { e ->
            ImportedSourceUi(
                id = e.id,
                uri = e.uri,
                displayName = e.displayName,
                setFamily = e.setFamily,
                isFolder = e.isFolder,
                totalFiles = e.totalFiles,
                supportedStyles = e.supportedStyles,
                supportedSamples = e.supportedSamples,
                unsupportedFiles = e.unsupportedFiles,
                inspectionSummary = e.inspectionSummary,
                importedAtMillis = e.importedAtMillis
            )
        }
    }

    val allLibraryStylesFlow: Flow<List<LibraryStyleItemUi>> = dao.observeStyleEntries().map { imported ->
        val builtInItems = BuiltInStyleBank.styles.map { s ->
            LibraryStyleItemUi(
                id = s.id,
                sourceId = null,
                name = s.name,
                category = s.category,
                setFamily = "BUILTIN",
                fileFormat = s.fileFormat,
                tempoBpm = s.tempoBpm,
                timeSignature = s.timeSignatureLabel,
                sectionSummary = "INTRO 1-3, MAIN A-D, FILL, BREAK, END 1-3",
                isSupported = true,
                compatibilityStatus = "PLAYABLE",
                parseMessage = s.parseDiagnostics,
                missingResources = "None",
                isBuiltIn = true,
                uri = null
            )
        }
        val importedItems = imported.map { e ->
            LibraryStyleItemUi(
                id = e.id,
                sourceId = e.sourceId,
                name = e.name,
                category = e.category,
                setFamily = e.setFamily,
                fileFormat = e.fileFormat,
                tempoBpm = e.tempoBpm,
                timeSignature = "${e.timeSigNum}/${e.timeSigDen}",
                sectionSummary = e.sectionSummary,
                isSupported = e.isSupported,
                compatibilityStatus = e.compatibilityStatus,
                parseMessage = e.parseMessage,
                missingResources = e.missingResources,
                isBuiltIn = false,
                uri = e.uri
            )
        }
        importedItems + builtInItems
    }

    val allRegistrationSlotsFlow: Flow<List<RegistrationSlotEntity>> = dao.observeAllRegistrationSlots()

    val worshipSongsFlow: Flow<List<WorshipSongUi>> = dao.observeWorshipSongs().map { list ->
        list.map { e ->
            WorshipSongUi(
                id = e.id,
                title = e.title,
                category = e.category,
                keySignature = e.keySignature,
                styleId = e.styleId,
                voiceId = e.voiceId,
                tempoBpm = e.tempoBpm,
                transposeSemitones = e.transposeSemitones,
                notes = e.notes
            )
        }
    }

    suspend fun ensureDefaultWorshipSongsSeeded() = withContext(Dispatchers.IO) {
        if (dao.countWorshipSongs() == 0) {
            val defaults = listOf(
                WorshipSongEntity(
                    title = "Yene Geta (Slow Ethio Worship)",
                    category = "Ethiopian Mezmur",
                    keySignature = "Am / C",
                    styleId = "builtin_ethio_worship_slow",
                    voiceId = "piano_worship",
                    tempoBpm = 72,
                    transposeSemitones = 0,
                    notes = "Start Intro 1 with Grand Piano + Strings R2; switch Main B on chorus."
                ),
                WorshipSongEntity(
                    title = "Hallelujah Praise (Eskista 6/8)",
                    category = "Church Praise",
                    keySignature = "D Maj",
                    styleId = "builtin_ethio_praise_68",
                    voiceId = "brass_addis",
                    tempoBpm = 118,
                    transposeSemitones = 2,
                    notes = "Use Main C/D with Kebero Multi-Pad 1 on upbeat celebration."
                ),
                WorshipSongEntity(
                    title = "Grace & Mercy Worship Ballad",
                    category = "Gospel Worship",
                    keySignature = "G Maj",
                    styleId = "builtin_gospel_worship",
                    voiceId = "ep_dx_modern",
                    tempoBpm = 76,
                    transposeSemitones = -5,
                    notes = "Soft worship intro; engage Right 2 Warm Strings on verse 2."
                ),
                WorshipSongEntity(
                    title = "Congregational Hymn (3/4)",
                    category = "Church Hymn",
                    keySignature = "F Maj",
                    styleId = "builtin_choir_hymn_34",
                    voiceId = "organ_church",
                    tempoBpm = 86,
                    transposeSemitones = 5,
                    notes = "Church Pipe Organ lead with 3/4 hymn rhythm accompaniment."
                )
            )
            defaults.forEach { dao.insertWorshipSong(it) }
        }
    }

    suspend fun addWorshipSong(song: WorshipSongEntity) = dao.insertWorshipSong(song)
    suspend fun deleteWorshipSong(songId: Long) = dao.deleteWorshipSong(songId)

    suspend fun saveRegistrationSlot(slot: RegistrationSlotEntity) = dao.upsertRegistrationSlot(slot)
    suspend fun getRegistrationSlot(bankNumber: Int, slotIndex: Int): RegistrationSlotEntity? =
        dao.getRegistrationSlot(bankNumber * 10 + slotIndex)

    suspend fun removeImportedSource(sourceId: Long) {
        dao.deleteStyleEntriesForSource(sourceId)
        dao.deleteSourceById(sourceId)
    }

    suspend fun removeStyleEntry(styleId: String) {
        parsedStyleCache.remove(styleId)
        dao.deleteStyleEntryById(styleId)
    }

    data class FolderImportOutcome(
        val statusMessage: String,
        val loadedSamples: List<Pair<LoadedWavSample, VoicePreset>>
    )

    /**
     * Imports either a Yamaha SET folder or an ORG.SET folder via distinct dedicated parsers.
     * Preserves original user files read-only.
     */
    suspend fun importFolderTree(treeUri: Uri, isOrgSet: Boolean): FolderImportOutcome =
        withContext(Dispatchers.IO) {
            val loadedSamples = mutableListOf<Pair<LoadedWavSample, VoicePreset>>()
            try {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                val setFamily = if (isOrgSet) "ORG_SET" else "YAMAHA_SET"
                val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
                val folderName = queryTreeDisplayName(treeUri, rootDocId)

                val discoveredFiles = mutableListOf<Triple<Uri, String, String>>()
                traverseDocumentTree(treeUri, rootDocId, folderName, discoveredFiles, depth = 0)

                val entries = mutableListOf<StyleEntryEntity>()
                var supportedStyles = 0
                var supportedSamples = 0
                var unsupportedCount = 0

                val sourceId = dao.insertSource(
                    ImportedSourceEntity(
                        uri = treeUri.toString(),
                        displayName = folderName,
                        setFamily = setFamily,
                        isFolder = true,
                        totalFiles = discoveredFiles.size,
                        supportedStyles = 0,
                        supportedSamples = 0,
                        unsupportedFiles = 0,
                        inspectionSummary = "Scanning..."
                    )
                )

                for ((fileUri, fileName, subFolder) in discoveredFiles) {
                    val bytes = readBytesSafely(fileUri, maxBytes = 2_000_000)
                    val styleId = "${if (isOrgSet) "org" else "yam"}_${UUID.randomUUID().toString().take(10)}"

                    if (bytes == null) {
                        unsupportedCount++
                        entries.add(
                            StyleEntryEntity(
                                id = styleId,
                                sourceId = sourceId,
                                name = fileName,
                                category = subFolder,
                                setFamily = setFamily,
                                fileFormat = "UNREADABLE",
                                uri = fileUri.toString(),
                                tempoBpm = 120,
                                timeSigNum = 4,
                                timeSigDen = 4,
                                sectionSummary = "Unreadable",
                                isSupported = false,
                                compatibilityStatus = "UNSUPPORTED",
                                parseMessage = "Could not open read stream via Android SAF.",
                                missingResources = "File read access"
                            )
                        )
                        continue
                    }

                    if (isOrgSet) {
                        val res = OrgSetParser.inspectOrgFile(styleId, fileName, subFolder, fileUri.toString(), bytes)
                        if (res.styleDefinition != null) {
                            supportedStyles++
                            parsedStyleCache[styleId] = res.styleDefinition
                        } else if (res.loadedWavSample != null) {
                            supportedSamples++
                            loadedSamples.add(res.loadedWavSample to wavToVoicePreset(styleId, res.loadedWavSample))
                        } else {
                            unsupportedCount++
                        }
                        entries.add(
                            StyleEntryEntity(
                                id = styleId,
                                sourceId = sourceId,
                                name = res.displayName,
                                category = subFolder,
                                setFamily = setFamily,
                                fileFormat = res.fileFormat,
                                uri = fileUri.toString(),
                                tempoBpm = res.tempoBpm,
                                timeSigNum = res.timeSigNumerator,
                                timeSigDen = res.timeSigDenominator,
                                sectionSummary = res.sectionSummary,
                                isSupported = res.isSupported,
                                compatibilityStatus = res.compatibilityStatus,
                                parseMessage = res.diagnostics,
                                missingResources = res.missingResources
                            )
                        )
                    } else {
                        val res = YamahaStyleParser.inspectAndParse(styleId, fileName, fileUri.toString(), bytes, subFolder)
                        if (res.styleDefinition != null) {
                            supportedStyles++
                            parsedStyleCache[styleId] = res.styleDefinition
                        } else if (res.loadedWavSample != null) {
                            supportedSamples++
                            loadedSamples.add(res.loadedWavSample to wavToVoicePreset(styleId, res.loadedWavSample))
                        } else {
                            unsupportedCount++
                        }
                        entries.add(
                            StyleEntryEntity(
                                id = styleId,
                                sourceId = sourceId,
                                name = res.displayName,
                                category = subFolder,
                                setFamily = setFamily,
                                fileFormat = res.fileFormat,
                                uri = fileUri.toString(),
                                tempoBpm = res.tempoBpm,
                                timeSigNum = res.timeSigNumerator,
                                timeSigDen = res.timeSigDenominator,
                                sectionSummary = res.sectionSummary,
                                isSupported = res.isSupported,
                                compatibilityStatus = res.compatibilityStatus,
                                parseMessage = res.diagnostics,
                                missingResources = res.missingResources
                            )
                        )
                    }
                }

                if (entries.isNotEmpty()) dao.insertStyleEntries(entries)
                val summary = "$setFamily inspected: $supportedStyles playable styles, $supportedSamples WAV samples, $unsupportedCount unsupported resources."
                dao.insertSource(
                    ImportedSourceEntity(
                        id = sourceId,
                        uri = treeUri.toString(),
                        displayName = folderName,
                        setFamily = setFamily,
                        isFolder = true,
                        totalFiles = discoveredFiles.size,
                        supportedStyles = supportedStyles,
                        supportedSamples = supportedSamples,
                        unsupportedFiles = unsupportedCount,
                        inspectionSummary = summary
                    )
                )
                FolderImportOutcome("Imported '$folderName' — $summary", loadedSamples)
            } catch (e: Exception) {
                FolderImportOutcome("Folder import failed: ${e.message ?: "Access denied"}", loadedSamples)
            }
        }

    suspend fun importSingleFile(fileUri: Uri): FolderImportOutcome = withContext(Dispatchers.IO) {
        val loadedSamples = mutableListOf<Pair<LoadedWavSample, VoicePreset>>()
        try {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val fileName = querySingleFileName(fileUri)
            val bytes = readBytesSafely(fileUri, maxBytes = 3_000_000)
                ?: return@withContext FolderImportOutcome("Could not read '$fileName'.", emptyList())

            val styleId = "file_${UUID.randomUUID().toString().take(10)}"
            val ext = fileName.substringAfterLast('.', "").lowercase()
            val isOrgHint = ext in setOf("ost", "pcg", "kmp", "ksf", "sf2", "set")

            val sourceId: Long
            val msg: String

            if (isOrgHint) {
                val res = OrgSetParser.inspectOrgFile(styleId, fileName, "Single File", fileUri.toString(), bytes)
                if (res.styleDefinition != null) parsedStyleCache[styleId] = res.styleDefinition
                if (res.loadedWavSample != null) {
                    loadedSamples.add(res.loadedWavSample to wavToVoicePreset(styleId, res.loadedWavSample))
                }
                sourceId = dao.insertSource(
                    ImportedSourceEntity(
                        uri = fileUri.toString(),
                        displayName = fileName,
                        setFamily = "ORG_SET",
                        isFolder = false,
                        totalFiles = 1,
                        supportedStyles = if (res.styleDefinition != null) 1 else 0,
                        supportedSamples = if (res.loadedWavSample != null) 1 else 0,
                        unsupportedFiles = if (!res.isSupported) 1 else 0,
                        inspectionSummary = res.diagnostics
                    )
                )
                dao.insertStyleEntries(
                    listOf(
                        StyleEntryEntity(
                            id = styleId, sourceId = sourceId, name = res.displayName,
                            category = "Imported ORG", setFamily = "ORG_SET", fileFormat = res.fileFormat,
                            uri = fileUri.toString(), tempoBpm = res.tempoBpm,
                            timeSigNum = res.timeSigNumerator, timeSigDen = res.timeSigDenominator,
                            sectionSummary = res.sectionSummary, isSupported = res.isSupported,
                            compatibilityStatus = res.compatibilityStatus, parseMessage = res.diagnostics,
                            missingResources = res.missingResources
                        )
                    )
                )
                msg = res.diagnostics
            } else {
                val res = YamahaStyleParser.inspectAndParse(styleId, fileName, fileUri.toString(), bytes, "Imported File")
                if (res.styleDefinition != null) parsedStyleCache[styleId] = res.styleDefinition
                if (res.loadedWavSample != null) {
                    loadedSamples.add(res.loadedWavSample to wavToVoicePreset(styleId, res.loadedWavSample))
                }
                sourceId = dao.insertSource(
                    ImportedSourceEntity(
                        uri = fileUri.toString(),
                        displayName = fileName,
                        setFamily = "YAMAHA_SET",
                        isFolder = false,
                        totalFiles = 1,
                        supportedStyles = if (res.styleDefinition != null) 1 else 0,
                        supportedSamples = if (res.loadedWavSample != null) 1 else 0,
                        unsupportedFiles = if (!res.isSupported) 1 else 0,
                        inspectionSummary = res.diagnostics
                    )
                )
                dao.insertStyleEntries(
                    listOf(
                        StyleEntryEntity(
                            id = styleId, sourceId = sourceId, name = res.displayName,
                            category = "Imported File", setFamily = "YAMAHA_SET", fileFormat = res.fileFormat,
                            uri = fileUri.toString(), tempoBpm = res.tempoBpm,
                            timeSigNum = res.timeSigNumerator, timeSigDen = res.timeSigDenominator,
                            sectionSummary = res.sectionSummary, isSupported = res.isSupported,
                            compatibilityStatus = res.compatibilityStatus, parseMessage = res.diagnostics,
                            missingResources = res.missingResources
                        )
                    )
                )
                msg = if (res.isSupported) "Loaded '${res.displayName}' (${res.fileFormat})." else res.diagnostics
            }

            FolderImportOutcome(msg, loadedSamples)
        } catch (e: Exception) {
            FolderImportOutcome("Import error: ${e.message ?: "Unable to read file"}", loadedSamples)
        }
    }

    suspend fun loadStyleDefinition(styleId: String): Pair<StyleDefinition?, String?> =
        withContext(Dispatchers.IO) {
            BuiltInStyleBank.byId(styleId)?.let { return@withContext it to null }
            parsedStyleCache[styleId]?.let { return@withContext it to null }

            val entry = dao.getStyleEntryById(styleId)
                ?: return@withContext null to "Style not found in library."
            if (entry.compatibilityStatus != "PLAYABLE") {
                return@withContext null to entry.parseMessage
            }
            val uri = runCatching { Uri.parse(entry.uri) }.getOrNull()
                ?: return@withContext null to "Invalid persisted URI."
            val bytes = readBytesSafely(uri, maxBytes = 2_000_000)
                ?: return@withContext null to "Persisted file no longer accessible — re-import folder."

            val parsed = YamahaStyleParser.inspectAndParse(
                id = entry.id,
                fileName = entry.name + ".sty",
                uriString = entry.uri,
                bytes = bytes,
                categoryLabel = entry.category
            )
            val def = parsed.styleDefinition
            if (def != null) {
                parsedStyleCache[styleId] = def
                def to null
            } else {
                null to parsed.diagnostics
            }
        }

    private fun wavToVoicePreset(id: String, wav: LoadedWavSample): VoicePreset = VoicePreset(
        id = id,
        name = wav.name,
        category = VoiceCategory.USER_SAMPLES,
        engineType = SynthEngineType.USER_WAV_SAMPLE,
        description = "User imported PCM WAV sample (${wav.sampleRate} Hz, root C4)",
        attackSeconds = 0.004f,
        decaySeconds = 0.2f,
        sustainLevel = 0.95f,
        releaseSeconds = 0.25f,
        sampleKey = wav.key,
        sampleRootMidi = wav.rootMidiNote
    )

    private fun traverseDocumentTree(
        treeUri: Uri,
        parentDocId: String,
        currentFolderLabel: String,
        results: MutableList<Triple<Uri, String, String>>,
        depth: Int
    ) {
        if (depth > 4 || results.size >= 180) return
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (cursor.moveToNext() && results.size < 180) {
                    val docId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "Unnamed" else "Unnamed"
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) ?: "" else ""
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        traverseDocumentTree(treeUri, docId, name, results, depth + 1)
                    } else if (!name.startsWith(".")) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        results.add(Triple(docUri, name, currentFolderLabel))
                    }
                }
            }
        }
    }

    private fun queryTreeDisplayName(treeUri: Uri, docId: String): String {
        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        return runCatching {
            context.contentResolver.query(
                docUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null
            )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: docId.substringAfterLast('/').substringAfterLast(':').ifBlank { "Imported SET" }
    }

    private fun querySingleFileName(uri: Uri): String {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment?.substringAfterLast('/') ?: "ImportedFile.sty"
    }

    private fun readBytesSafely(uri: Uri, maxBytes: Int): ByteArray? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val chunk = ByteArray(minOf(maxBytes, 65536))
            val out = java.io.ByteArrayOutputStream()
            var total = 0
            while (total < maxBytes) {
                val read = input.read(chunk, 0, minOf(chunk.size, maxBytes - total))
                if (read <= 0) break
                out.write(chunk, 0, read)
                total += read
            }
            out.toByteArray()
        }
    }.getOrNull()
}

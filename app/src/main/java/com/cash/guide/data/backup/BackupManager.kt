package com.cash.guide.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.cash.guide.data.db.CalculationDao
import com.cash.guide.data.db.CalculationEntity
import com.cash.guide.data.db.CalculationGroupDao
import com.cash.guide.data.db.CalculationGroupEntity
import com.cash.guide.data.db.CalculationItemEntity
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.data.db.HssabiDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPayload(
    val app: String = "SARF",
    val format: String = "SARF_BACKUP",
    val version: Int = 1,
    val exportedAtEpochMs: Long = System.currentTimeMillis(),
    val groups: List<CalculationGroupEntity> = emptyList(),
    val calculations: List<CalculationWithItems> = emptyList()
) {
    val totalCalculations: Int get() = calculations.size
    val totalGroups: Int get() = groups.size
}

class BackupManager(
    private val calculationDao: CalculationDao,
    private val groupDao: CalculationGroupDao
) {
    constructor(database: HssabiDatabase) : this(
        database.calculationDao(),
        database.calculationGroupDao()
    )

    suspend fun createBackupPayload(): BackupPayload = withContext(Dispatchers.IO) {
        val groups = groupDao.getAllGroups()
        val calculations = calculationDao.getAllSaved()
        BackupPayload(
            exportedAtEpochMs = System.currentTimeMillis(),
            groups = groups,
            calculations = calculations
        )
    }

    fun serializeToJson(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("app", payload.app)
        root.put("format", payload.format)
        root.put("version", payload.version)
        root.put("exportedAtEpochMs", payload.exportedAtEpochMs)

        val groupsArr = JSONArray()
        for (group in payload.groups) {
            val gObj = JSONObject()
            gObj.put("id", group.id)
            gObj.put("name", group.name)
            gObj.put("colorHex", group.colorHex)
            gObj.put("createdAtEpochMs", group.createdAtEpochMs)
            gObj.put("updatedAtEpochMs", group.updatedAtEpochMs)
            groupsArr.put(gObj)
        }
        root.put("groups", groupsArr)

        val calcsArr = JSONArray()
        for (item in payload.calculations) {
            val calcObj = JSONObject()
            val c = item.calculation
            val cEntity = JSONObject()
            cEntity.put("id", c.id)
            cEntity.put("title", c.title)
            cEntity.put("currency", c.currency)
            cEntity.put("createdAtEpochMs", c.createdAtEpochMs)
            cEntity.put("updatedAtEpochMs", c.updatedAtEpochMs)
            cEntity.put("status", c.status)
            if (c.note != null) cEntity.put("note", c.note)
            if (c.editingCalculationId != null) cEntity.put("editingCalculationId", c.editingCalculationId)
            if (c.groupId != null) cEntity.put("groupId", c.groupId)
            cEntity.put("paymentStatus", c.paymentStatus)
            cEntity.put("calcType", c.calcType)
            if (c.dueDateEpochMs != null) cEntity.put("dueDateEpochMs", c.dueDateEpochMs)
            cEntity.put("reminderEnabled", c.reminderEnabled)
            if (c.reminderTimeEpochMs != null) cEntity.put("reminderTimeEpochMs", c.reminderTimeEpochMs)
            calcObj.put("calculation", cEntity)

            val itemsArr = JSONArray()
            for (row in item.items) {
                val rObj = JSONObject()
                rObj.put("id", row.id)
                rObj.put("calculationId", row.calculationId)
                rObj.put("label", row.label)
                rObj.put("amountCentimes", row.amountCentimes)
                if (row.rawExpression != null) rObj.put("rawExpression", row.rawExpression)
                rObj.put("position", row.position)
                rObj.put("createdAtEpochMs", row.createdAtEpochMs)
                rObj.put("updatedAtEpochMs", row.updatedAtEpochMs)
                itemsArr.put(rObj)
            }
            calcObj.put("items", itemsArr)
            calcsArr.put(calcObj)
        }
        root.put("calculations", calcsArr)

        return root.toString(2)
    }

    fun parseFromJson(jsonString: String): Result<BackupPayload> = runCatching {
        val root = JSONObject(jsonString)
        val app = root.optString("app", "")
        val format = root.optString("format", "")
        if (format != "SARF_BACKUP" && app != "SARF") {
            throw IllegalArgumentException("Format de fichier non reconnu")
        }
        val version = root.optInt("version", 1)
        val exportedAt = root.optLong("exportedAtEpochMs", System.currentTimeMillis())

        val groupsList = mutableListOf<CalculationGroupEntity>()
        val groupsArr = root.optJSONArray("groups")
        if (groupsArr != null) {
            for (i in 0 until groupsArr.length()) {
                val gObj = groupsArr.getJSONObject(i)
                groupsList.add(
                    CalculationGroupEntity(
                        id = gObj.getString("id"),
                        name = gObj.getString("name"),
                        colorHex = gObj.optString("colorHex", "#E5A93C"),
                        createdAtEpochMs = gObj.optLong("createdAtEpochMs", System.currentTimeMillis()),
                        updatedAtEpochMs = gObj.optLong("updatedAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val calcsList = mutableListOf<CalculationWithItems>()
        val calcsArr = root.optJSONArray("calculations")
        if (calcsArr != null) {
            for (i in 0 until calcsArr.length()) {
                val obj = calcsArr.getJSONObject(i)
                val cObj = obj.getJSONObject("calculation")
                val calcEntity = CalculationEntity(
                    id = cObj.getString("id"),
                    title = cObj.optString("title", ""),
                    currency = cObj.optString("currency", "DIRHAM"),
                    createdAtEpochMs = cObj.optLong("createdAtEpochMs", System.currentTimeMillis()),
                    updatedAtEpochMs = cObj.optLong("updatedAtEpochMs", System.currentTimeMillis()),
                    status = cObj.optString("status", "SAVED"),
                    note = if (cObj.has("note") && !cObj.isNull("note")) cObj.getString("note") else null,
                    editingCalculationId = if (cObj.has("editingCalculationId") && !cObj.isNull("editingCalculationId")) cObj.getString("editingCalculationId") else null,
                    groupId = if (cObj.has("groupId") && !cObj.isNull("groupId")) cObj.getString("groupId") else null,
                    paymentStatus = cObj.optString("paymentStatus", "PAID"),
                    calcType = cObj.optString("calcType", if (cObj.optString("paymentStatus", "PAID") == "UNPAID") "CREDIT" else "PERSONNEL"),
                    dueDateEpochMs = if (cObj.has("dueDateEpochMs") && !cObj.isNull("dueDateEpochMs")) cObj.getLong("dueDateEpochMs") else null,
                    reminderEnabled = cObj.optBoolean("reminderEnabled", false),
                    reminderTimeEpochMs = if (cObj.has("reminderTimeEpochMs") && !cObj.isNull("reminderTimeEpochMs")) cObj.getLong("reminderTimeEpochMs") else null
                )

                val itemsList = mutableListOf<CalculationItemEntity>()
                val itemsArr = obj.optJSONArray("items")
                if (itemsArr != null) {
                    for (j in 0 until itemsArr.length()) {
                        val itObj = itemsArr.getJSONObject(j)
                        itemsList.add(
                            CalculationItemEntity(
                                id = itObj.getString("id"),
                                calculationId = itObj.getString("calculationId"),
                                label = itObj.optString("label", ""),
                                amountCentimes = itObj.getLong("amountCentimes"),
                                rawExpression = if (itObj.has("rawExpression") && !itObj.isNull("rawExpression")) itObj.getString("rawExpression") else null,
                                position = itObj.optInt("position", j),
                                createdAtEpochMs = itObj.optLong("createdAtEpochMs", System.currentTimeMillis()),
                                updatedAtEpochMs = itObj.optLong("updatedAtEpochMs", System.currentTimeMillis())
                            )
                        )
                    }
                }
                calcsList.add(CalculationWithItems(calculation = calcEntity, items = itemsList))
            }
        }

        BackupPayload(
            app = app,
            format = format,
            version = version,
            exportedAtEpochMs = exportedAt,
            groups = groupsList,
            calculations = calcsList
        )
    }

    suspend fun writeBackupToStream(outputStream: OutputStream): Unit = withContext(Dispatchers.IO) {
        val payload = createBackupPayload()
        val json = serializeToJson(payload)
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(json)
            writer.flush()
        }
    }

    suspend fun readBackupFromStream(inputStream: InputStream): Result<BackupPayload> = withContext(Dispatchers.IO) {
        runCatching {
            val json = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseFromJson(json).getOrThrow()
        }
    }

    suspend fun createShareableBackupFile(context: Context): Uri = withContext(Dispatchers.IO) {
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        val dateSuffix = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
        val file = File(backupDir, "sarf_backup_${dateSuffix}.calc")

        val payload = createBackupPayload()
        val json = serializeToJson(payload)
        file.writeText(json, Charsets.UTF_8)

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun restore(payload: BackupPayload, replaceExisting: Boolean): Unit = withContext(Dispatchers.IO) {
        groupDao.restoreGroups(payload.groups, replaceExisting)
        calculationDao.restoreCalculations(payload.calculations, replaceExisting)
    }
}

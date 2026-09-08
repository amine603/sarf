package com.cash.guide.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class CalculationTemplate(
    val id: String,
    val title: String,
    val calcType: String = "PERSONNEL", // "PERSONNEL" or "CREDIT"
    val currency: String = "DIRHAM",    // "DIRHAM" or "RIAL"
    val itemLabels: List<String> = emptyList(),
    val isCustom: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

class TemplateRepository(private val context: Context) {

    private val customTemplatesFile = File(context.filesDir, "calculation_templates.json")
    private val _customTemplates = MutableStateFlow<List<CalculationTemplate>>(emptyList())
    val customTemplates: Flow<List<CalculationTemplate>> = _customTemplates.asStateFlow()

    init {
        loadCustomTemplatesSync()
    }

    private fun loadCustomTemplatesSync() {
        try {
            if (!customTemplatesFile.exists()) {
                _customTemplates.value = emptyList()
                return
            }
            val jsonStr = customTemplatesFile.readText(Charsets.UTF_8)
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<CalculationTemplate>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val itemsArr = obj.optJSONArray("itemLabels") ?: JSONArray()
                val items = mutableListOf<String>()
                for (j in 0 until itemsArr.length()) {
                    items.add(itemsArr.getString(j))
                }
                list.add(
                    CalculationTemplate(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        calcType = obj.optString("calcType", "PERSONNEL"),
                        currency = obj.optString("currency", "DIRHAM"),
                        itemLabels = items,
                        isCustom = true,
                        createdAtEpochMs = obj.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
            _customTemplates.value = list
        } catch (e: Exception) {
            _customTemplates.value = emptyList()
        }
    }

    private suspend fun persistCustomTemplates() = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            _customTemplates.value.forEach { tpl ->
                val obj = JSONObject().apply {
                    put("id", tpl.id)
                    put("title", tpl.title)
                    put("calcType", tpl.calcType)
                    put("currency", tpl.currency)
                    val itemsArr = JSONArray()
                    tpl.itemLabels.forEach { itemsArr.put(it) }
                    put("itemLabels", itemsArr)
                    put("createdAtEpochMs", tpl.createdAtEpochMs)
                }
                jsonArray.put(obj)
            }
            customTemplatesFile.writeText(jsonArray.toString(2), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveCustomTemplate(
        title: String,
        calcType: String,
        currency: String,
        itemLabels: List<String>
    ): CalculationTemplate {
        val cleanTitle = title.trim().ifBlank { "Modèle sans titre" }
        val cleanItems = itemLabels.map { it.trim() }.filter { it.isNotBlank() }
        val newTemplate = CalculationTemplate(
            id = UUID.randomUUID().toString(),
            title = cleanTitle,
            calcType = calcType,
            currency = currency,
            itemLabels = cleanItems,
            isCustom = true,
            createdAtEpochMs = System.currentTimeMillis()
        )
        val updated = listOf(newTemplate) + _customTemplates.value.filterNot { it.title.equals(cleanTitle, ignoreCase = true) }
        _customTemplates.value = updated
        persistCustomTemplates()
        return newTemplate
    }

    suspend fun deleteCustomTemplate(id: String) {
        val updated = _customTemplates.value.filterNot { it.id == id }
        _customTemplates.value = updated
        persistCustomTemplates()
    }

    fun getBuiltInTemplates(isRtl: Boolean): List<CalculationTemplate> {
        return if (isRtl) {
            listOf(
                CalculationTemplate(
                    id = "builtin_commerce",
                    title = "🛒 سلعة وتجارة",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("سلعة للبيع", "تغليف وكرتون", "توصيل ونقل", "سلعة من عند الفورنيسور"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_chantier",
                    title = "🏗️ ورشة وبناء",
                    calcType = "CREDIT",
                    currency = "DIRHAM",
                    itemLabels = listOf("إسمنت ورمل", "بريك وحديد", "خدّامة (يومية)", "نقل ومازوط"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_daily",
                    title = "☕ مصاريف يومية",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("فطور الصباح", "قهوة واستراحة", "غداء", "تنقل وطاكسي", "مصاريف متنوعة"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_transport",
                    title = "🚗 تنقل وسفر",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("مازوط / إيصانص", "غسيل وتنظيف", "طريق سيار (بياج)", "موقف سيارات"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_home",
                    title = "🏠 كراء ومنزل",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("كراء الشهر", "فاتورة الماء والكهرباء", "أنترنيت وويفي", "واجبات السانديك"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_souq",
                    title = "🥩 سوق وتقدية",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("خضار ومسائل الطياب", "دجاج ولحم", "فواكه الموسم", "مواد تموينية وحانوت", "سمك"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_salaires",
                    title = "👥 أجور العمال",
                    calcType = "CREDIT",
                    currency = "DIRHAM",
                    itemLabels = listOf("أجرة عامل 1", "أجرة عامل 2", "ساعات إضافية", "تسبيق (أفونس)"),
                    isCustom = false
                )
            )
        } else {
            listOf(
                CalculationTemplate(
                    id = "builtin_commerce",
                    title = "🛒 Commerce & Marchandise",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("Marchandise d'achat", "Emballage & Cartons", "Transport & Livraison", "Avance Fournisseur"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_chantier",
                    title = "🏗️ Chantier & Travaux",
                    calcType = "CREDIT",
                    currency = "DIRHAM",
                    itemLabels = listOf("Ciment & Sable", "Briques & Ferraille", "Ouvriers (Journée)", "Transport & Carburant"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_daily",
                    title = "☕ Dépenses quotidiennes",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("Petit-déjeuner", "Café & Pause", "Déjeuner", "Taxi / Transport", "Frais divers"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_transport",
                    title = "🚗 Transport & Carburant",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("Plein Mazout", "Lavage & Entretien", "Péage autoroute", "Stationnement"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_home",
                    title = "🏠 Loyer & Maison",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("Loyer du mois", "Facture Eau & Électricité", "Internet Wifi", "Frais Syndic"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_souq",
                    title = "🥩 Marché & Épicerie",
                    calcType = "PERSONNEL",
                    currency = "DIRHAM",
                    itemLabels = listOf("Légumes frais", "Viande & Volailles", "Fruits de saison", "Épicerie générale", "Poissons"),
                    isCustom = false
                ),
                CalculationTemplate(
                    id = "builtin_salaires",
                    title = "👥 Salaires & Équipe",
                    calcType = "CREDIT",
                    currency = "DIRHAM",
                    itemLabels = listOf("Salaire ouvrier 1", "Salaire ouvrier 2", "Heures supplémentaires", "Avance sur salaire"),
                    isCustom = false
                )
            )
        }
    }

    fun getTemplate(id: String, isRtl: Boolean): CalculationTemplate? {
        val custom = _customTemplates.value.firstOrNull { it.id == id }
        if (custom != null) return custom
        return getBuiltInTemplates(isRtl).firstOrNull { it.id == id }
    }

    companion object {
        @Volatile
        private var INSTANCE: TemplateRepository? = null

        fun getInstance(context: Context): TemplateRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TemplateRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

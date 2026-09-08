package com.cash.guide.data

import android.content.Context
import com.cash.guide.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object DataSeeder {

    suspend fun seedCleanData(context: Context) = withContext(Dispatchers.IO) {
        val db = HssabiDatabase.getInstance(context)
        val calcDao = db.calculationDao()
        val groupDao = db.calculationGroupDao()

        // 1. Clear all existing data
        calcDao.deleteAllCalculations()
        groupDao.deleteAllGroups()

        val now = System.currentTimeMillis()
        val hour = 3600_000L
        val day = 86400_000L

        // 2. Groups
        val groups = listOf(
            CalculationGroupEntity(
                id = "group_marche",
                name = "Taqdiya & Marché",
                colorHex = "#E5A93C", // Pastel Warm Amber
                createdAtEpochMs = now - 15 * day,
                updatedAtEpochMs = now - 15 * day
            ),
            CalculationGroupEntity(
                id = "group_dar",
                name = "Mssarif d Dar",
                colorHex = "#9BD7D5", // Pastel Aqua
                createdAtEpochMs = now - 15 * day,
                updatedAtEpochMs = now - 15 * day
            ),
            CalculationGroupEntity(
                id = "group_voiture",
                name = "Voiture & Mazout",
                colorHex = "#89B5D8", // Pastel Steel Blue
                createdAtEpochMs = now - 14 * day,
                updatedAtEpochMs = now - 14 * day
            ),
            CalculationGroupEntity(
                id = "group_chantier",
                name = "Chantier & Travaux",
                colorHex = "#E28862", // Pastel Peach Coral
                createdAtEpochMs = now - 14 * day,
                updatedAtEpochMs = now - 14 * day
            ),
            CalculationGroupEntity(
                id = "group_khdama",
                name = "Salaires Khdama",
                colorHex = "#C9DDA0", // Pastel Sage Green
                createdAtEpochMs = now - 12 * day,
                updatedAtEpochMs = now - 12 * day
            ),
            CalculationGroupEntity(
                id = "group_commerce",
                name = "Fournisseurs & Sel3a",
                colorHex = "#B3C5E7", // Pastel Periwinkle
                createdAtEpochMs = now - 12 * day,
                updatedAtEpochMs = now - 12 * day
            ),
            CalculationGroupEntity(
                id = "group_loisirs",
                name = "Qahwa & Sorties",
                colorHex = "#D3C5E5", // Pastel Lavender
                createdAtEpochMs = now - 10 * day,
                updatedAtEpochMs = now - 10 * day
            )
        )

        for (g in groups) {
            groupDao.insertGroup(g)
        }

        // Helper data class for building seeds
        data class SeedItem(val label: String, val amountCentimes: Long, val expression: String? = null)
        data class SeedCalc(
            val title: String,
            val groupId: String?,
            val currency: String,
            val offsetMs: Long,
            val note: String? = null,
            val items: List<SeedItem>
        )

        val seedCalculations = listOf(
            // --- TODAY ---
            SeedCalc(
                title = "Taqdiya d Souq Larba3",
                groupId = "group_marche",
                currency = "DIRHAM",
                offsetMs = 1 * hour,
                note = "Taqdiya dial simana m3a l-khodra triya",
                items = listOf(
                    SeedItem("Lhem lbaqri (2kg)", 19000L, "190"),
                    SeedItem("Djej romi mghssoul (3kg)", 6500L, "65"),
                    SeedItem("Batata w Maticha w Bssla", 4500L, "45"),
                    SeedItem("Disser (Banan w Tffa7)", 3500L, "35"),
                    SeedItem("Zitoun mchermel w 7amed", 1500L, "15"),
                    SeedItem("Rbi3 w na3na3 w chiba", 800L, "8")
                )
            ),
            SeedCalc(
                title = "Café & Ftour Client",
                groupId = "group_loisirs",
                currency = "DIRHAM",
                offsetMs = 3 * hour,
                note = "Rendez-vous projet avec Si Mohamed",
                items = listOf(
                    SeedItem("2 Café Crème", 3600L, "36"),
                    SeedItem("2 Jus d'Orange Pressé", 4000L, "40"),
                    SeedItem("Omelette Khlii3", 4500L, "45"),
                    SeedItem("Bouteille d'eau Sidi Ali", 1200L, "12")
                )
            ),
            SeedCalc(
                title = "Hanout d L7ouma",
                groupId = "group_dar",
                currency = "DIRHAM",
                offsetMs = 6 * hour,
                items = listOf(
                    SeedItem("2 Lait Centrale UHT", 1600L, "16"),
                    SeedItem("Bidou d Zit 5L Lesieur", 8800L, "88"),
                    SeedItem("Fromage rouge 250g", 3200L, "32"),
                    SeedItem("Khobz dar (4 khobzat)", 800L, "8"),
                    SeedItem("Pack Danone vanille", 1400L, "14")
                )
            ),

            // --- YESTERDAY ---
            SeedCalc(
                title = "Plein Mazout & Lavage",
                groupId = "group_voiture",
                currency = "DIRHAM",
                offsetMs = 25 * hour,
                items = listOf(
                    SeedItem("Plein Diesel Shell V-Power", 52000L, "520"),
                    SeedItem("Lavage complet intérieur/extérieur", 5000L, "50"),
                    SeedItem("Liquide lave-glace", 2500L, "25"),
                    SeedItem("Café express station", 1500L, "15")
                )
            ),
            SeedCalc(
                title = "Factures d Chhar",
                groupId = "group_dar",
                currency = "DIRHAM",
                offsetMs = 28 * hour,
                note = "Factures réglées via l'application bancaire",
                items = listOf(
                    SeedItem("Facture Lydec (Lma w Ddo)", 48500L, "485"),
                    SeedItem("Abonnement Maroc Telecom Fibre", 24900L, "249"),
                    SeedItem("Cotisation Sandik Immeuble", 15000L, "150"),
                    SeedItem("Recharge Inwi 4G", 5000L, "50")
                )
            ),
            SeedCalc(
                title = "Pharmacie d Garde",
                groupId = "group_dar",
                currency = "DIRHAM",
                offsetMs = 33 * hour,
                items = listOf(
                    SeedItem("Doliprane 1000mg (2 boîtes)", 3400L, "34"),
                    SeedItem("Sirop Toux Humex", 4800L, "48"),
                    SeedItem("Vitamine C Effervescente", 5500L, "55"),
                    SeedItem("Sérum physiologique & Coton", 2200L, "22")
                )
            ),

            // --- 2 DAYS AGO ---
            SeedCalc(
                title = "Sel3a d Sbagha - Chantier",
                groupId = "group_chantier",
                currency = "DIRHAM",
                offsetMs = 48 * hour,
                items = listOf(
                    SeedItem("2 Sradel Vinyl Mat Blanc Astral", 76000L, "760"),
                    SeedItem("Sattel Kolat Ciment Sika", 18000L, "180"),
                    SeedItem("Enduit de lissage (2 sacs)", 14000L, "140"),
                    SeedItem("3 Rouleaux antigoutte", 9500L, "95"),
                    SeedItem("Papier verre w Scotch masquage", 6500L, "65")
                )
            ),
            SeedCalc(
                title = "Déjeuner Équipe Chantier",
                groupId = "group_loisirs",
                currency = "DIRHAM",
                offsetMs = 53 * hour,
                items = listOf(
                    SeedItem("Grillades mixtes (1kg kefta & kotlet)", 18000L, "180"),
                    SeedItem("Salades marocaines & Tektouka", 3500L, "35"),
                    SeedItem("Bouteille Oulmès & Coca", 3000L, "30"),
                    SeedItem("Pastèque & Melon", 2500L, "25")
                )
            ),

            // --- 3 DAYS AGO ---
            SeedCalc(
                title = "Salaires Semaine Khdama",
                groupId = "group_khdama",
                currency = "DIRHAM",
                offsetMs = 74 * hour,
                note = "Règlement semaine du 31 Août au 05 Septembre",
                items = listOf(
                    SeedItem("Rachid (M3ellem plâtrier)", 180000L, "1800"),
                    SeedItem("Hassan (Ouvrier qualifié)", 130000L, "1300"),
                    SeedItem("Mustapha (Aide & Manœuvre)", 100000L, "1000"),
                    SeedItem("Transport aller-retour équipe", 20000L, "200")
                )
            ),
            SeedCalc(
                title = "Fournisseur Carton & Emballage",
                groupId = "group_commerce",
                currency = "RIAL",
                offsetMs = 78 * hour,
                note = "Facture N° 842 payée en espèces",
                items = listOf(
                    SeedItem("500 Cartons format moyen", 125000L, "25000"),
                    SeedItem("20 Rouleaux Adhésif Marron", 45000L, "9000"),
                    SeedItem("Papier bulle 100 mètres", 60000L, "12000"),
                    SeedItem("Frais de livraison camion", 30000L, "6000")
                )
            ),

            // --- LAST WEEK ---
            SeedCalc(
                title = "Vidange & Plaquettes Voiture",
                groupId = "group_voiture",
                currency = "DIRHAM",
                offsetMs = 120 * hour,
                items = listOf(
                    SeedItem("Huile Moteur Total 5W30 (5L)", 46000L, "460"),
                    SeedItem("Filtre à Huile & Filtre à Air", 16000L, "160"),
                    SeedItem("Filtre à Gasoil", 14000L, "140"),
                    SeedItem("Jeu de Plaquettes de frein Bosch", 34000L, "340"),
                    SeedItem("Main d'œuvre garage Si Larbi", 15000L, "150")
                )
            ),
            SeedCalc(
                title = "Fournitures Scolaires Rentrée",
                groupId = "group_dar",
                currency = "DIRHAM",
                offsetMs = 145 * hour,
                note = "Librairie Al Wahda - Préparation rentrée",
                items = listOf(
                    SeedItem("Livres scolaires Primaire & Collège", 68000L, "680"),
                    SeedItem("Cahiers 200p & 100p (lot de 12)", 12000L, "120"),
                    SeedItem("2 Cartables ergonomiques", 35000L, "350"),
                    SeedItem("Trousses, stylos, feutres & règles", 11000L, "110")
                )
            ),
            SeedCalc(
                title = "Électricité & Câblage Magasin",
                groupId = "group_chantier",
                currency = "DIRHAM",
                offsetMs = 170 * hour,
                items = listOf(
                    SeedItem("3 Rouleaux Câble 2.5mm", 45000L, "450"),
                    SeedItem("Spots LED encastrables (lot de 8)", 32000L, "320"),
                    SeedItem("Disjoncteur différentiel Legrand", 18000L, "180"),
                    SeedItem("Prises et interrupteurs Simon", 14000L, "140"),
                    SeedItem("Goulottes & accessoires fixation", 7500L, "75")
                )
            ),
            SeedCalc(
                title = "Sortie Familiale Weekend Plage",
                groupId = "group_loisirs",
                currency = "DIRHAM",
                offsetMs = 195 * hour,
                items = listOf(
                    SeedItem("Parasol & 4 chaises pliantes location", 8000L, "80"),
                    SeedItem("Déjeuner Poissons frais Mohammedia", 38000L, "380"),
                    SeedItem("Glaces & Goûter pour les enfants", 7500L, "75"),
                    SeedItem("Péage et parking gardé", 4500L, "45")
                )
            ),
            SeedCalc(
                title = "Boucherie - Lhem & Kfta",
                groupId = "group_marche",
                currency = "DIRHAM",
                offsetMs = 220 * hour,
                items = listOf(
                    SeedItem("3kg Viande de veau filet", 33000L, "330"),
                    SeedItem("1.5kg Kefta fraîche assaisonnée", 16500L, "165"),
                    SeedItem("Saucisses de bœuf fraîches", 8500L, "85")
                )
            ),
            SeedCalc(
                title = "Voyage Professionnel Tanger",
                groupId = "group_commerce",
                currency = "DIRHAM",
                offsetMs = 245 * hour,
                items = listOf(
                    SeedItem("Billet Al Boraq Aller-Retour Casa-Tanger", 32000L, "320"),
                    SeedItem("Nuitée Hôtel ibis", 48000L, "480"),
                    SeedItem("Repas & Dîner d'affaires", 25000L, "250"),
                    SeedItem("Taxi & déplacements sur place", 9000L, "90")
                )
            ),
            SeedCalc(
                title = "Réparation Climatiseur & Gaz",
                groupId = "group_dar",
                currency = "DIRHAM",
                offsetMs = 270 * hour,
                items = listOf(
                    SeedItem("Recharge Gaz R410A", 35000L, "350"),
                    SeedItem("Nettoyage des filtres & désinfection", 12000L, "120"),
                    SeedItem("Main d'œuvre frigoriste", 15000L, "150")
                )
            ),
            SeedCalc(
                title = "Sel3a Fournisseur Tissus",
                groupId = "group_commerce",
                currency = "RIAL",
                offsetMs = 300 * hour,
                note = "Sel3a d l-khiyata dial l-3id",
                items = listOf(
                    SeedItem("Toub Mlifa première qualité (15m)", 210000L, "42000"),
                    SeedItem("Toub Soie pour doublure (20m)", 100000L, "20000"),
                    SeedItem("Sfifa w 3qad sfifa beldia", 70000L, "14000")
                )
            )
        )

        for (seed in seedCalculations) {
            val calcId = UUID.randomUUID().toString()
            val time = now - seed.offsetMs
            val calcEntity = CalculationEntity(
                id = calcId,
                title = seed.title,
                currency = seed.currency,
                createdAtEpochMs = time,
                updatedAtEpochMs = time,
                status = "SAVED",
                note = seed.note,
                groupId = seed.groupId
            )

            val itemEntities = seed.items.mapIndexed { idx, item ->
                CalculationItemEntity(
                    id = UUID.randomUUID().toString(),
                    calculationId = calcId,
                    label = item.label,
                    amountCentimes = item.amountCentimes,
                    rawExpression = item.expression ?: item.label,
                    position = idx,
                    createdAtEpochMs = time,
                    updatedAtEpochMs = time
                )
            }

            calcDao.upsertCalculationWithItems(calcEntity, itemEntities)
        }
    }
}
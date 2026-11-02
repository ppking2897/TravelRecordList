package io.github.ppking2897.travelrecord.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.fail

/**
 * 架構規則：哪一層、哪個套件可以依賴誰
 *
 * 規則與理由寫在 `.claude/docs/architecture-rules.md`，每條規則一個測試，名稱帶規則編號。
 * 單一模組裡編譯擋不住分層，所以直接讀 `src/` 的原始碼比對 import 與完整名稱的引用。
 * 例外清單只能變短：清單外出現新違規會紅，清單裡某項已經不違規也會紅。
 */
class ArchitectureRulesTest {

    @Test
    fun `ARCH001 domain 不依賴 data presentation di`() = assertRule(
        rule = "ARCH001 domain 不依賴 data、presentation、di",
        fix = "改依賴 domain 的介面或 model；實作與組裝放 data、di",
        files = mainFiles.filter { it.layer == DOMAIN },
    ) { _, target -> target.layer in setOf(DATA, PRESENTATION, DI) }

    @Test
    fun `ARCH002 presentation 不依賴 data`() = assertRule(
        rule = "ARCH002 presentation 不依賴 data",
        fix = "改用 domain 的 use case 或 repository 介面；測試要用的資料層在 test 套件組好（InMemoryRepositories、TestUseCases）",
        files = allFiles.filter { it.layer == PRESENTATION },
    ) { _, target -> target.layer == DATA }

    @Test
    fun `ARCH003 data 不依賴 presentation di`() = assertRule(
        rule = "ARCH003 data 不依賴 presentation、di",
        fix = "畫面要的格式在 presentation 轉換；組裝放 di",
        files = mainFiles.filter { it.layer == DATA },
    ) { _, target -> target.layer in setOf(PRESENTATION, DI) }

    @Test
    fun `ARCH004 domain 只用 Kotlin 標準庫 datetime coroutines`() = assertRule(
        rule = "ARCH004 domain 不用框架",
        fix = "domain 只用 kotlin.*、kotlinx.datetime、kotlinx.coroutines 與專案的 domain；用到框架的部分放 data 或 presentation",
        files = mainFiles.filter { it.layer == DOMAIN },
    ) { _, target ->
        if (target.isProject) target.layer == null
        else DOMAIN_ALLOWED_EXTERNAL.none { target.name.startsWith(it) }
    }

    @Test
    fun `ARCH005 feature 的 presentation 不依賴別的 feature 的 presentation`() = assertRule(
        rule = "ARCH005 畫面 feature 之間不互相依賴",
        fix = "共用的畫面元件移到 core.presentation；共用的資料走資料 feature 的 domain",
        files = mainFiles.filter { it.layer == PRESENTATION && it.feature != null },
    ) { source, target -> target.layer == PRESENTATION && target.feature != null && target.feature != source.feature }

    @Test
    fun `ARCH006 common 原始碼不依賴 android java`() = assertRule(
        rule = "ARCH006 共用程式碼不用 android、java",
        fix = "平台程式放 androidMain，用 expect／actual 接到 commonMain",
        files = allFiles.filter { it.sourceSet in setOf("commonMain", "commonTest") },
    ) { _, target -> !target.isProject && PLATFORM_PREFIXES.any { target.name.startsWith(it) } }

    @Test
    fun `ARCH007 core 不依賴 feature 的 data presentation di`() = assertRule(
        rule = "ARCH007 core 不依賴 feature 的 data、presentation、di",
        fix = "改依賴 feature 的 domain，或把這段移到 feature",
        files = mainFiles.filter { it.isCore },
        exceptions = ARCH007_EXCEPTIONS,
    ) { source, target ->
        val appDatabase = source.pkg == "$BASE.core.data.local.db" &&
            target.layer == DATA && target.name.contains(".data.local.")
        target.feature != null && target.layer in setOf(DATA, PRESENTATION, DI) && !appDatabase
    }

    @Test
    fun `ARCH008 feature 不依賴根套件`() = assertRule(
        rule = "ARCH008 feature 不依賴根套件",
        fix = "要共用的東西移到 core（例：LocalSnackbarHostState 搬到 core.presentation）",
        files = mainFiles.filter { it.feature != null },
        exceptions = ARCH008_EXCEPTIONS,
    ) { _, target -> target.isRoot }

    // ---- 比對 ----

    private fun assertRule(
        rule: String,
        fix: String,
        files: List<SourceFile>,
        exceptions: List<Allowed> = emptyList(),
        violates: (SourceFile, Reference) -> Boolean,
    ) {
        val anchor = rule.substringBefore(' ').lowercase()
        val found = files.flatMap { file ->
            file.references.filter { violates(file, it) }.map { file to it }
        }
        val isExcepted = { file: SourceFile, ref: Reference ->
            exceptions.any { it.path == file.path && it.target == ref.name }
        }
        val newViolations = found.filterNot { (file, ref) -> isExcepted(file, ref) }
        val staleExceptions = exceptions.filterNot { e ->
            found.any { (file, ref) -> e.path == file.path && e.target == ref.name }
        }
        if (newViolations.isEmpty() && staleExceptions.isEmpty()) return

        val message = buildString {
            appendLine(rule)
            newViolations.forEach { (file, ref) ->
                appendLine("  composeApp/src/${file.path}:${ref.line}")
                appendLine("  實際：${ref.describe()}")
                appendLine("  應該：$fix")
                appendLine("  規則：$DOC#$anchor")
            }
            staleExceptions.forEach { e ->
                appendLine("  composeApp/src/${e.path}")
                appendLine("  例外已不違規：${e.target}")
                appendLine("  應該：請從例外清單刪掉這項（${anchor.uppercase()}_EXCEPTIONS）")
            }
        }
        fail(message)
    }

    // ---- 讀原始碼 ----

    private data class Reference(val name: String, val line: Int, val isImport: Boolean) {
        val isProject = name == BASE || name.startsWith("$BASE.")
        private val segments = if (isProject) name.removePrefix(BASE).trim('.').split('.').filter { it.isNotEmpty() } else emptyList()
        val feature: String? = if (segments.firstOrNull() == "feature") segments.getOrNull(1) else null
        val layer: String? = if (isProject) layerOf(segments) else null

        /** 根套件的宣告：`travelrecord.App`、`travelrecord.LocalSnackbarHostState.current` */
        val isRoot = isProject && (segments.isEmpty() || segments.size == 1 || segments.first().first().isUpperCase())

        fun describe() = if (isImport) "import $name" else "直接寫完整名稱 $name"
    }

    private data class SourceFile(val sourceSet: String, val path: String, val pkg: String, val references: List<Reference>) {
        private val segments = pkg.removePrefix(BASE).trim('.').split('.').filter { it.isNotEmpty() }
        val feature: String? = if (segments.firstOrNull() == "feature") segments.getOrNull(1) else null
        val layer: String? = if (pkg.startsWith(BASE)) layerOf(segments) else null
        val isCore = segments.firstOrNull() == "core"
    }

    private data class Allowed(val path: String, val target: String, val reason: String)

    private companion object {
        const val BASE = "io.github.ppking2897.travelrecord"
        const val DOC = ".claude/docs/architecture-rules.md"
        const val DATA = "data"
        const val DOMAIN = "domain"
        const val PRESENTATION = "presentation"
        const val DI = "di"
        val LAYERS = setOf(DATA, DOMAIN, PRESENTATION, DI)
        val TEST_SOURCE_SETS = setOf("commonTest", "androidHostTest")
        val DOMAIN_ALLOWED_EXTERNAL = listOf("kotlin.", "kotlinx.datetime.", "kotlinx.coroutines.")
        val PLATFORM_PREFIXES = listOf("android.", "java.", "javax.")

        val ARCH007_EXCEPTIONS = listOf(
            "JsonSerializer.kt" to listOf(
                "feature.draft.data.model.DraftResponse",
                "feature.itinerary.data.model.ItineraryResponse",
                "feature.itinerary_item.data.model.ItineraryItemResponse",
            ),
            "LegacyJsonImporter.kt" to listOf(
                "feature.itinerary.data.local.toEntity",
                "feature.itinerary.data.mapper.toDomain",
                "feature.itinerary_item.data.local.toEntity",
                "feature.itinerary_item.data.mapper.toDomain",
            ),
        ).flatMap { (file, targets) ->
            targets.map {
                Allowed(
                    path = "commonMain/kotlin/io/github/ppking2897/travelrecord/core/data/local/$file",
                    target = "$BASE.$it",
                    reason = "2026-10-30 建立規則時已存在：舊版 JSON 格式與搬進 Room 的轉換",
                )
            }
        }

        val ARCH008_EXCEPTIONS = listOf(
            "footprint/presentation/FootprintScreen.kt",
            "itinerary_add_edit/presentation/ItineraryAddEditScreen.kt",
            "itinerary_detail/presentation/ItineraryDetailScreen.kt",
            "itinerary_item/presentation/AddEditItemScreen.kt",
            "itinerary_item/presentation/EditItemScreen.kt",
            "itinerary_list/presentation/ItineraryListScreen.kt",
            "memory/presentation/MemoryScreen.kt",
            "route/presentation/RouteViewScreen.kt",
            "today/presentation/TodayScreen.kt",
        ).map {
            Allowed(
                path = "commonMain/kotlin/io/github/ppking2897/travelrecord/feature/$it",
                target = "$BASE.LocalSnackbarHostState",
                reason = "2026-10-30 建立規則時已存在：LocalSnackbarHostState 定義在 App.kt，搬到 core.presentation 另外做",
            )
        }

        fun layerOf(segments: List<String>): String? = when {
            segments.firstOrNull() == "feature" -> segments.getOrNull(2)?.takeIf { it in LAYERS }
            segments.firstOrNull() == "core" -> segments.getOrNull(1)?.takeIf { it in LAYERS }
            // 舊寫法的頂層 data／domain／presentation 套件
            else -> segments.firstOrNull()?.takeIf { it in LAYERS && it != DI }
        }

        val allFiles: List<SourceFile> by lazy { readSources() }
        val mainFiles: List<SourceFile> by lazy { allFiles.filter { it.sourceSet !in TEST_SOURCE_SETS } }

        val packageRegex = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE)
        val importRegex = Regex("""^import\s+([\w.]+)""")
        val projectFqnRegex = Regex("""(?<![\w.])${Regex.escape(BASE)}(?:\.\w+)*""")

        /** 程式裡直接寫的完整名稱：小寫套件段 + 大寫開頭的類別，例 `kotlin.time.Instant`、`android.content.Context` */
        val externalFqnRegex = Regex("""(?<![\w.])[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+\.[A-Z]\w*""")

        fun readSources(): List<SourceFile> {
            // Gradle 的測試工作目錄是模組目錄（composeApp）
            val src = File("src")
            check(src.isDirectory) { "找不到 ${src.absolutePath}，測試要在 composeApp 模組目錄下執行" }
            val files = src.listFiles().orEmpty().filter { it.isDirectory }.flatMap { sourceSet ->
                File(sourceSet, "kotlin").walkTopDown().filter { it.isFile && it.extension == "kt" }.map { file ->
                    parse(sourceSet.name, file.relativeTo(src).invariantSeparatorsPath, file.readText())
                }.toList()
            }
            check(files.isNotEmpty()) { "${src.absolutePath} 底下沒有讀到任何 .kt 檔" }
            return files
        }

        fun parse(sourceSet: String, path: String, text: String): SourceFile {
            val pkg = packageRegex.find(text)?.groupValues?.get(1).orEmpty()
            val references = mutableListOf<Reference>()
            stripCommentsAndStrings(text).lines().forEachIndexed { index, line ->
                val trimmed = line.trim()
                val lineNo = index + 1
                when {
                    trimmed.startsWith("package ") -> Unit
                    trimmed.startsWith("import ") -> importRegex.find(trimmed)?.let {
                        references += Reference(it.groupValues[1].trimEnd('.'), lineNo, isImport = true)
                    }
                    else -> {
                        projectFqnRegex.findAll(line).forEach { references += Reference(it.value, lineNo, isImport = false) }
                        externalFqnRegex.findAll(line)
                            .filterNot { it.value.startsWith("$BASE.") }
                            .forEach { references += Reference(it.value, lineNo, isImport = false) }
                    }
                }
            }
            return SourceFile(sourceSet, path, pkg, references)
        }

        /** 把註解與字串內容換成空白，保留換行讓行號對得上 */
        fun stripCommentsAndStrings(text: String): String {
            val out = StringBuilder(text.length)
            var i = 0
            fun blank(until: Int) {
                while (i < until && i < text.length) {
                    out.append(if (text[i] == '\n') '\n' else ' ')
                    i++
                }
            }
            while (i < text.length) {
                when {
                    text.startsWith("//", i) -> blank(text.indexOf('\n', i).let { if (it < 0) text.length else it })
                    text.startsWith("/*", i) -> blank(text.indexOf("*/", i + 2).let { if (it < 0) text.length else it + 2 })
                    text.startsWith("\"\"\"", i) -> blank(text.indexOf("\"\"\"", i + 3).let { if (it < 0) text.length else it + 3 })
                    text[i] == '"' || text[i] == '\'' -> {
                        val quote = text[i]
                        var j = i + 1
                        while (j < text.length && text[j] != quote && text[j] != '\n') j += if (text[j] == '\\') 2 else 1
                        blank(minOf(j + 1, text.length))
                    }
                    else -> { out.append(text[i]); i++ }
                }
            }
            return out.toString()
        }
    }
}

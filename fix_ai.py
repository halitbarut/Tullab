import re

def process_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()
    for pattern, replacement in replacements:
        content = re.sub(pattern, replacement, content, flags=re.MULTILINE | re.DOTALL)
    with open(filepath, 'w') as f:
        f.write(content)

# DashboardScreen
db_replacements = [
    (r'\n                            TextButton\(onClick = onGenerateAgain\).*?    \}\n\}\n', ''),
    (r'        val previewAiState = AiInsightsUiState\([^)]+\)\n', ''),
    (r'            aiInsightsState = previewAiState,\n            onGenerateAiInsights = \{\},\n', '')
]

# HomeworkViewModel
hw_replacements = [
    (r'import com\.barutdev\.kora\.domain\.model\.ai\.[^\n]+\n', ''),
    (r'import com\.barutdev\.kora\.domain\.repository\.Ai[^\n]+\n', ''),
    (r'import com\.barutdev\.kora\.domain\.usecase\.GenerateAiInsightsUseCase\n', ''),
    (r'import com\.barutdev\.kora\.ui\.model\.Ai[^\n]+\n', ''),
    (r'    private val aiInsightsCacheRepository: AiInsightsCacheRepository,\n    private val aiInsightsGenerationTracker: AiInsightsGenerationTracker,\n', ''),
    (r'    private val generateAiInsightsUseCase: GenerateAiInsightsUseCase\n', ''),
    (r'    private val _aiInsightsState = MutableStateFlow.*?val aiInsightsState: StateFlow<AiInsightsUiState> = _aiInsightsState\.asStateFlow\(\)\n\n', ''),
    (r'    private var lastRequestedLocale: Locale\? = null\n.*?isCacheFetchInProgress: Boolean = false\n\n', ''),
    (r'    private fun observeAiInsights.*?private fun mapAiResultToUiState[^\}]+\}\n', '')
]

# HomeworkScreen
hws_replacements = [
    (r'import androidx\.compose\.material\.icons\.outlined\.Psychology\n', ''),
    (r'import com\.barutdev\.kora\.ui\.model\.AiInsightsUiState\nimport com\.barutdev\.kora\.ui\.model\.AiStatus\n', ''),
    (r'    val aiInsightsState by viewModel\.aiInsightsState\.collectAsStateWithLifecycle\(\)\n', ''),
    (r'    LaunchedEffect\(viewModel\.studentId, locale\) \{\n        if \(viewModel\.hasStudentReference\) \{\n            viewModel\.ensureAiInsights\(locale\)\n        \}\n    \}\n', ''),
    (r'        aiInsightsState = aiInsightsState,\n        onGenerateAiInsights = \{ viewModel\.retryAiInsights\(locale\) \},\n', ''),
    (r'    aiInsightsState: AiInsightsUiState,\n    onGenerateAiInsights: \(\) -> Unit,\n', ''),
    (r'        // Card 0: AI Assistant\n        AnimatedListItem\(index = 0\) \{\n            AiAssistantCard\(\n                studentName = studentName,\n                aiState = aiInsightsState,\n                onGenerateAgain = onGenerateAiInsights\n            \)\n        \}\n        \n', ''),
    (r'@OptIn\(ExperimentalAnimationApi::class\)\n@Composable\nprivate fun AiAssistantCard.*?\}\n\n@Composable\nprivate fun HomeworkListItem', '@Composable\nprivate fun HomeworkListItem'),
    (r'    val previewAiState = AiInsightsUiState\([^)]+\)\n', ''),
    (r'            aiInsightsState = previewAiState,\n            onGenerateAiInsights = \{\},\n', '')
]

# HomeworkViewModelTest
hw_test_replacements = [
    (r'import com\.barutdev\.kora\.domain\.model\.ai\.[^\n]+\n', ''),
    (r'import com\.barutdev\.kora\.domain\.repository\.Ai[^\n]+\n', ''),
    (r'import com\.barutdev\.kora\.domain\.usecase\.GenerateAiInsightsUseCase\n', ''),
    (r'        val generationTracker = FakeAiInsightsGenerationTracker\(\)\n        val cacheRepository = FakeAiInsightsCacheRepository\(\)\n        val aiUseCase = GenerateAiInsightsUseCase\(\n            studentRepository = studentRepository,\n            lessonRepository = FakeLessonRepository\(\),\n            homeworkRepository = homeworkRepository,\n            aiRepository = FakeAiRepository\(\)\n        \)\n', ''),
    (r'            aiInsightsCacheRepository = cacheRepository,\n            aiInsightsGenerationTracker = generationTracker,\n            generateAiInsightsUseCase = aiUseCase\n', ''),
    (r'private class FakeAiInsightsCacheRepository : AiInsightsCacheRepository \{.*?private class FakeAiRepository : AiRepository \{.*?\}', '')
]

process_file('app/src/main/java/com/barutdev/kora/ui/screens/dashboard/DashboardScreen.kt', db_replacements)
process_file('app/src/main/java/com/barutdev/kora/ui/screens/homework/HomeworkViewModel.kt', hw_replacements)
process_file('app/src/main/java/com/barutdev/kora/ui/screens/homework/HomeworkScreen.kt', hws_replacements)
process_file('app/src/test/java/com/barutdev/kora/ui/screens/homework/HomeworkViewModelTest.kt', hw_test_replacements)

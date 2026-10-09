package org.lepotager.executivefunction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.lepotager.executivefunction.R
import org.lepotager.executivefunction.domain.FocusSessionPreset
import org.lepotager.executivefunction.domain.HomeViewMode

internal data class FirstRunSetupConfig(
    val homeViewMode: HomeViewMode,
    val focusPreset: FocusSessionPreset,
    val quickStartEnabled: Boolean,
    val preferLearnedDuration: Boolean,
    val drawEnabled: Boolean,
    val pauseSuggestionsEnabled: Boolean,
    val pauseAfterMinutes: Int,
    val checkInEnabled: Boolean,
    val adaptationEnabled: Boolean,
    val calmMode: Boolean,
    val autoMiniWindow: Boolean,
)

private enum class FirstRunDifficulty {
    STARTING,
    CHOOSING,
    FOCUSING,
    TIME,
    SWITCHING,
    RETURNING,
    REMEMBERING,
    OTHER,
}

private enum class FirstRunSupport {
    SMALL_STEP,
    TIMER,
    TASK_DIE,
    ONE_NEXT,
    FEW_OPTIONS,
    QUICK_CAPTURE,
    PAUSE,
    MINI_WINDOW,
    SAVE_CONTEXT,
    CHECK_IN,
    REMINDER,
    CALM,
    MINIMAL,
}

private val pausePresets = listOf(15, 25, 45, 60)

/**
 * First-run configuration wizard inspired by the app research survey and Le Jardinier:
 * one useful question at a time, then only the branch-specific follow-up.
 * Nothing is sent anywhere and the user can skip or reopen this flow later.
 */
@Composable
internal fun FirstRunSetupFlow(
    initial: FirstRunSetupConfig,
    onApply: (FirstRunSetupConfig) -> Unit,
    onSkip: () -> Unit,
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var difficultyName by rememberSaveable { mutableStateOf("") }
    var supportNames by rememberSaveable { mutableStateOf("") }
    var homeViewModeName by rememberSaveable { mutableStateOf(initial.homeViewMode.name) }
    var focusPresetName by rememberSaveable { mutableStateOf(initial.focusPreset.name) }
    var quickStartEnabled by rememberSaveable { mutableStateOf(initial.quickStartEnabled) }
    var preferLearnedDuration by rememberSaveable { mutableStateOf(initial.preferLearnedDuration) }
    var drawEnabled by rememberSaveable { mutableStateOf(initial.drawEnabled) }
    var pauseEnabled by rememberSaveable { mutableStateOf(initial.pauseSuggestionsEnabled) }
    var pauseMinutes by rememberSaveable { mutableIntStateOf(initial.pauseAfterMinutes.coerceIn(5, 120)) }
    var customPauseSelected by rememberSaveable {
        mutableStateOf(initial.pauseAfterMinutes !in pausePresets)
    }
    var customPauseText by rememberSaveable {
        mutableStateOf(if (initial.pauseAfterMinutes !in pausePresets) initial.pauseAfterMinutes.toString() else "")
    }
    var checkInEnabled by rememberSaveable { mutableStateOf(initial.checkInEnabled) }
    var adaptationEnabled by rememberSaveable { mutableStateOf(initial.adaptationEnabled) }
    var calmMode by rememberSaveable { mutableStateOf(initial.calmMode) }
    var autoMini by rememberSaveable { mutableStateOf(initial.autoMiniWindow) }

    val difficulty = difficultyName.takeIf { it.isNotBlank() }?.let { FirstRunDifficulty.valueOf(it) }
    val supports = supportNames.split('|')
        .filter { it.isNotBlank() }
        .map { FirstRunSupport.valueOf(it) }
        .toSet()
    val homeViewMode = HomeViewMode.valueOf(homeViewModeName)
    val focusPreset = FocusSessionPreset.valueOf(focusPresetName)
    val customPauseValue = customPauseText.toIntOrNull()
    val customPauseValid = !pauseEnabled || !customPauseSelected ||
        (customPauseValue != null && customPauseValue in 5..120)
    val totalPages = 7

    fun toggleSupport(value: FirstRunSupport) {
        val next = supports.toMutableSet()
        when {
            value == FirstRunSupport.MINIMAL -> {
                if (FirstRunSupport.MINIMAL in next) {
                    next.remove(FirstRunSupport.MINIMAL)
                } else {
                    next.clear()
                    next.add(FirstRunSupport.MINIMAL)
                }
            }
            FirstRunSupport.MINIMAL in next -> {
                next.remove(FirstRunSupport.MINIMAL)
                next.add(value)
            }
            !next.add(value) -> next.remove(value)
        }
        if (next.size <= 2) supportNames = next.joinToString("|") { it.name }
    }

    fun applyBranchDefaults() {
        homeViewModeName = when {
            FirstRunSupport.MINIMAL in supports || FirstRunSupport.ONE_NEXT in supports -> HomeViewMode.ONE_NEXT.name
            FirstRunSupport.FEW_OPTIONS in supports -> HomeViewMode.NOW_NEXT.name
            else -> homeViewModeName
        }
        if (FirstRunSupport.MINIMAL in supports) {
            pauseEnabled = false
            checkInEnabled = false
            autoMini = false
            calmMode = true
            return
        }
        if (FirstRunSupport.TASK_DIE in supports) drawEnabled = true
        if (FirstRunSupport.PAUSE in supports) pauseEnabled = true
        if (FirstRunSupport.MINI_WINDOW in supports) autoMini = true
        if (FirstRunSupport.CHECK_IN in supports) checkInEnabled = true
        if (FirstRunSupport.CALM in supports) calmMode = true
    }

    val canContinue = when (page) {
        0 -> true
        1 -> difficulty != null
        2 -> supports.isNotEmpty()
        4 -> customPauseValid
        else -> true
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (page == 0) stringResource(R.string.setup_intro_eyebrow)
                    else stringResource(R.string.intro_step, page, totalPages - 1),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onSkip) { Text(stringResource(R.string.intro_skip)) }
            }

            when (page) {
                0 -> SetupIntro()
                1 -> DifficultyQuestion(
                    selected = difficulty,
                    onSelect = {
                        difficultyName = it.name
                        supportNames = ""
                    },
                )
                2 -> SupportQuestion(
                    difficulty = requireNotNull(difficulty),
                    selected = supports,
                    onToggle = ::toggleSupport,
                )
                3 -> HomeViewQuestion(
                    selected = homeViewMode,
                    onSelect = { homeViewModeName = it.name },
                )
                4 -> FocusDefaultsQuestion(
                    focusPreset = focusPreset,
                    onFocusPreset = { focusPresetName = it.name },
                    quickStartEnabled = quickStartEnabled,
                    onQuickStartEnabled = { quickStartEnabled = it },
                    preferLearnedDuration = preferLearnedDuration,
                    onPreferLearnedDuration = { preferLearnedDuration = it },
                    pauseEnabled = pauseEnabled,
                    onPauseEnabled = { pauseEnabled = it },
                    pauseMinutes = pauseMinutes,
                    customPauseSelected = customPauseSelected,
                    customPauseText = customPauseText,
                    customPauseValid = customPauseValid,
                    onPresetMinutes = {
                        pauseMinutes = it
                        customPauseSelected = false
                    },
                    onSelectCustom = {
                        customPauseSelected = true
                        if (customPauseText.isBlank()) customPauseText = pauseMinutes.toString()
                    },
                    onCustomPauseText = { raw ->
                        customPauseText = raw.filter(Char::isDigit).take(3)
                        customPauseText.toIntOrNull()?.takeIf { it in 5..120 }?.let { pauseMinutes = it }
                    },
                )
                5 -> LocalBehaviourQuestion(
                    drawEnabled = drawEnabled,
                    onDrawEnabled = { drawEnabled = it },
                    checkInEnabled = checkInEnabled,
                    onCheckInEnabled = { checkInEnabled = it },
                    adaptationEnabled = adaptationEnabled,
                    onAdaptationEnabled = { adaptationEnabled = it },
                    calmMode = calmMode,
                    onCalmMode = { calmMode = it },
                    autoMini = autoMini,
                    onAutoMini = { autoMini = it },
                )
                6 -> SetupReview(
                    difficulty = requireNotNull(difficulty),
                    homeViewMode = homeViewMode,
                    focusPreset = focusPreset,
                    quickStartEnabled = quickStartEnabled,
                    preferLearnedDuration = preferLearnedDuration,
                    drawEnabled = drawEnabled,
                    pauseEnabled = pauseEnabled,
                    pauseMinutes = pauseMinutes,
                    checkInEnabled = checkInEnabled,
                    adaptationEnabled = adaptationEnabled,
                    calmMode = calmMode,
                    autoMini = autoMini,
                )
            }

            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (page == totalPages - 1) {
                    Button(
                        onClick = {
                            onApply(
                                FirstRunSetupConfig(
                                    homeViewMode = homeViewMode,
                                    focusPreset = focusPreset,
                                    quickStartEnabled = quickStartEnabled,
                                    preferLearnedDuration = preferLearnedDuration,
                                    drawEnabled = drawEnabled,
                                    pauseSuggestionsEnabled = pauseEnabled,
                                    pauseAfterMinutes = pauseMinutes,
                                    checkInEnabled = checkInEnabled,
                                    adaptationEnabled = adaptationEnabled,
                                    calmMode = calmMode,
                                    autoMiniWindow = autoMini,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.setup_apply)) }
                } else {
                    Button(
                        enabled = canContinue,
                        onClick = {
                            if (page == 2) applyBranchDefaults()
                            page += 1
                        },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.intro_next)) }
                }
                if (page > 0) {
                    TextButton(
                        onClick = { page -= 1 },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.intro_back)) }
                }
            }
        }
    }
}

@Composable
private fun SetupIntro() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ToolBadge(ToolGlyphKind.START)
        Text(
            stringResource(R.string.setup_intro_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.setup_intro_body),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.setup_intro_privacy),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DifficultyQuestion(
    selected: FirstRunDifficulty?,
    onSelect: (FirstRunDifficulty) -> Unit,
) {
    QuestionHeader(R.string.setup_difficulty_eyebrow, R.string.setup_difficulty_title, R.string.setup_difficulty_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        difficultyOptions().forEach { (value, label) ->
            ChoiceButton(
                text = stringResource(label),
                selected = selected == value,
                onClick = { onSelect(value) },
            )
        }
    }
}

@Composable
private fun SupportQuestion(
    difficulty: FirstRunDifficulty,
    selected: Set<FirstRunSupport>,
    onToggle: (FirstRunSupport) -> Unit,
) {
    val (title, options) = supportOptions(difficulty)
    QuestionHeader(R.string.setup_support_eyebrow, title, R.string.setup_support_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            ChoiceButton(
                text = stringResource(label),
                selected = value in selected,
                onClick = {
                    if (
                        value == FirstRunSupport.MINIMAL ||
                        value in selected ||
                        selected.size < 2
                    ) {
                        onToggle(value)
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeViewQuestion(selected: HomeViewMode, onSelect: (HomeViewMode) -> Unit) {
    QuestionHeader(R.string.setup_home_eyebrow, R.string.setup_home_title, R.string.setup_home_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            HomeViewMode.ONE_NEXT to R.string.setup_home_one,
            HomeViewMode.NOW_NEXT to R.string.setup_home_now_next,
            HomeViewMode.LIST to R.string.setup_home_list,
        ).forEach { (mode, label) ->
            ChoiceButton(stringResource(label), selected == mode) { onSelect(mode) }
        }
    }
}

@Composable
private fun FocusDefaultsQuestion(
    focusPreset: FocusSessionPreset,
    onFocusPreset: (FocusSessionPreset) -> Unit,
    quickStartEnabled: Boolean,
    onQuickStartEnabled: (Boolean) -> Unit,
    preferLearnedDuration: Boolean,
    onPreferLearnedDuration: (Boolean) -> Unit,
    pauseEnabled: Boolean,
    onPauseEnabled: (Boolean) -> Unit,
    pauseMinutes: Int,
    customPauseSelected: Boolean,
    customPauseText: String,
    customPauseValid: Boolean,
    onPresetMinutes: (Int) -> Unit,
    onSelectCustom: () -> Unit,
    onCustomPauseText: (String) -> Unit,
) {
    QuestionHeader(R.string.setup_focus_eyebrow, R.string.setup_focus_presets_title, R.string.setup_focus_presets_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            FocusSessionPreset.SHORT to R.string.setup_focus_preset_short,
            FocusSessionPreset.NORMAL to R.string.setup_focus_preset_normal,
            FocusSessionPreset.LONG to R.string.setup_focus_preset_long,
            FocusSessionPreset.OPEN to R.string.setup_focus_preset_open,
        ).forEach { (preset, label) ->
            ChoiceButton(stringResource(label), focusPreset == preset) { onFocusPreset(preset) }
        }
        SettingSwitch(
            title = stringResource(R.string.setup_quick_start_title),
            support = stringResource(R.string.setup_quick_start_support),
            checked = quickStartEnabled,
            onCheckedChange = onQuickStartEnabled,
        )
        SettingSwitch(
            title = stringResource(R.string.setup_learned_duration_title),
            support = stringResource(R.string.setup_learned_duration_support),
            checked = preferLearnedDuration,
            onCheckedChange = onPreferLearnedDuration,
        )
        SettingSwitch(
            title = stringResource(R.string.pause_suggestions_option),
            support = stringResource(R.string.pause_suggestions_support),
            checked = pauseEnabled,
            onCheckedChange = onPauseEnabled,
        )
        if (pauseEnabled) {
            Text(stringResource(R.string.pause_after_label), style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pausePresets.forEach { minutes ->
                    FilterChip(
                        selected = !customPauseSelected && pauseMinutes == minutes,
                        onClick = { onPresetMinutes(minutes) },
                        label = { Text(stringResource(R.string.minutes_short, minutes)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            FilterChip(
                selected = customPauseSelected,
                onClick = onSelectCustom,
                label = { Text(stringResource(R.string.setup_pause_custom)) },
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
            )
            if (customPauseSelected) {
                OutlinedTextField(
                    value = customPauseText,
                    onValueChange = onCustomPauseText,
                    modifier = Modifier.fillMaxWidth().testTag("setup-custom-pause-minutes"),
                    label = { Text(stringResource(R.string.setup_pause_custom_label)) },
                    supportingText = {
                        Text(
                            stringResource(
                                if (customPauseValid) R.string.setup_pause_custom_help
                                else R.string.setup_pause_custom_error,
                            ),
                        )
                    },
                    isError = !customPauseValid,
                    singleLine = true,
                )
            }
        }
    }
}

@Composable
private fun LocalBehaviourQuestion(
    drawEnabled: Boolean,
    onDrawEnabled: (Boolean) -> Unit,
    checkInEnabled: Boolean,
    onCheckInEnabled: (Boolean) -> Unit,
    adaptationEnabled: Boolean,
    onAdaptationEnabled: (Boolean) -> Unit,
    calmMode: Boolean,
    onCalmMode: (Boolean) -> Unit,
    autoMini: Boolean,
    onAutoMini: (Boolean) -> Unit,
) {
    QuestionHeader(R.string.setup_behaviour_eyebrow, R.string.setup_behaviour_title, R.string.setup_behaviour_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingSwitch(
            title = stringResource(R.string.random_draw_option),
            support = stringResource(R.string.random_draw_option_support),
            checked = drawEnabled,
            onCheckedChange = onDrawEnabled,
        )
        SettingSwitch(
            title = stringResource(R.string.setup_checkin_title),
            support = stringResource(R.string.setup_checkin_support),
            checked = checkInEnabled,
            onCheckedChange = onCheckInEnabled,
        )
        SettingSwitch(
            title = stringResource(R.string.setup_adaptation_title),
            support = stringResource(R.string.setup_adaptation_support),
            checked = adaptationEnabled,
            onCheckedChange = onAdaptationEnabled,
        )
        SettingSwitch(
            title = stringResource(R.string.setup_calm_title),
            support = stringResource(R.string.setup_calm_support),
            checked = calmMode,
            onCheckedChange = onCalmMode,
        )
        SettingSwitch(
            title = stringResource(R.string.setup_auto_mini_title),
            support = stringResource(R.string.setup_auto_mini_support),
            checked = autoMini,
            onCheckedChange = onAutoMini,
        )
    }
}

@Composable
private fun SetupReview(
    difficulty: FirstRunDifficulty,
    homeViewMode: HomeViewMode,
    focusPreset: FocusSessionPreset,
    quickStartEnabled: Boolean,
    preferLearnedDuration: Boolean,
    drawEnabled: Boolean,
    pauseEnabled: Boolean,
    pauseMinutes: Int,
    checkInEnabled: Boolean,
    adaptationEnabled: Boolean,
    calmMode: Boolean,
    autoMini: Boolean,
) {
    QuestionHeader(R.string.setup_review_eyebrow, R.string.setup_review_title, R.string.setup_review_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReviewLine(stringResource(R.string.setup_review_main_difficulty), stringResource(difficultyLabel(difficulty)))
        ReviewLine(stringResource(R.string.setup_review_home), stringResource(when (homeViewMode) {
            HomeViewMode.ONE_NEXT -> R.string.setup_home_one
            HomeViewMode.NOW_NEXT -> R.string.setup_home_now_next
            HomeViewMode.LIST -> R.string.setup_home_list
        }))
        ReviewLine(stringResource(R.string.setup_review_focus_preset), stringResource(when (focusPreset) {
            FocusSessionPreset.SHORT -> R.string.setup_focus_preset_short
            FocusSessionPreset.NORMAL -> R.string.setup_focus_preset_normal
            FocusSessionPreset.LONG -> R.string.setup_focus_preset_long
            FocusSessionPreset.OPEN -> R.string.setup_focus_preset_open
        }))
        ReviewLine(stringResource(R.string.setup_review_quick_start), yesNo(quickStartEnabled))
        ReviewLine(stringResource(R.string.setup_review_learned_duration), yesNo(preferLearnedDuration))
        ReviewLine(
            stringResource(R.string.setup_review_pauses),
            if (pauseEnabled) stringResource(R.string.setup_review_enabled_minutes, pauseMinutes)
            else stringResource(R.string.setup_review_disabled),
        )
        ReviewLine(stringResource(R.string.random_draw_option), yesNo(drawEnabled))
        ReviewLine(stringResource(R.string.setup_checkin_title), yesNo(checkInEnabled))
        ReviewLine(stringResource(R.string.setup_adaptation_title), yesNo(adaptationEnabled))
        ReviewLine(stringResource(R.string.setup_calm_title), yesNo(calmMode))
        ReviewLine(stringResource(R.string.setup_auto_mini_title), yesNo(autoMini))
        Text(
            stringResource(R.string.setup_review_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun yesNo(value: Boolean): String = stringResource(if (value) R.string.setup_yes else R.string.setup_no)

@Composable
private fun ReviewLine(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    support: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 56.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(support, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ChoiceButton(text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
        ) { Text(text, modifier = Modifier.weight(1f), textAlign = TextAlign.Start) }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
        ) { Text(text, modifier = Modifier.weight(1f), textAlign = TextAlign.Start) }
    }
}

@Composable
private fun QuestionHeader(eyebrow: Int, title: Int, help: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(eyebrow),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun difficultyOptions(): List<Pair<FirstRunDifficulty, Int>> = listOf(
    FirstRunDifficulty.STARTING to R.string.setup_difficulty_starting,
    FirstRunDifficulty.CHOOSING to R.string.setup_difficulty_choosing,
    FirstRunDifficulty.FOCUSING to R.string.setup_difficulty_focusing,
    FirstRunDifficulty.TIME to R.string.setup_difficulty_time,
    FirstRunDifficulty.SWITCHING to R.string.setup_difficulty_switching,
    FirstRunDifficulty.RETURNING to R.string.setup_difficulty_returning,
    FirstRunDifficulty.REMEMBERING to R.string.setup_difficulty_remembering,
    FirstRunDifficulty.OTHER to R.string.setup_difficulty_other,
)

private fun difficultyLabel(value: FirstRunDifficulty): Int = difficultyOptions().first { it.first == value }.second

private fun supportOptions(value: FirstRunDifficulty): Pair<Int, List<Pair<FirstRunSupport, Int>>> = when (value) {
    FirstRunDifficulty.STARTING -> R.string.setup_support_starting_title to listOf(
        FirstRunSupport.SMALL_STEP to R.string.setup_support_small_step,
        FirstRunSupport.TIMER to R.string.setup_support_timer,
        FirstRunSupport.TASK_DIE to R.string.setup_support_die,
        FirstRunSupport.MINIMAL to R.string.setup_support_minimal,
    )
    FirstRunDifficulty.CHOOSING -> R.string.setup_support_choosing_title to listOf(
        FirstRunSupport.TASK_DIE to R.string.setup_support_die,
        FirstRunSupport.ONE_NEXT to R.string.setup_support_one_next,
        FirstRunSupport.FEW_OPTIONS to R.string.setup_support_few_options,
        FirstRunSupport.MINIMAL to R.string.setup_support_minimal,
    )
    FirstRunDifficulty.FOCUSING,
    FirstRunDifficulty.TIME,
    FirstRunDifficulty.SWITCHING,
    -> R.string.setup_support_focus_title to listOf(
        FirstRunSupport.QUICK_CAPTURE to R.string.setup_support_quick_capture,
        FirstRunSupport.PAUSE to R.string.setup_support_pause,
        FirstRunSupport.MINI_WINDOW to R.string.setup_support_mini,
        FirstRunSupport.CALM to R.string.setup_support_calm,
        FirstRunSupport.MINIMAL to R.string.setup_support_minimal,
    )
    FirstRunDifficulty.RETURNING,
    FirstRunDifficulty.REMEMBERING,
    -> R.string.setup_support_return_title to listOf(
        FirstRunSupport.SAVE_CONTEXT to R.string.setup_support_resume_context,
        FirstRunSupport.CHECK_IN to R.string.setup_support_checkin,
        FirstRunSupport.REMINDER to R.string.setup_support_reminder,
        FirstRunSupport.MINIMAL to R.string.setup_support_minimal,
    )
    FirstRunDifficulty.OTHER -> R.string.setup_support_other_title to listOf(
        FirstRunSupport.SMALL_STEP to R.string.setup_support_small_step,
        FirstRunSupport.TASK_DIE to R.string.setup_support_die,
        FirstRunSupport.QUICK_CAPTURE to R.string.setup_support_quick_capture,
        FirstRunSupport.MINIMAL to R.string.setup_support_minimal,
    )
}

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.lepotager.executivefunction.domain.FocusTimerMode

internal data class FirstRunSetupConfig(
    val timerMode: FocusTimerMode,
    val drawEnabled: Boolean,
    val pauseSuggestionsEnabled: Boolean,
    val pauseAfterMinutes: Int,
    val checkInEnabled: Boolean,
    val adaptationEnabled: Boolean,
    val calmMode: Boolean,
    val autoMiniWindow: Boolean,
    val pauseDurationMinutes: Int = 10,
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
private val breakPresets = listOf(5, 10, 15, 20)

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
    var timerModeName by rememberSaveable { mutableStateOf(initial.timerMode.name) }
    var drawEnabled by rememberSaveable { mutableStateOf(initial.drawEnabled) }
    var pauseEnabled by rememberSaveable { mutableStateOf(initial.pauseSuggestionsEnabled) }
    var pauseMinutes by rememberSaveable { mutableIntStateOf(initial.pauseAfterMinutes.coerceIn(5, 120)) }
    var customPauseSelected by rememberSaveable {
        mutableStateOf(initial.pauseAfterMinutes !in pausePresets)
    }
    var customPauseText by rememberSaveable {
        mutableStateOf(if (initial.pauseAfterMinutes !in pausePresets) initial.pauseAfterMinutes.toString() else "")
    }
    var breakMinutes by rememberSaveable { mutableIntStateOf(initial.pauseDurationMinutes.coerceIn(1, 60)) }
    var customBreakSelected by rememberSaveable {
        mutableStateOf(initial.pauseDurationMinutes !in breakPresets)
    }
    var customBreakText by rememberSaveable {
        mutableStateOf(if (initial.pauseDurationMinutes !in breakPresets) initial.pauseDurationMinutes.toString() else "")
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
    val timerMode = FocusTimerMode.valueOf(timerModeName)
    val customPauseValue = customPauseText.toIntOrNull()
    val customPauseValid = !pauseEnabled || !customPauseSelected ||
        (customPauseValue != null && customPauseValue in 5..120)
    val customBreakValue = customBreakText.toIntOrNull()
    val customBreakValid = !pauseEnabled || !customBreakSelected ||
        (customBreakValue != null && customBreakValue in 1..60)

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

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.setup_intro_eyebrow),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onSkip) { Text(stringResource(R.string.intro_skip)) }
            }

            ConversationHistory(
                page = page,
                difficulty = difficulty,
                supports = supports,
                timerMode = timerMode,
                pauseEnabled = pauseEnabled,
                pauseMinutes = pauseMinutes,
                pauseDurationMinutes = breakMinutes,
                drawEnabled = drawEnabled,
                checkInEnabled = checkInEnabled,
                adaptationEnabled = adaptationEnabled,
                calmMode = calmMode,
                autoMini = autoMini,
            )

            when (page) {
                0 -> {
                    SetupIntro()
                    Button(
                        onClick = { page = 1 },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.setup_chat_start)) }
                }

                1 -> DifficultyQuestion(
                    selected = difficulty,
                    onSelect = {
                        difficultyName = it.name
                        supportNames = ""
                        page = 2
                    },
                )

                2 -> {
                    SupportQuestion(
                        difficulty = requireNotNull(difficulty),
                        selected = supports,
                        onToggle = ::toggleSupport,
                    )
                    Button(
                        enabled = supports.isNotEmpty(),
                        onClick = {
                            applyBranchDefaults()
                            page = 3
                        },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.setup_chat_confirm)) }
                }

                3 -> {
                    FocusDefaultsQuestion(
                        timerMode = timerMode,
                        onTimerMode = { timerModeName = it.name },
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
                        breakMinutes = breakMinutes,
                        customBreakSelected = customBreakSelected,
                        customBreakText = customBreakText,
                        customBreakValid = customBreakValid,
                        onPresetBreakMinutes = {
                            breakMinutes = it
                            customBreakSelected = false
                        },
                        onSelectCustomBreak = {
                            customBreakSelected = true
                            if (customBreakText.isBlank()) customBreakText = breakMinutes.toString()
                        },
                        onCustomBreakText = { raw ->
                            customBreakText = raw.filter(Char::isDigit).take(2)
                            customBreakText.toIntOrNull()?.takeIf { it in 1..60 }?.let { breakMinutes = it }
                        },
                    )
                    Button(
                        enabled = customPauseValid && customBreakValid,
                        onClick = { page = 4 },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.setup_chat_confirm)) }
                }

                4 -> {
                    LocalBehaviourQuestion(
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
                    Button(
                        onClick = { page = 5 },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.setup_chat_confirm)) }
                }

                5 -> {
                    SetupReview(
                        difficulty = requireNotNull(difficulty),
                        timerMode = timerMode,
                        drawEnabled = drawEnabled,
                        pauseEnabled = pauseEnabled,
                        pauseMinutes = pauseMinutes,
                        pauseDurationMinutes = breakMinutes,
                        checkInEnabled = checkInEnabled,
                        adaptationEnabled = adaptationEnabled,
                        calmMode = calmMode,
                        autoMini = autoMini,
                    )
                    Button(
                        onClick = {
                            onApply(
                                FirstRunSetupConfig(
                                    timerMode = timerMode,
                                    drawEnabled = drawEnabled,
                                    pauseSuggestionsEnabled = pauseEnabled,
                                    pauseAfterMinutes = pauseMinutes,
                                    checkInEnabled = checkInEnabled,
                                    adaptationEnabled = adaptationEnabled,
                                    calmMode = calmMode,
                                    autoMiniWindow = autoMini,
                                    pauseDurationMinutes = breakMinutes,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) { Text(stringResource(R.string.setup_apply)) }
                }
            }

            if (page in 2..5) {
                TextButton(
                    onClick = { page -= 1 },
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                ) { Text(stringResource(R.string.intro_back)) }
            }
        }
    }
}

@Composable
private fun ConversationHistory(
    page: Int,
    difficulty: FirstRunDifficulty?,
    supports: Set<FirstRunSupport>,
    timerMode: FocusTimerMode,
    pauseEnabled: Boolean,
    pauseMinutes: Int,
    pauseDurationMinutes: Int,
    drawEnabled: Boolean,
    checkInEnabled: Boolean,
    adaptationEnabled: Boolean,
    calmMode: Boolean,
    autoMini: Boolean,
) {
    if (page <= 1) return

    val difficultyValue = difficulty ?: return
    ChatExchange(
        question = stringResource(R.string.setup_difficulty_title),
        answer = stringResource(difficultyLabel(difficultyValue)),
    )

    if (page >= 3 && supports.isNotEmpty()) {
        val supportTitle = supportOptions(difficultyValue).first
        val supportLabels = supportOptions(difficultyValue).second
            .filter { it.first in supports }
            .map { stringResource(it.second) }
        ChatExchange(
            question = stringResource(supportTitle),
            answer = supportLabels.joinToString(" · "),
        )
    }

    if (page >= 4) {
        val timer = stringResource(
            if (timerMode == FocusTimerMode.STOPWATCH) R.string.timer_mode_stopwatch
            else R.string.timer_mode_countdown,
        )
        val pause = if (pauseEnabled) {
            stringResource(
                R.string.setup_chat_pause_on,
                DurationText.minutes(pauseMinutes),
                DurationText.minutes(pauseDurationMinutes),
            )
        } else {
            stringResource(R.string.setup_chat_pause_off)
        }
        ChatExchange(
            question = stringResource(R.string.setup_focus_title),
            answer = "$timer · $pause",
        )
    }

    if (page >= 5) {
        val enabled = buildList {
            if (drawEnabled) add(stringResource(R.string.random_draw_option))
            if (checkInEnabled) add(stringResource(R.string.setup_checkin_title))
            if (adaptationEnabled) add(stringResource(R.string.setup_adaptation_title))
            if (calmMode) add(stringResource(R.string.setup_calm_title))
            if (autoMini) add(stringResource(R.string.setup_auto_mini_title))
        }
        ChatExchange(
            question = stringResource(R.string.setup_behaviour_title),
            answer = enabled.takeIf { it.isNotEmpty() }?.joinToString(" · ")
                ?: stringResource(R.string.setup_chat_no_automatic),
        )
    }
}

@Composable
private fun ChatExchange(question: String, answer: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChatBubble(text = question, fromUser = false)
        ChatBubble(text = answer, fromUser = true)
    }
}

@Composable
private fun ChatBubble(text: String, fromUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (fromUser) 20.dp else 5.dp,
                bottomEnd = if (fromUser) 5.dp else 20.dp,
            ),
            color = if (fromUser) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (fromUser) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun SetupIntro() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ToolBadge(ToolGlyphKind.START)
        ChatBubble(
            text = stringResource(R.string.setup_intro_title) + "\n\n" +
                stringResource(R.string.setup_intro_body),
            fromUser = false,
        )
        Text(
            stringResource(R.string.setup_intro_privacy),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
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
private fun FocusDefaultsQuestion(
    timerMode: FocusTimerMode,
    onTimerMode: (FocusTimerMode) -> Unit,
    pauseEnabled: Boolean,
    onPauseEnabled: (Boolean) -> Unit,
    pauseMinutes: Int,
    customPauseSelected: Boolean,
    customPauseText: String,
    customPauseValid: Boolean,
    onPresetMinutes: (Int) -> Unit,
    onSelectCustom: () -> Unit,
    onCustomPauseText: (String) -> Unit,
    breakMinutes: Int,
    customBreakSelected: Boolean,
    customBreakText: String,
    customBreakValid: Boolean,
    onPresetBreakMinutes: (Int) -> Unit,
    onSelectCustomBreak: () -> Unit,
    onCustomBreakText: (String) -> Unit,
) {
    val customPauseValue = customPauseText.toIntOrNull()
    QuestionHeader(R.string.setup_focus_eyebrow, R.string.setup_focus_title, R.string.setup_focus_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ChoiceButton(
            text = stringResource(R.string.setup_timer_stopwatch_default),
            selected = timerMode == FocusTimerMode.STOPWATCH,
            onClick = { onTimerMode(FocusTimerMode.STOPWATCH) },
        )
        ChoiceButton(
            text = stringResource(R.string.setup_timer_countdown_default),
            selected = timerMode == FocusTimerMode.COUNTDOWN,
            onClick = { onTimerMode(FocusTimerMode.COUNTDOWN) },
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
                        label = { Text(DurationText.minutes(minutes)) },
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
                        when {
                            !customPauseValid -> Text(stringResource(R.string.setup_pause_custom_error))
                            customPauseValue != null && customPauseValue >= 60 ->
                                Text(stringResource(R.string.duration_equivalent, DurationText.minutes(customPauseValue)))
                            else -> Text(stringResource(R.string.setup_pause_custom_help))
                        }
                    },
                    isError = !customPauseValid,
                    singleLine = true,
                )
            }
            Text(stringResource(R.string.setup_break_duration_label), style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                breakPresets.forEach { minutes ->
                    FilterChip(
                        selected = !customBreakSelected && breakMinutes == minutes,
                        onClick = { onPresetBreakMinutes(minutes) },
                        label = { Text(DurationText.minutes(minutes)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            FilterChip(
                selected = customBreakSelected,
                onClick = onSelectCustomBreak,
                label = { Text(stringResource(R.string.setup_break_custom)) },
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
            )
            if (customBreakSelected) {
                OutlinedTextField(
                    value = customBreakText,
                    onValueChange = onCustomBreakText,
                    modifier = Modifier.fillMaxWidth().testTag("setup-custom-break-minutes"),
                    label = { Text(stringResource(R.string.setup_break_custom_label)) },
                    supportingText = {
                        Text(stringResource(if (customBreakValid) R.string.setup_break_custom_help else R.string.setup_break_custom_error))
                    },
                    isError = !customBreakValid,
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
    timerMode: FocusTimerMode,
    drawEnabled: Boolean,
    pauseEnabled: Boolean,
    pauseMinutes: Int,
    pauseDurationMinutes: Int,
    checkInEnabled: Boolean,
    adaptationEnabled: Boolean,
    calmMode: Boolean,
    autoMini: Boolean,
) {
    QuestionHeader(R.string.setup_review_eyebrow, R.string.setup_review_title, R.string.setup_review_help)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReviewLine(stringResource(R.string.setup_review_main_difficulty), stringResource(difficultyLabel(difficulty)))
        ReviewLine(
            stringResource(R.string.setup_review_timer),
            stringResource(if (timerMode == FocusTimerMode.STOPWATCH) R.string.timer_mode_stopwatch else R.string.timer_mode_countdown),
        )
        ReviewLine(
            stringResource(R.string.setup_review_pauses),
            if (pauseEnabled) stringResource(R.string.setup_review_enabled_minutes, DurationText.minutes(pauseMinutes))
            else stringResource(R.string.setup_review_disabled),
        )
        if (pauseEnabled) ReviewLine(
            stringResource(R.string.setup_review_break_duration),
            DurationText.minutes(pauseDurationMinutes),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(eyebrow),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomEnd = 20.dp,
                bottomStart = 5.dp,
            ),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    stringResource(title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    stringResource(help),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
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

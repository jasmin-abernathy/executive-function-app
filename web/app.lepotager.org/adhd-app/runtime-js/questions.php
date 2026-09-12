<?php
declare(strict_types=1);
header('Content-Type: application/javascript; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Pragma: no-cache');
header('X-Content-Type-Options: nosniff');
?>
/* Question definitions — CSP-safe */
/* v0.4.6: questions embedded directly to bypass stalled static asset requests */
window.SURVEY_QUESTIONS = (() => {
  const paths = {
    start: {
      label: 'Getting started',
      short: 'Starting something',
      theme: 'peach',
      description: 'Beginning tasks, finding a first step, and reducing activation effort.',
      causeQuestion: {
        id: 'start_causes',
        type: 'multi',
        max: 2,
        eyebrow: 'Getting started',
        title: 'When starting something feels difficult, what is usually closest to your experience?',
        help: 'Choose up to two answers. Your next questions will adapt to your choices.',
        options: [
          ['vague', 'The task is too vague.'],
          ['large', 'The task feels too large.'],
          ['choices', 'There are too many possible starting points.'],
          ['activation', 'I know exactly what to do, but still cannot begin.'],
          ['boring', 'The task feels boring or insufficiently rewarding.'],
          ['pressure', 'I feel pressure to do it properly.'],
          ['practical', 'Starting requires too many physical or practical steps.'],
          ['varies', 'It varies too much to choose.'],
          ['other', 'Something else.']
        ]
      },
      conditional: {
        vague: {
          id: 'start_vague_help', type: 'single', eyebrow: 'Clarifying the task',
          title: 'When a task feels vague, what kind of help would be most useful?',
          options: [
            ['one_action', 'Show me one very small first action.'],
            ['simple_questions', 'Ask me a few simple questions and suggest a first action.'],
            ['auto_steps', 'Break the task into several small steps automatically.'],
            ['example', 'Show me an example of what “started” could look like.'],
            ['choose_method', 'Let me choose between these methods.'],
            ['none', 'I would not want the app to break tasks down for me.']
          ]
        },
        large: {
          id: 'start_large_help', type: 'single', eyebrow: 'Reducing the task',
          title: 'When a task feels too large, what should the app offer first?',
          options: [
            ['minimum', 'A deliberately minimal version of the task.'],
            ['first_step', 'Only the first small step.'],
            ['three_steps', 'A short plan with no more than three steps.'],
            ['timebox', 'A five-minute attempt with permission to stop.'],
            ['partial', 'A clear way to record partial progress.'],
            ['ask', 'Ask me what feels manageable today.']
          ]
        },
        choices: {
          id: 'start_choices_help', type: 'single', eyebrow: 'Reducing choice',
          title: 'When there are too many possible starting points, what should the app do?',
          options: [
            ['one_hidden', 'Recommend one action without showing the others.'],
            ['one_reveal', 'Recommend one action but let me reveal alternatives.'],
            ['three', 'Show me no more than three possible actions.'],
            ['urgency', 'Sort the options by urgency.'],
            ['energy', 'Sort the options by required energy.'],
            ['random', 'Choose one at random when the choice does not matter.'],
            ['ask', 'Ask me each time.']
          ]
        },
        activation: {
          id: 'start_activation_help', type: 'single', eyebrow: 'Crossing the starting line',
          title: 'When you know what to do but still cannot begin, which aid sounds most useful?',
          options: [
            ['five_minutes', 'A five-minute “just begin” session.'],
            ['countdown', 'A short guided countdown.'],
            ['one_button', 'A single button that immediately starts the session.'],
            ['prep_step', 'A tiny preparation step before the real task.'],
            ['visual_shift', 'A quiet visual change that marks the beginning.'],
            ['companion', 'Encouragement from a companion or guide.'],
            ['none', 'No special starting aid.']
          ]
        },
        boring: {
          id: 'start_reward_help', type: 'single', eyebrow: 'Immediate motivation',
          title: 'What might make a boring task easier to start?',
          options: [
            ['instant_reward', 'Receiving a small reward immediately after starting.'],
            ['visible_progress', 'Seeing visible progress as soon as I begin.'],
            ['chosen_reward', 'Connecting the task to a reward I chose earlier.'],
            ['varied_reveal', 'Making the reward reveal slightly different each time.'],
            ['challenge', 'Turning the first few minutes into a small challenge.'],
            ['none', 'Nothing involving rewards.'],
            ['unsure', 'I am not sure.']
          ]
        },
        pressure: {
          id: 'start_pressure_help', type: 'single', eyebrow: 'Reducing pressure',
          title: 'When pressure or perfectionism gets in the way, what would feel most helpful?',
          options: [
            ['minimum', 'Define a deliberately minimal version of the task.'],
            ['good_enough', 'Show what would count as “good enough for today.”'],
            ['hide_full', 'Hide the full task and show only the first step.'],
            ['private', 'Let me start without tracking performance.'],
            ['partial_counts', 'Remind me that partial progress still counts.'],
            ['no_quality', 'Avoid commenting on the quality of my work.'],
            ['none', 'I would not want the app to address this.']
          ]
        },
        practical: {
          id: 'start_practical_help', type: 'single', eyebrow: 'Reducing setup',
          title: 'When practical setup creates friction, what should the app do?',
          options: [
            ['prep_checklist', 'Show a tiny preparation checklist.'],
            ['first_object', 'Tell me only the first object or place I need.'],
            ['remember_setup', 'Remember the setup that worked last time.'],
            ['prepare_later', 'Let me schedule preparation as a separate action.'],
            ['environment', 'Help me remove one environmental obstacle.'],
            ['nothing', 'Do nothing special.']
          ]
        },
        varies: {
          id: 'start_varies_help', type: 'single', eyebrow: 'Flexible support',
          title: 'If the obstacle changes from day to day, how should the app respond?',
          options: [
            ['ask_one', 'Ask one quick question before suggesting help.'],
            ['toolbox', 'Show a small toolbox of starting methods.'],
            ['remember', 'Remember which method worked in similar situations.'],
            ['default', 'Use one simple default unless I change it.'],
            ['manual', 'Let me choose without making a suggestion.']
          ]
        },
        other: {
          id: 'start_other_text', type: 'text', optional: true, eyebrow: 'Your experience',
          title: 'What else makes starting difficult for you?',
          help: 'A short answer is enough. You can also skip this question.',
          placeholder: 'For example: I need another person nearby…',
          maxLength: 500
        }
      },
      common: [
        {
          id: 'start_setup_time', type: 'single', eyebrow: 'Getting started',
          title: 'How much preparation would still feel realistic on a difficult day?',
          options: [
            ['none', 'None — the app should offer something immediately.'],
            ['30_seconds', 'About 30 seconds.'],
            ['two_minutes', 'One or two minutes.'],
            ['five_minutes', 'Up to five minutes.'],
            ['depends', 'It depends too much on the day.'],
            ['advance', 'I prefer preparing everything in advance.']
          ]
        },
        {
          id: 'start_control', type: 'single', eyebrow: 'Control',
          title: 'When the app recommends a first action, how much control would you want?',
          options: [
            ['one', 'Show one action and let me begin immediately.'],
            ['another', 'Show one action with a “suggest another” button.'],
            ['alternatives', 'Show one recommendation and two alternatives.'],
            ['manual', 'Always let me select the action myself.'],
            ['settings', 'Let me choose the behaviour in the settings.'],
            ['unsure', 'I am not sure.']
          ]
        }
      ],
      secondary: [
        {
          id: 'secondary_start_view', type: 'single', eyebrow: 'A second need: getting started',
          title: 'Which starting screen sounds most helpful?',
          options: [
            ['one_action', 'One tiny suggested action.'],
            ['three_choices', 'Up to three starting choices.'],
            ['countdown', 'A short start countdown.'],
            ['toolbox', 'A choice of starting methods.'],
            ['none', 'No dedicated starting screen.']
          ]
        },
        {
          id: 'secondary_start_setup', type: 'single', eyebrow: 'A second need: getting started',
          title: 'On a difficult day, should starting require any setup?',
          options: [['none','No setup.'],['brief','Up to one minute.'],['some','A few minutes is fine.'],['depends','It depends.']]
        }
      ]
    },

    planning: {
      label: 'Choosing what comes next',
      short: 'Deciding what to do next',
      theme: 'blue',
      description: 'Prioritising, keeping plans flexible, and avoiding overwhelming lists.',
      causeQuestion: {
        id: 'planning_causes', type: 'multi', max: 2, eyebrow: 'Planning',
        title: 'What usually makes planning or choosing difficult?',
        help: 'Choose up to two answers.',
        options: [
          ['setup', 'Setting up a planning system takes too much effort.'],
          ['equal', 'Too many things feel equally important.'],
          ['duration', 'I struggle to estimate how long things will take.'],
          ['changes', 'My plans stop working when the day changes.'],
          ['forget', 'I make plans but forget to look at them.'],
          ['overwhelm', 'Seeing the full list becomes overwhelming.'],
          ['organising', 'I spend more time organising than doing.'],
          ['information', 'I do not know what information to include.'],
          ['other', 'Something else.']
        ]
      },
      conditional: {
        setup: {
          id: 'planning_setup_help', type: 'single', eyebrow: 'Reducing planning effort',
          title: 'How should the app reduce planning setup?',
          options: [
            ['suggest', 'Suggest a plan from a few quick answers.'],
            ['one_action', 'Skip planning and suggest one next action.'],
            ['reuse', 'Reuse patterns from previous days.'],
            ['fixed_points', 'Ask only for fixed commitments.'],
            ['inbox', 'Let me add activities without organising them first.'],
            ['manual', 'I prefer setting everything up myself.']
          ]
        },
        equal: {
          id: 'planning_equal_help', type: 'single', eyebrow: 'Choosing priorities',
          title: 'When several things feel equally important, what should the app do?',
          options: [
            ['one', 'Choose one reasonable next action.'],
            ['questions', 'Ask two or three quick priority questions.'],
            ['deadline', 'Prioritise deadlines and fixed commitments.'],
            ['energy', 'Match the choice to my available energy.'],
            ['random', 'Choose randomly when the difference is small.'],
            ['manual', 'Show the options and let me decide.']
          ]
        },
        duration: {
          id: 'planning_duration_help', type: 'single', eyebrow: 'Time estimates',
          title: 'How should uncertain duration estimates work?',
          options: [
            ['range', 'Use broad ranges such as 10–20 minutes.'],
            ['buffer', 'Use conservative estimates with extra time included.'],
            ['learn', 'Improve estimates from my past sessions.'],
            ['modes', 'Offer short, normal and extended versions.'],
            ['none', 'Do not show estimates.'],
            ['manual', 'Let me enter them myself.']
          ]
        },
        changes: {
          id: 'planning_changes_help', type: 'single', eyebrow: 'Flexible plans',
          title: 'If the day changes unexpectedly, what should happen?',
          options: [
            ['move', 'Automatically move unfinished activities forward.'],
            ['ask', 'Ask what should still happen today.'],
            ['keep_one', 'Keep only the most important activity.'],
            ['fresh', 'Put everything aside and help me make a fresh plan.'],
            ['unchanged', 'Leave the plan unchanged until I edit it.'],
            ['options', 'Offer several recovery options.']
          ]
        },
        forget: {
          id: 'planning_forget_help', type: 'single', eyebrow: 'Returning to the plan',
          title: 'What might help you remember to return to the plan?',
          options: [
            ['widget', 'A quiet home-screen or desktop widget.'],
            ['fixed_times', 'A few check-ins at times I choose.'],
            ['context', 'Show the plan when I finish another activity.'],
            ['calendar', 'Connect it to my calendar.'],
            ['one_reminder', 'One reminder only, not repeated notifications.'],
            ['none', 'I do not want reminders to reopen the plan.']
          ]
        },
        overwhelm: {
          id: 'planning_overwhelm_help', type: 'single', eyebrow: 'Reducing visual load',
          title: 'When the full list is overwhelming, what should remain visible?',
          options: [
            ['one', 'Only the next action.'],
            ['three', 'No more than three actions.'],
            ['today', 'Only today’s actions.'],
            ['group', 'One group or project at a time.'],
            ['hide', 'Keep everything hidden until I ask.'],
            ['custom', 'Let me customise the view.']
          ]
        },
        organising: {
          id: 'planning_organising_help', type: 'single', eyebrow: 'Doing rather than organising',
          title: 'How should the app stop planning from becoming its own task?',
          options: [
            ['limit', 'End planning automatically after a short limit.'],
            ['minimum', 'Ask only what is needed for the next action.'],
            ['suggest', 'Suggest priorities without requiring categories.'],
            ['capture', 'Let me capture first and organise later.'],
            ['start_button', 'Always keep a visible “start now” button.'],
            ['manual', 'I do not want the app to intervene.']
          ]
        },
        information: {
          id: 'planning_information_help', type: 'single', eyebrow: 'Task details',
          title: 'What information should be required when adding an activity?',
          options: [
            ['title_only', 'Only a title.'],
            ['title_date', 'A title and optional date.'],
            ['guided', 'A few optional prompts.'],
            ['template', 'A template based on the activity type.'],
            ['detailed', 'A detailed form.'],
            ['custom', 'Let me choose the required fields.']
          ]
        },
        other: {
          id: 'planning_other_text', type: 'text', optional: true, eyebrow: 'Your experience',
          title: 'What else makes planning difficult for you?', placeholder: 'A short answer is enough…', maxLength: 500
        }
      },
      common: [
        {
          id: 'planning_open_view', type: 'single', eyebrow: 'The home screen',
          title: 'When you open the app, what would you prefer to see?',
          options: [
            ['one', 'One suggested next action.'],
            ['one_hidden', 'One suggested action with hidden alternatives.'],
            ['one_two', 'One suggested action and two visible alternatives.'],
            ['few', 'Three to five actions.'],
            ['complete', 'My complete list.'],
            ['change', 'A layout I can change myself.'],
            ['adaptive', 'Different layouts depending on the day.']
          ]
        },
        {
          id: 'planning_stack', type: 'single', eyebrow: 'Organising the rest',
          title: 'How should the app organise activities that are not currently visible?',
          options: [
            ['stack', 'Keep them in an ordered stack.'],
            ['daypart', 'Group them by part of the day.'],
            ['energy', 'Group them by energy level.'],
            ['project', 'Group them by project or context.'],
            ['hidden', 'Keep them hidden until I ask.'],
            ['calendar', 'Use a traditional calendar.'],
            ['views', 'Let me choose between several views.']
          ]
        }
      ],
      secondary: [
        {
          id: 'secondary_planning_view', type: 'single', eyebrow: 'A second need: planning',
          title: 'What should the app show first when you need direction?',
          options: [['one','One next action.'],['three','Up to three options.'],['day','A short day plan.'],['full','The full list.'],['adaptive','A view adapted to the day.']]
        },
        {
          id: 'secondary_planning_change', type: 'single', eyebrow: 'A second need: planning',
          title: 'Should the plan automatically adapt when the day changes?',
          options: [['yes','Yes, automatically.'],['ask','Only after asking me.'],['options','Offer several options.'],['no','No.']]
        }
      ]
    },

    time: {
      label: 'Time and transitions',
      short: 'Managing time or switching activities',
      theme: 'lavender',
      description: 'Seeing time pass, stopping safely, and moving between activities.',
      causeQuestion: {
        id: 'time_causes', type: 'multi', max: 2, eyebrow: 'Time and transitions',
        title: 'Which time-related difficulty affects you most?',
        help: 'Choose up to two answers.',
        options: [
          ['lose', 'I lose track of time.'],
          ['underestimate', 'I underestimate how long things will take.'],
          ['overfill', 'I overestimate how much I can fit into a day.'],
          ['switch', 'I find it difficult to switch activities.'],
          ['stop', 'I find it difficult to stop an activity.'],
          ['appointments', 'I forget upcoming appointments or fixed commitments.'],
          ['timer_stress', 'Timers make me anxious or irritated.'],
          ['varies', 'My experience varies considerably.'],
          ['other', 'Something else.']
        ]
      },
      conditional: {
        lose: {
          id: 'time_lose_help', type: 'single', eyebrow: 'Seeing time pass',
          title: 'Which cue would help you notice time without demanding too much attention?',
          options: [
            ['quiet_timer', 'A visible but quiet timer.'],
            ['visual', 'A gentle visual indication of passing time.'],
            ['interval', 'A subtle cue at intervals I choose.'],
            ['spoken', 'An optional spoken time cue.'],
            ['check', 'Only show elapsed time when I check.'],
            ['none', 'No automatic cue.']
          ]
        },
        underestimate: {
          id: 'time_underestimate_help', type: 'single', eyebrow: 'Estimating duration',
          title: 'How should the app help with underestimated durations?',
          options: [
            ['range', 'Use a broad range.'],
            ['buffer', 'Add an automatic buffer.'],
            ['history', 'Compare with similar past activities.'],
            ['checkpoints', 'Suggest checkpoints instead of one end time.'],
            ['manual', 'Let me adjust estimates myself.'],
            ['none', 'Do not estimate.']
          ]
        },
        overfill: {
          id: 'time_overfill_help', type: 'single', eyebrow: 'Realistic days',
          title: 'How should the app react when a day looks too full?',
          options: [
            ['warn', 'Quietly flag that the plan may not fit.'],
            ['remove', 'Suggest what could be removed or postponed.'],
            ['buffer', 'Reserve unplanned buffer time automatically.'],
            ['essential', 'Ask me to choose one essential action.'],
            ['versions', 'Offer light, normal and ambitious versions of the day.'],
            ['nothing', 'Do not comment on how full the day is.']
          ]
        },
        switch: {
          id: 'time_switch_help', type: 'single', eyebrow: 'Switching activities',
          title: 'Before a planned transition, what would help most?',
          options: [
            ['warning', 'A subtle warning.'],
            ['countdown', 'A visible countdown.'],
            ['save', 'A suggestion to save what I am doing.'],
            ['transition_task', 'A short transition task.'],
            ['postpone', 'A button to postpone the transition.'],
            ['hyperfocus', 'A button to enter hyperfocus mode instead.'],
            ['none', 'No automatic intervention.']
          ]
        },
        stop: {
          id: 'time_stop_help', type: 'single', eyebrow: 'Stopping safely',
          title: 'When it is time to stop, what should the app offer?',
          options: [
            ['note', 'Save a quick note about where I stopped.'],
            ['next', 'Record the next action automatically.'],
            ['stack', 'Put the unfinished activity back in the stack.'],
            ['closing', 'Offer a short closing checklist.'],
            ['break', 'Give me a short break before the next activity.'],
            ['nothing', 'Do nothing unless I ask.']
          ]
        },
        appointments: {
          id: 'time_appointments_help', type: 'single', eyebrow: 'Fixed commitments',
          title: 'How should fixed commitments interrupt other activities?',
          options: [
            ['early', 'Warn me early enough to prepare and travel.'],
            ['stages', 'Use several preparation cues.'],
            ['protect', 'Protect preparation time in the plan.'],
            ['focus_exception', 'Allow them to interrupt focus or hyperfocus.'],
            ['calendar', 'Use my external calendar.'],
            ['manual', 'Let me configure each commitment.']
          ]
        },
        timer_stress: {
          id: 'time_timer_stress_help', type: 'single', eyebrow: 'Low-pressure time support',
          title: 'If timers create pressure, which alternative sounds least stressful?',
          options: [
            ['elapsed', 'Show elapsed time without a deadline.'],
            ['visual', 'Use a non-numeric visual cue.'],
            ['range', 'Show a broad time range.'],
            ['hidden', 'Keep time hidden unless I ask.'],
            ['end_only', 'Notify me only at a chosen end point.'],
            ['none', 'No time display.']
          ]
        },
        varies: {
          id: 'time_varies_help', type: 'single', eyebrow: 'Flexible time support',
          title: 'If your time needs vary, how should the app adapt?',
          options: [
            ['session_choice', 'Ask at the start of each session.'],
            ['profiles', 'Offer a few time-support profiles.'],
            ['remember', 'Remember preferences by activity type.'],
            ['default', 'Use one quiet default.'],
            ['manual', 'Never change without my input.']
          ]
        },
        other: {
          id: 'time_other_text', type: 'text', optional: true, eyebrow: 'Your experience',
          title: 'What else makes time or transitions difficult?', placeholder: 'A short answer is enough…', maxLength: 500
        }
      },
      common: [
        {
          id: 'time_support_default', type: 'single', eyebrow: 'Default time support',
          title: 'Which kind of time support would feel least intrusive?',
          options: [
            ['quiet_timer', 'A visible but quiet timer.'],
            ['on_request', 'A timer that appears only when I request it.'],
            ['warnings', 'Gentle warnings before transitions.'],
            ['ranges', 'Estimated duration ranges instead of exact times.'],
            ['visual', 'A visual indication of passing time.'],
            ['fixed', 'Reminders connected to fixed commitments.'],
            ['none', 'No time support by default.'],
            ['custom', 'Let me customise it.']
          ]
        },
        {
          id: 'time_phase_change', type: 'single', eyebrow: 'Moving to the next phase',
          title: 'How should a focus or break phase change?',
          options: [
            ['automatic', 'Automatically when the timer ends.'],
            ['confirm', 'Only when I confirm that I am ready.'],
            ['auto_stay', 'Automatically, with an option to stay.'],
            ['tap', 'Through a simple tap.'],
            ['gesture', 'Through a simple gesture.'],
            ['choose', 'Let me choose for each session.']
          ]
        }
      ],
      secondary: [
        {
          id: 'secondary_time_cue', type: 'single', eyebrow: 'A second need: time',
          title: 'Which time cue would feel least intrusive?',
          options: [['timer','A quiet timer.'],['visual','A visual cue.'],['warnings','Transition warnings.'],['on_request','Only on request.'],['none','No cue.']]
        },
        {
          id: 'secondary_time_stop', type: 'single', eyebrow: 'A second need: time',
          title: 'Should the app help you save your place before switching?',
          options: [['always','Yes, automatically.'],['offer','Offer it.'],['manual','Only when requested.'],['no','No.']]
        }
      ]
    },

    focus: {
      label: 'Focus and hyperfocus',
      short: 'Staying focused or handling hyperfocus',
      theme: 'yellow',
      description: 'Protecting attention, capturing interruptions, and stopping without losing context.',
      causeQuestion: {
        id: 'focus_causes', type: 'multi', max: 2, eyebrow: 'Focus and hyperfocus',
        title: 'What would you most want support with?',
        help: 'Choose up to two answers.',
        options: [
          ['ideas', 'Becoming distracted by unrelated ideas.'],
          ['switching', 'Switching tasks too frequently.'],
          ['notifications', 'Being interrupted by notifications.'],
          ['people', 'Being interrupted by other people or events.'],
          ['long', 'Staying in one activity much longer than intended.'],
          ['stop_context', 'Being unable to stop without losing my train of thought.'],
          ['capture', 'Remembering ideas without leaving the current task.'],
          ['timers', 'Using ordinary timers that do not match how I focus.'],
          ['other', 'Something else.']
        ]
      },
      conditional: {
        ideas: {
          id: 'focus_ideas_help', type: 'single', eyebrow: 'Distracting ideas',
          title: 'When an unrelated idea appears, what should happen?',
          options: [
            ['text_note', 'One tap opens a text note.'],
            ['task', 'One tap creates a task for later.'],
            ['choice', 'One button opens a choice between note and task.'],
            ['voice', 'Voice capture should be available.'],
            ['inbox', 'The idea goes into a temporary inbox.'],
            ['review', 'The app asks when I want to review it.']
          ]
        },
        switching: {
          id: 'focus_switching_help', type: 'single', eyebrow: 'Staying with one activity',
          title: 'What would help reduce unplanned task switching?',
          options: [
            ['single_screen', 'Keep only the current activity visible.'],
            ['capture', 'Offer instant capture for other thoughts.'],
            ['confirm', 'Ask before switching to another app activity.'],
            ['session', 'Use a short commitment session.'],
            ['why', 'Show why I chose the current activity.'],
            ['none', 'Do not intervene.']
          ]
        },
        notifications: {
          id: 'focus_notifications_help', type: 'single', eyebrow: 'Notification control',
          title: 'How should focus mode handle notifications?',
          options: [
            ['all', 'Silence all nonessential app notifications.'],
            ['selected', 'Allow only selected people or apps.'],
            ['batch', 'Collect them for review after the session.'],
            ['visual_only', 'Allow quiet visual indicators only.'],
            ['device_tools', 'Guide me to use the device’s own focus controls.'],
            ['none', 'Do not manage notifications.']
          ]
        },
        people: {
          id: 'focus_people_help', type: 'single', eyebrow: 'External interruptions',
          title: 'How should the app help with interruptions from people or events?',
          options: [
            ['status', 'Show a shareable focus status.'],
            ['exceptions', 'Allow selected urgent interruptions.'],
            ['resume_note', 'Make returning easy after the interruption.'],
            ['auto_pause', 'Pause the session automatically when I choose.'],
            ['quick_message', 'Offer a prewritten “I will reply later” message.'],
            ['nothing', 'Do nothing special.']
          ]
        },
        long: {
          id: 'focus_long_help', type: 'single', eyebrow: 'Hyperfocus duration',
          title: 'During hyperfocus, what should the app do?',
          options: [
            ['silent', 'Stay silent until I check it.'],
            ['elapsed', 'Show a subtle indication of elapsed time.'],
            ['ask', 'Ask occasionally whether I want to continue.'],
            ['fixed_only', 'Warn me only before fixed commitments.'],
            ['maximum', 'Let me define a maximum duration beforehand.'],
            ['configure', 'Let me configure this for each session.']
          ]
        },
        stop_context: {
          id: 'focus_stop_context_help', type: 'single', eyebrow: 'Stopping without losing context',
          title: 'What would make it safer to stop?',
          options: [
            ['where', 'Save where I stopped.'],
            ['next', 'Save the next action.'],
            ['snapshot', 'Create a short session snapshot.'],
            ['materials', 'Keep related notes or materials together.'],
            ['resume', 'Schedule a specific resume point.'],
            ['nothing', 'I would rather manage this myself.']
          ]
        },
        capture: {
          id: 'focus_capture_help', type: 'single', eyebrow: 'Quick capture',
          title: 'How many actions should quick capture require during focus?',
          options: [
            ['typing', 'One tap and immediate typing.'],
            ['task', 'One tap to save a task.'],
            ['note', 'One tap to save a note.'],
            ['choice', 'Two taps to choose between task and note.'],
            ['voice', 'Voice capture.'],
            ['full', 'I do not mind opening a full form.']
          ]
        },
        timers: {
          id: 'focus_timers_help', type: 'single', eyebrow: 'Focus session structure',
          title: 'Which type of focus session would suit you best?',
          options: [
            ['short', 'A short 5–10 minute starting session.'],
            ['standard', 'A standard 20–30 minute session.'],
            ['custom', 'A custom-length session.'],
            ['open', 'An open-ended hyperfocus session.'],
            ['no_timer', 'A session without a timer.'],
            ['step', 'A session organised around completing one step.']
          ]
        },
        other: {
          id: 'focus_other_text', type: 'text', optional: true, eyebrow: 'Your experience',
          title: 'What else makes focus or hyperfocus difficult?', placeholder: 'A short answer is enough…', maxLength: 500
        }
      },
      common: [
        {
          id: 'focus_visible', type: 'single', eyebrow: 'The concentration veil',
          title: 'What should remain visible during a focus session?',
          options: [
            ['timer', 'Only the timer.'],
            ['task', 'The timer and current task.'],
            ['capture', 'The timer, task and quick-capture button.'],
            ['steps', 'The current task and remaining steps.'],
            ['overlay', 'A very minimal colour overlay.'],
            ['touch', 'Nothing unless I touch the screen.'],
            ['custom', 'Let me customise the display.']
          ]
        },
        {
          id: 'focus_session_types', type: 'multi', max: 4, eyebrow: 'Session modes',
          title: 'Which types of focus session would be useful?',
          help: 'Choose up to four.',
          options: [
            ['short', 'A short 5–10 minute starting session.'],
            ['standard', 'A standard 20–30 minute session.'],
            ['custom', 'A custom-length session.'],
            ['hyperfocus', 'An open-ended hyperfocus session.'],
            ['no_timer', 'A session without a timer.'],
            ['one_step', 'A session organised around completing one step.'],
            ['none', 'I generally do not want formal focus sessions.']
          ]
        }
      ],
      secondary: [
        {
          id: 'secondary_focus_display', type: 'single', eyebrow: 'A second need: focus',
          title: 'What should a focus screen primarily show?',
          options: [['timer','Only a timer.'],['task','Timer and task.'],['capture','Task and quick capture.'],['minimal','A minimal colour overlay.'],['custom','A custom view.']]
        },
        {
          id: 'secondary_focus_hyper', type: 'single', eyebrow: 'A second need: focus',
          title: 'Should hyperfocus have a dedicated mode?',
          options: [['yes','Yes.'],['optional','Yes, but entirely optional.'],['same','Use the normal focus mode.'],['no','No.']]
        }
      ]
    },

    recovery: {
      label: 'Recovery and continuity',
      short: 'Resuming after an interruption or time away',
      theme: 'green',
      description: 'Preserving context, restarting gently, and never punishing absence.',
      causeQuestion: {
        id: 'recovery_causes', type: 'multi', max: 2, eyebrow: 'Recovery and continuity',
        title: 'After an interruption, what is most difficult?',
        help: 'Choose up to two answers.',
        options: [
          ['remember_task', 'Remembering what I was doing.'],
          ['next_step', 'Remembering the next step.'],
          ['context', 'Regaining the right mental context.'],
          ['motivation', 'Rebuilding motivation.'],
          ['time_left', 'Estimating how much time remains.'],
          ['relevance', 'Deciding whether the task is still relevant.'],
          ['guilt', 'Avoiding guilt or frustration.'],
          ['other', 'Something else.']
        ]
      },
      conditional: {
        remember_task: {
          id: 'recovery_remember_help', type: 'single', eyebrow: 'Remembering the activity',
          title: 'What should the app preserve when you stop?',
          options: [
            ['note', 'A short note about where I stopped.'],
            ['snapshot', 'A simple snapshot of the activity.'],
            ['materials', 'The notes or materials I was using.'],
            ['time', 'The time already spent.'],
            ['choice', 'Let me choose each time.'],
            ['nothing', 'Nothing automatically.']
          ]
        },
        next_step: {
          id: 'recovery_next_help', type: 'single', eyebrow: 'Saving the next step',
          title: 'How should the next action be preserved?',
          options: [
            ['automatic', 'Record it automatically when possible.'],
            ['one_prompt', 'Ask me one quick question before I stop.'],
            ['button', 'Offer a “save next step” button.'],
            ['suggest', 'Suggest a likely next step for me to confirm.'],
            ['manual', 'Let me write it manually.'],
            ['none', 'Do not preserve it.']
          ]
        },
        context: {
          id: 'recovery_context_help', type: 'single', eyebrow: 'Regaining context',
          title: 'What would help you recover the mental context?',
          options: [
            ['summary', 'A short summary of where I stopped.'],
            ['recent', 'The most recent notes and actions.'],
            ['why', 'A reminder of why the activity mattered.'],
            ['materials', 'The relevant materials grouped together.'],
            ['warmup', 'A small warm-up step.'],
            ['none', 'I do not want a special context screen.']
          ]
        },
        motivation: {
          id: 'recovery_motivation_help', type: 'single', eyebrow: 'Rebuilding momentum',
          title: 'What would make resuming feel easier?',
          options: [
            ['smaller', 'A smaller restart step.'],
            ['resume', 'A large “Resume” button.'],
            ['choice', 'A choice between resume, simplify or postpone.'],
            ['welcome', 'A calm supportive message.'],
            ['reward', 'A small reward for returning.'],
            ['neutral', 'No motivational language.']
          ]
        },
        time_left: {
          id: 'recovery_time_help', type: 'single', eyebrow: 'Remaining effort',
          title: 'How should the app represent the work that remains?',
          options: [
            ['range', 'A broad time range.'],
            ['steps', 'The number of remaining steps.'],
            ['small_next', 'Only the next small action.'],
            ['reestimate', 'Ask me to make a fresh estimate.'],
            ['history', 'Use the previous session as a clue.'],
            ['none', 'Do not estimate what remains.']
          ]
        },
        relevance: {
          id: 'recovery_relevance_help', type: 'single', eyebrow: 'Reassessing the activity',
          title: 'When you return, how should the app help decide whether the activity still matters?',
          options: [
            ['ask', 'Ask whether it is still relevant.'],
            ['context', 'Show the deadline and original reason.'],
            ['options', 'Offer resume, postpone, archive or delete.'],
            ['fresh', 'Compare it with today’s priorities.'],
            ['nothing', 'Leave the decision entirely to me.']
          ]
        },
        guilt: {
          id: 'recovery_guilt_help', type: 'single', eyebrow: 'Returning without punishment',
          title: 'Which return experience would feel least guilt-inducing?',
          options: [
            ['fresh_question', 'Ask what I need today.'],
            ['resume_or_fresh', 'Offer a choice between resuming and starting fresh.'],
            ['one_suggestion', 'Show one gentle suggestion.'],
            ['silent', 'Say nothing about the absence.'],
            ['supportive', 'Use a short supportive message.'],
            ['custom', 'Let me choose the return tone.']
          ]
        },
        other: {
          id: 'recovery_other_text', type: 'text', optional: true, eyebrow: 'Your experience',
          title: 'What else makes returning difficult?', placeholder: 'A short answer is enough…', maxLength: 500
        }
      },
      common: [
        {
          id: 'recovery_return_screen', type: 'single', eyebrow: 'Returning after time away',
          title: 'You have not used the app for several days or weeks. What should appear first?',
          options: [
            ['last', 'The last activity I was working on.'],
            ['one', 'One gentle suggestion.'],
            ['fresh', 'A completely fresh start.'],
            ['short_list', 'A short list of unfinished activities.'],
            ['need', 'A question asking what I need today.'],
            ['choice', 'A choice between resuming and starting again.'],
            ['nothing', 'Nothing related to my absence.']
          ]
        },
        {
          id: 'recovery_never', type: 'multi', max: 6, eyebrow: 'Non-punitive progress',
          title: 'What should the app never do after an absence?',
          help: 'Choose everything that would bother you.',
          options: [
            ['reset', 'Reset a streak.'],
            ['remove', 'Remove points or progress.'],
            ['overdue', 'Display a large overdue-task list.'],
            ['comment', 'Comment on how long I was away.'],
            ['enthusiastic', 'Send an overly enthusiastic welcome message.'],
            ['repair', 'Ask me to repair my entire plan.'],
            ['zero', 'Treat my return as starting from zero.'],
            ['none', 'None of these would bother me.']
          ]
        }
      ],
      secondary: [
        {
          id: 'secondary_recovery_first', type: 'single', eyebrow: 'A second need: recovery',
          title: 'What should appear first after an interruption?',
          options: [['resume','A resume button.'],['next','The next action.'],['summary','A short summary.'],['choice','Resume, simplify or postpone.'],['nothing','Nothing special.']]
        },
        {
          id: 'secondary_recovery_absence', type: 'single', eyebrow: 'A second need: recovery',
          title: 'Should the app mention a long absence?',
          options: [['no','No.'],['neutral','Only neutrally.'],['supportive','With a short supportive message.'],['custom','Let me choose.']]
        }
      ]
    },

    low_energy: {
      label: 'Low-energy days',
      short: 'Getting through low-energy days',
      theme: 'lilac',
      description: 'Reducing demands, accepting variable capacity, and making rest legitimate.',
      causeQuestion: {
        id: 'low_energy_causes', type: 'multi', max: 2, eyebrow: 'Low-energy days',
        title: 'On a low-energy day, what becomes most difficult?',
        help: 'Choose up to two answers.',
        options: [
          ['choose', 'Choosing what matters.'],
          ['long_list', 'Looking at a long list.'],
          ['start', 'Starting even a small action.'],
          ['visual', 'Processing too much visual information.'],
          ['complete', 'Completing an entire task.'],
          ['deadlines', 'Handling deadlines.'],
          ['notifications', 'Responding to notifications.'],
          ['accept', 'Accepting that I can do less than planned.'],
          ['other', 'Something else.']
        ]
      },
      conditional: {
        choose: {
          id: 'low_energy_choose_help', type: 'single', eyebrow: 'Choosing with less effort',
          title: 'How should the app help choose what matters on a low-energy day?',
          options: [
            ['one', 'Suggest one essential action.'],
            ['fixed', 'Protect fixed commitments first.'],
            ['energy', 'Match activities to low energy.'],
            ['questions', 'Ask two quick questions.'],
            ['rest', 'Include rest as a valid option.'],
            ['manual', 'Let me choose without a recommendation.']
          ]
        },
        long_list: {
          id: 'low_energy_list_help', type: 'single', eyebrow: 'Reducing the list',
          title: 'What should happen to the full activity list?',
          options: [
            ['hide', 'Hide it temporarily.'],
            ['one', 'Show only one essential activity.'],
            ['three', 'Show no more than three.'],
            ['small_versions', 'Replace tasks with smaller versions.'],
            ['postpone', 'Move nonessential activities automatically.'],
            ['unchanged', 'Leave it unchanged.']
          ]
        },
        start: {
          id: 'low_energy_start_help', type: 'single', eyebrow: 'Starting with limited energy',
          title: 'What kind of first step would feel realistic?',
          options: [
            ['two_minutes', 'A two-minute action.'],
            ['five_minutes', 'A five-minute attempt.'],
            ['prepare', 'Only prepare the environment.'],
            ['partial', 'Complete one part and stop.'],
            ['body_double', 'Use optional body-doubling support.'],
            ['rest', 'Choose rest without losing progress.']
          ]
        },
        visual: {
          id: 'low_energy_visual_help', type: 'single', eyebrow: 'Reducing visual load',
          title: 'What should a low-stimulation screen remove?',
          options: [
            ['decorations', 'Decorations and animations.'],
            ['secondary', 'Secondary information.'],
            ['colours', 'Most colour differences.'],
            ['progress', 'Progress and reward elements.'],
            ['navigation', 'Nonessential navigation.'],
            ['custom', 'Let me choose what disappears.']
          ]
        },
        complete: {
          id: 'low_energy_complete_help', type: 'single', eyebrow: 'Partial completion',
          title: 'How should partial completion be represented?',
          options: [
            ['steps', 'Mark completed steps without closing the whole task.'],
            ['percentage', 'Show a simple percentage.'],
            ['progress_today', 'Record that I made progress today.'],
            ['start_reward', 'Give credit for beginning.'],
            ['enough', 'Let me choose what counts as enough today.'],
            ['none', 'Do not quantify partial completion.']
          ]
        },
        deadlines: {
          id: 'low_energy_deadlines_help', type: 'single', eyebrow: 'Deadlines and limited capacity',
          title: 'How should deadlines be handled on a low-energy day?',
          options: [
            ['essential', 'Show only genuinely urgent deadlines.'],
            ['prepare', 'Suggest the smallest protective action.'],
            ['reschedule', 'Help me reschedule what can move.'],
            ['support', 'Suggest asking for help or an extension.'],
            ['unchanged', 'Leave deadlines unchanged.'],
            ['hide', 'Temporarily hide them.']
          ]
        },
        notifications: {
          id: 'low_energy_notifications_help', type: 'single', eyebrow: 'Notification load',
          title: 'What should happen to notifications?',
          options: [
            ['pause', 'Pause nonessential notifications.'],
            ['batch', 'Collect them into one later summary.'],
            ['essential', 'Allow only selected essential reminders.'],
            ['reduce', 'Reduce their frequency.'],
            ['manual', 'Let me control each category.'],
            ['unchanged', 'Leave them unchanged.']
          ]
        },
        accept: {
          id: 'low_energy_accept_help', type: 'single', eyebrow: 'Variable capacity',
          title: 'What would make doing less feel legitimate rather than like failure?',
          options: [
            ['day_version', 'A clearly labelled low-energy version of the day.'],
            ['progress', 'Progress that includes rest and adjustment.'],
            ['no_comparison', 'No comparison with previous days.'],
            ['language', 'Neutral language without productivity pressure.'],
            ['enough', 'A way to define “enough for today.”'],
            ['nothing', 'I do not want the app to comment on this.']
          ]
        },
        other: {
          id: 'low_energy_other_text', type: 'text', optional: true, eyebrow: 'Your experience',
          title: 'What else becomes difficult on low-energy days?', placeholder: 'A short answer is enough…', maxLength: 500
        }
      },
      common: [
        {
          id: 'low_energy_changes', type: 'multi', max: 4, eyebrow: 'Low-energy mode',
          title: 'What should a low-energy mode change?',
          help: 'Choose up to four.',
          options: [
            ['fewer', 'Show fewer activities.'],
            ['five', 'Suggest a five-minute first step.'],
            ['visual', 'Reduce visual stimulation.'],
            ['notifications', 'Pause nonessential notifications.'],
            ['deadlines', 'Hide deadlines temporarily.'],
            ['one', 'Show only one essential activity.'],
            ['smaller', 'Offer smaller versions of planned tasks.'],
            ['partial', 'Count partial completion as progress.'],
            ['no_suggestions', 'Avoid automatic suggestions.'],
            ['rest', 'Ask whether rest should be the next action.'],
            ['nothing', 'Nothing automatically.']
          ]
        },
        {
          id: 'low_energy_activation', type: 'single', eyebrow: 'Activation',
          title: 'How should low-energy mode be activated?',
          options: [
            ['manual', 'I activate it manually.'],
            ['opening', 'The app asks when I open it.'],
            ['planning', 'I choose my energy level during planning.'],
            ['schedule', 'It activates according to a schedule I define.'],
            ['suggest', 'The app may suggest it based on my recent app activity.'],
            ['none', 'I do not want a separate low-energy mode.']
          ]
        }
      ],
      secondary: [
        {
          id: 'secondary_low_energy_view', type: 'single', eyebrow: 'A second need: low-energy days',
          title: 'What should a low-energy view prioritise?',
          options: [['one','One essential action.'],['small','Smaller task versions.'],['rest','Rest as a valid option.'],['quiet','A low-stimulation screen.'],['none','No separate view.']]
        },
        {
          id: 'secondary_low_energy_progress', type: 'single', eyebrow: 'A second need: low-energy days',
          title: 'Should partial progress count visibly?',
          options: [['yes','Yes.'],['steps','Only completed steps.'],['private','Record it privately.'],['no','No.']]
        }
      ]
    },

    capture: {
      label: 'Quick capture',
      short: 'Capturing tasks or ideas before forgetting them',
      theme: 'pink',
      description: 'Saving thoughts with almost no friction and organising them later.',
      causeQuestion: {
        id: 'capture_types', type: 'multi', max: 3, eyebrow: 'Quick capture',
        title: 'What do you most often need to capture quickly?',
        help: 'Choose up to three answers.',
        options: [
          ['task', 'A task.'],
          ['idea', 'An idea.'],
          ['remember', 'Something to remember later.'],
          ['message', 'A message or conversation to return to.'],
          ['current', 'A detail related to my current activity.'],
          ['thought', 'A worry or thought I need to put aside.'],
          ['appointment', 'An appointment or date.'],
          ['resource', 'A link, image or document.'],
          ['other', 'Something else.']
        ]
      },
      conditional: {
        task: {
          id: 'capture_task_help', type: 'single', eyebrow: 'Capturing a task',
          title: 'What should be required when adding a task quickly?',
          options: [
            ['text', 'Only the text.'],
            ['date', 'Text and an optional date.'],
            ['type', 'Text and a task type.'],
            ['importance', 'Text and importance.'],
            ['prompt', 'A few optional prompts.'],
            ['full', 'A detailed form.']
          ]
        },
        idea: {
          id: 'capture_idea_help', type: 'single', eyebrow: 'Capturing an idea',
          title: 'After an idea is saved, what should happen?',
          options: [
            ['inbox', 'Put it in a simple inbox.'],
            ['hide', 'Hide it until a review time I choose.'],
            ['task', 'Ask whether it should become a task.'],
            ['suggest', 'Suggest where it belongs.'],
            ['current', 'Connect it to my current activity.'],
            ['untouched', 'Leave it untouched until I organise it.']
          ]
        },
        remember: {
          id: 'capture_remember_help', type: 'single', eyebrow: 'Remembering later',
          title: 'How should the app bring the item back later?',
          options: [
            ['specific', 'At a specific time I choose.'],
            ['context', 'In the relevant context or project.'],
            ['review', 'During a regular review.'],
            ['next_open', 'The next time I open the app.'],
            ['suggest', 'Let the app suggest a reasonable time.'],
            ['manual', 'Only when I look for it.']
          ]
        },
        message: {
          id: 'capture_message_help', type: 'single', eyebrow: 'Returning to conversations',
          title: 'What information should be saved with a conversation to revisit?',
          options: [
            ['person', 'The person and a short note.'],
            ['channel', 'The person, channel and next action.'],
            ['date', 'A preferred reply date.'],
            ['link', 'A link or screenshot.'],
            ['minimal', 'Only a short reminder.'],
            ['custom', 'Let me choose each time.']
          ]
        },
        current: {
          id: 'capture_current_help', type: 'single', eyebrow: 'Preserving current context',
          title: 'Should a quick note automatically connect to the current activity?',
          options: [
            ['always', 'Yes, automatically.'],
            ['offer', 'Offer the connection.'],
            ['session', 'Only during focus sessions.'],
            ['manual', 'Only when I choose it.'],
            ['no', 'No.']
          ]
        },
        thought: {
          id: 'capture_thought_help', type: 'single', eyebrow: 'Putting a thought aside',
          title: 'What should happen to a thought you need to set aside?',
          options: [
            ['private_inbox', 'Save it to a private temporary inbox.'],
            ['review', 'Ask when I want to revisit it.'],
            ['end_session', 'Show it after the current focus session.'],
            ['archive', 'Archive it without creating a task.'],
            ['delete', 'Offer automatic deletion after a chosen time.'],
            ['manual', 'Leave it untouched.']
          ]
        },
        appointment: {
          id: 'capture_appointment_help', type: 'single', eyebrow: 'Dates and appointments',
          title: 'How should a quickly captured date be handled?',
          options: [
            ['calendar', 'Offer to add it to my calendar.'],
            ['inbox', 'Keep it in an inbox until I confirm details.'],
            ['reminder', 'Create a simple reminder.'],
            ['questions', 'Ask only for the missing essential details.'],
            ['manual', 'Do nothing automatically.']
          ]
        },
        resource: {
          id: 'capture_resource_help', type: 'single', eyebrow: 'Links and files',
          title: 'How should links, images or documents be organised?',
          options: [
            ['inbox', 'Put them in a resource inbox.'],
            ['current', 'Connect them to the current activity.'],
            ['project', 'Ask for a project only.'],
            ['review', 'Show them during a later review.'],
            ['suggest', 'Suggest a destination.'],
            ['manual', 'Leave them unorganised.']
          ]
        },
        other: {
          id: 'capture_other_text', type: 'text', optional: true, eyebrow: 'Your experience',
          title: 'What else do you need to capture quickly?', placeholder: 'A short answer is enough…', maxLength: 500
        }
      },
      common: [
        {
          id: 'capture_required', type: 'single', eyebrow: 'Capture friction',
          title: 'What should be required when capturing something quickly?',
          options: [
            ['text', 'Only the text.'],
            ['time', 'Text and a rough time or date.'],
            ['type', 'Text and a type, such as task or note.'],
            ['importance', 'Text and importance.'],
            ['detailed', 'A detailed form.'],
            ['depends', 'It should depend on what I am capturing.']
          ]
        },
        {
          id: 'capture_focus_actions', type: 'single', eyebrow: 'Capture during focus',
          title: 'During a focus or hyperfocus session, how many actions should quick capture require?',
          options: [
            ['typing', 'One tap and immediate typing.'],
            ['task', 'One tap to save a task.'],
            ['note', 'One tap to save a note.'],
            ['choice', 'Two taps to choose between task and note.'],
            ['voice', 'Voice capture.'],
            ['full', 'I do not mind opening a full form.']
          ]
        }
      ],
      secondary: [
        {
          id: 'secondary_capture_required', type: 'single', eyebrow: 'A second need: quick capture',
          title: 'What should quick capture require?',
          options: [['text','Only text.'],['type','Text and note/task type.'],['date','Text and an optional date.'],['prompts','A few prompts.']]
        },
        {
          id: 'secondary_capture_after', type: 'single', eyebrow: 'A second need: quick capture',
          title: 'Where should captured items go?',
          options: [['inbox','A simple inbox.'],['later','A later review.'],['current','The current activity.'],['suggest','A suggested location.'],['manual','Leave them untouched.']]
        }
      ]
    }
  };

  const initial = [
    {
      id: 'age_eligibility', type: 'single', eyebrow: 'Pre-test eligibility',
      title: 'Are you 18 years old or older?',
      help: 'This first pre-test is limited to adults. This answer is used only to determine whether you can take part in this round.',
      options: [
        ['yes', 'Yes, I am 18 or older.'],
        ['no', 'No, I am under 18.']
      ]
    },
    {
      id: 'recent_difficulty', type: 'single', eyebrow: 'A recent real-life situation',
      title: 'Without focusing on your worst day, think about a recent everyday situation where you wanted or needed to do something and it felt hard. Which part was most difficult?',
      help: 'Choose the answer that fits best. You do not need to think about your worst moment: answer based on what feels generally true for you or what comes to mind most easily.',
      options: [
        ['beginning', 'Beginning the activity.'],
        ['deciding', 'Deciding what to do first.'],
        ['staying', 'Staying with it.'],
        ['time', 'Keeping track of time.'],
        ['switching', 'Stopping or switching.'],
        ['returning', 'Returning after an interruption.'],
        ['remembering', 'Remembering it at the right time.'],
        ['other', 'Something else.'],
        ['unsure', 'I cannot think of a recent example.']
      ]
    },
    {
      id: 'current_strategy', type: 'single', optional: true, eyebrow: 'A recent real-life situation',
      title: 'What did you do in that situation?',
      help: 'Optional. Choose the answer that comes closest.',
      options: [
        ['app', 'Used an app, timer or planning tool.'],
        ['wrote', 'Wrote something down.'],
        ['person', 'Asked another person for help.'],
        ['urgency', 'Waited until urgency made it easier.'],
        ['smaller', 'Made the task smaller.'],
        ['switched', 'Switched to something else.'],
        ['no_strategy', 'Did not find a workable strategy.'],
        ['other', 'Something else.'],
        ['remember_not', 'I do not remember.']
      ]
    },
    {
      id: 'relationship', type: 'single', optional: true, eyebrow: 'A little context',
      title: 'What is your relationship with ADHD?',
      help: 'This question is optional and is not used to diagnose anyone.',
      options: [
        ['diagnosed', 'I have received an ADHD diagnosis.'],
        ['assessment', 'An assessment is in progress.'],
        ['suspect', 'I think I may be affected, without an assessment in progress.'],
        ['support', 'I am a relative, supporter or professional.'],
        ['not_direct', 'I am not directly affected.'],
        ['prefer_not', 'I prefer not to answer.']
      ]
    },
    {
      id: 'age_range', type: 'single', optional: true, eyebrow: 'A little context',
      title: 'What is your age range?',
      options: [
        ['18_24', '18–24'], ['25_34', '25–34'], ['35_44', '35–44'],
        ['45_54', '45–54'], ['55_plus', '55 or older'], ['prefer_not', 'I prefer not to answer.']
      ]
    },
    {
      id: 'existing_tools', type: 'multi', max: 5, optional: true, eyebrow: 'Current tools',
      title: 'Do you currently use any tools or methods to organise daily life?',
      help: 'Choose everything that applies, or skip this question.',
      options: [
        ['calendar', 'Calendar or agenda'], ['tasks', 'Task list'], ['timer', 'Timer or Pomodoro method'],
        ['routine', 'Routine app'], ['focus', 'Focus app'], ['paper', 'Paper notes or notebook'],
        ['automation', 'Automations or reminders'], ['none', 'No regular method'], ['other', 'Something else']
      ]
    },
    {
      id: 'primary_path', type: 'path', eyebrow: 'Your main need',
      title: 'What would you most want this app to make easier?',
      help: 'Choose the area that matters most right now. This will change the questions you see next.'
    }
  ];

  const secondaryQuestion = {
    id: 'secondary_path', type: 'secondary_path', optional: true, eyebrow: 'An optional second area',
    title: 'Is there another area you would also like the app to support?',
    help: 'You can explore one additional area, or continue without it.'
  };

  const shared = [
    {
      id: 'visual_home_density', type: 'visual_single', eyebrow: 'Interface examples',
      title: 'Which home screen would feel easiest to process?',
      help: 'These are simple comparison wireframes, not final designs.',
      cards: [
        { value: 'one', title: 'One next action', caption: 'Only the current action is visible.', preview: 'home_one' },
        { value: 'few', title: 'One action + alternatives', caption: 'One suggested action and two alternatives.', preview: 'home_few' },
        { value: 'list', title: 'Short daily list', caption: 'Several activities are visible at once.', preview: 'home_list' },
        { value: 'custom', title: 'I would want to configure this', caption: 'The best view would depend on the day.', preview: 'home_custom' }
      ]
    },
    {
      id: 'visual_focus_surface', type: 'visual_single', eyebrow: 'Interface examples',
      title: 'Which focus-session display looks least distracting?',
      help: 'Choose the display that feels easiest to stay with.',
      cards: [
        { value: 'timer_only', title: 'Timer only', caption: 'A very sparse session view.', preview: 'focus_timer' },
        { value: 'task_timer', title: 'Task + timer', caption: 'Current task remains visible.', preview: 'focus_task' },
        { value: 'veil_capture', title: 'Colour veil + quick capture', caption: 'A calm overlay with a note button.', preview: 'focus_veil' },
        { value: 'tap_to_show', title: 'Mostly hidden', caption: 'Show controls only after a tap.', preview: 'focus_hidden' }
      ]
    },
    {
      id: 'visual_quick_capture', type: 'visual_single', eyebrow: 'Interface examples',
      title: 'During a focus session, which quick-capture interaction would interrupt you least?',
      cards: [
        { value: 'text_only', title: 'Instant text note', caption: 'One tap, then type.', preview: 'capture_text' },
        { value: 'note_task', title: 'Choose note or task', caption: 'One extra choice before saving.', preview: 'capture_choice' },
        { value: 'inbox', title: 'Temporary inbox', caption: 'Save it quickly, classify later.', preview: 'capture_inbox' },
        { value: 'full_form', title: 'Full form', caption: 'Add more detail before saving.', preview: 'capture_form' }
      ]
    },
    {
      id: 'visual_return_screen', type: 'visual_single', eyebrow: 'Interface examples',
      title: 'After a break or several days away, which return screen feels most comfortable?',
      cards: [
        { value: 'resume', title: 'Resume only', caption: 'A large button to continue.', preview: 'return_resume' },
        { value: 'resume_or_fresh', title: 'Resume or start fresh', caption: 'Choose how to continue.', preview: 'return_choice' },
        { value: 'summary', title: 'Gentle summary', caption: 'See where you stopped without blame.', preview: 'return_summary' },
        { value: 'question', title: 'What do you need today?', caption: 'Begin with a fresh support prompt.', preview: 'return_question' }
      ]
    },
    {
      id: 'suggestions_discomfort', type: 'multi', max: 4, optional: true, eyebrow: 'Possible drawbacks',
      title: 'What might make automatic suggestions uncomfortable?',
      help: 'Choose everything that applies, or skip.',
      options: [
        ['why', 'Not knowing why something was suggested.'],
        ['ordered', 'Feeling ordered around.'],
        ['unsuitable', 'Repeatedly seeing unsuitable suggestions.'],
        ['same', 'Being shown the same action again and again.'],
        ['control', 'Losing control over my plan.'],
        ['none', 'Nothing in particular.']
      ]
    },
    {
      id: 'reward_discomfort', type: 'multi', max: 5, optional: true, eyebrow: 'Possible drawbacks',
      title: 'What might make a reward system discouraging?',
      help: 'Choose everything that applies, or skip.',
      options: [
        ['loss', 'Losing progress.'],
        ['repetitive', 'Rewards becoming repetitive.'],
        ['watched', 'Feeling watched or evaluated.'],
        ['praise', 'Praise that feels excessive.'],
        ['pressure', 'Feeling pressured to open the app daily.'],
        ['compare', 'Comparing myself with other users.'],
        ['any', 'Any reward system would bother me.']
      ]
    },
    {
      id: 'companion_discomfort', type: 'multi', max: 5, optional: true, eyebrow: 'Possible drawbacks',
      title: 'When might a companion become distracting or uncomfortable?',
      help: 'Choose everything that applies, or skip.',
      options: [
        ['overwhelmed', 'When I am already overwhelmed.'],
        ['focus', 'During focus sessions.'],
        ['missed', 'When it comments on missed activities.'],
        ['animations', 'When animations cannot be disabled.'],
        ['emotional', 'When it uses overly emotional language.'],
        ['none', 'I would not mind a companion.'],
        ['prefer_none', 'I would prefer no companion at all.']
      ]
    },
    {
      id: 'progress_style', type: 'single', eyebrow: 'Long-term progress',
      title: 'Which visible sign of progress would feel most pleasant?',
      options: [
        ['home', 'A personal room or home gradually expanding.'],
        ['objects', 'New objects or details appearing in a space.'],
        ['companion', 'A companion reacting to my progress.'],
        ['abstract', 'A simple abstract progress indicator.'],
        ['history', 'A written history of what I have done.'],
        ['none', 'No visible progression system.'],
        ['disable', 'Let me disable or change it.']
      ]
    },
    {
      id: 'personality', type: 'single', eyebrow: 'Tone and personality',
      title: 'What personality should the app have?',
      options: [
        ['calm', 'A calm companion.'], ['practical', 'A practical assistant.'], ['gentle', 'A gentle guide.'],
        ['energetic', 'An energetic coach.'], ['discreet', 'A discreet tool with very little personality.'],
        ['adaptive', 'Different personalities depending on the situation.'], ['other', 'Something else.']
      ]
    },
    {
      id: 'name_first', type: 'single', eyebrow: 'First impression',
      title: 'Without any explanation, which name would make you most curious about the app?',
      help: 'The names are shown in a random order.',
      randomize: true,
      options: [['astelle','Astelle'],['tramelia','Tramelia'],['sillage','Sillage'],['none','None of them'],['unsure','I cannot choose yet']]
    },
    {
      id: 'name_association', type: 'text', optional: true, eyebrow: 'First impression',
      title: 'What does your chosen name make you imagine?',
      help: 'This is optional. A few words are enough.',
      placeholder: 'For example: calm, stars, movement…', maxLength: 500
    },
    {
      id: 'name_rate_astelle', type: 'universe', universe: 'astelle', eyebrow: 'Name universe 1 of 3',
      title: 'How does the Astelle universe feel to you?',
      description: 'A soft, celestial home that helps you find a next point of light without pushing you. Its space grows through cumulative use and never shrinks after time away.'
    },
    {
      id: 'name_rate_tramelia', type: 'universe', universe: 'tramelia', eyebrow: 'Name universe 2 of 3',
      title: 'How does the Tramelia universe feel to you?',
      description: 'A living network of threads connecting tasks, energy, time and ideas. The experience focuses on making relationships visible without turning life into a rigid plan.'
    },
    {
      id: 'name_rate_sillage', type: 'universe', universe: 'sillage', eyebrow: 'Name universe 3 of 3',
      title: 'How does the Sillage universe feel to you?',
      description: 'A calm path built from the traces you leave. Every return continues the path, and progress remains visible even when your rhythm changes or you take a long break.'
    },
    {
      id: 'name_final', type: 'single', eyebrow: 'Final name choice',
      title: 'Now that you know the project better, which universe would you choose?',
      randomize: true,
      options: [['astelle','Astelle'],['tramelia','Tramelia'],['sillage','Sillage'],['none','None of them'],['unsure','I still cannot choose']]
    },
    {
      id: 'survey_ease', type: 'single', eyebrow: 'About this questionnaire',
      title: 'How easy was this questionnaire to follow?',
      options: [['very_easy','Very easy'],['easy','Rather easy'],['difficult','Rather difficult'],['very_difficult','Very difficult']]
    },
    {
      id: 'survey_save_helpful', type: 'single', eyebrow: 'About this questionnaire',
      title: 'Did knowing you could save and return make the questionnaire easier to begin?',
      options: [
        ['yes_lot', 'Yes, considerably.'],
        ['yes_little', 'Yes, slightly.'],
        ['no_difference', 'No difference.'],
        ['more_complicated', 'It made the process feel more complicated.'],
        ['not_notice', 'I did not notice that option.']
      ]
    },
    {
      id: 'survey_save_clear', type: 'single', eyebrow: 'About this questionnaire',
      title: 'Was the save-and-return option easy to understand?',
      options: [['very_clear','Very clear'],['clear','Rather clear'],['unclear','Rather unclear'],['very_unclear','Very unclear']]
    },
    {
      id: 'survey_friction', type: 'multi', max: 6, optional: true, eyebrow: 'About this questionnaire',
      title: 'Did you experience any of these difficulties?',
      help: 'Choose everything that applies, or skip.',
      options: [
        ['too_many', 'Too many questions.'], ['too_much_text', 'Too much text.'], ['too_many_choices', 'Too many choices.'],
        ['remember', 'Difficulty remembering previous options.'], ['choose', 'Difficulty choosing an answer.'],
        ['repetition', 'The questionnaire felt repetitive.'], ['explanations', 'Not enough explanation.'],
        ['none', 'No particular difficulty.'], ['other', 'Something else.']
      ]
    },
    {
      id: 'survey_comment', type: 'text', optional: true, eyebrow: 'One last thought',
      title: 'What would make this questionnaire or the future app easier to use?',
      help: 'Optional. A short answer is enough.', placeholder: 'Your suggestion…', maxLength: 1200
    }
  ];

  const universeWords = [
    ['reassuring', 'Reassuring'], ['motivating', 'Motivating'], ['calm', 'Calm'], ['playful', 'Playful'],
    ['clear', 'Clear'], ['too_childish', 'Too childish'], ['too_abstract', 'Too abstract'],
    ['too_serious', 'Too serious'], ['memorable', 'Memorable'], ['not_for_me', 'Not for me']
  ];

  Object.entries(paths).forEach(([key, path]) => {
    path.causeQuestion.type = 'single';
    delete path.causeQuestion.max;
    path.causeQuestion.help = 'Choose the answer that fits best. The next question will adapt to it.';
  });

  return {
    paths,
    initial,
    secondaryQuestion,
    shared,
    universeWords,
    stages: [
      ['context', 'Context'],
      ['needs', 'Adaptive path'],
      ['experience', 'Design choices'],
      ['universe', 'Name study'],
      ['finish', 'Finish']
    ]
  };
})();

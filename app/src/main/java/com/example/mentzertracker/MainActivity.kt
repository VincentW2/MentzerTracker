package com.example.mentzertracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mentzertracker.ui.theme.MentzerTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentzerTrackerTheme {
                AppRoot()
            }
        }
    }
}

// ---------- DATA MODELS ----------

data class Exercise(
    val id: String,
    val name: String
)

data class WorkoutTemplate(
    val id: String,        // "A" or "B"
    val name: String,      // "Workout A"
    val exerciseIds: List<String>
)

data class ExerciseSetEntry(
    val exerciseId: String,
    val weight: Float,
    val reps: Int
)

data class WorkoutLogEntry(
    val id: Long,              // timestamp
    val templateId: String,    // "A" or "B"
    val date: String,
    val sets: List<ExerciseSetEntry>
)
data class SessionPoint(
    val sessionIndex: Int,
    val date: String,
    val weight: Float,
    val reps: Int
)


// ---------- HARD-CODED EXERCISES & TEMPLATES ----------

val allExercises = listOf(
    Exercise("squat", "Smith Squat"),
    Exercise("deadlift", "Deadlift"),
    Exercise("pulldown", "Close-Grip Palm-Up Pulldown"),
    Exercise("incline_press", "Incline Press"),
    Exercise("dips", "Dips")
)

// Edit these however you want, but keep them as A / B
val workoutA = WorkoutTemplate(
    id = "A",
    name = "Workout A",
    exerciseIds = listOf("squat", "pulldown")
)

val workoutB = WorkoutTemplate(
    id = "B",
    name = "Workout B",
    exerciseIds = listOf("deadlift", "incline_press", "dips")
)

val templates = listOf(workoutA, workoutB)

@Composable
fun AppRoot() {
    val context = LocalContext.current

    // Initialize from SharedPreferences once
    var showSplash by remember {
        mutableStateOf(!hasSeenSplash(context))
    }

    if (showSplash) {
        SplashScreen(
            onStart = {
                setHasSeenSplash(context)
                showSplash = false
            }
        )
    } else {
        WorkoutTrackerApp()
    }
}


@Composable
fun SplashScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "MentzerTracker",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Welcome to MentzerTracker.\nTrack your A/B workouts Mentzer-style.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onStart) {
                Text("Start")
            }
        }
    }
}


// ---------- TOP-LEVEL UI ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackerApp() {
    var selectedTemplateId by remember { mutableStateOf("A") }

    // In-memory list of workout logs
    val logEntries = remember { mutableStateListOf<WorkoutLogEntry>() }

    val currentTemplate = templates.first { it.id == selectedTemplateId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mentzer A/B Tracker") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TemplateSelector(
                selectedTemplateId = selectedTemplateId,
                onTemplateSelected = { selectedTemplateId = it }
            )

            LogWorkoutSection(
                template = currentTemplate,
                onSave = { sets ->
                    val entry = WorkoutLogEntry(
                        id = System.currentTimeMillis(),
                        templateId = currentTemplate.id,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date()),
                        sets = sets
                    )
                    logEntries.add(entry)
                }
            )

            ProgressSection(
                logs = logEntries,
                exercises = allExercises
            )
        }
    }
}

// ---------- TEMPLATE SELECTOR (A / B) ----------

@Composable
fun TemplateSelector(
    selectedTemplateId: String,
    onTemplateSelected: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        templates.forEach { template ->
            val isSelected = template.id == selectedTemplateId
            if (isSelected) {
                Button(onClick = { onTemplateSelected(template.id) }) {
                    Text(template.name)
                }
            } else {
                OutlinedButton(
                    onClick = { onTemplateSelected(template.id) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(template.name)
                }
            }
        }
    }
}

// ---------- LOGGING A WORKOUT ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutSection(
    template: WorkoutTemplate,
    onSave: (List<ExerciseSetEntry>) -> Unit
) {
    Text("Log ${template.name}", style = MaterialTheme.typography.titleMedium)

    // Per-exercise fields
    val weightState = remember { mutableStateMapOf<String, String>() }
    val repsState = remember { mutableStateMapOf<String, String>() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        template.exerciseIds.forEach { exerciseId ->
            val exercise = allExercises.first { it.id == exerciseId }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    exercise.name,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = weightState[exerciseId] ?: "",
                    onValueChange = { weightState[exerciseId] = it },
                    label = { Text("lbs") },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = repsState[exerciseId] ?: "",
                    onValueChange = { repsState[exerciseId] = it },
                    label = { Text("reps") },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

                )
            }
        }

        Button(
            onClick = {
                val sets = template.exerciseIds.mapNotNull { id ->
                    val w = weightState[id]?.toFloatOrNull()
                    val r = repsState[id]?.toIntOrNull()
                    if (w != null && r != null) {
                        ExerciseSetEntry(
                            exerciseId = id,
                            weight = w,
                            reps = r
                        )
                    } else null
                }
                if (sets.isNotEmpty()) {
                    onSave(sets)
                    // Clear fields after save
                    template.exerciseIds.forEach { id ->
                        weightState[id] = ""
                        repsState[id] = ""
                    }
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Save session")
        }
    }
}

// ---------- SIMPLE PROGRESS VIEW ----------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgressSection(
    logs: List<WorkoutLogEntry>,
    exercises: List<Exercise>
) {
    Text("Progress", style = MaterialTheme.typography.titleMedium)

    if (logs.isEmpty()) {
        Text("No sessions logged yet.")
        return
    }

    // Build history per exercise: (session index, date, weight)
    val histories = remember(logs) {
        exercises.associateWith { ex ->
            logs
                .flatMapIndexed { index, log ->
                    log.sets
                        .filter { it.exerciseId == ex.id }
                        .map { setEntry ->
                            Triple(index + 1, log.date, setEntry.weight)
                        }
                }
        }.filterValues { it.isNotEmpty() }
    }

    if (histories.isEmpty()) {
        Text("No data for any exercises yet.")
        return
    }

    // Default to first exercise that actually has history
    var selectedExercise by remember { mutableStateOf(histories.keys.first()) }

    // If selected exercise loses data (edge case), fall back
    if (!histories.containsKey(selectedExercise)) {
        selectedExercise = histories.keys.first()
    }

    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExerciseDropdown(
            exercisesWithHistory = histories.keys.toList(),
            selected = selectedExercise,
            onSelectedChange = { selectedExercise = it }
        )

        Spacer(Modifier.height(4.dp))

        // Card containing pager: Graph | Text
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Small header showing which page you're on
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedExercise.name,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isGraph = pagerState.currentPage == 0
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Graph",
                            tint = if (isGraph)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "List",
                            tint = if (!isGraph)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                val history = histories[selectedExercise] ?: emptyList()

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> ExerciseLineChart(
                            history = history
                        )

                        1 -> ExerciseHistoryList(
                            history = history
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ExerciseDropdown(
    exercisesWithHistory: List<Exercise>,
    selected: Exercise,
    onSelectedChange: (Exercise) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = selected.name,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Choose exercise"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        exercisesWithHistory.forEach { ex ->
            DropdownMenuItem(
                text = { Text(ex.name) },
                onClick = {
                    onSelectedChange(ex)
                    expanded = false
                }
            )
        }
    }
}
@Composable
fun ExerciseLineChart(
    history: List<Triple<Int, String, Float>>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No data yet for this exercise.")
        }
        return
    }

    val primary = MaterialTheme.colorScheme.primary
    val fillColor = primary.copy(alpha = 0.25f)

    // Normalize data
    val weights = history.map { it.third }
    val minWeight = weights.minOrNull() ?: 0f
    val maxWeight = weights.maxOrNull() ?: 0f
    val range = (maxWeight - minWeight).takeIf { it > 0f } ?: 1f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        val leftPadding = 16.dp.toPx()
        val rightPadding = 16.dp.toPx()
        val topPadding = 16.dp.toPx()
        val bottomPadding = 24.dp.toPx()

        val usableWidth = width - leftPadding - rightPadding
        val usableHeight = height - topPadding - bottomPadding

        val stepX = if (history.size == 1) 0f else usableWidth / (history.size - 1)

        // Map points
        val points = history.mapIndexed { index, triple ->
            val weight = triple.third
            val x = leftPadding + stepX * index
            val normalized = (weight - minWeight) / range
            val y = topPadding + (1f - normalized) * usableHeight
            Offset(x, y)
        }

        // Draw axis (simple baseline)
        val axisY = topPadding + usableHeight
        drawLine(
            color = Color.Gray.copy(alpha = 0.6f),
            start = Offset(leftPadding, axisY),
            end = Offset(width - rightPadding, axisY),
            strokeWidth = 2.dp.toPx()
        )

        if (points.size >= 2) {
            // Line path
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            // Fill path (area under the line)
            val fillPath = Path().apply {
                moveTo(points.first().x, axisY)
                for (pt in points) {
                    lineTo(pt.x, pt.y)
                }
                lineTo(points.last().x, axisY)
                close()
            }

            drawPath(
                path = fillPath,
                color = fillColor
            )

            drawPath(
                path = linePath,
                color = primary,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw circles at each point
        points.forEach { pt ->
            drawCircle(
                color = primary,
                radius = 6.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.Black,
                radius = 3.dp.toPx(),
                center = pt
            )
        }
    }
}
@Composable
fun ExerciseHistoryList(
    history: List<Triple<Int, String, Float>>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No history yet.")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(history) { (index, date, weight) ->
            Text("Session $index ($date): ${weight} lbs")
        }
    }
}

private const val PREFS_NAME = "mentzer_prefs"
private const val KEY_HAS_SEEN_SPLASH = "has_seen_splash"

private fun hasSeenSplash(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_HAS_SEEN_SPLASH, false)
}

private fun setHasSeenSplash(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_HAS_SEEN_SPLASH, true).apply()
}

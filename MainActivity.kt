package com.anonto.academy

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate

private val Bg = Color(0xFF090B12)
private val Card = Color(0xFF121724)
private val Accent = Color(0xFF57E6C5)
private val Purple = Color(0xFF9D8CFF)

data class Lesson(val id: Int, val title: String, val module: String, val teacher: String, val mood: String)
data class Module(val title: String, val icon: String, val lessons: List<Lesson>)

class AcademyViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("academy_progress", Context.MODE_PRIVATE)

    var xp by mutableIntStateOf(prefs.getInt("xp", 0)); private set
    var completed by mutableStateOf(prefs.getStringSet("completed", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()); private set
    var quizCompleted by mutableStateOf(prefs.getStringSet("quiz_completed", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()); private set
    var ctfCompleted by mutableStateOf(prefs.getBoolean("ctf_completed", false)); private set
    var streak by mutableIntStateOf(prefs.getInt("streak", 0)); private set
    var teacherVoiceEnabled by mutableStateOf(prefs.getBoolean("teacher_voice", true)); private set
    var subtitlesEnabled by mutableStateOf(prefs.getBoolean("subtitles", true)); private set
    var offlineMode by mutableStateOf(prefs.getBoolean("offline_mode", false)); private set
    var remindersEnabled by mutableStateOf(prefs.getBoolean("reminders", true)); private set

    val modules = listOf(
        Module("Cybersecurity পরিচিতি", "🛡️", listOf("Cybersecurity কী?", "Hacker, Ethical Hacker ও Cybercriminal", "White/Black/Grey Hat", "Cyber Attack কীভাবে ঘটে", "নিরাপদভাবে Ethical Hacking শেখা", "Module Quiz").mapIndexed { i, t -> Lesson(i, t, "Cybersecurity পরিচিতি", "Teacher ${i + 1}", "futuristic") }),
        Module("Internet & Networking", "🌐", listOf("Internet কী?", "IP ও MAC", "Ports, Router, Server, Client", "DNS", "HTTP বনাম HTTPS", "TCP বনাম UDP", "Networking Practice", "Module Quiz").mapIndexed { i, t -> Lesson(10 + i, t, "Internet & Networking", "Network Teacher", "digital") }),
        Module("Linux Basics", "🐧", listOf("Linux পরিচিতি", "Terminal", "Files & Directories", "Permissions", "Processes & Services", "Basic Networking Commands", "Safe Linux Practice").mapIndexed { i, t -> Lesson(20 + i, t, "Linux Basics", "Linux Teacher", "technical") }),
        Module("Privacy & Account Security", "🔐", listOf("Strong Password", "Password Manager", "Hashing Concept", "MFA", "Phishing চিনে রাখা", "Browser Privacy", "Device Security").mapIndexed { i, t -> Lesson(30 + i, t, "Privacy & Account Security", "Privacy Teacher", "calm") }),
        Module("Surface / Deep / Dark Web", "🕸️", listOf("Surface Web", "Deep Web", "Dark Web", "Tor Concept", "Onion Services", "Legal ও Illegal Use", "Privacy & Safety", "Module Quiz").mapIndexed { i, t -> Lesson(40 + i, t, "Surface / Deep / Dark Web", "Dark Web Teacher", "mystery") }),
        Module("OSINT", "🔎", listOf("OSINT কী?", "Public Information", "Search Techniques", "Domain/DNS Information", "Metadata", "Source Verification", "Fictional Investigation", "Module Quiz").mapIndexed { i, t -> Lesson(50 + i, t, "OSINT", "OSINT Detective", "investigation") }),
        Module("Web Security", "🌐", listOf("Website কীভাবে কাজ করে", "Authentication বনাম Authorization", "Input Validation", "XSS Concept", "SQL Injection Concept", "CSRF Concept", "Secure Development", "Safe Vulnerability Lab").mapIndexed { i, t -> Lesson(60 + i, t, "Web Security", "Web Security Teacher", "cyber") }),
        Module("Ethical Hacking Lab", "🧪", listOf("Legal Lab Rules", "Reconnaissance Concepts", "Vulnerability Assessment", "Safe Scanning Concepts", "Finding Vulnerabilities", "Understanding Findings", "Security Report").mapIndexed { i, t -> Lesson(70 + i, t, "Ethical Hacking Lab", "Lab Teacher", "energetic") }),
        Module("CTF Arena", "🏁", listOf("CTF কী?", "Flags", "Beginner Web", "Beginner Linux", "OSINT Challenge", "Encoding/Decoding", "Safe Walkthrough", "Final CTF").mapIndexed { i, t -> Lesson(80 + i, t, "CTF Arena", "CTF Teacher", "cyberpunk") }),
        Module("Malware & Incident Response", "🚨", listOf("Malware Basics", "Virus/Worm/Trojan", "Ransomware Concepts", "Detection Basics", "Incident Response", "Containment & Recovery", "Fictional Case Study", "Final Quiz").mapIndexed { i, t -> Lesson(90 + i, t, "Malware & Incident Response", "IR Teacher", "suspense") })
    )

    private fun save() {
        prefs.edit()
            .putInt("xp", xp)
            .putStringSet("completed", completed.map(Int::toString).toSet())
            .putStringSet("quiz_completed", quizCompleted.map(Int::toString).toSet())
            .putBoolean("ctf_completed", ctfCompleted)
            .putInt("streak", streak)
            .apply()
    }

    private fun touchActivity() {
        val today = LocalDate.now()
        val last = prefs.getString("last_activity", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        streak = when {
            last == today -> streak.coerceAtLeast(1)
            last == today.minusDays(1) -> streak + 1
            else -> 1
        }
        prefs.edit().putString("last_activity", today.toString()).putInt("streak", streak).apply()
    }

    fun complete(id: Int) {
        if (completed.contains(id)) return
        completed = completed + id
        xp += 25
        touchActivity()
        save()
    }

    fun quizCorrect(lessonId: Int) {
        if (quizCompleted.contains(lessonId)) return
        quizCompleted = quizCompleted + lessonId
        xp += 50
        touchActivity()
        save()
    }

    fun completeCtf() {
        if (ctfCompleted) return
        ctfCompleted = true
        xp += 100
        touchActivity()
        save()
    }

    fun setTeacherVoice(value: Boolean) { teacherVoiceEnabled = value; prefs.edit().putBoolean("teacher_voice", value).apply() }
    fun setSubtitles(value: Boolean) { subtitlesEnabled = value; prefs.edit().putBoolean("subtitles", value).apply() }
    fun setOffline(value: Boolean) { offlineMode = value; prefs.edit().putBoolean("offline_mode", value).apply() }
    fun setReminders(value: Boolean) { remindersEnabled = value; prefs.edit().putBoolean("reminders", value).apply() }

    fun moduleProgress(module: Module): Float = if (module.lessons.isEmpty()) 0f else module.lessons.count { completed.contains(it.id) }.toFloat() / module.lessons.size
    fun achievementUnlocked(name: String): Boolean = when (name) {
        "First Lesson" -> completed.isNotEmpty()
        "10 Lessons" -> completed.size >= 10
        "Networking Explorer" -> modules[1].lessons.all { completed.contains(it.id) }
        "Linux Beginner" -> modules[2].lessons.all { completed.contains(it.id) }
        "OSINT Detective" -> modules[5].lessons.all { completed.contains(it.id) }
        "CTF Starter" -> ctfCompleted
        "7-Day Streak" -> streak >= 7
        "Final Exam Complete" -> modules.last().lessons.all { completed.contains(it.id) }
        else -> false
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AnontoAcademyApp() }
    }
}

@Composable fun AnontoAcademyApp(vm: AcademyViewModel = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Card, primary = Accent, secondary = Purple)) {
        Scaffold(containerColor = Bg, bottomBar = {
            NavigationBar(containerColor = Card) {
                listOf(Icons.Default.Home to "Home", Icons.Default.School to "Learn", Icons.Default.Flag to "CTF", Icons.Default.EmojiEvents to "Awards", Icons.Default.Settings to "Settings").forEachIndexed { i, pair ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(pair.first, null) }, label = { Text(pair.second) })
                }
            }
        }) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                if (selectedLesson != null) LessonScreen(selectedLesson!!, vm) { selectedLesson = null } else when (tab) {
                    0 -> HomeScreen(vm) { selectedLesson = it }
                    1 -> LearnScreen(vm) { selectedLesson = it }
                    2 -> CtfScreen(vm)
                    3 -> AwardsScreen(vm)
                    else -> SettingsScreen(vm)
                }
            }
        }
    }
}

@Composable fun Header(title: String, subtitle: String? = null) { Column(Modifier.padding(20.dp, 18.dp, 20.dp, 8.dp)) { Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold); subtitle?.let { Text(it, color = Color.LightGray) } } }
@Composable fun Stat(label: String, value: String, icon: String) { Card(Modifier.padding(5.dp), colors = CardDefaults.cardColors(Card), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(14.dp)) { Text(icon); Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text(label, color = Color.LightGray, fontSize = 12.sp) } } }

@Composable fun HomeScreen(vm: AcademyViewModel, onLesson: (Lesson) -> Unit) {
    LazyColumn {
        item { Header("🛡️ Anonto Academy", "শিখো • Practice করো • Mission সম্পন্ন করো"); Image(painterResource(R.drawable.anonto_academy_logo), "Anonto Academy logo", Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(210.dp)) }
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp)) { Stat("XP", vm.xp.toString(), "⭐"); Stat("Lessons", vm.completed.size.toString(), "📚"); Stat("Streak", if (vm.streak > 0) "Day ${vm.streak}" else "—", "🔥") } }
        item { Card(Modifier.padding(15.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFF17223A))) { Column(Modifier.padding(18.dp)) { Text("🎯 আজকের Mission", fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("একটি fictional security log-এ suspicious event শনাক্ত করো।", Modifier.padding(vertical = 8.dp)); Button(onClick = { onLesson(vm.modules[5].lessons[6]) }) { Text("Mission শুরু") } } } }
        item { Text("Continue Learning", Modifier.padding(20.dp, 8.dp), fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        items(vm.modules.take(3)) { m -> ModuleCard(m, vm, onLesson) }
    }
}

@Composable fun LearnScreen(vm: AcademyViewModel, onLesson: (Lesson) -> Unit) { LazyColumn { item { Header("📚 Learning Roadmap", "Beginner → Intermediate → Advanced") }; items(vm.modules) { ModuleCard(it, vm, onLesson) } } }

@Composable fun ModuleCard(m: Module, vm: AcademyViewModel, onLesson: (Lesson) -> Unit) {
    val progress = vm.moduleProgress(m)
    Card(Modifier.padding(horizontal = 15.dp, vertical = 6.dp).fillMaxWidth().clickable { onLesson(m.lessons.first()) }, colors = CardDefaults.cardColors(Card)) {
        Column(Modifier.padding(16.dp)) { Text("${m.icon}  ${m.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("${m.lessons.size} lessons • Safe Lab • Quiz", color = Color.LightGray); LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth().padding(top = 10.dp)); Text("${(progress * 100).toInt()}% complete", color = Accent, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
    }
}

@Composable fun LessonScreen(lesson: Lesson, vm: AcademyViewModel, onBack: () -> Unit) {
    var playing by remember { mutableStateOf(false) }
    var answered by remember { mutableStateOf(vm.quizCompleted.contains(lesson.id)) }
    var selectedAnswer by remember { mutableStateOf<Int?>(if (answered) 0 else null) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(playing, vm.teacherVoiceEnabled) {
        var player: MediaPlayer? = null
        if (playing && vm.teacherVoiceEnabled) {
            player = MediaPlayer.create(ctx, R.raw.teacher_voice_reference_mp3)
            player?.setOnCompletionListener { playing = false }
            player?.start()
        } else if (!vm.teacherVoiceEnabled) playing = false
        onDispose { player?.release() }
    }
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text(lesson.title, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        Card(Modifier.padding(15.dp).fillMaxWidth().height(210.dp), colors = CardDefaults.cardColors(Color(0xFF151B2B))) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🎬", fontSize = 54.sp); Text("Lesson Preview", fontSize = 20.sp); Text("Teacher: ${lesson.teacher}", color = Color.LightGray) } } }
        LazyColumn(Modifier.weight(1f)) {
            item { Column(Modifier.padding(18.dp)) {
                Text("আজকের পাঠ", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("${lesson.module} • ${lesson.mood}", color = Color.LightGray); Spacer(Modifier.height(14.dp)); Text("সহজ বাংলায় concept → example → safe practice → quiz।", fontSize = 16.sp); Spacer(Modifier.height(14.dp)); Text("🛡️ Safety: শুধু নিজের বা অনুমতিপ্রাপ্ত system-এ practice করবে।", color = Accent); Spacer(Modifier.height(18.dp))
                Button(onClick = { vm.complete(lesson.id) }) { Text(if (vm.completed.contains(lesson.id)) "✓ Completed" else "Lesson Complete +25 XP") }
                if (vm.teacherVoiceEnabled) { Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { playing = !playing }) { Text(if (playing) "⏸ Stop Teacher Voice" else "▶ Teacher Voice Reference") } }
                if (vm.subtitlesEnabled) { Spacer(Modifier.height(12.dp)); Text("💬 বাংলা সহায়ক নোট: এই পাঠে নিরাপদ, অনুমতিপ্রাপ্ত পরিবেশে cybersecurity concept অনুশীলন করা হবে।", color = Color.LightGray) }
                Spacer(Modifier.height(18.dp)); Text("📝 Quick Quiz", fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("নিরাপদভাবে cybersecurity শেখার সঠিক পদ্ধতি কোনটি?", Modifier.padding(vertical = 8.dp))
                val options = listOf("নিজের/অনুমতিপ্রাপ্ত lab-এ practice করা", "অনুমতি ছাড়া অন্যের system পরীক্ষা করা", "অন্যের password সংগ্রহ করা")
                options.forEachIndexed { index, option -> OutlinedButton(onClick = { if (!answered) selectedAnswer = index }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) { Text(if (selectedAnswer == index) "● $option" else "○ $option") } }
                Button(onClick = { if (!answered && selectedAnswer != null) { if (selectedAnswer == 0) vm.quizCorrect(lesson.id); answered = true } }, enabled = selectedAnswer != null && !answered, modifier = Modifier.padding(top = 6.dp)) { Text(if (answered) "✓ Submitted" else "Submit Answer") }
                if (answered) Text(if (selectedAnswer == 0) "সঠিক! +50 XP" else "ভুল উত্তর। আবার lesson-এর Safety অংশটি দেখুন।", color = if (selectedAnswer == 0) Accent else Color(0xFFFF8A80), modifier = Modifier.padding(top = 8.dp))
            } }
        }
    }
}

@Composable fun CtfScreen(vm: AcademyViewModel) {
    var selected by remember { mutableStateOf<Int?>(if (vm.ctfCompleted) 0 else null) }
    var submitted by remember { mutableStateOf(vm.ctfCompleted) }
    val options = listOf("নিরাপদ fictional log-এ suspicious IP খোঁজা", "বাস্তব server-এ অনুমতি ছাড়া scan চালানো", "অন্যের account-এ login চেষ্টা করা")
    LazyColumn { item { Header("🏁 CTF Arena", "শুধু isolated educational challenges") }; item { Card(Modifier.padding(15.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Card)) { Column(Modifier.padding(18.dp)) { Text("🧩 Final CTF Mission", fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("একটি fictional security log থেকে suspicious event শনাক্ত করো।", Modifier.padding(vertical = 10.dp)); options.forEachIndexed { index, option -> OutlinedButton(onClick = { if (!submitted) selected = index }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) { Text(if (selected == index) "● $option" else "○ $option") } }; Button(onClick = { if (!submitted && selected != null) { if (selected == 0) vm.completeCtf(); submitted = true } }, enabled = selected != null && !submitted, modifier = Modifier.padding(top = 8.dp)) { Text(if (submitted) "✓ Submitted" else "Submit Challenge") }; if (submitted) Text(if (selected == 0) "সঠিক! CTF complete +100 XP" else "ভুল। নিরাপদ fictional lab-এর নিয়ম মনে রাখুন।", color = if (selected == 0) Accent else Color(0xFFFF8A80), modifier = Modifier.padding(top = 8.dp)) } } }; item { Text("Challenge Tracks", Modifier.padding(20.dp), fontWeight = FontWeight.Bold, fontSize = 20.sp) }; items(listOf("🌐 Beginner Web", "🐧 Beginner Linux", "🔎 OSINT", "🔐 Encoding & Hash Concepts", "📋 Log Investigation")) { t -> Card(Modifier.padding(8.dp, 4.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Card)) { ListItem(headlineContent = { Text(t) }, supportingContent = { Text("Safe fictional challenge") }) } } }
}

@Composable fun AwardsScreen(vm: AcademyViewModel) {
    val badges = listOf("First Lesson", "10 Lessons", "Networking Explorer", "Linux Beginner", "OSINT Detective", "CTF Starter", "7-Day Streak", "Final Exam Complete")
    LazyColumn { item { Header("🏆 Achievements", "তোমার Anonto Academy progress"); Row(Modifier.fillMaxWidth().padding(15.dp)) { Stat("XP", vm.xp.toString(), "⭐"); Stat("Badges", badges.count(vm::achievementUnlocked).toString(), "🏅") } }; items(badges) { b -> val unlocked = vm.achievementUnlocked(b); Card(Modifier.padding(8.dp, 4.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Card)) { ListItem(leadingContent = { Text(if (unlocked) "🏅" else "🔒") }, headlineContent = { Text(b) }, supportingContent = { Text(if (unlocked) "Unlocked" else "Achievement track") }) } } }
}

@Composable fun SettingsScreen(vm: AcademyViewModel) {
    LazyColumn { item { Header("⚙️ Settings", "Audio, privacy ও learning preferences") }; item { SettingRow("🔊 Teacher Voice", vm.teacherVoiceEnabled, vm::setTeacherVoice) }; item { SettingRow("💬 Bangla Subtitles", vm.subtitlesEnabled, vm::setSubtitles) }; item { SettingRow("📥 Offline Mode", vm.offlineMode, vm::setOffline) }; item { SettingRow("🔔 Learning Reminders", vm.remindersEnabled, vm::setReminders) }; item { Card(Modifier.padding(8.dp, 4.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Card)) { ListItem(headlineContent = { Text("🔐 Privacy & Safety") }, supportingContent = { Text("শুধু অনুমতিপ্রাপ্ত lab/system-এ practice করুন। Progress এই ডিভাইসেই সংরক্ষিত থাকে।") }) } }; item { Text("Offline Mode চালু থাকলে bundled lessons ইন্টারনেট ছাড়াই ব্যবহার করা যায়।", color = Color.LightGray, modifier = Modifier.padding(16.dp)) } }
}

@Composable fun SettingRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) { Card(Modifier.padding(8.dp, 4.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Card)) { ListItem(headlineContent = { Text(label) }, trailingContent = { Switch(checked = checked, onCheckedChange = onChecked) }) } }

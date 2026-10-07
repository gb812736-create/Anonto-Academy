package com.anonto.academy

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

private val Bg = Color(0xFF090B12)
private val Card = Color(0xFF121724)
private val Accent = Color(0xFF57E6C5)
private val Purple = Color(0xFF9D8CFF)

 data class Lesson(val id:Int, val title:String, val module:String, val teacher:String, val mood:String)
 data class Module(val title:String, val icon:String, val lessons:List<Lesson>)

class AcademyViewModel: ViewModel() {
    var xp by mutableIntStateOf(0); private set
    var completed by mutableStateOf(setOf<Int>()); private set
    var streak by mutableIntStateOf(0); private set
    val modules = listOf(
        Module("Cybersecurity পরিচিতি","🛡️", listOf("Cybersecurity কী?","Hacker, Ethical Hacker ও Cybercriminal","White/Black/Grey Hat","Cyber Attack কীভাবে ঘটে","নিরাপদভাবে Ethical Hacking শেখা","Module Quiz").mapIndexed { i,t -> Lesson(i,t,"Cybersecurity পরিচিতি","Teacher ${i+1}","futuristic") }),
        Module("Internet & Networking","🌐", listOf("Internet কী?","IP ও MAC","Ports, Router, Server, Client","DNS","HTTP বনাম HTTPS","TCP বনাম UDP","Networking Practice","Module Quiz").mapIndexed { i,t -> Lesson(10+i,t,"Internet & Networking","Network Teacher","digital") }),
        Module("Linux Basics","🐧", listOf("Linux পরিচিতি","Terminal","Files & Directories","Permissions","Processes & Services","Basic Networking Commands","Safe Linux Practice").mapIndexed { i,t -> Lesson(20+i,t,"Linux Basics","Linux Teacher","technical") }),
        Module("Privacy & Account Security","🔐", listOf("Strong Password","Password Manager","Hashing Concept","MFA","Phishing চিনে রাখা","Browser Privacy","Device Security").mapIndexed { i,t -> Lesson(30+i,t,"Privacy & Account Security","Privacy Teacher","calm") }),
        Module("Surface / Deep / Dark Web","🕸️", listOf("Surface Web","Deep Web","Dark Web","Tor Concept","Onion Services","Legal ও Illegal Use","Privacy & Safety","Module Quiz").mapIndexed { i,t -> Lesson(40+i,t,"Surface / Deep / Dark Web","Dark Web Teacher","mystery") }),
        Module("OSINT","🔎", listOf("OSINT কী?","Public Information","Search Techniques","Domain/DNS Information","Metadata","Source Verification","Fictional Investigation","Module Quiz").mapIndexed { i,t -> Lesson(50+i,t,"OSINT","OSINT Detective","investigation") }),
        Module("Web Security","🌐", listOf("Website কীভাবে কাজ করে","Authentication বনাম Authorization","Input Validation","XSS Concept","SQL Injection Concept","CSRF Concept","Secure Development","Safe Vulnerability Lab").mapIndexed { i,t -> Lesson(60+i,t,"Web Security","Web Security Teacher","cyber") }),
        Module("Ethical Hacking Lab","🧪", listOf("Legal Lab Rules","Reconnaissance Concepts","Vulnerability Assessment","Safe Scanning Concepts","Finding Vulnerabilities","Understanding Findings","Security Report").mapIndexed { i,t -> Lesson(70+i,t,"Ethical Hacking Lab","Lab Teacher","energetic") }),
        Module("CTF Arena","🏁", listOf("CTF কী?","Flags","Beginner Web","Beginner Linux","OSINT Challenge","Encoding/Decoding","Safe Walkthrough","Final CTF").mapIndexed { i,t -> Lesson(80+i,t,"CTF Arena","CTF Teacher","cyberpunk") }),
        Module("Malware & Incident Response","🚨", listOf("Malware Basics","Virus/Worm/Trojan","Ransomware Concepts","Detection Basics","Incident Response","Containment & Recovery","Fictional Case Study","Final Quiz").mapIndexed { i,t -> Lesson(90+i,t,"Malware & Incident Response","IR Teacher","suspense") })
    )
    var ctfCompleted by mutableStateOf(false); private set
    fun complete(id:Int) { if (!completed.contains(id)) { completed = completed + id; xp += 25; streak = if (streak == 0) 1 else streak } }
    fun quizCorrect() { xp += 50 }
    fun completeCtf() { if (!ctfCompleted) { ctfCompleted = true; xp += 100 } }
    fun moduleProgress(module: Module): Float = if (module.lessons.isEmpty()) 0f else module.lessons.count { completed.contains(it.id) }.toFloat() / module.lessons.size
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AnontoAcademyApp() } }
}

@Composable fun AnontoAcademyApp(vm: AcademyViewModel = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    MaterialTheme(colorScheme = darkColorScheme(background=Bg, surface=Card, primary=Accent, secondary=Purple)) {
        Scaffold(containerColor=Bg, bottomBar={ NavigationBar(containerColor=Card) {
            listOf(Icons.Default.Home to "Home", Icons.Default.School to "Learn", Icons.Default.Flag to "CTF", Icons.Default.EmojiEvents to "Awards", Icons.Default.Settings to "Settings").forEachIndexed { i,(icon,label) ->
                NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icon,null)},label={Text(label)})
            }
        }}) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                if (selectedLesson != null) LessonScreen(selectedLesson!!, vm) { selectedLesson=null } else when(tab) {
                    0 -> HomeScreen(vm) { selectedLesson=it }
                    1 -> LearnScreen(vm) { selectedLesson=it }
                    2 -> CtfScreen(vm)
                    3 -> AwardsScreen(vm)
                    else -> SettingsScreen()
                }
            }
        }
    }
}

@Composable fun Header(title:String, subtitle:String?=null) { Column(Modifier.padding(20.dp,18.dp,20.dp,8.dp)) { Text(title, fontSize=28.sp, fontWeight=FontWeight.Bold); subtitle?.let{Text(it, color=Color.LightGray)} } }
@Composable fun Stat(label:String,value:String,icon:String){ Card(Modifier.padding(5.dp),colors=CardDefaults.cardColors(Card),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Text(icon);Text(value,fontSize=22.sp,fontWeight=FontWeight.Bold);Text(label,color=Color.LightGray,fontSize=12.sp)}}}

@Composable fun HomeScreen(vm:AcademyViewModel,onLesson:(Lesson)->Unit){
    LazyColumn {
        item {
            Header("🛡️ Anonto Academy","শিখো • Practice করো • Mission সম্পন্ন করো")
            Image(painterResource(R.drawable.anonto_academy_logo), "Anonto Academy logo", Modifier.fillMaxWidth().padding(horizontal=20.dp).height(210.dp))
        }
        item { Row(Modifier.fillMaxWidth().padding(horizontal=15.dp)){Stat("XP",vm.xp.toString(),"⭐");Stat("Lessons",vm.completed.size.toString(),"📚");Stat("Streak",if(vm.streak>0) "Day ${vm.streak}" else "—","🔥")} }
        item { Card(Modifier.padding(15.dp).fillMaxWidth(),colors=CardDefaults.cardColors(Color(0xFF17223A))){Column(Modifier.padding(18.dp)){Text("🎯 আজকের Mission",fontSize=20.sp,fontWeight=FontWeight.Bold);Text("একটি fictional security log-এ suspicious event শনাক্ত করো।",Modifier.padding(vertical=8.dp));Button(onClick={onLesson(vm.modules[5].lessons[6])}){Text("Mission শুরু")}}}}
        item { Text("Continue Learning",Modifier.padding(20.dp,8.dp),fontWeight=FontWeight.Bold,fontSize=20.sp) }
        items(vm.modules.take(3)){m -> ModuleCard(m,vm,onLesson)}
    }
}

@Composable fun LearnScreen(vm:AcademyViewModel,onLesson:(Lesson)->Unit){LazyColumn{item{Header("📚 Learning Roadmap","Beginner → Intermediate → Advanced")};items(vm.modules){ModuleCard(it,vm,onLesson)}}}

@Composable fun ModuleCard(m:Module,vm:AcademyViewModel,onLesson:(Lesson)->Unit){
    val progress=vm.moduleProgress(m)
    Card(Modifier.padding(horizontal=15.dp,vertical=6.dp).fillMaxWidth().clickable{onLesson(m.lessons.first())},colors=CardDefaults.cardColors(Card)){
        Column(Modifier.padding(16.dp)){
            Text("${m.icon}  ${m.title}",fontSize=18.sp,fontWeight=FontWeight.Bold)
            Text("${m.lessons.size} lessons • Safe Lab • Quiz",color=Color.LightGray)
            LinearProgressIndicator(progress={progress},Modifier.fillMaxWidth().padding(top=10.dp))
            Text("${(progress*100).toInt()}% complete",color=Accent,fontSize=12.sp,modifier=Modifier.padding(top=4.dp))
        }
    }
}

@Composable fun LessonScreen(lesson:Lesson,vm:AcademyViewModel,onBack:()->Unit){
    var playing by remember { mutableStateOf(false) }
    var answered by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    val ctx=androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(playing) {
        var player: MediaPlayer? = null
        if (playing) {
            player = MediaPlayer.create(ctx, R.raw.teacher_voice_reference_mp3)
            player?.setOnCompletionListener { playing = false }
            player?.start()
        }
        onDispose { player?.release() }
    }
    Column(Modifier.fillMaxSize().background(Bg)){
        Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")};Text(lesson.title,fontWeight=FontWeight.Bold,fontSize=20.sp)}
        Card(Modifier.padding(15.dp).fillMaxWidth().height(210.dp),colors=CardDefaults.cardColors(Color(0xFF151B2B))){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("🎬",fontSize=54.sp);Text("Lesson Preview",fontSize=20.sp);Text("Teacher: ${lesson.teacher}",color=Color.LightGray)}}}
        LazyColumn(Modifier.weight(1f)){item{Column(Modifier.padding(18.dp)){
            Text("আজকের পাঠ",fontSize=24.sp,fontWeight=FontWeight.Bold)
            Text("${lesson.module} • ${lesson.mood}",color=Color.LightGray)
            Spacer(Modifier.height(14.dp))
            Text("সহজ বাংলায় concept → example → safe practice → quiz।",fontSize=16.sp)
            Spacer(Modifier.height(14.dp))
            Text("🛡️ Safety: শুধু নিজের বা অনুমতিপ্রাপ্ত system-এ practice করবে।",color=Accent)
            Spacer(Modifier.height(18.dp))
            Button(onClick={vm.complete(lesson.id)}){Text(if(vm.completed.contains(lesson.id))"✓ Completed":"Lesson Complete +25 XP")}
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick={playing=!playing}){Text(if(playing)"⏸ Stop Teacher Voice":"▶ Teacher Voice Reference")}
            Spacer(Modifier.height(18.dp))
            Text("📝 Quick Quiz",fontWeight=FontWeight.Bold,fontSize=20.sp)
            Text("নিরাপদভাবে cybersecurity শেখার সঠিক পদ্ধতি কোনটি?",Modifier.padding(vertical=8.dp))
            val options=listOf("নিজের/অনুমতিপ্রাপ্ত lab-এ practice করা","অনুমতি ছাড়া অন্যের system পরীক্ষা করা","অন্যের password সংগ্রহ করা")
            options.forEachIndexed { index, option ->
                OutlinedButton(onClick={if(!answered){selectedAnswer=index}},modifier=Modifier.fillMaxWidth().padding(vertical=3.dp)){
                    Text(if(selectedAnswer==index) "● $option" else "○ $option")
                }
            }
            Button(onClick={if(!answered && selectedAnswer!=null){ if(selectedAnswer==0) vm.quizCorrect(); answered=true }}, enabled=selectedAnswer!=null && !answered, modifier=Modifier.padding(top=6.dp)){
                Text(if(answered)"✓ Submitted":"Submit Answer")
            }
            if(answered){ Text(if(selectedAnswer==0) "সঠিক! +50 XP" else "ভুল উত্তর। আবার lesson-এর Safety অংশটি দেখুন।", color=if(selectedAnswer==0) Accent else Color(0xFFFF8A80), modifier=Modifier.padding(top=8.dp)) }
        }}}
    }
}

@Composable fun CtfScreen(vm:AcademyViewModel){
    var selected by remember { mutableStateOf<Int?>(null) }
    var submitted by remember { mutableStateOf(false) }
    val options=listOf("নিরাপদ fictional log-এ suspicious IP খোঁজা","বাস্তব server-এ অনুমতি ছাড়া scan চালানো","অন্যের account-এ login চেষ্টা করা")
    LazyColumn{item{Header("🏁 CTF Arena","শুধু isolated educational challenges")};item{Card(Modifier.padding(15.dp).fillMaxWidth(),colors=CardDefaults.cardColors(Card)){Column(Modifier.padding(18.dp)){Text("🧩 Final CTF Mission",fontSize=22.sp,fontWeight=FontWeight.Bold);Text("একটি fictional security log থেকে suspicious event শনাক্ত করো।",Modifier.padding(vertical=10.dp));options.forEachIndexed{index,option->OutlinedButton(onClick={if(!submitted)selected=index},modifier=Modifier.fillMaxWidth().padding(vertical=3.dp)){Text(if(selected==index)"● $option" else "○ $option")}};Button(onClick={if(!submitted && selected!=null){if(selected==0)vm.completeCtf();submitted=true}},enabled=selected!=null&&!submitted,modifier=Modifier.padding(top=8.dp)){Text(if(submitted)"✓ Submitted":"Submit Challenge")};if(submitted)Text(if(selected==0)"সঠিক! CTF complete +100 XP":"ভুল। নিরাপদ fictional lab-এর নিয়ম মনে রাখুন।",color=if(selected==0)Accent else Color(0xFFFF8A80),modifier=Modifier.padding(top=8.dp))}}};item{Text("Challenge Tracks",Modifier.padding(20.dp),fontWeight=FontWeight.Bold,fontSize=20.sp)};items(listOf("🌐 Beginner Web","🐧 Beginner Linux","🔎 OSINT","🔐 Encoding & Hash Concepts","📋 Log Investigation")){t->Card(Modifier.padding(8.dp,4.dp,8.dp,4.dp).fillMaxWidth(),colors=CardDefaults.cardColors(Card)){ListItem(headlineContent={Text(t)},supportingContent={Text("Safe fictional challenge")})}}}}

@Composable fun AwardsScreen(vm:AcademyViewModel){LazyColumn{item{Header("🏆 Achievements","তোমার Anonto Academy progress")};item{Row(Modifier.fillMaxWidth().padding(15.dp)){Stat("XP",vm.xp.toString(),"⭐");Stat("Badges","${minOf(vm.completed.size/5,10)}","🏅")}};items(listOf("First Lesson","10 Lessons","Networking Explorer","Linux Beginner","OSINT Detective","CTF Starter","7-Day Streak","Final Exam Complete")){b->Card(Modifier.padding(8.dp,4.dp).fillMaxWidth(),colors=CardDefaults.cardColors(Card)){ListItem(leadingContent={Text("🏅")},headlineContent={Text(b)},supportingContent={Text("Achievement track")})}}}}
@Composable fun SettingsScreen(){
    var teacherVoice by rememberSaveable { mutableStateOf(true) }
    var subtitles by rememberSaveable { mutableStateOf(true) }
    var offline by rememberSaveable { mutableStateOf(false) }
    var notifications by rememberSaveable { mutableStateOf(true) }
    LazyColumn{item{Header("⚙️ Settings","Audio, privacy ও learning preferences")}
        item{SettingRow("🔊 Teacher Voice",teacherVoice){teacherVoice=it}}
        item{SettingRow("💬 Bangla Subtitles",subtitles){subtitles=it}}
        item{SettingRow("📥 Offline Lessons",offline){offline=it}}
        item{SettingRow("🔔 Smart Notifications",notifications){notifications=it}}
        item{Card(Modifier.padding(8.dp,4.dp).fillMaxWidth(),colors=CardDefaults.cardColors(Card)){ListItem(headlineContent={Text("🔐 Privacy & Safety")},supportingContent={Text("শুধু অনুমতিপ্রাপ্ত lab/system-এ practice করুন।")})}}
    }
}

@Composable fun SettingRow(label:String,checked:Boolean,onChecked:(Boolean)->Unit){
    Card(Modifier.padding(8.dp,4.dp).fillMaxWidth(),colors=CardDefaults.cardColors(Card)){ListItem(headlineContent={Text(label)},trailingContent={Switch(checked=checked,onCheckedChange=onChecked)})}
}

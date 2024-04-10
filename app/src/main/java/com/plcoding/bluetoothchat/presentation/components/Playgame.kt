package com.plcoding.bluetoothchat.presentation.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.plcoding.bluetoothchat.Joysticks.RockerView
import com.plcoding.bluetoothchat.R
import com.plcoding.bluetoothchat.presentation.BluetoothUiState
import androidx.compose.ui.semantics.Role
import com.plcoding.bluetoothchat.Main.MainUserActivity
import com.plcoding.bluetoothchat.MostlyActivity
import com.plcoding.bluetoothchat.di.UserManager
import com.plcoding.bluetoothchat.presentation.BluetoothViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

@Composable
fun CountdownTimer( dialogState: MutableState<Boolean>) {
    var timerValue by remember { mutableStateOf(5) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            for (i in 5 downTo 0) {
                timerValue = i
                delay(1000)
            }
            dialogState.value=false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CountdownCircle(timerValue)
    }
}

@Composable
fun CountdownCircle(timerValue: Int) {
    val stroke = Stroke(8f)
    val sweepAngle by animateFloatAsState(
        targetValue = 360f * (5 - timerValue) / 5,
        animationSpec = tween(durationMillis = 1000)
    )

    Canvas(
        modifier = Modifier.size(200.dp),
        onDraw = {
            // 绘制灰色圆圈
            drawArc(
                color = Color(0x83C8C9C9),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            // 绘制红色倒计时圆圈
//            drawArc(
//                color = Color.Red,
//                startAngle = -90f,
//                sweepAngle = sweepAngle,
//                useCenter = false,
//                style = stroke,
//                topLeft = Offset(0f, 0f),
//                size = size
//            )

            // 绘制倒计时数字
            drawIntoCanvas {
                val paint = Paint().asFrameworkPaint()
                paint.textSize = 100f // 设置字体大小
                paint.color = Color.White.toArgb() // 设置字体颜色
                val text = timerValue.toString()
                val textBounds = android.graphics.Rect()
                paint.getTextBounds(text, 0, text.length, textBounds)
                val x = (size.width - textBounds.width()) / 2f
                val y = (size.height + textBounds.height()) / 2f
                it.nativeCanvas.drawText(text, x, y, paint)
            }
        }
    )
}


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Playgame(
    state: BluetoothUiState,
    viewModel: BluetoothViewModel,
    onDisconnect: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    context: Context // 添加Context参数

) {
    /**
     * 积分
     */
    var intergral by remember {
        mutableStateOf<Float?>(0.0F)
    }
    /**
     * 是不是我出界了
     */
    var isoutsid by remember {
        mutableStateOf<String?>(null)
    }
    var sendmessageDialog by remember {
        mutableStateOf<Boolean?>(true)
    }
    /**
     * 跳转
     */
    val context = LocalContext.current
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 处理ActivityResult
            // 在这里处理Activity返回的数据或其他逻辑
        }
    }
    val scope = rememberCoroutineScope()
    /**
     * 摇杆感应x,y位置
     */
    var x: String = ""
    var y: String = ""
    val message = rememberSaveable {
        mutableStateOf("")
    }

    /**
     * 向小车发送数据
     * 开火，发动技能，向前，向后，左转，右转，比赛开始，比赛结束
     */
    val ATS: String = "ATS"
    var fire by remember { mutableStateOf("0") }//开火
    var fire_b: Boolean = false
    var who: String = "0"//哪方发动技能
    var skill: String = "0"//发动技能
    var direction: String = "0"//方向
    var isgame: String = "1"//比赛进程
    val item: Int = state.messages.size

    /**
     * 拿到收到的数据
     */
    var message1: String = state.messages.lastOrNull()?.message ?: ""
    val senderName: String = state.pairedDevices.lastOrNull()?.name?:""
    val issend= remember { mutableStateOf(true) }
    var message2: List<Int>? = null
    if (message1.isNotEmpty()&&issend.value) {
        message2 = parseAsciiString(message1)
        // 继续处理 message2
    Log.d("sendx",message1)
    Log.d("sends",message2.toString())
    } else {
        // 处理空字符串的情况
    }
    /**
     * 对小车接收数据
     * 防御模块血量，攻击模块血量，行走模块血量，核心模块是否被摧毁，防御模块装甲值，武器模块伤害，射速，行走模块速度
     */
    var prevent_blood by remember {
        mutableStateOf<Float?>(100.0F)
    }
    var arms_blood by remember {
        mutableStateOf<Float?>(100.0F)
    }
    var walk_blood by remember {
        mutableStateOf<Float?>(100.0F)
    }
    var isDie by remember {
        mutableStateOf<String?>("0")
    }
    var meHurt: String = "0"
    var firingRate: String = "0"
    var workRate1: String = "0"
    var workRate2: String = "0"
    /**
     * 按字节赋值
     */
    if (message2 != null&& message2.size >= 12) {
        prevent_blood = message2[3].toFloat()//防御模块血量
        arms_blood = message2[4].toFloat()//武器模块血量
        walk_blood = message2[5].toFloat()//行走模块血量
        isDie = message2[6].toFloat().toString()//核心模块是否被摧毁
        meHurt = message2[7].toString()//我方武器模块伤害
        firingRate = message2[8].toFloat().toString()//射速
        workRate1 = message2[9].toFloat().toString()//行走模块速度
        workRate2 = message2[10].toFloat().toString()//行走模块速度
    } else {
        // 处理长度不足的情况
    }
    var you_hurt: String = meHurt//敌方武器模块伤害

    /**
     * 选择的技能
     */
    var is_skill by remember {
        mutableStateOf<String?>("0")
    }
    val openAlertDialog = remember { mutableStateOf(true) }//选择技能的弹出框状态
    var prepareDialog =remember { mutableStateOf(false) }//准备技能的弹出框
    var outsideDialog = remember { mutableStateOf(false) }//选择用户出界的弹出框状态
    var daojishiDialog= remember { mutableStateOf(false) }
    var iifenDiaLog= remember { mutableStateOf(true) }
    //表情选择的：
    var selectedEmoticon by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isemojiselcet by remember { mutableStateOf(false) }
    is_skill = AlertDialogSample(
        dialogState = openAlertDialog,
        prepareState =prepareDialog,
        workRote = workRate2,
        fireRote = firingRate,
        preventBlood = prevent_blood.toString(),
        armsBlood = arms_blood.toString(),
        walkBlood = walk_blood.toString(),
        myhurt = meHurt
    )
    val endAlertDialog = remember {
        mutableStateOf(false)
    }
    var endType: String = "0"

    /**
     * 一个小测试
     */
    val inputString = "1234567"
    val byteArray = inputString.toByteArray()

    /**
     * 关于血量过低的ui
     */
    val glowingAlpha = remember { Animatable(1f) }
    LaunchedEffect(prevent_blood) {
        if (prevent_blood!! <= 10) {
            glowingAlpha.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            glowingAlpha.snapTo(1f)
        }
    }
    /**
     * wifi局域网TCP事宜定义
     */
    var mClientSocket by remember {
        mutableStateOf<Socket?>(null)
    }
    var mInputStream by remember {
        mutableStateOf<InputStream?>(null)
    }
    var mOutputStream by remember {
        mutableStateOf<OutputStream?>(null)
    }
    var mIsConnected by remember {
        mutableStateOf<Boolean?>(false)
    }

    val mHandler = Handler(Looper.getMainLooper())
    var messageType by remember {
        mutableStateOf<String?>(null)
    }
    var payload by remember {
        mutableStateOf<JSONObject?>(JSONObject())
    }
    var skill_TCP by remember {
        mutableStateOf<String?>(null)
    }

    /**
     * 一个吐司
     */
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 线程发送信息
     */
    fun showToastOnUiThread(messageId: Int) {
        mHandler.post {
            Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 对方释放技能
     */
    fun handleParsedJsonData(messageType: String, skill: String) {
        // 根据解析后的数据执行相应操作，例如根据消息类型处理技能信息
        if ("0" == messageType) {
            showToast("错误反馈")
        }
        if ("1" == messageType) {
            showToast("游戏开始")
        }
        if ("2" == messageType) {
            endType = messageType
            showToast("时间到！游戏结束\n游戏即将进入结算界面......")
        }
        if ("3" == messageType) {
            endType = messageType
            showToast("机器人出界，游戏结束！")
        }
        if ("4" == messageType) {
            if ("1" == skill) {
                showToast("Car2执行索敌操作")
            } else if ("2" == skill) {
                showToast("Car2执行蓄能操作")
            } else if ("3" == skill) {
                showToast("您已被缴械，无法攻击！")
            } else if ("4" == skill) {
                showToast("Car2执行庇护操作")
            } else if ("5" == skill) {
                showToast("Car2,执行破阵操作")
            }

        }
    }

    /**
     * 接收消息
     */
    fun startMessageReceiverThread() {
        Thread {
            val buffer = ByteArray(1024)
            var length: Int
            while (mIsConnected == true) {
                try {
                    length = mInputStream?.read(buffer) ?: -1
                    if (length > 0) {
                        val receivedMessage = String(buffer, 0, length, StandardCharsets.UTF_8)

                        // 解析接收到的 JSON 数据
                        try {
                            val jsonObject = JSONObject(receivedMessage)
                            messageType = jsonObject.getString("messageType")
                            payload = jsonObject.getJSONObject("payload")
                             skill = payload!!.getString("skill")

                            Log.d("Received JSON", "messageType: $messageType, skill: $skill")

                            // 在 UI 线程更新 UI
                            mHandler.post {
                                // 处理解析后的数据，例如更新 UI 控件
                                handleParsedJsonData(messageType.toString(), skill)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    showToastOnUiThread(R.string.toast_connection_lost)
                    mIsConnected = false
                }
            }
        }.start()
    }

    /**
     * 连接ESP32
     */
    fun connectToEsp32() {
        Thread {
            try {
                mClientSocket = Socket()
                mClientSocket?.connect(InetSocketAddress("192.168.4.1", 6666), 5000)
                mInputStream = mClientSocket?.getInputStream()
                mOutputStream = mClientSocket?.getOutputStream()

                // 启动接收消息的线程
                startMessageReceiverThread()

                mIsConnected = true
                showToastOnUiThread(R.string.toast_connected_to_esp32)
            } catch (e: IOException) {
                e.printStackTrace()
                showToastOnUiThread(R.string.toast_connection_failed)
            }
        }.start()
    }

    /**
     * 断开连接
     */
    fun disconnectFromEsp32() {
        try {
            if (mIsConnected == true) {
                mClientSocket?.close()
                mIsConnected = false
                showToastOnUiThread(R.string.toast_disconnected_from_esp32)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 发送消息
     */

    suspend fun sendPredefinedMessage(messagetype: String, skillType: String,intergral1:Float) =
        withContext(Dispatchers.IO) {
            /**
             * 0,错误反馈
             * 1，玩家准备信号
             * 2.玩家释放技能
             * 3.游戏结算积分上报
             */
            // 预定义的字段值
            val carName = senderName
            val messageType = messagetype
            val skill = skillType
            val username = UserManager.loggedInUsername

            // 创建JSON对象
            val json = JSONObject()
            try {
                json.put("carName", carName)
                json.put("messageType", messageType)
                val payload = JSONObject()
                if(messageType=="2"){
                    payload.put("skill", skill)
                    json.put("payload", payload)
                }
                if(messageType=="3"){
                    payload.put("integaral",intergral1)//发送积分
                    json.put("payload", payload)
                }
                json.put("username", username)

                // 将JSON对象转换为字符串
                val jsonString = json.toString()

                // 发送JSON格式的数据到ESP32
                val buffer = jsonString.toByteArray(StandardCharsets.UTF_8)
                mOutputStream?.write(buffer)


            } catch (e: JSONException) {
                e.printStackTrace()
                showToastOnUiThread(R.string.toast_failed_to_send_message)
            } catch (e: IOException) {
                e.printStackTrace()
                showToastOnUiThread(R.string.toast_failed_to_send_message)
            }
        }

    LaunchedEffect(Unit) {
        connectToEsp32()
    }
//    DisposableEffect(Unit) {
//        onDispose {
//            disconnectFromEsp32()
//        }
//    }
    /**
     * 一个选择技能的框
     */


    Image(
        painter = painterResource(id =  map_iv(UserManager.getSelectedMaps())),
        contentDescription = null, // 可以设置图片描述
        modifier = Modifier.fillMaxSize()
    )
    Box {
        Image(
            painter = painterResource(id = map_iv(UserManager.getSelectedMaps())),
            contentDescription = null, // 可以设置图片描述
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds

        )
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column {
                Image(
                    painter = painterResource(id = robot(arms_blood, walk_blood!!)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp, 135.dp)
                        .offset(y = 30.dp, x = 55.dp),
                    contentScale = ContentScale.FillBounds,
                    alignment = Alignment.CenterStart
                )
                AndroidView(
                    factory =
                    { context ->
                        RockerView(context).apply {
                            getDate { rocker, pX, pY ->
                                x = pX.toString()
                                y = pY.toString()
                                //onSendMessage(ATS,"  "+pX+"  "+pY.toString())
                                if (y == "-12.484848") {
                                    //向前移动
                                    direction = "1"
                                    delayMilliseconds(30)
                                    onSendMessage(
                                        ATS,
                                        fire + who + skill + direction + isgame +you_hurt
                                    )
                                } else if (y == "12.484848") {
                                    //向后移动
                                    direction = "2"
                                    delayMilliseconds(30)
                                    onSendMessage(
                                        ATS,
                                        fire + who + skill + direction + isgame + you_hurt
                                    )
                                } else if (x == "12.484848") {
                                    //向右运动
                                    direction = "4"
                                    delayMilliseconds(30)
                                    Log.d("move", fire)
                                    onSendMessage(
                                        ATS,
                                        fire + who + skill + direction + isgame + you_hurt
                                    )
                                } else if (x == "-12.484848") {
                                    //向左运动
                                    direction = "3"
                                    delayMilliseconds(30)
                                    onSendMessage(
                                        ATS,
                                        fire + who + skill + direction + isgame + you_hurt
                                    )
                                } else {
                                    direction = "0"
                                    onSendMessage(
                                        ATS,
                                        fire + who + skill + direction + isgame + you_hurt
                                    )
                                }

                            }
                        }
                    },
                    modifier = Modifier
                        .offset(y = 15.dp),
                )

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.padding(start = 80.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp)
                    ) {
                        Text(
                            text = "武器模块" + arms_blood + "%",
                            style = TextStyle(fontSize = 11.sp),
                            color = Color(0x6AEDEDED),
                            modifier = Modifier
                                .offset(y = 20.dp, x = 50.dp)
                        )

                        LinearProgressIndicator(
                            progress = arms_blood !!/ 100,
                            color = Color(0xCDEB6363),// 进度值，范围为0.0到1.0
                            modifier = Modifier
                                .size(100.dp, 12.dp)
                                .offset(y = 20.dp, x = 50.dp) // 填充父布局的宽度
                        )

                    }
                    Column(
                        modifier = Modifier.padding(start = 20.dp)
                    ) {
                        Text(
                            text = "行走模块" + walk_blood + "%",
                            style = TextStyle(fontSize = 11.sp),
                            color = Color(0x6AEDEDED),
                            modifier = Modifier
                                .offset(y = 20.dp, x = 50.dp)
                        )
                        LinearProgressIndicator(
                            progress = walk_blood !!/ 100,
                            color = Color(0xCDFF3A3A),// 进度值，范围为0.0到1.0
                            modifier = Modifier
                                .size(100.dp, 12.dp)
                                .offset(y = 20.dp, x = 50.dp) // 填充父布局的宽度
                        )
                    }
                    Column(
                        modifier = Modifier.padding(start = 20.dp)
                    ) {
                        Text(
                            text = "血量" + prevent_blood + "%",
                            style = TextStyle(fontSize = 11.sp),
                            color = Color(0x6AEDEDED),
                            modifier = Modifier
                                .offset(y = 20.dp, x = 50.dp)
                        )
                        LinearProgressIndicator(
                            progress = prevent_blood !!/ 100,
                            color = Color(0xCDB10000),// 进度值，范围为0.0到1.0
                            modifier = Modifier
                                .size(100.dp, 12.dp)
                                .offset(y = 20.dp, x = 50.dp) // 填充父布局的宽度
                        )
                        Text(text = skill_text(is_skill!!),
                            style = TextStyle(fontSize = 10.sp),
                            color = Color(0x6AEDEDED),
                            modifier = Modifier
                                .offset(y = 50.dp, x = 80.dp)

                        )
                        Image(
                            painter = painterResource(id = skill_image(is_skill!!)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(70.dp, 75.dp)
                                .offset(y = 50.dp, x = 70.dp)
                                .clickable {
                                    scope.launch {
                                        sendPredefinedMessage("2", is_skill!!, intergral!!)
                                    }
                                    onSendMessage(
                                        ATS,
                                        fire + who + skill + direction + isgame + you_hurt
                                    )
                                },
                            contentScale = ContentScale.FillBounds,
                            alignment = Alignment.CenterStart,

                            )

                        Card(
                            shape = RoundedCornerShape(700.dp),
                            backgroundColor = Color(0x6AEDEDED),
                            elevation = 4.dp,
                            modifier = Modifier
                                .size(100.dp, 100.dp)
                                .offset(y = 90.dp, x = 58.dp)
                                .clickable(
                                    enabled = true,
                                    onClickLabel = "开火键",
                                    onClick = {
                                        fire_b = true
                                        Log.d("TAG", "Playgame: ${fire}")
                                        onSendMessage(
                                            ATS,
                                            "1" + who + skill + direction + isgame + you_hurt
                                        )
                                    },
                                    role = Role.Button,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.zidan),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                    }

                }
            }
        }
        // 显示 emoji 表情选择框
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    isDropdownExpanded = !isDropdownExpanded
                }
        ) {
            Image(
                painter = painterResource(id =R.drawable.emoji2),
                contentDescription = null, // 可以设置图片描述
                modifier = Modifier.size(26.dp)
            )
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                // 表情选择框的内容，可以是列表或网格视图
                EmoticonPicker { emoticon ->
                    selectedEmoticon = emoticon
                    isDropdownExpanded = false
                    isemojiselcet=true
                    // 在页面中央显示选定的表情，然后两秒后消失

                }
            }
        }
        LaunchedEffect(selectedEmoticon) {
            delay(3000)
            selectedEmoticon = null
        }
        // 显示选定的表情
        if(isemojiselcet){
            selectedEmoticon?.let { emoticon ->
                Column (
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally){
                    Text(
                        text = emoticon,
                        fontSize = 60.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(16.dp)
                    )
                }

            }

        }


        drawGlowingBorder(glowingAlpha.value, Color.Red, 9f, prevent_blood !!<= 10)// 绘制发光边框
        if (prevent_blood!! <= 10) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x435C0404)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "血量不足",
                    color = Color(0xE9F50000),
                    modifier = Modifier.alpha(glowingAlpha.value)
                )
            }

        }


    }
    /**
     * 用户准备
     */
    if (prepareDialog.value) {
        AlertDialog(
            onDismissRequest = { prepareDialog.value = false },
            title = { Text(text = "请准备") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                }
            },
            buttons = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { prepareDialog.value = false
                            scope.launch {
                                sendPredefinedMessage("1", "0",0.0f)
                            }
                            daojishiDialog.value= true
                            //准备就发送游戏正在进行
                            onSendMessage(
                                ATS,
                                fire + who + skill + direction + isgame + you_hurt
                            )

                        }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF008DCC), // 按钮的背景颜色
                            contentColor = Color.White)
                    ) {
                        Text("准备")
                    }
                }
            }

        )


}
    /**
     *倒计时组件显示
     */
    if(daojishiDialog.value){
        CountdownTimer(daojishiDialog)
    }
    if(prevent_blood==0.0F){
        scope.launch {
            sendPredefinedMessage("4", "0",intergral!!)
        }

    }

    /**
     * 计算用户积分
     */
    //如果事件耗尽但是双方都存活
    if(iifenDiaLog.value){
        if(messageType=="2"){
            if(isDie=="0"){
                intergral = intergral!! + 20
            }
            if(walk_blood!!>0.0F){
                intergral = intergral!! + 5
            }
            if(arms_blood!!>0.0F){
                intergral = intergral!! + 5
            }
            intergral = intergral!! + (prevent_blood!!)*0.7F
            iifenDiaLog.value=false
        }else if(messageType=="6"&&isDie=="0"){
            //若有一方核心模块被击毁则另我赢方
            intergral = intergral!! + 100
            if(walk_blood!!>0.0F){
                intergral = intergral!! + 5
            }
            if(arms_blood!!>0.0F){
                intergral = intergral!! + 5
            }
            intergral = intergral!! + (prevent_blood!!/100.0F)*0.7F
            iifenDiaLog.value=false
        }else if(isDie=="1"){
            //我方核心模块击毁输了
            intergral=0.0F
            iifenDiaLog.value=false
        }else if(messageType=="3"&&messageType=="6"){
            //机器人出界导致游戏结束
            if(isoutsid=="1"){
                //我方出界
                intergral=0.0F

            }else if (isoutsid=="0"){
                //对方出界
                if(walk_blood!!>0.0F){
                    intergral = intergral!! + 5
                }
                if(arms_blood!!>0.0F){
                    intergral = intergral!! + 5
                }
                intergral = intergral!! + (prevent_blood!!/100.0F)*0.7F
                intergral = intergral!! + 100
            }
            iifenDiaLog.value=false
        }
    }

    /**
     * 判断游戏结束原因
     */
    if (messageType== "2" ) {
        endAlertDialog.value =true
                AlertDialog(
                    onDismissRequest = { endAlertDialog.value = false },
                    title = {
                        Text(text = "游戏结束")
                    },
                    text = {
                            Text(text = "时间到\n"+intergral+"\n对战时常为：02:00\n由于不当操作对局无效")
                    },
                    buttons = {
                        TextButton(onClick = { endAlertDialog.value = false
                            if(sendmessageDialog == true){
                                scope.launch {
                                    sendPredefinedMessage("3", "0",intergral!!)
                                }
                                sendmessageDialog=false
                            }
                            onSendMessage(
                                ATS,
                                fire + who + skill + direction + "0" + you_hurt
                            )
                            val intent= Intent(context,MainUserActivity::class.java)
                            activityLauncher.launch(intent)}) {
                            Text(text = "退出")
                        }
                    }


                )

    }
    if(messageType=="6"&&isDie=="1"){
        endAlertDialog.value =true
        AlertDialog(
            onDismissRequest = { endAlertDialog.value = false },
            title = {
                Text(text = "游戏结束")
            },
            text = {
                        Text(text = "对方被击毁，游戏结束\n"+intergral+"\n对战时常为：01:02\n由于不当操作对局无效")
            },
            buttons = {
                TextButton(onClick = { endAlertDialog.value = false
                    if(sendmessageDialog == true){
                        scope.launch {
                            sendPredefinedMessage("3", "0",intergral!!)
                        }
                        sendmessageDialog=false
                    }
                    onSendMessage(
                        ATS,
                        fire + who + skill + direction + "0" + you_hurt
                    )
                    val intent= Intent(context,MainUserActivity::class.java)
                    activityLauncher.launch(intent)}) {
                    Text(text = "退出")
                }
            }


        )

    }
    if(messageType=="6"){
        endAlertDialog.value =true
        AlertDialog(
            onDismissRequest = { endAlertDialog.value = false },
            title = {
                Text(text = "游戏结束")
            },
            text = {
                    Text(text = "对方被击毁，游戏结束\n0.0"+"\n对战时常为：01:02\n由于不当操作对局无效")
            },
            buttons = {
                TextButton(onClick = { endAlertDialog.value = false
                    if(sendmessageDialog == true){
                        scope.launch {
                            sendPredefinedMessage("3", "0",intergral!!)
                        }
                        sendmessageDialog=false
                    }
                    onSendMessage(
                        ATS,
                        fire + who + skill + direction + "0" + you_hurt
                    )
                    val intent= Intent(context,MainUserActivity::class.java)
                    activityLauncher.launch(intent)}) {
                    Text(text = "退出")
                }
            }


        )

    }
    if ( messageType == "3" ) {
        endAlertDialog.value=true
            AlertDialog(
                onDismissRequest = { endAlertDialog.value = false },
                title = {
                    Text(text = "游戏结束\n"+intergral)
                        },
                text = {
                    if (messageType == "3") {
                        Text(text = "机器人出界\n"+intergral+"\n对战时常为：00:59\n由于不当操作对局无效")
                    }
                },
                buttons = {
                    TextButton(onClick = { endAlertDialog.value = false
                        outsideDialog.value=true
                        onSendMessage(
                            ATS,
                            fire + who + skill + direction + "0" + you_hurt
                        )

                 }) {
                        Text(text = "退出")
                    }
                }


            )


    }
    if(outsideDialog.value){
            AlertDialog(
                onDismissRequest = { outsideDialog.value= false },
                title = {
                    Text(text = "有一方出界，游戏结束")
                },
                text = {
                    Text(text = "你出界了吗？？？")
                },
                confirmButton = {
                    Button(onClick = { outsideDialog.value= false
                        isoutsid="1"
                        if(sendmessageDialog == true){
                            scope.launch {
                                sendPredefinedMessage("3", "0",intergral!!)
                            }
                            sendmessageDialog=false
                        }
                    val intent=Intent(context,MainUserActivity::class.java)
                    activityLauncher.launch(intent)

                    },) {
                        Text(text = "是我")
                    }
                },
                dismissButton = {
                    Button(onClick = { outsideDialog.value= false
                        isoutsid="0"
                        if(sendmessageDialog == true){
                            scope.launch {
                                sendPredefinedMessage("3", "0",intergral!!)
                            }
                            sendmessageDialog=false
                        }
                        val intent=Intent(context,MainUserActivity::class.java)
                        activityLauncher.launch(intent)
                    },
                        ){
                        Text(text = "不是我")
                    }
                }
            )


    }

}

/**
 * 任意毫秒延时函数
 */
fun delayMilliseconds(milliseconds: Long) {
    val start = System.currentTimeMillis()
    var elapsed: Long
    do {
        elapsed = System.currentTimeMillis() - start
    } while (elapsed < milliseconds)
}

fun parseAsciiString(ascii: String): List<Int> {

    return try {
        // ascii.split(" ").map { it.toInt() }
        val asciiValues = ascii.split(" ").map {
            if (it.length == 2 && it.all { c -> c in '0'..'9' || c in 'A'..'F' || c in 'a'..'f' }) Integer.parseInt(
                it,
                16
            ) else throw NumberFormatException()
        }
        val asciiChars = asciiValues.map { it.toChar().toInt() }
        asciiChars
    } catch (e: NumberFormatException) {
        emptyList()

    }

}

/**
 * 更改robot ui 图片
 */
fun robot(arm: Float?, walk: Float): Int {
    var armResource = if (arm!! > 50.0 && walk > 50.0) {
        R.drawable.robot
    } else if (arm <= 50.0F && arm != 0.0F && walk > 50.0) {
        R.drawable.robot_arm1
    } else if (arm <= 50.0F && arm != 0.0F && walk <= 50.0 && walk != 0.0F) {

        R.drawable.robot_w_a
    } else if (arm == 0.0F && walk > 50.0) {
        R.drawable.robot_arm2
    } else if (arm == 0.0F && walk == 0.0F) {
        R.drawable.robot_all
    } else if (arm > 50.0 && walk <= 50.0 && walk != 0.0F) {
        R.drawable.robot_walk1
    } else if (arm > 50.0 && walk == 0.0F) {
        R.drawable.robot_walk2
    } else {
        R.drawable.robot
    }
    // 根据arm和walk的值选择不同的图片资源
    return armResource
}

/**
 * 更新技能ui 图片
 */
fun skill_image(skill: String): Int {
    var skillResource = if (skill == "1") {
        R.drawable.suodi
    } else if (skill == "2") {
        R.drawable.xuneng
    } else if (skill == "3") {

        R.drawable.jiaoxie
    } else if (skill == "4") {
        R.drawable.bihu
    } else if (skill == "5") {
        R.drawable.pozhen
    } else {
        R.drawable.pozhen
    }
    return skillResource
}

/**
 * 接收返回地图的信息
 */
fun map_iv(map:String):Int{
    var map_ivResource = if (map == "1") {
        R.drawable.map1
    } else if (map== "2") {
        R.drawable.map2
    } else if (map == "3") {
        R.drawable.map3
    } else if (map == "4") {
        R.drawable.map4
    }  else {
        R.drawable.bg1
    }
    return map_ivResource

}
/**
 * 更新技能文字
 */
fun skill_text(skill: String): String {
    var skillText = if (skill == "1") {
        "索敌"
    } else if (skill == "2") {
        "蓄能"
    } else if (skill == "3") {
        "缴械"
    } else if (skill == "4") {
        "庇护"
    } else if (skill == "5") {
        "破阵"
    } else {
        "技能"
    }
    return skillText
}


@Composable
fun drawGlowingBorder(alpha: Float, color: Color, width: Float, isBright: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (isBright) {
            drawRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height),
                style = Stroke(width)
            )
        } else {
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height),
                style = Stroke(width)
            )
        }
    }
}

/*
 * 选择技能的弹出框
 */
@Composable
fun AlertDialogSample(
    dialogState: MutableState<Boolean>,
    prepareState:MutableState<Boolean>, workRote: String, fireRote: String, preventBlood: String,
    armsBlood: String, walkBlood: String, myhurt: String
): String {
    var selectedOption by remember { mutableStateOf("") }
    if (dialogState.value) {
        AlertDialog(
            onDismissRequest = { dialogState.value = false },
            backgroundColor = Color(0xFFFFEBEB),
            title = {
                Text(text = "请选择一个技能")
            },
            text = {
                Column {
                    Row(modifier = Modifier.padding(4.dp, 2.dp)) {
                        Text(text = "射速:" + fireRote)
                        Spacer(modifier = Modifier.width(5.dp)) // 添加一个空间来增加按钮之间的间距
                        Text(text = "伤害:" + myhurt)
                        Spacer(modifier = Modifier.width(5.dp)) // 添加一个空间来增加按钮之间的间距
                        Text(text = "装甲值:" + preventBlood)
                    }
                    Spacer(modifier = Modifier.width(7.dp)) // 添加一个空间来增加按钮之间的间距
                    Row(modifier = Modifier.padding(4.dp, 2.dp)) {
                        Text(text = "满载移速:" + workRote)
                        Spacer(modifier = Modifier.width(5.dp)) // 添加一个空间来增加按钮之间的间距
                        Text(text = "武器血量:" + armsBlood)
                        Spacer(modifier = Modifier.width(5.dp)) // 添加一个空间来增加按钮之间的间距
                    }
                    Spacer(modifier = Modifier.width(7.dp)) // 添加一个空间来增加按钮之间的间距
                    Text(
                        text = "行走模块血量:" + walkBlood,
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }

            },
            buttons = {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp, 8.dp)) {
                        Button(
                            onClick = {
                                selectedOption = "1"
                                dialogState.value = false
                                prepareState.value=true
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2DBEFF), // 按钮的背景颜色
                                contentColor = Color.White // 按钮中文本的颜色
                            )
                        ) {
                            Text(text = "索敌")
                        }
                        Spacer(modifier = Modifier.width(30.dp)) // 添加一个空间来增加按钮之间的间距
                        Button(
                            onClick = {
                                selectedOption = "2"
                                dialogState.value=false
                                prepareState.value=true
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF08AFFA), // 按钮的背景颜色
                                contentColor = Color.White // 按钮中文本的颜色
                            )
                        ) {
                            Text(text = "蓄能")
                        }
                        Spacer(modifier = Modifier.width(30.dp)) // 添加一个空间来增加按钮之间的间距
                        Button(
                            onClick = {
                                selectedOption = "3"
                                dialogState.value = false
                                prepareState.value=true
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF008CCC), // 按钮的背景颜色
                                contentColor = Color.White // 按钮中文本的颜色
                            )
                        ) {
                            Text(text = "缴械")
                        }
                    }
                    Row(modifier = Modifier.padding(20.dp, 8.dp)) {
                        Spacer(modifier = Modifier.width(45.dp)) // 添加一个空间来增加按钮之间的间距
                        Button(
                            onClick = {
                                selectedOption = "4"
                                dialogState.value = false
                                prepareState.value=true
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF76D30B), // 按钮的背景颜色
                                contentColor = Color.White // 按钮中文本的颜色
                            )
                        ) {
                            Text(text = "庇护")
                        }
                        Spacer(modifier = Modifier.width(30.dp)) // 添加一个空间来增加按钮之间的间距
                        Button(
                            onClick = {
                                selectedOption = "5"
                                dialogState.value = false
                                prepareState.value=true
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF00BE08), // 按钮的背景颜色
                                contentColor = Color.White // 按钮中文本的颜色
                            )
                        ) {
                            Text(text = "破阵")
                        }

                    }
                }

            }

        )

    }
    return selectedOption
}

/**
 * 表情选择
 */
@Composable
fun EmoticonPicker(onEmoticonSelected: (String) -> Unit) {
    val emoticons = listOf("😀", "😂", "😊", "😍", "🥳", "😎")

    LazyColumn(
        modifier = Modifier
            .background(Color.Transparent)
            .width(50.dp)
            .height(200.dp)
    ) {
        items(emoticons) { emoticon ->
            EmoticonItem(
                emoticon = emoticon,
                onEmoticonSelected = onEmoticonSelected
            )
        }
    }
}


@Composable
fun EmoticonItem(emoticon: String, onEmoticonSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onEmoticonSelected(emoticon) }
            .size(48.dp)
            .background(Color.LightGray)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoticon,
            fontSize = 24.sp
        )
    }
}

@Preview(
    name = "Landscape Preview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_NO,
    widthDp = 1000,
    heightDp = 600
)
@Composable
fun PreviewMyComposeLayout() {

}

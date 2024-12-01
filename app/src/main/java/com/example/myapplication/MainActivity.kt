package com.example.myapplication



import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.fmm.doudizhu.*
import org.json.JSONObject




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Greeting(name = "Android")

        // 获取当前上下文并转换为 Activity
        val context = LocalContext.current
        val activity = context as? MainActivity // 确保转换为 MainActivity
        // 添加四个按钮
        Button(onClick = {
            Log.d("Screenshot", "按钮 1 事件被执行")
            Toast.makeText(context, "按钮 1 被点击", Toast.LENGTH_SHORT).show()
            val bitmap= doudizhu_main.getImage(activity)
            val newBitmap= doudizhu_main.getIamgeFormated(bitmap)
            Log.d("BitmapInfo", "Width:"+ newBitmap.width+", Height: "+newBitmap.height)
        }) {
            Text("按钮 1")
        }
        Spacer(modifier = Modifier.height(8.dp)) // 添加间隔

        Button(onClick = {
            Log.d("Button", "最小化应用并开启悬浮窗显示")
            // TODO: 处理按钮 2 点击事件

            doudizhu_main.showFloat(context,activity)
            doudizhu_main.hideMainApp(activity)
        }) {
            Text("按钮 2")
        }
        Spacer(modifier = Modifier.height(8.dp)) // 添加间隔

        Button(onClick = {
            Log.d("Button", "设置文本")
            val jsonObject = JSONObject()
            jsonObject.put("type", "changeCard")
            val cardsObject=JSONObject()
            cardsObject.put("left","2-3-4-5-6-7-8-9-10-10-10-10-J-Q-K-A-大王-小王-2-10");
            cardsObject.put("my","2-3-4-5-6-7-8-9-10-10-10-10-J-Q-K-A-大王-小王-2-10");
            cardsObject.put("right","2-3-4-5-6-7-8-9-10-10-10-10-J-Q-K-A-大王-小王-2-10");
            jsonObject.put("data", cardsObject)
            doudizhu_main.sendServiceMessage(context,jsonObject)
        }) {
            Text("按钮 3")
        }
        Spacer(modifier = Modifier.height(8.dp)) // 添加间隔

        Button(onClick = {
            Log.d("Button", "使用模型识别屏幕数据")
           doudizhu_main.detectImage(activity,context)

        }) {
            Text("按钮 4")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}


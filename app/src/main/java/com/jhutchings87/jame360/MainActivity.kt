package com.jhutchings87.jame360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jhutchings87.jame360.ui.navigation.Jame360NavHost
import com.jhutchings87.jame360.ui.theme.Jame360Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Jame360App()
        }
    }
}

@Composable
fun Jame360App() {
    Jame360Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Jame360NavHost()
        }
    }
}

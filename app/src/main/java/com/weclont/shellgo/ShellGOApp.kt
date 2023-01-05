package com.weclont.shellgo

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.weclont.shellgo.ui.theme.ShellGOTheme

@Composable
fun ShellGOApp() {
    var text by rememberSaveable {
        mutableStateOf(
            FileUtil.getFile("config.json")
        )
    }
    ShellGOTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ShellGO") },
                )
            },
        ) {
            Column(
                modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth(1f),
                )
                Row {
                    var textLabel by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                        mutableStateOf(TextFieldValue("start service"))
                    }
                    OutlinedTextField(
                        value = textLabel,
                        onValueChange = { textLabel = it },
                        label = { Text("Exec") },
                        modifier = Modifier.fillMaxWidth(0.55f)
                    )
                    Spacer(modifier = Modifier.padding(all = 3.dp))
                    Button(onClick = {
                        val cmdText = textLabel.text
                        val cmd = cmdText.split(" ")
                        if (cmd[0] == "start" && cmd[1] == "service") {
                            val newIntent =
                                Intent(MainApplication.getServiceContext(), MainService::class.java)
                            MainApplication.getServiceContext().startService(newIntent)
                            Toast.makeText(
                                MainApplication.getServiceContext(),
                                "Start Service - Success",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (cmd[0] == "stop" && cmd[1] == "service") {
                            val newIntent =
                                Intent(MainApplication.getServiceContext(), MainService::class.java)
                            newIntent.putExtra("isServiceDestroyed", true)
                            MainApplication.getServiceContext().startService(newIntent)
                            Toast.makeText(
                                MainApplication.getServiceContext(),
                                "Stop Service - Success",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (cmd[0] == "start" && cmd[1] != "") {
                            val newIntent =
                                Intent(MainApplication.getServiceContext(), MainService::class.java)
                            newIntent.putExtra("isUserCommand", true)
                            newIntent.putExtra("UserCommand", cmdText)
                            MainApplication.getServiceContext().startService(newIntent)
                            Toast.makeText(
                                MainApplication.getServiceContext(),
                                "${if (cmd[0] == "start") "Start" else "Stop"} ${cmd[1]} - Success",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (cmd[0] == "stop" && cmd[1] != "") {
                            val newIntent =
                                Intent(MainApplication.getServiceContext(), MainService::class.java)
                            newIntent.putExtra("isUserCommand", true)
                            newIntent.putExtra("UserCommand", cmdText)
                            MainApplication.getServiceContext().startService(newIntent)
                            Toast.makeText(
                                MainApplication.getServiceContext(),
                                "${if (cmd[0] == "start") "Start" else "Stop"} ${cmd[1]} - Success",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                    }) {
                        Text("Exec")
                    }
                    Spacer(modifier = Modifier.padding(all = 3.dp))
                    Button(onClick = {
                        FileUtil.saveFile(text, "config.json")
                        Toast.makeText(
                            MainApplication.getServiceContext(),
                            "Configure Saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

package com.plcoding.bluetoothchat.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>//类型列表状态名
    val pairedDevices: StateFlow<List<BluetoothDevice>>//配对设备名
    val errors: SharedFlow<String>

    fun startDiscovery()//开始发现
    fun stopDiscovery()//停止发现

    fun startBluetoothServer(): Flow<ConnectionResult>//打开服务器？？
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>//连接到设备
    suspend fun trySendMessage(head:String,message: String): BluetoothMessage?//尝试发送消息

    fun closeConnection()//关闭连接
    fun release()//释放资源

}
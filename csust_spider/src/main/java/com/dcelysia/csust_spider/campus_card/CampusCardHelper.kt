package com.example.csustdataget.CampusCard

import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.util.Log
import com.example.csustdataget.CampusCard.model.QueryEleRequest
import com.example.csustdataget.CampusCard.repository.CampusCardRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CampusCardHelper {
    private val json by lazy { Gson() }
    private val TAG = "CampusCardHelper"
    private val repository by lazy { CampusCardRepository.instance }
    private val buildingMap = mapOf(
        "金盆岭校区" to "0030000000002502",
        "西苑2栋" to "9",
        "东苑11栋" to "178",
        "西苑5栋" to "33",
        "东苑14栋" to "132",
        "东苑6栋" to "131",
        "南苑7栋" to "97",
        "东苑9栋" to "162",
        "西苑11栋" to "75",
        "西苑6栋" to "41",
        "东苑4栋" to "171",
        "西苑8栋" to "57",
        "东苑15栋" to "133",
        "西苑9栋" to "65",
        "南苑5栋" to "96",
        "西苑10栋" to "74",
        "东苑12栋" to "179",
        "南苑4栋" to "95",
        "东苑5栋" to "130",
        "西苑3栋" to "17",
        "西苑4栋" to "25",
        "外教楼" to "180",
        "南苑3栋" to "94",
        "西苑7栋" to "49",
        "西苑1栋" to "1",
        "南苑8栋" to "98",
        "云塘校区" to "0030000000002501",
        "16栋A区" to "471",
        "16栋B区" to "472",
        "17栋" to "451",
        "弘毅轩1栋A区" to "141",
        "弘毅轩1栋B区" to "148",
        "弘毅轩2栋A区1-6楼" to "197",
        "弘毅轩2栋B区" to "201",
        "弘毅轩2栋C区" to "205",
        "弘毅轩2栋D区" to "206",
        "弘毅轩3栋A区" to "155",
        "弘毅轩3栋B区" to "183",
        "弘毅轩4栋A区" to "162",
        "弘毅轩4栋B区" to "169",
        "留学生公寓" to "450",
        "敏行轩1栋A区" to "176",
        "敏行轩1栋B区" to "184",
        "敏行轩2栋A区" to "513",
        "敏行轩2栋B区" to "520",
        "敏行轩3栋A区" to "527",
        "敏行轩3栋B区" to "528",
        "敏行轩4栋A区" to "529",
        "敏行轩4栋B区" to "530",
        "行健轩1栋A区" to "85",
        "行健轩1栋B区" to "92",
        "行健轩2栋A区" to "99",
        "行健轩2栋B区" to "106",
        "行健轩3栋A区" to "113",
        "行健轩3栋B区" to "120",
        "行健轩4栋A区" to "127",
        "行健轩4栋B区" to "134",
        "行健轩5栋A区" to "57",
        "行健轩5栋B区" to "64",
        "行健轩6栋A区" to "71",
        "行健轩6栋B区" to "78",
        "至诚轩1栋A区" to "1",
        "至诚轩1栋B区" to "8",
        "至诚轩2栋A区" to "15",
        "至诚轩2栋B区" to "22",
        "至诚轩3栋A区" to "29",
        "至诚轩3栋B区" to "36",
        "至诚轩4栋B区" to "50",
        "至诚轩4栋A区" to "43"
    )

    /**
    @param campusName:校区名字
    @param buildingName:楼栋名字
    @param roomId:房间号
     */
    suspend fun queryElectricity(
        campusName: String,
        buildingName: String,
        roomId: String
    ): Double? {
        val campusId = buildingMap[campusName]
        val buildingId = buildingMap[buildingName]
        if (campusId.isNullOrBlank() || buildingId.isNullOrBlank()) {
            Log.e(TAG, "queryElectricity: invalid campus or building -> $campusName / $buildingName")
            return null
        }
        return try {
            // 切到 IO 线程执行网络请求（repository.getElectricity 应为 suspend）
            withContext(Dispatchers.IO) {
                val requestObj = mapOf(
                    "query_elec_roominfo" to QueryEleRequest(
                        aid = campusId,
                        room = QueryEleRequest.Room(roomid = roomId, room = roomId),
                        floor = QueryEleRequest.Floor(floorid = "", floor = ""),
                        area = QueryEleRequest.Area(area = campusName, areaname = campusName),
                        building = QueryEleRequest.Building(buildingid = buildingId, building = "")
                    )
                )
                val jsonDataString = json.toJson(requestObj)
                val response = repository.getElectricity(jsonDataString)
                Log.d(TAG,response)
                if (response.contains("无法获取房间信息")){
                    null
                }
                else{
                    extractElectricityFromString(response)
                }


            }
        } catch (e: Exception) {
            Log.e(TAG, "queryElectricity failed", e)
            null
        }
    }

    fun extractElectricityFromString(text: String?): Double? {
        if (text.isNullOrBlank()) return null

        val patterns = listOf(
            "电量[:：\\s]*([0-9]+(?:\\.[0-9]+)?)",
            "剩余[:：\\s]*([0-9]+(?:\\.[0-9]+)?)",
            "\"balance\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)",
            "balance[:：\\s]*([0-9]+(?:\\.[0-9]+)?)",
            "([0-9]+(?:\\.[0-9]+)?)\\s*(?:度|kwh|kWh)"
        )

        for (p in patterns) {
            val m = Regex(p, RegexOption.IGNORE_CASE).find(text)
            val captured = m?.groups?.get(1)?.value
            if (!captured.isNullOrEmpty()) {
                return try {
                    captured.toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }

        // fallback: 第一个出现的数字
        val general = Regex("([0-9]+(?:\\.[0-9]+)?)").find(text)
        return try {
            general?.groups?.get(1)?.value?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

}
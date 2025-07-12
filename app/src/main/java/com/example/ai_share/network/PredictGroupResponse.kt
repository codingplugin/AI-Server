package com.example.ai_share.network
 
data class PredictGroupResponse(
    val results: Map<String, List<String>> // name -> list of base64 images
) 
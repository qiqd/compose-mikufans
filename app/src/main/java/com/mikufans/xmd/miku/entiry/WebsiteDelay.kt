package com.mikufans.xmd.miku.entiry

import com.mikufans.xmd.miku.service.HtmlParser
import java.io.Serializable

data class WebsiteDelay(val service: HtmlParser, val delay: Int) : Serializable

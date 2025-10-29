package com.mikufans.xmd.miku.entiry

import com.mikufans.xmd.miku.service.HtmlParser


data class WebsiteDelay(val service: HtmlParser, val delay: Int) : java.io.Serializable

package com.jhutchings87.jame360.ui.calendar

import com.jhutchings87.jame360.data.model.ServiceType
import java.time.LocalDate

data class CalendarEntry(
    val date: LocalDate?,
    val title: String,
    val subtitle: String,
    val hasFile: Boolean,
    val source: ServiceType,
    val serverName: String
)

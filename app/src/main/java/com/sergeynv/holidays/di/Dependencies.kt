package com.sergeynv.holidays.di

import com.sergeynv.holidays.api.HolidaysService

/** Dependency graph. To be replaced a proper DI framework (Hilt)*/
object Dependencies {
    var holidaysService: HolidaysService = retrofit
}
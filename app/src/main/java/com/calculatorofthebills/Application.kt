package com.calculatorofthebills

import android.app.Application
import com.orhanobut.hawk.Hawk
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModuleMainViewModel = module {
    single { CalculatorMainViewModel() }
}

class CalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
        startKoin {
            modules(
                module {
                    single { CalculatorMainViewModel() }
                }
            )
        }
    }
}

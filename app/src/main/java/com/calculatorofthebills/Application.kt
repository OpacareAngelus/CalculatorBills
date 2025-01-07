package com.calculatorofthebills

import android.app.Application
import com.calculatorofthebills.util.room.AppDatabase
import com.orhanobut.hawk.Hawk
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModuleMainViewModel = module {
    single { CalculatorMainViewModel() }
}

val databaseModule = module {
    single { AppDatabase.getInstance(get()).transactionDao() }
}

class CalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
        startKoin {
            androidContext(this@CalculatorApplication)
            modules(appModuleMainViewModel, databaseModule)
        }
    }
}

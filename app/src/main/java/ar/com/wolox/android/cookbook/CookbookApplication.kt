package ar.com.wolox.android.cookbook

import android.os.Build
import ar.com.wolox.android.cookbook.CookbookModules.initializeModules
import ar.com.wolox.android.cookbook.common.di.CookbookNetworkingComponent
import ar.com.wolox.android.cookbook.common.di.DaggerAppComponent
import ar.com.wolox.android.cookbook.common.di.DaggerCookbookNetworkingComponent
import ar.com.wolox.android.cookbook.notifications.helper.NotificationChannelFactory
import ar.com.wolox.wolmo.core.WolmoApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.gson.FieldNamingPolicy
import com.readystatesoftware.chuck.ChuckInterceptor
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import me.aartikov.alligator.AndroidNavigator
import me.aartikov.alligator.NavigationContextBinder
import me.aartikov.alligator.Navigator
import me.aartikov.alligator.ScreenResolver
import me.aartikov.alligator.navigationfactories.GeneratedNavigationFactory
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import javax.inject.Inject

class CookbookApplication : WolmoApplication() {

    @Inject
    lateinit var notificationChannelFactory: NotificationChannelFactory

    override fun onInit() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        // Initialize Application stuff here
        initializeLeakCanary()
        initializeFresco()
        initializeAlligator()

        startKoin {
            androidContext(this@CookbookApplication)
            initializeModules()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelFactory.init()
        }
    }

    private fun initializeAlligator() {
        androidNavigator = AndroidNavigator(GeneratedNavigationFactory())
    }

    private fun initializeLeakCanary() {
        LeakCanary.install(this)
    }

    private fun initializeFresco() {
        Fresco.initialize(this)
    }

    override fun applicationInjector(): AndroidInjector<CookbookApplication> {
        return DaggerAppComponent.builder()
                .networkingComponent(buildNetworkingComponent())
                .sharedPreferencesName(SHARED_PREFERENCES_NAME)
                .application(this)
                .create(this)
    }

    private fun buildNetworkingComponent(): CookbookNetworkingComponent {
        val builder = DaggerCookbookNetworkingComponent.builder()
                .baseUrl(PLACEHOLDER_URL)
                .gsonNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)

        if (BuildConfig.DEBUG) {
            builder.okHttpInterceptors(arrayOf(
                buildHttpLoggingInterceptor(Level.BODY),
                ChuckInterceptor(this)))
        }

        return builder.build()
    }

    /**
     * Returns a [HttpLoggingInterceptor] with the newLevel given by **newLevel**.
     *
     * @param newLevel - Logging newLevel for the interceptor.
     * @return New instance of interceptor
     */
    private fun buildHttpLoggingInterceptor(newLevel: HttpLoggingInterceptor.Level): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { this.level = newLevel }
    }

    companion object {

        const val SHARED_PREFERENCES_NAME = "wolmo-cookbook-sp"
        const val PLACEHOLDER_URL = "https://jsonplaceholder.typicode.com"

        private lateinit var androidNavigator: AndroidNavigator

        fun getNavigator(): Navigator = androidNavigator
        fun getNavigationContextBinder(): NavigationContextBinder = androidNavigator
        fun getScreenResolver(): ScreenResolver = androidNavigator.screenResolver
    }
}

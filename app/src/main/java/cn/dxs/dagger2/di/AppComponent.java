package cn.dxs.dagger2.di;

import javax.inject.Singleton;

import cn.dxs.dagger2.App;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * @author lijian
 * @date 2019-09-19 15:53
 */
@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        LayoutManagerModules.class,
        AppModules.class,
        ActivityModules.class})
public interface AppComponent extends AndroidInjector<App> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<App> {
    }

}

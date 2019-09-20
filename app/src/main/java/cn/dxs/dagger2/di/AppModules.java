package cn.dxs.dagger2.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Singleton;

import cn.dxs.dagger2.App;
import cn.dxs.dagger2.pojo.Dish;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * @author lijian
 * @date 2019-09-19 16:02
 */
@Module
public abstract class AppModules {

    public static final String KEY_MENU = "menu";
    private static final String SP_COOK = "cook";

    @Singleton
    @Provides
    public static Set<Dish> providerMenus(SharedPreferences sp, Gson gson) {
        Set<Dish> menus;

        String menuJson = sp.getString(KEY_MENU, null);
        if (menuJson == null) {
            return new LinkedHashSet<>();
        }
        menus = gson.fromJson(menuJson, new TypeToken<Set<Dish>>() {
        }.getType());

        return menus;
    }

    @Singleton
    @Provides
    public static SharedPreferences providerSharedPreferences(Context context) {
        return context.getSharedPreferences(SP_COOK, Context.MODE_PRIVATE);
    }


    @Singleton
    @Provides
    public static Gson providerGson() {
        return new Gson();
    }

    @Singleton
    @Binds
    public abstract Context context(App app);
}

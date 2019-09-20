package cn.dxs.dagger2.di;

import cn.dxs.dagger2.ui.addedit.AddEditDishActivity;
import cn.dxs.dagger2.ui.addedit.AddEditModules;
import cn.dxs.dagger2.ui.dishes.DishesActivity;
import cn.dxs.dagger2.ui.dishes.DishesModules;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * @author lijian
 * @date 2019-09-19 15:34
 */
@Module
public abstract class ActivityModules {

    @DishesScoped
    @ContributesAndroidInjector(modules = DishesModules.class)
    public abstract DishesActivity contributesDishesActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = AddEditModules.class)
    public abstract AddEditDishActivity contributesAddEditDishActivity();
}

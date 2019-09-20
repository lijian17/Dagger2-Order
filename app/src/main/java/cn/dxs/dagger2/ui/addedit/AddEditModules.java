package cn.dxs.dagger2.ui.addedit;

import cn.dxs.dagger2.di.ActivityScoped;
import cn.dxs.dagger2.di.FragmentScoped;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

/**
 * @author lijian
 * @date 2019-09-19 15:07
 */
@Module
abstract public class AddEditModules {

    @Provides
    @ActivityScoped
    public static String providesDishId(AddEditDishActivity activity) {
        String id = activity.getIntent().getStringExtra(AddEditDishFragment.EDIT_DISH_ID);
        return id == null ? "" : id;
    }

    @Binds
    abstract AddEditDishContract.Presenter addEditDishesPresenter(AddEditDishPresenter presenter);

    @Binds
    abstract AddEditDishContract.View addEditView(AddEditDishFragment addEditDishFragment);

    @FragmentScoped
    @ContributesAndroidInjector()
    abstract public AddEditDishFragment addEditDishFragment();

}

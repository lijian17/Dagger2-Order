package cn.dxs.dagger2.ui.addedit;

import cn.dxs.dagger2.BasePresenter;
import cn.dxs.dagger2.BaseView;
import cn.dxs.dagger2.pojo.Dish;

/**
 * @author lijian
 * @date 2019-09-19 15:04
 */
public interface AddEditDishContract {

    interface View extends BaseView<Presenter> {
        void showDish(Dish dish);

        void showEmptyDishError();

        void saveSucceed(Dish dish);
    }

    interface Presenter extends BasePresenter<View> {
        void saveDish(String name, String description);

        void loadDish();
    }

}

package cn.dxs.dagger2.ui.dishes;

import java.util.List;
import java.util.Map;

import cn.dxs.dagger2.BasePresenter;
import cn.dxs.dagger2.BaseView;
import cn.dxs.dagger2.pojo.Dish;

/**
 * @author lijian
 * @date 2019-09-19 15:15
 */
public interface DishesContract {

    interface View extends BaseView<Presenter> {
        void showDishes(List<Dish> dishes);
    }

    interface Presenter extends BasePresenter<View> {

        void loadDishes();

        String order(Map<Dish, Boolean> selectMap);

        boolean deleteDish(String id);
    }
}

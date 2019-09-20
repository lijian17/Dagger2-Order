package cn.dxs.dagger2.ui.dishes;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import cn.dxs.dagger2.di.AppModules;
import cn.dxs.dagger2.pojo.Dish;

/**
 * @author lijian
 * @date 2019-09-19 16:20
 */
public class DishesPresenter implements DishesContract.Presenter {

    private DishesContract.View mView;

    @Inject
    Set<Dish> dishes;

    @Inject
    Gson gson;

    @Inject
    SharedPreferences sp;

    @Inject
    public DishesPresenter() {

    }

    @Override
    public void loadDishes() {
        mView.showDishes(new ArrayList<>(dishes));
    }

    @Override
    public String order(Map<Dish, Boolean> selectMap) {
        if (selectMap == null || selectMap.size() == 0) return "";
        StringBuilder sb = new StringBuilder();

        for (Dish dish : dishes) {
            if (selectMap.get(dish)) {
                sb.append(dish.getName()).append("、");
            }
        }
        if (TextUtils.isEmpty(sb.toString())) return "";

        return "烹饪: " + sb.toString();
    }

    @Override
    public boolean deleteDish(String id) {
        for (Dish dish : dishes) {
            if (dish.getId().equals(id)) {
                dishes.remove(dish);
                sp.edit().putString(AppModules.KEY_MENU, gson.toJson(dishes)).apply();
                return true;
            }
        }
        return false;
    }


    @Override
    public void takeView(DishesContract.View view) {
        mView = view;
        loadDishes();
    }

    @Override
    public void dropView() {
        mView = null;
    }
}

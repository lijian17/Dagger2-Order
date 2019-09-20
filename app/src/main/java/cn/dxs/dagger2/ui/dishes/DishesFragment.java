package cn.dxs.dagger2.ui.dishes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import cn.dxs.dagger2.R;
import cn.dxs.dagger2.pojo.Dish;
import cn.dxs.dagger2.ui.addedit.AddEditDishActivity;
import cn.dxs.dagger2.ui.addedit.AddEditDishFragment;
import dagger.android.support.DaggerFragment;

/**
 * @author lijian
 * @date 2019-09-19 15:14
 */
public class DishesFragment extends DaggerFragment implements DishesContract.View {

    RecyclerView rvDishes;

    @Inject
    DishesAdapter dishesAdapter;

    @Named("vertical")
    @Inject
    LinearLayoutManager layoutManager;

    @Inject
    DishesContract.Presenter mPresenter;

    @Inject
    public DishesFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dishes, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.takeView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.dropView();
    }

    public void initView(View view) {
        rvDishes = view.findViewById(R.id.rv_dishes);
        rvDishes.setAdapter(dishesAdapter);
        rvDishes.setLayoutManager(layoutManager);
        registerForContextMenu(rvDishes);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_dishes_item, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Dish dish = dishesAdapter.getLongClickDish();
        if (dish == null) return false;
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(getActivity(), AddEditDishActivity.class);
                intent.putExtra(AddEditDishFragment.EDIT_DISH_ID, dish.getId());
                startActivity(intent);
                break;
            case R.id.action_delete:
                if (mPresenter.deleteDish(dish.getId())) {
                    dishesAdapter.removedDish(dish);
                }
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void showDishes(List<Dish> dishes) {
        dishesAdapter.setDishes(dishes);
    }

    public String order() {
        return mPresenter.order(dishesAdapter.getSelectMap());
    }

}

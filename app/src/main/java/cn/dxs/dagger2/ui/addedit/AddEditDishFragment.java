package cn.dxs.dagger2.ui.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import cn.dxs.dagger2.R;
import cn.dxs.dagger2.pojo.Dish;
import dagger.android.support.DaggerFragment;

/**
 * @author lijian
 * @date 2019-09-19 14:52
 */
public class AddEditDishFragment extends DaggerFragment implements  AddEditDishContract.View{

    public static final String EDIT_DISH_ID = "edit_dish_id";

    private EditText etDish;
    private EditText etDescription;
    private Button btSummit;

    @Nullable
    @Inject
    String dishId;

    @Inject
    AddEditDishContract.Presenter mPresenter;

    @Inject
    public AddEditDishFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_dish, container, false);
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

    private void initView(View view){
        etDish = view.findViewById(R.id.et_dish);
        etDescription = view.findViewById(R.id.et_dish_description);
        btSummit = view.findViewById(R.id.bt_summit);
        btSummit.setOnClickListener(this::summit);
    }

    public void summit(View view){
        mPresenter.saveDish(etDish.getText().toString(), etDescription.getText().toString());
    }

    @Override
    public void showDish(Dish dish) {
        etDish.setText(dish.getName());
        etDescription.setText(dish.getDescription());
    }

    @Override
    public void showEmptyDishError() {
        Snackbar.make(etDish, R.string.empty_dish_message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void saveSucceed(Dish dish) {
        if (getActivity() != null) getActivity().finish();
    }

}

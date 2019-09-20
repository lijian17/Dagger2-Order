package cn.dxs.dagger2.ui.addedit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import javax.inject.Inject;

import cn.dxs.dagger2.ActivityUtils;
import cn.dxs.dagger2.R;
import dagger.android.support.DaggerAppCompatActivity;

/**
 * @author lijian
 * @date 2019-09-19 14:51
 */
public class AddEditDishActivity extends DaggerAppCompatActivity {

    @Inject
    AddEditDishFragment mAddEditDishFragment;

    @Inject
    String dishId;

    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);

        AddEditDishFragment addEditDishFragment = (AddEditDishFragment) getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        if (addEditDishFragment == null) {
            addEditDishFragment = mAddEditDishFragment;
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), addEditDishFragment, R.id.content_fragment);
        }

        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        if (TextUtils.isEmpty(dishId)) {
            toolbar.setTitle(R.string.add_dish);
        } else {
            toolbar.setTitle(R.string.edit_dish);
        }
        setSupportActionBar(toolbar);
    }
}

package cn.dxs.dagger2.di;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * @author lijian
 * @date 2019-09-19 15:42
 */
@Documented
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface DishesScoped {
}

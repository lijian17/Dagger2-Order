package cn.dxs.dagger2.di;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * 在dagger中，非作用域组件不能依赖于作用域组件。
 * 由于 {@link OrderAppComponent} 是一个作用域组件({@code @Singleton}，
 * 我们创建了一个自定义作用域供所有片段组件使用。
 * 此外，具有特定作用域的组件不能具有具有相同作用域的子组件。
 *
 * @author lijian
 * @date 2019-09-19 15:45
 */
@Documented
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityScoped {
}

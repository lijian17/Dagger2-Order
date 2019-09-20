package cn.dxs.dagger2;

/**
 * @author lijian
 * @date 2019-09-19 14:45
 */
public interface BasePresenter<T extends BaseView> {

    /**
     * 恢复时将演示者与视图绑定。演示者将在此处执行初始化。
     *
     * @param view 与此演示者关联的视图
     */
    void takeView(T view);

    /**
     * 销毁时删除对视图的引用
     */
    void dropView();
}

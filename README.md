# Dagger2应用实战-简易点餐demo
现在我们来看下如何使用Dagger2来开发一个简单的Demo，这里笔者开发的Demo是一个简单的点餐Demo。这个Demo的功能非常简单，提供了菜单展示、菜单添加/编辑/删除和下单功能。而下单功能只是简单地把菜品名用Snackbar显示到屏幕上。
  
## Demo展
### 操作展示
![](dagger2Order-1.gif)

### 代码目录
![](dagger2Order-2.png)

这个Demo采用经典的MVP架构，我们先来简单分析下Demo的细节实现。

1. 使用SharedPreferences提供简单的缓存功能（存储菜单）。
2. 使用Gson把列表序列化成Json格式数据，然后以String的形式保存在SharedPreferences中。
3. 使用Dagger2实现依赖注入功能。

这样基本就实现了一个简单的点菜Demo了。

## Dagger在Demo中的应用解释
当我们使用SharedPreferences和Gson实现缓存功能的时候我们会发现，项目中很多地方都会需要这个SharedPreferences和Gson对象。所以我们可以得出两个结论：

1. 项目中多个模块会用到一些公共实例。
2. 这些公共实例应该是单例对象。

我们看看是如何通过使用Dagger2提供全局的Modules来实现这类型对象的依赖注入。

### CookAppModules
```java
@Module
public abstract class CookAppModules {

    public static final String KEY_MENU = "menu";
    private static final String SP_COOK = "cook";

    @Singleton
    @Provides
    public static Set<Dish> providerMenus(SharedPreferences sp, Gson gson){
        Set<Dish> menus;
        String menuJson = sp.getString(KEY_MENU, null);
        if (menuJson == null){
            return new LinkedHashSet<>();
        }
        menus = gson.fromJson(menuJson, new TypeToken<Set<Dish>>(){}.getType());
        return menus;
    }

    @Singleton
    @Provides
    public static SharedPreferences providerSharedPreferences(Context context){
        return context.getSharedPreferences(SP_COOK, Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    public static Gson providerGson(){
        return new Gson();
    }

    @Singleton
    @Binds
    public abstract Context context(OrderApp application);

}
```

在这里以dishes模块为例子，dishes中DishesPresenter是负责数据的处理的，所以我们会在DishesPresenter注入这些实例。

### DishesPresenter
```java
public class DishesPresenter implements DishesContract.Presenter{

   private DishesContract.View mView;

   @Inject
   Set<Dish> dishes;

   @Inject
   Gson gson;

   @Inject
   SharedPreferences sp;

   @Inject
   public DishesPresenter(){

   }

   @Override
   public void loadDishes() {
       mView.showDishes(new ArrayList<>(dishes));
   }

   @Override
   public String order(Map<Dish, Boolean> selectMap) {
       if (selectMap == null || selectMap.size() == 0) return "";
       StringBuilder sb = new StringBuilder();

       for (Dish dish : dishes){
           if (selectMap.get(dish)){
               sb.append(dish.getName()).append("、");
           }
       }
       if (TextUtils.isEmpty(sb.toString())) return "";

       return "烹饪: " + sb.toString();
   }

   @Override
   public boolean deleteDish(String id) {
       for (Dish dish : dishes){
           if (dish.getId().equals(id)){
               dishes.remove(dish);
               sp.edit().putString(CookAppModules.KEY_MENU, gson.toJson(dishes)).apply();
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
```

上面的代码能很好地体验Dagger2的好处，假如我们项目中有比较复杂的对象在很多地方都会用到的话，我们可以通过这种方式来简化我们的代码。

Dishes模块的UI是由Activity加Fragment实现的，Fragment实现了主要的功能，而Activity只是简单作为Fragment的外层。它们分别是：DishesActivity和DishesFragment

DishesActivity依赖了DishesFragment对象，而在DishesFragment则依赖了DishesAdapter、RecyclerView.LayoutManager、DishesContract.Presenter对象。

我们先来分别看看DishesActivity与DishesFragment的关键代码。


### DishesActivity
```java
public class DishesActivity extends DaggerAppCompatActivity {

    @Inject
    DishesFragment mDishesFragment;
    
    ...
}
```

### DishesFragment
```java
public class DishesFragment extends DaggerFragment implements DishesContract.View{

    RecyclerView rvDishes;

    @Inject
    DishesAdapter dishesAdapter;

    @Inject
    RecyclerView.LayoutManager layoutManager;

    @Inject
    DishesContract.Presenter mPresenter;
    
    @Inject
    public DishesFragment(){

    }
    
 }
```
 
DishesFragment通过Dagger2注入了DishesAdapter、RecyclerView.LayoutManager、DishesContract.Presenter，而这些实例是由DishesModules提供的。


### DishesModules
```java
@Module
public abstract class DishesModules {

    @ContributesAndroidInjector
    abstract public DishesFragment dishesFragment();

    @Provides
    static DishesAdapter providerDishesAdapter(){
        return new DishesAdapter();
    }
    
    @Binds
    abstract DishesContract.View dishesView(DishesFragment dishesFragment);

    @Binds
    abstract RecyclerView.LayoutManager layoutManager(LinearLayoutManager linearLayoutManager);


}
```

这里我们先说明下这几个注解的作用。

- @ContributesAndroidInjector
你可以把它看成Dagger2是否要自动把需要的用到的Modules注入到DishesFragment中。这个注解是Dagger2 For Android简化代码的关键，下面的小节会通过一个具体例子来说明。

- @Module
被这个注解标记的类可以看作为依赖对象的提供者，可以通过这个被标记的类结合其它注解来实现依赖关系的关联。

- @Provides
主要作用就是用来提供一些第三方类库的对象或提供一些构建非常复杂的对象在Dagger2中类似工厂类的一个角色。

- @Binds
主要作用就是确定接口与具体的具体实现类，这样说得比较抽象，我们还是看看例子吧。
在DishesFragment中有这么一句代码：

```java
@Inject
DishesContract.Presenter mPresenter;
```

我们知道DishesContract.Presenter是一个接口而这个接口可能有很多不同的实现类，而@Binds的作用就是用来确定这个具体实现类的。以看看PresenterModules的代码：
```java
@Module
public abstract class PresenterModules {
    @Binds
    abstract DishesContract.Presenter dishesPresenter(DishesPresenter presenter);

    ...
}
```
从这句代码可以看出，使用@Inject注入的DishesContract.Presenter对象的具体实现类是DishesPresenter。

## Dagger2 For Android是如何注入依赖的？

我们在用Dagger2的时候是通过一些模版代码来实现依赖注入的（ DaggerXXXComponent.builder().inject(xxx) 这种模版代码），但是在Demo中的DishesFragment根本没看到类似的代码啊，那么这些对象是什么时候注入到DishesFragment中的呢？

答案就是`@ContributesAndroidInjector`注解

我们先来看看Dagger2是通过什么方式来实现自动把依赖注入到DishesActivity中的。

### ActivityModules
```java
@Module
public abstract class ActivityModules {

    @ContributesAndroidInjector(modules = DishesModules.class)
    abstract public DishesActivity contributesDishActivity();

    @ContributesAndroidInjector(modules = AddEditModules.class)
    abstract public AddEditDishActivity contributesAddEditDishActivity();

}
```

没错，就是@ContributesAndroidInjector这个注解，modules就代表这个DishesActivity需要依赖哪个Modules。这篇教程我们不解释它的具体实现原理，你只需要知道@ContributesAndroidInjector的作用就可以了。

我们以前使用Dagger2的时候，需要些很多Component来辅助我们实现依赖注入，而现在我们整个App中只需要写一个Component就可以了。@ContributesAndroidInjector注解会帮助我们生成其它需要的Component，并且自动处理Component之间的关系，自动帮我们使用生成的Component来注入依赖。

我们先看看我们现在整个模块中唯一存在的Component是怎么使用的。

### OrderAppComponent
```java
@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        LayoutManagerModules.class,
        CookAppModules.class,
        PresenterModules.class,
        ActivityModules.class})
public interface OrderAppComponent extends AndroidInjector<OrderApp>{

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<OrderApp>{
    }

}
```

### OrderApp
```java
public class OrderApp extends DaggerApplication {


    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerOrderAppComponent.builder().create(this);
    }
}
```
为了加深大家对@ContributesAndroidInjecto注解的理解，我们稍微修改下DishesModules

```java
@Module
public abstract class DishesModules {

    //@ContributesAndroidInjector
    //abstract public DishesFragment dishesFragment();

    @Provides
    static DishesAdapter providerDishesAdapter(){
        return new DishesAdapter();
    }

    @Binds
    abstract DishesContract.View dishesView(DishesFragment dishesFragment);

    @Binds
    abstract RecyclerView.LayoutManager layoutManager(LinearLayoutManager linearLayoutManager);


}
```

### DishesActivity
```java
public class DishesActivity extends DaggerAppCompatActivity {

    //@Inject
    DishesFragment mDishesFragment;

    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        DishesFragment dishesFragment
                = (DishesFragment) getSupportFragmentManager().findFragmentById(R.id.content_fragment);

        if (dishesFragment == null){
            mDishesFragment = new DishesFragment();//新增代码
            dishesFragment = mDishesFragment;
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), dishesFragment, R.id.content_fragment);
        }
        initView();

    }
    ...
}
```

```java
//DaggerFragment改为Fragment
public class DishesFragment extends Fragment implements DishesContract.View{
}
```

这个时候，我们运行的时候会发现，DishesFragment中的依赖注入失败了，运行时会抛出空指针异常，没注入需要的数据。导致这个原因是因为我们在这里使用new来创建DishesFragment实例的，为什么使用new的时候会Dagger2没有帮我们注入实例呢？

当我们使用@Inject来注入DishesFragment的时候，Dagger2会自动帮我们判断DishesFragment所依赖的对象（@Inject注解标记），如果能直接注入的对象则直接注入到Fragment中，否则则从DishesModules中寻找是否有需要的对象，有的话则注入到DishesFragment中。而我们使用new来创建DishesFragment时Dagger2无法通过DishesModules来查找对象，因为我们没有声明DishesFragment与DishesModules的联系，DishesFragment也没有自动注入注解的标记（ 没有实现HasSupportFragmentInjector ）。所以Dagger2无法判断它们依赖关系也没办法自动帮DishesFragment自动注入依赖。

如果我们坚持要使用new的方式来依赖DishesFragment的话，则可以通过@ContributesAndroidInjecto注解来实现它们之间的关联。具体实现方式如下：

### DishesModules
```java
@Module(includes = PresenterModules.class)
public abstract class DishesModules {

    @ContributesAndroidInjector
    abstract public DishesFragment dishesFragment(); //增加这个抽象方法

    @Provides
    static DishesAdapter providerDishesAdapter(){
        return new DishesAdapter();
    }

    @Binds
    abstract DishesContract.View dishesView(DishesFragment dishesFragment);

    @Binds
    abstract RecyclerView.LayoutManager layoutManager(LinearLayoutManager linearLayoutManager);


}
```

DishesFragment继承于DaggerFragment

```java
public class DishesFragment extends DaggerFragment implements DishesContract.View{
    ...
}
```

改成这样，我们通过new方法来创建DishesFragment的时候也能实现通过注解进行依赖注入了，为什么会这样呢？因为@ContributesAndroidInjector的作用时帮我们生成需要的Subcomponent，然后在DaggerFragment通过 DispatchingAndroidInjector 对象来实现依赖注入（ 底层原理和我们使用DaggerXXXComponent手动实现依赖注入差不多 ）。我们可以看看DishesModules中被@ContributesAndroidInjector注解的方法生成的代码。
```java
@Module(subcomponents = DishesModules_DishesFragment.DishesFragmentSubcomponent.class)
public abstract class DishesModules_DishesFragment {
  private DishesModules_DishesFragment() {}

  @Binds
  @IntoMap
  @FragmentKey(DishesFragment.class)
  abstract AndroidInjector.Factory<? extends Fragment> bindAndroidInjectorFactory(
      DishesFragmentSubcomponent.Builder builder);

  @Subcomponent
  public interface DishesFragmentSubcomponent extends AndroidInjector<DishesFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DishesFragment> {}
  }
}
```

可以看出，编生成的代码符合我们上面的结论。

## Dagger2 For Android使用要点
我们现在来总结下，简化版的Dagger实现依赖注入的几个必要条件：

1. 第三方库通过Modules的@provides注解来提供依赖
2. 提供一个全局唯一的Component，并且Modules中需要天际AndroidSupportInjectionModule类，它的作用时关联需求与依赖之间的关系
3. Application需要继承DaggerApplication类，并且在applicationInjector构建并返回全剧唯一的Component实例
4. 其它需要使用依赖注入的组建都需要继承Dagger组件名字类，并且需要在相应的Modules中通过@ContributesAndroidInjector注解标记需要注入依赖的组建。

上面四个步骤就是使用Dagger2实现依赖注入的要点了，总的来说，复杂度比之前的方法简单了非常多，要写的模版代码也减少了非常多。

一般来说，上面的知识点已经足够让我们在项目中正常使用Dagger2了，但是在使用中还会遇到一些其它的问题，Dagger2也提供了解决方法。如果希望进一步了解的话，可以继续阅读下文。


## Dagger2拓展
### @Scope
Scope字面的意思是作用域，在我们使用Dagger2的时候经常会用到@Singleton这个注解，这个注解的意思的作用是提供单例对象。而我们在使用@Singleton这个注解的时候，会同时@Provides和@Component，为什么要这样做呢？因为@Scope的作用范围其实就是单例的作用范围，这个范围主要是通过Component来确定的。

所以@Scope的作用就是以指定Component的范围为边界，提供局部的单例对象。我们可以以上面的例子为例验证这个论点论点。

我们在DishesActivity中增加一句代码，作用时注入DishesPresneter对象。
```java
@Inject
DishesContract.Presenter mPresenter;
```
从上面的代码中，我们知道DishesFragment中也用同样的方式来注入过DishesPresneter对象，那么它们有什么区别的，我们通过调试功能来看下。

![](dagger2Order-3.png)
![](dagger2Order-4.png)


可以看出，DishesActivity和DishesFragment中的DishesPresenter不是同一个实例，它们的内存地址是不一样的。如果我们在PresenterModules的dishesPresenter方法中加上@Singleton

```java
@Singleton
@Binds
abstract DishesContract.Presenter dishesPresenter(DishesPresenter presenter);
```

可以预见，DishesActivity和DishesFragment中的DishesPresenter会变成同一个实例，在这个例子中@Singleton的作用是提供全局的单例（ 因为OrderAppComponent这个全局唯一的Component也被标注成@Singleton ）。这种用法比较简单，这里不再深入。而比较难理解的就是自定义Scope了，下面我们通过一个例子来加深大家对自定义Scope的理解。

### @DishesScoped
```java
@Documented
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface DishesScoped {
}
```

为了使测试效果更明显，我们稍微修改下Order这个Demo。

### DishesModules
```java
@Module
public abstract class DishesModules {
   ...
    @DishesScoped  // 添加注解
    @Binds
    abstract DishesContract.Presenter dishesPresenter(DishesPresenter presenter);
   ...

}
```

### ActivityModules
```java
@Module
public abstract class ActivityModules {

    @DishesScoped  // 添加注解
    @ContributesAndroidInjector(modules = DishesModules.class)
    abstract public DishesActivity contributesDishActivity();
}
```

然后现在我们来运行Demo，看下DishesActivity和DishesFragment中的DishesContract.Presenter的对象：

![](dagger2Order-5.png)

![](dagger2Order-6.png)

可以看出，它们是同一个对象，这验证了我们上面的结论。这里又个小问题就是，我们之前说@Scope是通过Component来确定作用边界的，但是上面这个例子中，并没有对任何Component类使用@Dishes注解啊？那么这里是如何确认边界的呢？

我们可以看看Dagger生成的类`ActivityModules_ContributesDishActivity`，这个类是根据ActivityModules中的contributesDishActivity方法生成的。
```java
@Module(subcomponents = ActivityModules_ContributesDishActivity.DishesActivitySubcomponent.class)
public abstract class ActivityModules_ContributesDishActivity {
  private ActivityModules_ContributesDishActivity() {}

  @Binds
  @IntoMap
  @ActivityKey(DishesActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindAndroidInjectorFactory(
      DishesActivitySubcomponent.Builder builder);

  @Subcomponent(modules = DishesModules.class)
  @DishesScoped   //看这里
  public interface DishesActivitySubcomponent extends AndroidInjector<DishesActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DishesActivity> {}
  }
}
```

谜底揭晓，当我们为contributesDishActivity添加上@DishesScoped注解后，自动生成的DishesActivitySubcomponent类被@DishesScoped注解了。所以@DishesScoped是通过DishesActivitySubcomponent来确认作用范围的，这也符合上面的结论。

### @Scope的实现原理

@Scope实现单例的原理其实很简单，我们可以看下加了@DishesScoped后Dagger为我们生成的注入辅助代码。在这里我们只看关键方法:
```java
private void initialize(final DishesActivitySubcomponentBuilder builder) {
      this.dishesFragmentSubcomponentBuilderProvider =
          new Provider<DishesModules_DishesFragment.DishesFragmentSubcomponent.Builder>() {
            @Override
            public DishesModules_DishesFragment.DishesFragmentSubcomponent.Builder get() {
              return new DishesFragmentSubcomponentBuilder();
            }
          };
      this.dishesPresenterProvider =
          DishesPresenter_Factory.create(
              DaggerOrderAppComponent.this.providerMenusProvider,
              DaggerOrderAppComponent.this.providerGsonProvider,
              DaggerOrderAppComponent.this.providerSharedPreferencesProvider);
      this.dishesPresenterProvider2 = DoubleCheck.provider((Provider) dishesPresenterProvider);   //这句代码是实现单例的关键。
    }
```

可以看到，我们的dishesPresenterProvider2这个对象的初始化是通过双锁校验的方式来实现单例的，所以这个对象是一个单例对象。而其它没有使用@Spoce注解的类则没有使用双锁校验的方式实现初始化，Dagger通过@Scope实现单例的原理其实非常简单。关于@Spoce的介绍就到这里了，如果需要深入的话，可以进一步查看Dagger2生成的辅助代码。

### @Qualifier和@Named注解
除了作用域的问题之外我们还会经常会遇到一个问题，总所周知，Dagger2是自动判断依赖关系的，如果我们的代码中需要使用同一个类生成两个或多个不同的对象呢？例如我们的LinearManager，我们现在想用Dagger提供一个横向的Manager，如果直接写在项目中是会报错的，因为Dagger无法判断需要注入/依赖的对象是哪个。如下面的代码：

### LayoutManagerModules
```java
@Module
public class LayoutManagerModules {

    @Provides
    public LinearLayoutManager providesLinearLayoutManager(Context context){
        return new LinearLayoutManager(context);
    }
    
    @Provides 
    public LinearLayoutManager providesHorizonalLinearLayoutManager(Context context){
        return new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    }

}
```

这段代码肯定是会报错的，如果我们想实现这个功能的话，这个时候我们就需要用到@Qualifier或者@Named注解了。

我们先用@Named来实现上面这个需求。

### LayoutManagerModules
```java
@Module
public class LayoutManagerModules {

    @Named("vertical")
    @Provides
    public LinearLayoutManager providesLinearLayoutManager(Context context){
        return new LinearLayoutManager(context);
    }

    @Named("horizontal")
    @Provides
    public LinearLayoutManager providesHorizonalLinearLayoutManager(Context context){
        return new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    }


}
```

### DishesModules
```java
public class DishesFragment extends DaggerFragment implements DishesContract.View{

    RecyclerView rvDishes;

    @Inject
    DishesAdapter dishesAdapter;

    @Named("horizontal")
    @Inject
    LinearLayoutManager layoutManager;
}
```

在注入的时候，我们通过 @Named("horizontal")就能控制实际是注入哪个LayoutManager了。在定义依赖的时候@Name注解要配合@Providers，而在使用的时候配合@Inject来使用。

### @Qualifier

@Qualifier的作用和@Named是一样的，@Name也被@Qualifier注解。在使用@Named的时候需要加上我们定义的key所以略显麻烦，我们可以通过自定义@Qualifier注解来解决这个问题。而自定义@Qualifier注解的方式和自定义@Spoce是一样的，非常简单，这里不作深入介绍了。

Dagger2还提供了例如懒加载等功能，使用起来都是比较简单的，这里限于篇幅就不作进一步介绍了。有兴趣的读者可以查阅源码或者看官方文档来体验下。

# 小结
Dagger2 For Android是一款非常适合移动端使用的依赖注入框架。它提供了静态编译的方式来实现依赖注入，性能非常好。并且最新版本的Dagger 2.17对Android提供了非常友好的支持，现在使用Dagger2的时候，我们不需要再手写注入代码，这一切Dagger2都帮我们自动实现了。总的来说，Dagger2是非常适合于应用到我们的项目中的。并且Dagger2实现依赖注入的方式非常有趣，能掌握这项技术的话，对我们的提升是非常大的，希望各位读者在阅读了本文后能够去体验一下。

# Creating an automatic Tabs based main navigation for your Vaadin Flow application

Vaadin has been often been chosen for apps where "getting the thing done" is the most essential thing. Impressive eye candy and the best possible user experience is usually just a nice-have-feature in many internal business apps. Tabsheet, or *Tabs* as it is called in Vaadin 10+, is probably not the best thing to use as your main navigation, but it is something that gets the thing done and what even your mother can use.

One might think it is trivial to use the Tabs component as the main navigation of your application, but it actually not.  If you want to use it "properly" with Router, that gives you the deep linking features in Vaadin. Tab changes should adjust the URL in the browser and when arriving directly to a specific view, the Tabs component should focus the corresponding view. 

With these tips and tricks, you can create a basic navigation framework for your app.

## Create the main layout based on Tabs

As with all main navigation solution you'll need a concept called router layout. In Vaadin Flow you can have multiple levels of layouts, but in this example, we only use the main layout which we'll call TabBasedMainLayout. It will have the following responsibilities:

 * Defines the main navigation with Tabs component.
 * Contains the actual views. In the example, we are using VerticalLayout so that comes automatically, just by implementing the interface RouterLayout.
 * Provides a helper method to register views, so that individual tabs contain RouterLink, which again makes sure that the browser URL is changed when switching between tabs.
 * Automatically registers all views that are using this parent layout (handy for most simple apps).
 * Creates human-readable tab titles. In the example, we just "de-camelcase" the class name, but in a real-life app you might want to override that for i18n.
 * Provides an API for views to make sure the correct tab is selected (the case when the user navigates directly to a certain view).

My example solution looks like this:


```
public class TabBasedMainLayout extends VerticalLayout implements RouterLayout {

    Tabs tabs = new Tabs();
    Map<Class<? extends Component>, Tab> viewToTab = new HashMap<>();

    public TabBasedMainLayout() {
        automaticallyRegisterAvailableViews();
        add(tabs);
    }

    private void automaticallyRegisterAvailableViews() {
        // Get all routes and register all views to tabbar that have this class
        // as parent layout
        UI.getCurrent().getRouter().getRoutes().stream()
                .filter(r -> r.getParentLayout() == getClass())
                .forEach(r -> registerView(r.getNavigationTarget()));

        // The Vaadin 13+ way to do it:
        // RouteConfiguration.forApplicationScope()
        //        .getAvailableRoutes().stream()
        //        .filter(r -> r.getParentLayout() == getClass())
        //        .forEach(r -> registerView(r.getNavigationTarget()));
    }

    private void registerView(Class<? extends Component> clazz) {
        Tab tab = new Tab();
        RouterLink rl = new RouterLink(
                createTabTitle(clazz), 
                clazz
        );
        tab.add(rl);
        tabs.add(tab);
        viewToTab.put(clazz, tab);

    }

    protected static String createTabTitle(Class<? extends Component> clazz) {
        return // Make somewhat sane title for the tab from class name
                StringUtils.join(
                        StringUtils.splitByCharacterTypeCamelCase(
                                clazz.getSimpleName()), " ");
    }

    public void selectTab(Component view) {
        tabs.setSelectedTab(viewToTab.get(view.getClass()));
    }

}

```

## Define the views

To use a router layout, your views must specify *layout* parameter in the @Route annotation. That alone makes your app somewhat working, but if you arrive directly to a specific view of your app, the Tabs component probably incorrectly shows the first tab as selected. Thus we need to somehow call TabBasedMainLayout.selectTab() method when an individual view is instantiated.

If you are using some DI framework or AspectJ, you probably already have an idea how to solve it in a clean way. For example, in a Spring app, you could use BeanPostProcessor or inject the main layout to each of your views and call the method from the constructor.

In this example, I wanted to use a platform-independent mechanism, which actually simplifies our views a bit too. I created an abstract superclass for all views, that ensures in attach listener that the parent layout has to correct tab selected. This approach requires one "unsafe cast", but it is fairly safe as we are only accepting views with a specific parent to this layout with our code in the parent layout. The solution also allows us to reduce some boilerplate by moving the @Route annotations from views to the abstract superclass. 

The abstract AbstractTabView looks like this:

```
@Route(layout = TabBasedMainLayout.class)
public abstract class AbstractTabView extends VerticalLayout {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ensureSelectTab();
    }

    /**
     * Ensure the correct tab is selected when landing straight to this view.
     *
     * If you have a DI framework like Spring in use, you could also have a
     * separate aspect to do this, for example with BeanPostProcessor.
     */
    private void ensureSelectTab() {
        TabBasedMainLayout ml = (TabBasedMainLayout) getParent().get();
        ml.selectTab(this);
    }

}
```

## The actual views

Finally creating actual views to our apps is trivial. We'll only need to extend from the AbstractTabView. Views are automatically registered to a specific URL by naming convention and listed in the main navigation, based on Tabs component. When you change between your views, you can rest assured that deep linking works correctly. 

With following dummy views you have MainView registered to root, AnotherView to */anotherview* and ThirdView to */thirdview*:

```
public class MainView extends AbstractTabView {

    public MainView() {
        add(new Paragraph("This is main view content"));
    }

}

public class AnotherView extends AbstractTabView {

    public AnotherView() {
        add(new Paragraph("This is another view content"));
    }

}

public class ThirdView extends AbstractTabView {

    public ThirdView() {
        add(new Paragraph("This is third view content"));
    }

}

```


## Running the example 

This project can be used as a starting point to create your own Vaadin Flow application with Spring Boot.
It contains all the necessary configuration and some placeholder files to get you started.

Import the project to the IDE of your choosing as a Maven project. 

Run application using `mvn spring-boot:run` or directly running Application class from your IDE.

Open http://localhost:8080/ in browser

## Read more

Read more about [configuring the Router in Vaadin](https://vaadin.com/docs/flow/routing/tutorial-routing-annotation.html)

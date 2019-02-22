package org.peimari.tab.as.main.layout.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * This is the main layout for your application. It automatically registers all your
 * main views to a tabbar that can be used to navigate from view to view.
 * 
 * @author mstahv
 */
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

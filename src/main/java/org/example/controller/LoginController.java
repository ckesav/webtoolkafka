package org.example.controller;

import org.example.controller.configuration.user.forms.UserForm;
import org.example.manager.ui.BreadCrumbManager;
import org.example.manager.ui.datatable.*;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(path = "/dn", method = RequestMethod.GET)
    public String welcome(
            final Model model,
            final Pageable pageable,
            @RequestParam Map<String,String> allParams
    ) {

        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model);
        manager.addCrumb("Cluster Explorer", null);

        // Retrieve how many views for each cluster
        final Map<Long, Long> viewsByClusterId = new HashMap<>();
        viewsByClusterId.put(1L,2L);
        logger.info("Hi this is welcome page");

        final Datatable.Builder<User> builder = Datatable.newBuilder(User.class)
                .withRepository(userRepository)
                .withPageable(pageable)
                .withRequestParams(allParams)
                .withUrl("/configuration/user")
                .withLabel("Users")
                // Only show active users.
                .withConstraint("isActive", true, ConstraintOperator.EQUALS)
                // With Create Link
                .withCreateLink("/configuration/user/create")
                // Email Column
                .withColumn(DatatableColumn.newBuilder(User.class)
                        .withFieldName("email")
                        .withLabel("Email")
                        .withRenderFunction(User::getEmail)
                        .withIsSortable(true)
                        .build())
                // Name Column
                .withColumn(DatatableColumn.newBuilder(User.class)
                        .withFieldName("displayName")
                        .withLabel("Name")
                        .withRenderFunction(User::getDisplayName)
                        .withIsSortable(true)
                        .build())
                // Role Column
                .withColumn(DatatableColumn.newBuilder(User.class)
                        .withFieldName("role")
                        .withLabel("Role")
                        .withRenderFunction((user) -> {
                            switch (user.getRole()) {
                                case ROLE_ADMIN:
                                    return "Admin";
                                case ROLE_USER:
                                    return "User";
                                default:
                                    return "Unknown";
                            }
                        })
                        .withIsSortable(true)
                        .build())
                // Action Column
                .withColumn(DatatableColumn.newBuilder(User.class)
                        .withLabel("Action")
                        .withFieldName("id")
                        .withIsSortable(false)
                        .withHeaderAlignRight()
                        .withRenderTemplate(ActionTemplate.newBuilder(User.class)
                                // Edit Link
                                .withEditLink(User.class, (record) -> "/configuration/user/edit/" + record.getId())
                                // Delete Link
                                .withDeleteLink(User.class, (record) -> "/configuration/user/delete/" + record.getId())
                                .build())
                        .build())
                .withSearch("email", "displayName");

        Datatable<User> udt = builder.build();

        // Add datatable attribute
        model.addAttribute("datatable", udt);

        logger.info("column size"+udt.getColumns().size());
        for (DatatableColumn<User> dtuser:udt.getColumns()){
            logger.info(dtuser.toString());
        }
        return "welcome";
    }

    /**
     * Helper for setting up BreadCrumbs for User actions.
     */
    private void setupBreadCrumbs(final Model model, final String name, final String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
                .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Users", "/configuration/user");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Users", null);
        }
    }
}

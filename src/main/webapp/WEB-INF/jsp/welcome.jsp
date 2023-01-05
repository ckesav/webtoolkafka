<link href="https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css" type="text/css" rel="stylesheet" />
<script src="https://code.jquery.com/jquery-3.3.1.js"></script>
<script src="https://cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js"></script>
<html>
<script type="text/javascript">
var data = "${datatable};

</script>
<head>
    <title>Welcome</title>
</head>
<body>
    <!-- Main content -->
        <main class="main">

            <!-- Breadcrumb -->
            <!-- Handles showing breadcrumbs -->
            <ol class="breadcrumb" th:if="${BreadCrumbs != null}">
                <li class="breadcrumb-item" th:classappend="${itrStat.last} ? 'active'" th:each="crumb, itrStat: ${BreadCrumbs.getCrumbs()}">
                    <a th:if="${crumb.url != null}" th:href="@{${crumb.url}}" th:text="${crumb.name}"></a>
                    <span th:if="${crumb.url == null}" th:text="${crumb.name}"></span>
                </li>

                <!-- Breadcrumb Menu-->
                <section layout:fragment="breadcrumb-menu">
                    <!-- empty by default -->
                </section>

            </ol>

            <div class="container-fluid">
                <div class="animated fadeIn">
                    <!-- Include Flash Attribute Messages -->
                    <div th:replace="fragments/alert :: alert (message=${FlashMessage})"></div>

                    <!-- Container for Dynamic Alerts -->
                    <div id="AlertContainer"></div>

                    <!-- Start main content -->
                    <section layout:fragment="content">
                        <p>Page content goes here</p>
                    </section>
                </div>

            </div>
            <!-- /.container-fluid -->
        </main>
</body>
</html>
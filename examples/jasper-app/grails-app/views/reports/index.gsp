<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Reports Example</title>
</head>

<body>
<div id="content" role="main">
    <div class="container">
        <section class="row colset-2-its">
            <div id="controllers" role="navigation">
                <h2>Report Examples</h2>
                <ul>
                    <li class="controller">
                        <g:link controller="reports" action="generate" id="testme">testme html</g:link>
                        <g:link controller="reports" action="generate" id="testme" params="[format: 'pdf']"> pdf</g:link>
                        <g:link controller="reports" action="generate" id="testme" params="[format: 'xls']"> xls</g:link>
                    </li>
                </ul>
            </div>
        </section>
    </div>
</div>

</body>
</html>

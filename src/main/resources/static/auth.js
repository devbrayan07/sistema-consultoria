(function () {

    const TOKEN_KEY = 'token';
    const USER_KEY = 'usuario';


    function parseUsuario(raw) {

        if (!raw) {
            return null;
        }

        try {

            return JSON.parse(raw);

        } catch (erro) {

            localStorage.removeItem(USER_KEY);

            return null;
        }
    }


    window.auth = {

        getToken() {

            const token =
                localStorage.getItem(TOKEN_KEY);


            if (
                !token
                ||
                token === 'null'
                ||
                token === 'undefined'
            ) {

                return null;
            }


            return token;
        },


        setSessao(data) {

            if (
                !data
                ||
                !data.token
            ) {

                throw new Error(
                    'Resposta de autenticação inválida.'
                );
            }


            const usuario = {

                id:
                    data.id
                    ??
                    null,

                nome:
                    data.nome
                    ??
                    '',

                email:
                    data.email
                    ??
                    '',

                tipo:
                    data.tipo
                    ??
                    ''
            };


            localStorage.setItem(
                TOKEN_KEY,
                data.token
            );


            localStorage.setItem(
                USER_KEY,
                JSON.stringify(usuario)
            );
        },


        getUsuario() {

            const usuario =
                localStorage.getItem(
                    USER_KEY
                );


            return parseUsuario(
                usuario
            );
        },


        isAutenticado() {

            return Boolean(
                this.getToken()
            );
        },


        validarProtecaoRota() {

            if (
                !this.isAutenticado()
            ) {

                this.limparSessao();


                window.location.replace(
                    'index.html'
                );


                return false;
            }


            return true;
        },


        limparSessao() {

            localStorage.removeItem(
                TOKEN_KEY
            );


            localStorage.removeItem(
                USER_KEY
            );
        },


        logout() {

            this.limparSessao();


            window.location.replace(
                'index.html'
            );
        }
    };

})();
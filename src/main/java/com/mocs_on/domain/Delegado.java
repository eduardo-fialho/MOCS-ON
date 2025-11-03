
package com.mocs_on.domain;

public class Delegado extends Usuario{
    
    Delegado(){
        super();
    }
    
    Delegado(Integer id, String nome, String email, String senha){
        super(id, nome, email, senha);
    }
}

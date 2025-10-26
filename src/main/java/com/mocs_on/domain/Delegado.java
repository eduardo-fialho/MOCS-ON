
package com.mocs_on.domain;

public class Delegado extends Usuario{
    
    Delegado(){
        super();
    }
    
    Delegado(Integer id, String nome, String email){
        super(id, nome, email);
    }
}

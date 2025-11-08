package com.mocs_on.domain;

/*
    Um link relacionado é um link para algum site ou documento que poderá ser anexado à uma publicação
    post, spotted etc...
    Ele estará relacionado com um post dado o id
*/

public class LinkRelacionado {
    private int id;
    private String link;
    
    LinkRelacionado(int id, String link){
        this.id = id;
        this.link = link;
    }
    
    public String getLink(){
        return this.link;
    }
    
    public Integer getId(){
        return this.id;
    }
}


package com.mocs_on.domain;


import java.time.LocalDateTime;

public class SpottedPost extends Post{
    
    public SpottedPost(String mensagem, PostStatus status, LocalDateTime data){
        super(mensagem, "Spotted", status, data);
    }
    
    public SpottedPost(){
        super("", "Spotted", PostStatus.PUBLICO, LocalDateTime.now());
    }
    
}

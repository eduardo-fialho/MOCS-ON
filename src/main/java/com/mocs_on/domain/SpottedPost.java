
package com.mocs_on.domain;
import java.time.LocalDateTime;

public class SpottedPost extends Post{
    
    public SpottedPost(String mensagem, String[] links, PostStatus status, LocalDateTime data){
        super(mensagem, "Spotted", links, status, data);
    }
    
}

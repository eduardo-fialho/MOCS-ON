package com.mocs_on.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTool {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java com.mocs_on.auth.PasswordHashTool <senha>");
            return;
        }
        String raw = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(raw);
        System.out.println("Senha original: " + raw);
        System.out.println("Hash Bcrypt: " + hash);
    }
}

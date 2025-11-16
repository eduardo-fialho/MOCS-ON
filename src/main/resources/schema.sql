CREATE if not exists TABLE usuarios (
    id SERIAL PRIMARY KEY,     
    nome VARCHAR(100) NOT NULL,  
    email VARCHAR(100) UNIQUE NOT NULL, 
    senha VARCHAR(255) NOT NULL,   
    tipo VARCHAR(50) NOT NULL);
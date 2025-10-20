# MOCS-ON

O “MOCS ON” tem como missão proporcionar uma plataforma digital integrada para o gerenciamento do projeto MOCS, Modelo de Comitês Simulados, com o objetivo de promover a participação ativa da comunidade envolvida no projeto, construindo um ambiente online para a exposição, organização e interação entre o secretariado, equipe, professores, participantes e outros interessados.
Descrição do projeto.

## Equipe de Desenvolvimento:
| Ordem | Nome          |
|:------|:--------------|
| 1     | Ana Sofia De Miranda|
| 2     | Arthur Henrique Neves|
| 3     | Eduardo Lopes Fialho|
| 4     | Hike Penedo Mendes|
| 5     | Samuel Soares Dos Santos|
| 6     |Túlio Araujo Grossi|

## Atores do Sistema:
| Ator      | Definição     |
|:----------|:--------------|
| Secretariado| Secretários são organizadores do projeto. Usuário com permissão total para gerenciar a plataforma. Pode controlar gestão de delegados, gestão de delegações, gestão de comitê, gestão de logística e gestão de projetos         |
| Equipe  | Assistentes e docentes participantes do projeto. Usuário com permissões especiais, pode controlar gestões gerais do projeto.         |
| Diretor de comitê | Responsável pela elaboração e condução do comitê; guias, informações, sessões, delegações e agenda. Usuário deve controlar gestão de comitê e gestão de delegações.          |
| Imprensa | Delegado do comitê de imprensa. Por isso, tem permissões especiais.Usuário deve controlar gestão de imprensa; publicar posts         |
| Delegado | O delegado é a pessoa física, quem faz a inscrição para participar de um comitê.Durante o evento, o delegado deve representar sua delegação, que é a personalidade do comitê atribuída a ele.Pode visualizar posts e informações sobre comitês, submeter arquivos como DPOs (Documento de Posicionamento Oficial), Propostas de Resolução, entre outros.          |
| Visitante | Usuário anônimo. Pode visualizar informações gerais sem fazer login.          |
| Apoiador | Instituições que contribuem com doações ou apoio financeiro.           |

## Requisitos Funcionais:
| Id     | Ator           | Descrição   |
|:-------|:--------------|:------------|
| REQ001 | Todos         | Deve poder fazer login pelo email e senha cadastrados. |
| REQ002 | Todos         | Usuários devem poder recuperar a senha via e-mail cadastrado. |
| REQ003 | Sistema       | Todas as edições feitas no sistema devem ser registradas com data e autor. |
| REQ004 | Todos         | Usuários podem visualizar e editar seus dados pessoais. |
| REQ005 | Todos         | Usuários podem acessar seu histórico de presença, cargos, documentos submetidos e outras participações. |
| REQ006 | Secretário    | Acesso total a todos os menus de gestões. |
| REQ007 | Secretário    | Pode criar, editar e atribuir funções a qualquer usuário da plataforma. |
| REQ008 | Sistema       | Deve manter um histórico de cada Usuário. |
| REQ009 | Secretário    | Pode criar e editar comitês, além de atribuir diretores aos respectivos comitês. |
| REQ010 | Secretário / Equipe / Diretor | Criação e Edição de informações gerais, posts, guias, agenda de sessão, cronogramas e outros documentos oficiais do comitê. |
| REQ011 | Diretor       | Controle dos documentos submetidos por delegados. |
| REQ012 | Secretário / Equipe / Diretor | Criação, Leitura e Edição de delegações de cada comitê. |
| REQ013 | Secretário / Equipe / Diretor | Gerenciar a lista de presença e os documentos dos delegados nas sessões. |
| REQ014 | Secretário / Equipe / Diretor | Devem avaliar o desempenho de cada delegado em termos de participação, responsabilidade e entrega de documentos, com base em critérios definidos. |
| REQ015 | Todos         | Todos podem visualizar informações públicas no mural geral: cronogramas, guias, datas e notícias oficiais. |
| REQ016 | Sistema       | Notificações importantes devem ser enviadas por email para os delegados. |
| REQ017 | Sistema       | Cada Comitê deve ter um próprio mural de posts. Deve conter os documentos aprovados pelos diretores e avisos relacionados. |
| REQ018 | Secretário / Equipe / Diretor | Aprovação de documentos deve ter status. Como “Recebido”, “Corrigir”, “Aprovado”. |
| REQ019 | Todos         | Documentos podem ser reagidos com emojis. |
| REQ020 | Todos         | Deve ser possível postar “Spotteds” e “Pérolas” nos murais de avisos. |
| REQ021 | Sistema       | Toda alteração no perfil de usuário deve ser notificada por email após ser salva. |
| REQ022 | Secretário / Equipe | Gestão de recursos necessários para o evento, permitindo que os responsáveis façam reservas e requisições. |
| REQ023 | Secretário / Equipe | Controle de materiais emprestados. |
| REQ024 | Secretário / Equipe | Controle de salas/locais agendados. |
| REQ025 | Secretário    | Controle de gastos. |
| REQ026 | Secretário / Equipe | Registrar presença e participantes dos treinamentos. |
| REQ027 | Secretário    | Agendar treinamentos quinzenais. |
| REQ028 | Secretário    | Gestão de recursos necessários; materiais e agendamento de locais. |
| REQ029 | Delegados     | Podem enviar documentos como DPOs, propostas de resolução e anexos diretamente pela plataforma. |
| REQ030 | Sistema       | A plataforma deve ser responsiva, funcionando em dispositivos móveis. |
| REQ031 | Sistema       | Todos os documentos oficiais devem estar disponíveis para download em uma aba específica. |
| REQ032 | Sistema       | Deve existir um formulário - ouvidoria para o secretariado - para reporte de bugs, problemas técnicos, dúvidas, denúncias e sugestões. |
| REQ033 | Sistema       | Usuários devem receber certificados pela plataforma. |
| REQ034 | Sistema       | Deve existir um controle de prazos para tarefas e documentos, alertando os usuários sobre as datas de entrega ou ações pendentes. |




## Regras de Negócio:
| Id     | Nome       | Descrição   |
|:-------|:-----------|:------------|
| RN001  | Login via email   | O login do usuário é feito com seu email cadastrado.   |
| RN002  | Um comitê por delegado   |Um delegado só pode estar inscrito em um único comitê por edição do MOCS.|
| RN003  | Aprovação da imprensa    | Toda publicação da Imprensa deve ser aprovada por um Diretor da Imprensa ou superior.  |
| RN004  | Sem acúmulo de função   | Membros do Secretariado não podem ser Delegados   |
| RN005  | Documentos oficiais   | Apenas documentos postados ou aprovados por diretores ou superiores são considerados oficiais.   |
| RN006  | Inscrição completa obrigatória     | Um delegado só pode se inscrever após preencher todos os campos obrigatórios.  |
| RN007  | Homologação de inscrição  | A  inscrição do delegado só é homologada quando é confirmado a disponibilidade de comitês e pagamento correto   |
| RN008  | Aprovação de postagens   | Posts só ficam visíveis após aprovação por um membro Diretor ou superior.  |
| RN009  | Documentos apenas por usuários cadastrados     | Apenas usuários cadastrados podem submeter qualquer tipo de documento.   |
| RN010  | Delegado não edita delegação   | Delegados não podem alterar sua própria delegação   |
| RN011  | Comitês arquivados são imutáveis   | Comitês encerrados/arquivados não podem ser editados.   |
| RN012  | Registro de contribuição de apoiador     | Todo apoiador deve ter sua contribuição registrada com data e tipo (financeira ou material).   |
| RN013  | Troca de senha por e-mail   | A senha só pode ser trocada com confirmação via e-mail.   |
| RN014  | Hierarquia de Permissões   | Cada ator/usuário tem permissões de acesso e de edição diferentes   |
| RN015  | Presença mínima para certificado     | Certificados são liberados apenas após presença mínima de 50%.   |
| RN016  | Arquivos permitidos   | Os arquivos enviados devem ter tamanho máximo de 10 MB e estar nos formatos permitidos: .pdf, .docx, .png.   |
| RN017  | Acesso de Visitantes  | Usuários não autenticados só podem acessar informações públicas.   |
| RN018  | Delegado não altera seu histórico     | Delegados não podem alterar informações de histórico; participações anteriores, documentos enviados, presença, etc.   |

## Casos de Uso:
| Id     | Caso de Uso                           | Requisitos Associados  | Regras de Negócio Associadas |
|:-------|:--------------------------------------|:-----------------------|:----------------------------|
| CSU01  | Login na plataforma                   | REQ001, REQ002          | RN001, RN013                |
| CSU02  | Recuperar senha                       | REQ002                  | RN013                       |
| CSU03  | Editar perfil de usuário              | REQ004, REQ021          | RN010, RN018                |
| CSU04  | Emitir certificado                    | REQ033                  | RN015                       |
| CSU05  | Criar usuários; cadastrar e editar dados pessoais e histórico | REQ003, REQ007 | RN004                       |
| CSU06  | Acessar histórico do usuário          | REQ005, REQ008          | RN018                       |
| CSU07  | Submeter documentos no comitê         | REQ011, REQ026, REQ019  | RN009, RN016                |
| CSU08  | Submeter um post no comitê ou geral   | REQ017, REQ018          | RN003, RN008, RN005         |
| CSU09  | Avaliar documento submetido           | REQ018                  | RN005                       |
| CSU10  | Preencher e salvar a lista de presença| REQ026                  | RN015                       |
| CSU11  | Criar comitê; cadastrar e editar página inicial, documentos e posts | REQ009, REQ010 | RN011                       |
| CSU12  | Visualizar mural geral                | REQ015                  |                             |
| CSU13  | Submeter e aprovar documentos e posts no mural | REQ017, REQ018 | RN003, RN008, RN005         |
| CSU14  | Reagir a documentos                   | REQ019                  |                             |
| CSU15  | Postar “Spotteds” e “Pérolas”         | REQ020                  | RN005                       |
| CSU16  | Gerenciar controle de prazos          | REQ034                  |                             |
| CSU17  | Registrar recursos e materiais        | REQ022, REQ023, REQ024, REQ025 | RN012   |
| CSU18  | Responsividade e acesso mobile        | REQ030                  |                             |

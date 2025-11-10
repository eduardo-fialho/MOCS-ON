# MOCS-ON

## Visão Geral
MOCS ON é uma plataforma digital integrada para gerir o Modelo de Comitês Simulados (MOCS). O objetivo é oferecer um ambiente único para secretariado, equipe de apoio, diretores, participantes e parceiros acompanharem atividades, documentos, comunicações e decisões do projeto.

### Destaques da plataforma
- Centraliza comitês, delegações e logística do evento em um só painel.
- Mantém histórico de ações e documentos para garantir rastreabilidade.
- Suporta processos de avaliação, comunicação e transparência com a comunidade.

## Equipe de Desenvolvimento
| Ordem | Nome |
|:-----:|:-----|
| 1 | Ana Sofia de Miranda |
| 2 | Arthur Henrique Neves |
| 3 | Eduardo Lopes Fialho |
| 4 | Hike Penedo Mendes |
| 5 | Samuel Soares dos Santos |
| 6 | Túlio Araújo Grossi |

## Atores do Sistema
| Ator | Definição |
|:-----|:----------|
| Secretariado | Gestão completa do projeto. Controla delegados, delegações, comitês, logística e projetos especiais. |
| Equipe | Assistentes e docentes responsáveis por apoiar as frentes operacionais do evento. Possuem permissões especiais. |
| Diretor de comitê | Conduz cada comitê, produz guias e acompanha delegados, agenda e documentação. |
| Imprensa | Delegados do comitê de imprensa com permissões para publicar e moderar posts institucionais. |
| Delegado | Participante inscrito em um comitê específico. Visualiza informações e entrega documentos e DPOs. |
| Visitante | Usuário não autenticado com acesso apenas a informações públicas. |
| Apoiador | Instituições e parceiros que oferecem apoio financeiro ou material e precisam ter seus registros oficiais. |

## Requisitos Funcionais
| Id | Ator | Descrição |
|:---|:-----|:----------|
| REQ001 | Todos | Login com e-mail e senha cadastrados. |
| REQ002 | Todos | Recuperação de senha via e-mail. |
| REQ003 | Sistema | Registro de toda edição com data e autor. |
| REQ004 | Todos | Visualização e edição dos dados pessoais. |
| REQ005 | Todos | Acesso ao histórico de presença, cargos e documentos submetidos. |
| REQ006 | Secretário | Acesso completo a todos os menus de gestão. |
| REQ007 | Secretário | Criação, edição e atribuição de funções a qualquer usuário. |
| REQ008 | Sistema | Manutenção do histórico detalhado de cada usuário. |
| REQ009 | Secretário | Criação e edição de comitês, incluindo a atribuição de diretores. |
| REQ010 | Secretário / Equipe / Diretor | Criação e edição de posts, guias, agendas e demais documentos oficiais. |
| REQ011 | Diretor | Controle dos documentos enviados pelos delegados. |
| REQ012 | Secretário / Equipe / Diretor | CRUD completo das delegações de cada comitê. |
| REQ013 | Secretário / Equipe / Diretor | Gestão de listas de presença e documentos durante as sessões. |
| REQ014 | Secretário / Equipe / Diretor | Avaliação de desempenho de cada delegado. |
| REQ015 | Todos | Acesso público a murais, cronogramas e comunicados oficiais. |
| REQ016 | Sistema | Envio de notificações importantes por e-mail. |
| REQ017 | Sistema | Mural próprio para cada comitê com posts e documentos aprovados. |
| REQ018 | Secretário / Equipe / Diretor | Aprovação de documentos com status (Recebido, Corrigir, Aprovado). |
| REQ019 | Todos | Reações com emojis em documentos e posts. |
| REQ020 | Todos | Publicação de "Spotteds" e "Pérolas" nos murais. |
| REQ021 | Sistema | Notificação por e-mail a cada alteração de perfil salva. |
| REQ022 | Secretário / Equipe | Gestão de recursos e reservas necessárias ao evento. |
| REQ023 | Secretário / Equipe | Controle de materiais emprestados. |
| REQ024 | Secretário / Equipe | Controle de salas e espaços agendados. |
| REQ025 | Secretário | Monitoramento de gastos do projeto. |
| REQ026 | Secretário | Registro de presença nos treinamentos. |
| REQ027 | Secretário | Agendamento de treinamentos quinzenais. |
| REQ028 | Secretário / Equipe | Gestão de materiais e locais necessários para cada atividade. |
| REQ029 | Delegados | Envio de DPOs, propostas de resolução e anexos pela plataforma. |
| REQ030 | Sistema | Interface responsiva para desktop e dispositivos móveis. |
| REQ031 | Sistema | Centralização de documentos oficiais para download. |
| REQ032 | Sistema | Formulário de ouvidoria para bugs, dúvidas e denúncias. |
| REQ033 | Sistema | Emissão de certificados pela plataforma. |
| REQ034 | Sistema | Controle de prazos e alertas de tarefas e documentos. |

## Regras de Negócio
| Id | Nome | Descrição |
|:---|:-----|:----------|
| RN001 | Login via e-mail | Autenticação obrigatoriamente realizada com o e-mail cadastrado. |
| RN002 | Um comitê por delegado | Cada delegado só pode representar um comitê por edição do MOCS. |
| RN003 | Aprovação da imprensa | Postagens da imprensa precisam de aprovação de um Diretor da área ou superior. |
| RN004 | Sem acúmulo de função | Membros do secretariado não podem ser delegados. |
| RN005 | Documentos oficiais | Apenas documentos aprovados por diretores ou superiores são oficiais. |
| RN006 | Inscrição completa | Inscrição confirmada apenas com todos os campos obrigatórios preenchidos. |
| RN007 | Homologação de inscrição | Homologação depende de vaga em comitês e pagamento validado. |
| RN008 | Aprovação de postagens | Posts só ficam visíveis após aprovação. |
| RN009 | Documentos por usuários cadastrados | Apenas usuários autenticados podem enviar documentos. |
| RN010 | Delegado não altera delegação | Delegados não podem modificar sua própria delegação. |
| RN011 | Comitês arquivados são imutáveis | Comitês encerrados não podem ser editados. |
| RN012 | Registro de apoiadores | Toda contribuição deve registrar data e tipo (financeira ou material). |
| RN013 | Troca de senha por e-mail | Alteração de senha exige confirmação via e-mail. |
| RN014 | Hierarquia de permissões | Cada ator possui níveis específicos de acesso e edição. |
| RN015 | Presença mínima | Certificados liberados apenas com presença mínima de 50%. |
| RN016 | Arquivos permitidos | Limite de 10 MB; formatos aceitos: PDF, DOCX e PNG. |
| RN017 | Acesso de visitantes | Visitantes só visualizam conteúdo público. |
| RN018 | Histórico imutável do delegado | Delegados não editam registros históricos de participação. |

## Casos de Uso
| Id | Caso de Uso | Requisitos Associados | Regras de Negócio Associadas |
|:---|:-----------|:----------------------|:-----------------------------|
| CSU01 | Login na plataforma | REQ001 | RN001 |
| CSU02 | Criar e editar tipos de usuário | REQ004 | - |
| CSU03 | Criar e editar comitês | REQ003, REQ009, REQ017 | RN011 |
| CSU04 | Utilizar a ouvidoria | REQ032 | RN009 |
| CSU05 | Receber e avaliar documentos | REQ010, REQ011, REQ018, REQ019, REQ031 | RN005, RN016 |
| CSU06 | Submeter documentos | REQ003, REQ029 | RN009, RN016 |
| CSU07 | Submeter post em comitê | REQ003, REQ017 | RN008, RN017 |
| CSU08 | Interagir com post | REQ017 | RN008, RN017 |
| CSU09 | Registrar lista de presença | REQ013 | - |
| CSU10 | Consulta informal | REQ015 | - |
| CSU11 | Enviar "Spotteds" e "Pérolas" | REQ003, REQ020 | - |
| CSU12 | Workflow da imprensa | REQ003 | RN003, RN008 |
| CSU13 | Enviar avisos gerais | REQ003 | - |
| CSU14 | Recuperar senha | REQ002 | RN013 |
| CSU15 | Publicar agenda do comitê | REQ010 | - |
| CSU16 | Publicar agenda diária | REQ010 | - |
| CSU17 | Criar e editar delegações | REQ012 | RN010 |
| CSU18 | Disponibilizar guia de estudos | REQ010, REQ031 | RN005 |

## Planejamento de Sprints
Planejamento da construção do projeto resumido em 3 sprints.

### Macrocronograma
| Sprint | Foco | Principais entregas |
|:------|:-----|:---------------------|
| Sprint 1 | Fundamentos operacionais | Autenticação, ouvidoria, avisos e criação básica de comitês. |
| Sprint 2 | Engajamento e produção | Fluxos de posts, documentos e delegações. |
| Sprint 3 | Governança e relatórios | Agendas, métricas e publicação de guias. |

### Sprint 1 — Kick-off funcional
| Responsável | Item | Descrição resumida |
|:-----------|:-----|:-------------------|
| Ana Sofia de Miranda | CSU04 | Implementar formulário de ouvidoria integrado aos alertas do secretariado. |
| Hike Penedo Mendes | CSU01 | Autenticação com persistência de sessão e feedback de erro. |
| Eduardo Lopes Fialho | CSU13 | Envio de avisos gerais para murais institucionais. |
| Samuel Soares dos Santos | CSU11 | Publicação e moderação de Spotteds e Pérolas. |
| Arthur Henrique Neves | CSU14 | Recuperação e redefinição de senha por e-mail seguro. |
| Túlio Araújo Grossi | CSU03 | CRUD de comitês com atribuição de diretores. |

### Sprint 2 — Engajamento
| Responsável | Item | Descrição resumida |
|:-----------|:-----|:-------------------|
| Ana Sofia de Miranda | CSU06 | Upload de documentos pelos delegados com validação de formatos. |
| Hike Penedo Mendes | CSU07 | Fluxo de submissão de posts dentro dos comitês. |
| Eduardo Lopes Fialho | CSU05 | Painel para análise e aprovação de documentos. |
| Samuel Soares dos Santos |CSU08 | Reações e comentários moderados em posts. |
| Arthur Henrique Neves | CSU02 | Administração dos perfis e tipos de usuários. |
| Túlio Araújo Grossi | CSU17 | Gestão de delegações por comitê. |

### Sprint 3 — Governança
| Responsável | Item | Descrição resumida |
|:-----------|:-----|:-------------------|
| Ana Sofia de Miranda |CSU15 | Publicação das agendas de cada comitê. |
| Hike Penedo Mendes | CSU10 | Consulta informal e painel de informações públicas. |
| Eduardo Lopes Fialho | CSU18 | Publicação de guias e materiais de estudo centralizados. |
| Samuel Soares dos Santos | CSU12 | Workflow completo do comitê de imprensa. |
| Arthur Henrique Neves | CSU09 | Registro avançado de listas de presença. |
| Túlio Araújo Grossi | CSU16 | Agenda diária consolidada para logística do evento. |

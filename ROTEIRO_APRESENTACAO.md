# Roteiro para Apresentação do Projeto: Teoria da Computação

Este documento apresenta um roteiro estruturado para a apresentação do projeto, abordando a organização do código, as responsabilidades de cada classe e a lógica de negócio aplicada às conversões entre autômatos e gramáticas regulares.

---

## 1. Introdução e Visão Geral do Projeto
- **Objetivo:** Desenvolver uma ferramenta educacional e prática para simulação, conversão e minimização de Autômatos Finitos (DFA/NFA) e Gramáticas Regulares (GR).
- **Tecnologias:** Java 17+, JavaFX para interface gráfica, Maven para gerenciamento de dependências.

---

## 2. Arquitetura do Sistema
O projeto segue uma arquitetura em camadas para separação de responsabilidades:
- **`domain.model`**: Entidades fundamentais (o "coração" da teoria).
- **`domain.service`**: Lógica de processamento e algoritmos (regras de negócio).
- **`application`**: Controladores de alto nível e tratamento de entrada (Parsers e DTOs).
- **`presentation`**: Interface visual (JavaFX).

---

## 3. Detalhamento das Classes (Funcionalidades)

### Camada de Modelo (`domain.model`)
1. **`Automaton` (Abstrata)**: Define a estrutura base de um autômato (Estados, Alfabeto, Transições, Estado Inicial).
2. **`Dfa` e `Nfa`**: Especializações para Autômatos Finitos Determinísticos e Não-Determinísticos.
3. **`State`**: Representa um estado do autômato, contendo seu ID e se é inicial ou de aceitação.
4. **`Transition`**: Representa a transição entre estados por um símbolo do alfabeto (incluindo o símbolo vazio &epsilon;).
5. **`RegularGrammar`**: Representa a Quádrupla (N, T, P, S).
6. **`ProductionRule`**: Modela as produções da gramática (ex: `A -> aB` ou `A -> a`).

---

## 4. Lógica de Negócio e Algoritmos (`domain.service`)

### A. Conversão de Gramática Regular (GR) para Autômato (NFA)
*Classe: `GrammarConverterService`*
- **Lógica:**
    - Cada **Não-Terminal** da gramática torna-se um **Estado** no autômato.
    - O Símbolo Inicial da gramática torna-se o **Estado Inicial**.
    - Produções do tipo `A -> aB` geram uma transição `&delta;(A, a) = B`.
    - Produções do tipo `A -> a` geram uma transição `&delta;(A, a) = F`, onde `F` é um estado final criado automaticamente.
    - Se houver `S -> &epsilon;`, o estado inicial também é marcado como de aceitação.

### B. Conversão de Autômato para Gramática Regular (GR)
*Classe: `GrammarConverterService`*
- **Lógica:**
    - Cada **Estado** vira um **Não-Terminal**.
    - Transições `&delta;(S1, a) = S2` geram a regra `S1 -> aS2`.
    - Se o estado de destino `S2` for de aceitação, gera-se também a regra terminal `S1 -> a`.
    - Se o estado inicial for de aceitação, adiciona-se a produção de &epsilon; (`S -> &epsilon;`).

### C. Conversão de NFA para DFA (Construção de Subconjuntos)
*Classe: `SubsetConstructionService`*
- **Lógica:**
    - Utiliza o algoritmo de **fecho-&epsilon;** (epsilon-closure).
    - Cada novo estado no DFA representa um **conjunto de estados** do NFA.
    - O estado inicial do DFA é o fecho-&epsilon; do estado inicial do NFA.
    - Um estado do DFA é de aceitação se contiver pelo menos um estado de aceitação do NFA original.

### D. Minimização de DFA (Algoritmo de Hopcroft)
*Classe: `HopcroftMinimizationService`*
- **Lógica:**
    - Primeiro, remove-se os estados inalcançáveis.
    - Divide os estados em dois grupos iniciais: **Aceitação** e **Não-Aceitação**.
    - Refina a partição recursivamente: dois estados permanecem no mesmo grupo apenas se, para cada símbolo do alfabeto, eles transitam para estados que também estão no mesmo grupo (equivalência de estados).

---

## 5. Simulação e Validação
*Classe: `SimulationService`*
- Implementa a lógica de percorrer o autômato com uma cadeia de entrada.
- Retorna um **Trace** (rastreamento), permitindo ao usuário visualizar passo a passo quais estados foram visitados, facilitando a depuração pedagógica.

---

## 6. Interface e UX (`presentation`)
- **Visualização de Grafo**: Uso de visualização personalizada para desenhar os estados e transições de forma intuitiva.
- **Tabela de Transição**: Exibição tabular para conferência rápida da lógica do autômato.
- **Editor de Gramática**: Campo de texto que utiliza o `GrammarTextParser` para validar a sintaxe da gramática em tempo real.

---

## 7. Conclusão
- O sistema demonstra a equivalência entre GR e AF.
- A implementação modular permite fácil extensão (ex: adicionar suporte a Expressões Regulares no futuro).
- A ferramenta cumpre seu papel didático ao transformar conceitos abstratos em visualizações concretas.

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

## 3. Detalhamento das Classes e Funções (`domain.model`)

Nesta camada, as classes foram projetadas para serem majoritariamente **imutáveis**, garantindo que o estado de um autômaton ou gramática não seja alterado após sua criação, o que previne bugs colaterais.

1. **`Automaton` (Abstrata)**: 
   - **Função**: Serve como contrato base para DFA e NFA. 
   - **Responsabilidade**: Armazena o quíntuplo $\{\Sigma, Q, \delta, q_0, F\}$ e fornece métodos de conveniência como `getTransitionsFrom(state, symbol)` e `getAcceptingStates()`.

2. **`State`**: 
   - **Função**: Representa um estado único no sistema. 
   - **Detalhe**: Utiliza o campo `id` (String) como chave primária. Sobrescreve `equals` e `hashCode` para permitir o uso eficiente em `HashSet` e `HashMap`.

3. **`Transition`**: 
   - **Função**: Mapeia um par `(Origem, Símbolo)` a um conjunto de `Destinos`.
   - **Diferencial**: Suporta transições vazias (&epsilon;) usando uma constante `EPSILON = ""`. Em DFAs, o conjunto de destinos sempre contém exatamente um elemento (ou zero, se não houver transição).

4. **`RegularGrammar`**: 
   - **Função**: Encapsula a quádrupla $\{N, T, P, S\}$.
   - **Validação**: No momento da criação, garante que o símbolo inicial pertença aos Não-Terminais.

5. **`ProductionRule`**: 
   - **Função**: Modela regras de produção lineares à direita.
   - **Formatos suportados**: `A -> aB` (Terminal seguido de Não-Terminal), `A -> a` (Apenas Terminal) e `S -> &epsilon;` (Epsilon, restrito ao símbolo inicial).

---

## 4. Lógica de Negócio e Algoritmos (`domain.service`)

### A. Conversão NFA para DFA (Subset Construction)
*Classe: `SubsetConstructionService`*
- **Algoritmo**: Construção de Subconjuntos (Powerset Construction).
- **Passos Detalhados**:
    1. **Epsilon-Closure**: Para qualquer estado ou conjunto de estados, calcula-se recursivamente todos os estados alcançáveis via &epsilon;.
    2. **Função Move**: Calcula para onde um conjunto de estados vai dado um símbolo $a$.
    3. **Mapeamento de Estados**: Cada estado do DFA gerado é nomeado com base no conjunto de estados do NFA que ele representa (ex: `{q0, q1}`).
    4. **Estado Morto (Dead State)**: O algoritmo identifica transições indefinidas e as direciona para um "Estado Morto" `{}` para garantir que o DFA seja completo (útil para certas provas teóricas).

### B. Minimização de DFA (Algoritmo de Hopcroft)
*Classe: `HopcroftMinimizationService`*
- **Algoritmo**: Particionamento de Refinamento.
- **Lógica Profunda**:
    1. **Eliminação de Inalcançáveis**: Primeiro, realiza um BFS a partir do estado inicial para remover estados que não podem ser visitados.
    2. **Partição Inicial**: Divide os estados em dois grupos: $P_0 = \{F, Q-F\}$ (Finais e Não-Finais).
    3. **Refinamento Iterativo**: Para cada grupo $G$ na partição, verifica se todos os estados em $G$ transitam para o mesmo grupo $G'$ dado um símbolo $a$. Se um estado "destoa", o grupo é quebrado.
    4. **Convergência**: O algoritmo para quando nenhum grupo pode mais ser refinado (ponto fixo).

### C. Conversão entre GR e Automatos
*Classe: `GrammarConverterService`*
- **GR para NFA**:
    - $A \to aB \implies \delta(A, a) = B$
    - $A \to a \implies \delta(A, a) = F$ (onde $F$ é um estado final comum criado para fechar as produções terminais).
- **Automato para GR**:
    - Cada estado torna-se um Não-Terminal.
    - Se $\delta(S1, a) = S2$, gera $S1 \to aS2$.
    - Se $S2 \in F$ (é final), gera também a produção terminal $S1 \to a$.

---

## 5. Estruturas de Dados e Decisões de Projeto

A escolha das estruturas de dados foi guiada pela eficiência computacional e pela fidelidade aos conceitos matemáticos:

1. **`Set` (HashSet e LinkedHashSet)**:
   - **Por que?** A definição de autômatos e gramáticas baseia-se fortemente em **Conjuntos** (Alfabeto, Estados, Não-Terminais). 
   - **Vantagem**: Garante que não existam estados ou transições duplicadas e oferece busca em tempo constante $O(1)$.
   - **Nota**: Usamos `LinkedHashSet` na apresentação para garantir que a ordem de exibição na interface seja previsível e consistente.

2. **`Map` (HashMap e LinkedHashMap)**:
   - **Uso**: No algoritmo de Hopcroft para mapear estados a seus respectivos grupos de partição, e no Subset Construction para mapear conjuntos de estados de NFA para novos estados de DFA.
   - **Eficiência**: Permite a detecção rápida de equivalência de estados via assinaturas de transição.

3. **`Deque` (ArrayDeque)**:
   - **Uso**: Implementação de pilhas e filas para os algoritmos de BFS (Reachable States) e Epsilon-Closure.
   - **Por que?** Mais performático que `Stack` ou `LinkedList` em Java para operações de push/pop.

4. **Imutabilidade e Records**:
   - As classes de modelo não possuem "setters". Isso garante que uma vez que o sistema validou uma gramática ou autômato, ele permanece íntegro durante toda a simulação, evitando erros comuns de estado inconsistente.

---

## 6. Simulação, Validação e Interface

### A. Simulação e Trace
*Classe: `SimulationService`*
- **Trace de Execução**: Em vez de apenas retornar "Aceita/Rejeita", o serviço gera uma `SimulationTrace`.
- **Lógica**: Utiliza uma abordagem determinística para DFA (segue um caminho) e mantém o histórico de cada passo (Estado Atual, Símbolo Consumido, Próximo Estado).

### B. Parser de Gramática
*Classe: `GrammarTextParser`*
- **Função**: Transforma a entrada de texto do usuário (ex: `S -> aS | b`) em objetos `RegularGrammar`.
- **Algoritmo**: Utiliza Expressões Regulares (Regex) para validar a sintaxe de cada linha e extrair Terminais e Não-Terminais, lançando `MalformedGrammarException` em caso de erro.

### C. Camada de Apresentação (JavaFX)
- **`MainViewController`**: Orquestra a troca de painéis (Gramática, Definição, Conversão, Simulação).
- **`AutomatonGraphView`**: Utiliza cálculos de geometria básica para renderizar estados como círculos e transições como setas/arcos, permitindo a visualização dinâmica do grafo.
- **`ConversionsController`**: Atua como ponte entre a UI e os serviços de conversão (`SubsetConstructionService`, `HopcroftMinimizationService`).

---

## 7. Conclusão
O sistema demonstra a equivalência entre GR e AF através de uma implementação modular. A separação clara entre a **Matemática** (model), os **Algoritmos** (service) e a **Interface** (presentation) permite que o código seja usado tanto como uma ferramenta prática quanto como uma base de estudo para estudantes de Teoria da Computação.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

**AFD&GR** (`automaton-simulator`) — a JavaFX desktop simulator for the SIN 141 (Teoria da Computação) course covering finite automata (NFA/DFA) and regular grammars. Maven project, JDK 21, JavaFX 21, JUnit 5. The full Portuguese specification is in `Instruções de Desenvolvimento do Projeto.pdf` — consult it before adding features.

**Hard constraint from the spec**: all algorithms must be implemented from scratch. No regex libraries, no external automata libraries.

## Build & Run

```bash
mvn compile                       # compila tudo
mvn test                          # roda todos os testes (32)
mvn test -Dtest=StateTest         # roda um único teste
mvn javafx:run                    # abre a janela JavaFX
mvn package                       # gera target/automaton-simulator-*.jar
mvn clean                         # limpa target/
```

## Architecture

Layered, with strict downward dependencies: **presentation → application → domain.service → domain.model**. Packages live under `br.ufv.sin141` (a course-code reference — do not rename).

- **`domain.model`** — pure entities: `State`, `Transition`, `Automaton` (abstract, with `Nfa` / `Dfa` subclasses), `ProductionRule`, `RegularGrammar`. Immutable: constructors copy collections via `Set.copyOf`. `State.equals/hashCode` are based on `id` alone — two `State` instances with the same id are considered the same state.
- **`domain.service`** — stateless algorithms: `SubsetConstructionService` (NFA→DFA with ε-closure), `HopcroftMinimizationService` (uses **Moore** partitioning, the simpler variant the spec permits), `SimulationService` (with both `simulate(...)` returning `boolean` and `simulateWithTrace(...)` returning a `SimulationTrace` DTO), `GrammarConverterService` (GR↔AF). Services take domain objects in, return new domain objects out.
- **`application.exception`** — three spec-mandated runtime exceptions: `AlphabetViolationException`, `MalformedGrammarException`, `NondeterministicTransitionException`. All extend `RuntimeException`.
- **`application.dto`** — view-shaped data: `TransitionTableRow`, `SimulationStep`, `SimulationTrace`.
- **`application.parser`** — `GrammarTextParser` reads multi-line grammar text (`S -> aS | bA` / `A -> ε`). Accepts `e`, `ε`, `&`, or `E` as epsilon.
- **`application.controller.TheoryAppController`** — single façade. **The UI must never call services directly.** Holds session state (`currentAutomaton`, `currentDfa`, `minimizedDfa`, `currentGrammar`) and exposes view-friendly methods (`asTableRows`, `formatGrammar`, `convertCurrentToDfa`, `simulateWord`, etc.).
- **`presentation`** — JavaFX UI: `MainApp` boots the stage; `main-view.fxml` is a `TabPane` with 4 tabs (`panel/{automaton-definition,conversions,simulation,grammar}-view.fxml`). Each tab has its own controller in `presentation.panel`. The `TheoryAppController` is shared across panels via a static singleton on `MainViewController.appController()` — this is a deliberate DI simplification for a single-window desktop app.
- **`presentation.view`** — reusable JavaFX components: `AutomatonGraphView` (custom `Pane` that draws states/transitions with circular layout, supports zoom via `zoomProperty()` + Ctrl+scroll); `AutomatonView` (`VBox` combining title + transition table + zoomable diagram, used in 3 of 4 panels).

## Conventions Worth Knowing

- **Epsilon symbol**: stored as the empty string `""` via the constant `Transition.EPSILON`. UI renders it as `ε`.
- **Generated DFA state IDs**: subset construction labels DFA states as `"{q0,q1}"` (sorted, comma-separated). Minimization labels merged groups as `"[q0|q1]"`. Tests that assert on IDs rely on this format.
- **Dead state**: subset construction adds an explicit dead state `"{}"` only when the input NFA has missing transitions. Otherwise it's omitted.
- **DFA completeness**: `Dfa` constructor validates that no ε-transition exists and each (state, symbol) has at most one target.
- **Session state propagation**: `TheoryAppController.setCurrentAutomaton(...)` resets `currentDfa` and `minimizedDfa`. Setting a new automaton invalidates derived state.
- **Error handling in UI**: every panel controller catches `RuntimeException` from `app.*` calls and translates to `Alert.AlertType.ERROR` + status label. Don't propagate stack traces to the user.

## Notes for Future Work

- Painéis JavaFX consomem **só** o `TheoryAppController` — se precisar de um dado novo (formatado), adicione um método no façade em vez de importar serviços/entidades direto no `presentation.panel`.
- Quando estender o domínio (ex.: novos tipos de autômato), prefira adicionar subclasse de `Automaton` em vez de mexer na classe base — os serviços iteram sobre transições genéricas.
- Testes seguem o padrão `<ClasseSob teste>Test.java` em `src/test/java/...` espelhando a estrutura de `src/main/java/...`.

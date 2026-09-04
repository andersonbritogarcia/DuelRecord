### [x] Task 3.1: DeckIdentity & Mechanics Engine
* **Módulo:** `match` (base)
* **Escopo:**
  1. Criar entidades `DeckIdentity` e `DeckIdentityCard`.
  2. Suportar papéis: `COMMANDER`, `PARTNER`, `COMPANION`, `BACKGROUND`.
  3. Implementar algoritmo de união de identidade de cor (ex: W + UB = WUB; sem cor = "C").
  4. Deduplicação determinística via SHA-256 `signature` canônica.
  5. Modelar enums base do módulo `match`: `DeckCardRole`, `GameFormat`, `Platform`, `MatchStructure`.
* **Critérios de Aceite:**
  * Cadastrar Yoshimaru (W) + Kraum (UR) gera um `DeckIdentity` com `color_identity = WUR`.
  * Cadastrar Tasigur (UBG) + Lutri (UR Companion) gera identidade de cor combinada e persiste as roles corretamente.
  * Cadastrar deck incolor resulta em `color_identity = C`.
  * Submissão idêntica reutiliza a `DeckIdentity` existente.
* **Status:** Concluído com 102 testes passando e verificação Modulith 100% verde.

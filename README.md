# Territory Predictive Insights: monitoramento de alagamentos
 
Global Solution 2º/2026 · Programação Orientada a Objetos · Turma de Fevereiro
Tema **CitizenTech** · Recorte escolhido: **🌊 Clima local: chuva intensa e risco de alagamento**
 
| RM | Integrante |
|----|------------|
| 563556 | Luis Otavio Santini Feitosa |
| 564066 | Lucas Andrade Souza |
 
---
 
## 1. Público beneficiado e decisão apoiada
 
**Quem sofre o problema:** moradores das áreas baixas e das encostas de um território periférico (aqui, o território
fictício *Várzea do Córrego Verde*), onde chuva forte faz o córrego transbordar e o solo saturado escorrega.
Quem **usa** o sistema é a equipe local que precisa agir antes da água: a associação de moradores, o núcleo
comunitário de Defesa Civil (NUDEC) ou a subprefeitura.
 
**Decisão que o sistema apoia:** *"preciso avisar os moradores, abrir o abrigo ou acionar a Defesa Civil agora,
ou ainda dá para só acompanhar?"*. O sistema lê pluviômetros, sensores de nível do córrego e de umidade do solo,
grava tudo no banco, prevê o próximo valor e, quando a previsão atinge um limiar, emite um alerta que diz **qual
sensor**, **qual valor previsto**, **qual limite foi atingido** e **qual ação é sugerida**.
 
> O território é descrito por **região e equipamento** (pontos P01/P02, sensores PLV-01, NIV-01…).
> Nenhum dado pessoal de pessoas reais é usado.
 
---
 
## 2. Como rodar
 
**Pré-requisitos:** JDK 17 ou superior e Maven 3.8+ (ou IntelliJ, que já traz o Maven).
 
```bash
# 1) rodar os testes (JUnit 5 + Mockito)
mvn test
 
# 2) rodar o sistema (menu de console)
mvn compile exec:java
 
# alternativa: gerar o jar e executar
mvn package
java -jar target/territory-insights.jar
```
 
No **IntelliJ**: abra a pasta do projeto (ele reconhece o `pom.xml`), espere baixar as dependências e rode
`SistemaMonitoramento.main()`.
 
**Banco de dados:** H2 **em arquivo**, criado sozinho em `./dados/territorio.mv.db` na primeira execução
(o script de criação está em `src/main/resources/schema.sql`). Não precisa instalar nada.
Feche o programa e abra de novo: o histórico e os alertas continuam lá.
Para começar do zero, apague a pasta `dados/`.
 
**Roteiro rápido de demonstração:**
1. Opção **2**: com o banco vazio, o sistema **se recusa a prever** ("só há 0 leituras…").
2. Opção **1** com `20` ciclos: grava leituras. Algumas falham de propósito (sensor sem sinal ou valor impossível)
   e aparecem com mensagem clara, sem serem gravadas.
3. Opção **2**: as duas estratégias rodam sobre o histórico do banco e emitem alertas explicados.
4. Opção **4**: alertas já emitidos. Opção **3**: histórico de um sensor. Opção **6**: status completo.
5. Opção **5 → c**: alterar limiar exige o código de acesso `DEFESA2026`. Teste também um código errado
   e limiares fora de ordem.
6. Saia (opção **0**), rode de novo e veja na opção 3/4 que os dados sobreviveram.
7. Opção **7** (bônus): exporta o histórico de um sensor para `exportacoes/*.csv`.
---
 
## 3. Estrutura
 
```
src/main/java/br/com/fiap/territorio/
├── dominio/        ComponenteTerritorial (abstrata) ← PontoMonitoramento
│                   Sensor (interface) ← SensorSimulado (abstrata) ← Pluviometro, SensorNivelRio, SensorUmidadeSolo
│                   EstrategiaPrevisao (abstrata) ← PrevisaoMediaMovel, PrevisaoComparacao
│                   DadosTerritorio, Medicao, Alerta, Previsao, NivelAlerta, TipoSensor, StatusComponente
├── excecao/        DadosInvalidosException, FonteIndisponivelException, AcessoNegadoException, PersistenciaException
├── persistencia/   ConexaoBanco, MedicaoDAO, AlertaDAO  (JDBC + PreparedStatement + try-with-resources)
├── servico/        ServicoMonitoramento, FabricaPrevisao (Factory), ConfiguracaoSistema (Singleton),
│                   RelatorioBuilder + Relatorio (Builder), ResultadoPrevisao, ExportadorCsv (bônus)
└── SistemaMonitoramento.java   menu principal (Scanner)
 
src/test/java/br/com/fiap/territorio/
├── dominio/        MedicaoTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest, DadosTerritorioTest
├── servico/        ServicoMonitoramentoTest (Mockito), ConfiguracaoSistemaTest, FabricaPrevisaoTest
└── persistencia/   MedicaoDAOTest (integração com H2 em memória)
```
 
---
 
## 4. Decisões da equipe
 
**Caminho JDBC (e não Spring Boot + JPA).** O sistema é um menu de console com duas tabelas. JDBC deixa
visível cada SQL e cada recurso aberto e fechado, que é exatamente o que a GS avalia (PreparedStatement e
try-with-resources), sem a "mágica" do Spring para explicar na correção. Os DAOs recebem uma `ConexaoBanco`,
então os testes trocam o banco de arquivo por um H2 em memória sem mudar código.
 
**H2 em arquivo, não em memória.** O requisito diz que "os dados sobrevivem ao reiniciar". H2 em memória
apagaria tudo ao fechar. Em arquivo continua sem instalação nenhuma para quem corrige.
 
**Duas exceções obrigatórias com papéis diferentes.**
- `DadosInvalidosException` é *unchecked*: representa um dado errado (chuva negativa, população negativa,
  limiar fora de ordem). É lançada nos construtores, setters e na configuração, e capturada no menu.
- `FonteIndisponivelException` é *checked*: sensor cair em campo é algo **esperado**, então o compilador
  obriga quem chama `lerValor()` a tratar.
- Criamos mais duas: `AcessoNegadoException` (código errado ao mexer em limiar) e `PersistenciaException`
  (embrulha `SQLException` mantendo a causa original, para o menu não depender de JDBC).
- Não existe `catch` vazio: toda captura imprime o que aconteceu e o que foi (ou não) gravado.
**Faixa física ≠ limiar de alerta.** `TipoSensor` define o que é *fisicamente possível* (chuva de 0 a 200 mm/h).
Fora disso é defeito do sensor, e a `Medicao` nem chega a existir. O limiar diz o que é *perigoso*
(50 mm/h = crítico). São perguntas diferentes, por isso ficam em lugares diferentes.
 
**Sensores simulados com "passeio aleatório".** Cada leitura parte da anterior e varia um pouco, assim a
chuva "aperta" e "alivia" como na vida real, o que dá sentido à estratégia de comparação. 5% das leituras
simulam sensor sem sinal e 3% simulam leitura corrompida, para exercitar as exceções de verdade.
 
**Escolha da estratégia sem `if`/`instanceof`.** O serviço percorre `List<EstrategiaPrevisao>` e chama
`prever()`: quem decide o cálculo é o objeto (polimorfismo). A lista vem da `FabricaPrevisao`, que usa um
`Map<String, Supplier<EstrategiaPrevisao>>`.
 
**As duas estratégias.**
- *Média Móvel* (janela 5): média das últimas 5 leituras do banco. Suaviza picos isolados.
- *Comparação de Períodos* (2 blocos de 3): `previsto = médiaAtual + (médiaAtual − médiaAnterior)`.
  Reage mais rápido quando a chuva está aumentando, e nunca prevê valor negativo.
- As duas chamam `super(nome, minimoLeituras)` no construtor e sobrescrevem `prever()` e `descrever()`
  (esta última reaproveitando `super.descrever()`).
### Padrões criacionais: por que ali e não em outro lugar
 
| Padrão | Onde | Por que ali |
|---|---|---|
| **Singleton** | `ConfiguracaoSistema` | Os limiares precisam ser **uma única fonte de verdade**. Se o menu alterasse uma cópia e o serviço lesse outra, o sistema alertaria (ou deixaria de alertar) com um limite que a equipe acha que já mudou. Construtor privado e `getInstancia()`. Não usamos Singleton nos DAOs nem no serviço porque eles precisam ser trocados por mocks/bancos diferentes nos testes. |
| **Factory** | `FabricaPrevisao` | É o único ponto que sabe quais estratégias existem. A configuração guarda códigos (`MEDIA_MOVEL`, `COMPARACAO`) e a fábrica os transforma em objetos via `Map`, sem `if` espalhado. Uma 3ª estratégia = 1 linha no registro; serviço e menu não mudam. Não usamos Factory para `Medicao` porque ela tem um único jeito de ser criada, e o construtor com validação basta. |
| **Builder** | `RelatorioBuilder` | O "status completo" tem várias seções opcionais (pontos, últimas leituras, limiares, alertas). Um construtor com 6 parâmetros e vários `null` seria ilegível e fácil de errar a ordem. O builder monta passo a passo e só exige o território. Não usamos Builder em `Alerta` porque todos os campos dele são obrigatórios. |
 
O código **chama** os padrões em vez de duplicar a criação: o serviço pede estratégias à fábrica
(`getEstrategiasAtivas()`), todos pegam a configuração por `getInstancia()` e o status é sempre montado
pelo builder.
 
---
 
## 5. Testes
 
`mvn test` roda **27 testes**, todos no padrão **AAA** (comentários `// Arrange`, `// Act`, `// Assert`):
 
- **`assertThrows`** com as exceções customizadas: chuva negativa, umidade > 100%, população negativa,
  limiar fora de ordem, código de acesso errado, estratégia desconhecida, sensor fora do ar
  (`FonteIndisponivelException`).
- **Mockito** (`ServicoMonitoramentoTest`): `MedicaoDAO` e `AlertaDAO` são mocks. Testamos a lógica de
  alerta **sem banco**: previsão de 60 mm/h grava 2 alertas CRÍTICO com limite 50 (conferido com
  `ArgumentCaptor`); chuva fraca e histórico curto **nunca** chamam `alertaDAO.salvar()` (`verify(..., never())`);
  sensor com falha ou leitura impossível não gravam nada.
- **Testes de verdade:** os valores esperados são calculados à mão, por exemplo (10+20+30+40+50)/5 = 30, com
  duas leituras antigas de 100 que **não** podem entrar na conta. Se a média usar o histórico inteiro, o teste
  fica vermelho (dá 50).
- `MedicaoDAOTest` é um teste de **integração** extra, com H2 em memória, que prova que o SQL dos DAOs funciona.
---
 
## 6. Reflexão crítica
 
**Quando o sistema deve se calar?** Com poucas leituras, a "previsão" é só a última medição disfarçada de
estatística. Por isso cada estratégia declara um mínimo (5 leituras para a média móvel, 6 para a comparação)
e, abaixo dele, devolve uma previsão marcada como **não confiável**, que **nunca** vira alerta. A mensagem
diz quantas leituras faltam. Preferimos o silêncio explícito a um alerta falso, porque alarme falso
repetido treina a comunidade a ignorar o próximo, e o próximo pode ser o verdadeiro. Ainda assim, 5 ou 6
leituras é um mínimo técnico, não uma garantia: o sistema também deveria se calar (ou avisar que está
"cego") quando um ponto fica INOPERANTE, o que o status completo mostra.
 
**O que os dados não representam?** Um pluviômetro por setor diz pouco sobre a rua de baixo: chuva forte
é muito localizada e o alagamento depende de bueiro entupido, lixo no córrego e obra mal feita, coisas que
nenhum sensor nosso mede. Os limiares de nível e umidade são ilustrativos, e cada córrego tem a sua cota de
transbordamento. Os valores são simulados: provam que a estrutura funciona, não que as previsões acertam
o mundo real. E média e tendência simples não enxergam o que vem de fora, como uma frente fria chegando
ou a chuva da cabeceira que ainda vai descer o córrego.
 
**Por que o alerta apoia a decisão em vez de decidir por ela?** Quem está no território sabe o que o sistema
não sabe: que o abrigo está fechado, que a ponte já foi interditada, que há acamados numa rua específica.
Mandar a comunidade sair de casa tem custo real, e quem responde por essa decisão é uma pessoa, não um
`if`. Por isso o alerta **explica** o porquê (sensor, valor previsto, limite atingido, estratégia usada)
e **sugere** uma ação, para a pessoa conferir o raciocínio e decidir. Pelo mesmo motivo, mudar limiar exige
código de acesso: silenciar um alerta de enchente tem que ser uma decisão consciente e rastreável.
 
---
 
## 7. Bônus (+0,5)
 
**Exportação do histórico consultado em CSV** (menu, opção 7): gera `exportacoes/historico_<SENSOR>_<data>.csv`
com `;` e vírgula decimal, pronto para abrir no Excel em português (`ExportadorCsv`, com try-with-resources).
 
---
 
## 8. Divisão de tarefas
 
| Integrante | Responsabilidade principal |
|---|---|
| **Luis Otavio Santini Feitosa** (563556) | Domínio e exceções (`ComponenteTerritorial`, `Sensor` e implementações, `DadosTerritorio`, `Medicao`, `Alerta`, pacote `excecao`), estratégias de previsão e padrões (`EstrategiaPrevisao`, `FabricaPrevisao`, `ConfiguracaoSistema`, `RelatorioBuilder`) |
| **Lucas Andrade Souza** (564066) | Persistência (`ConexaoBanco`, `MedicaoDAO`, `AlertaDAO`, `schema.sql`), `ServicoMonitoramento`, menu (`SistemaMonitoramento`), bônus CSV e testes (JUnit + Mockito) |
 
O enunciado sugere 3 integrantes; nosso grupo tem 2, então as frentes "previsões/alertas/testes" foram
divididas entre nós dois. Cada um estudou também as classes do outro, para que qualquer um explique
qualquer trecho.

---

## 9. Uso de IA

Usamos o assistente **Claude (Anthropic)** apenas na fase de **planejamento**, antes de escrever o código.
A partir do enunciado da GS, pedimos ajuda para:

- **Organizar a ordem de desenvolvimento:** dividir o projeto em etapas (domínio → exceções →
  persistência → previsões e alertas → padrões criacionais → testes), seguindo a sugestão do próprio
  enunciado, para não começarmos pela parte mais difícil nem nos perdermos no escopo.
- **Montar um checklist dos critérios de avaliação**, para conferirmos ao final se cada requisito
  foi atendido (ex.: 3 implementações de `Sensor`, nenhum `catch` vazio, `assertThrows`, mock do DAO).
- **Tirar dúvidas conceituais** pontuais, como a diferença entre exceção checked e unchecked e quando
  usar `ArgumentCaptor` no Mockito.

**O código, os testes e as decisões de modelagem foram escritos pela equipe.** A IA não gerou as classes
do projeto. Quando ela explicou um conceito, nós o implementamos do nosso jeito, consultando a
documentação oficial do Java, do H2, do JUnit e do Mockito e os projetos anteriores da disciplina.

**Como revisamos:** cada integrante revisou o código do outro, rodamos `mvn test` e o roteiro de
demonstração da seção 2 nas nossas máquinas, e refizemos à mão os cálculos esperados nos testes.
